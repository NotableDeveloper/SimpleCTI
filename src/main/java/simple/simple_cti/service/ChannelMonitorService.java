package simple.simple_cti.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import simple.simple_cti.dto.ChannelInfo;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SSE(Server-Sent Events)를 통해 Vue 프론트엔드에 채널 상태를 실시간 Push하는 서비스.
 *
 * SseEmitter를 CopyOnWriteArrayList로 관리합니다:
 *   - 다수의 클라이언트(브라우저 탭)가 동시에 구독 가능
 *   - 전송 실패 또는 완료된 Emitter는 자동으로 목록에서 제거
 *   - timeout: Long.MAX_VALUE (클라이언트가 연결을 끊지 않는 한 유지)
 */
@Slf4j
@Service
public class ChannelMonitorService {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ==================== SSE 구독 ====================

    /**
     * 새 SSE 구독자를 등록하고 SseEmitter를 반환합니다.
     * ChannelController의 GET /api/channel-events에서 호출됩니다.
     *
     * @return 새로 생성된 SseEmitter
     */
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        // 전송 완료 / 타임아웃 / 에러 시 emitter를 목록에서 제거합니다.
        emitter.onCompletion(() -> {
            emitters.remove(emitter);
            log.debug("SSE emitter removed (completed). Active: {}", emitters.size());
        });
        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            log.debug("SSE emitter removed (timeout). Active: {}", emitters.size());
        });
        emitter.onError(e -> {
            emitters.remove(emitter);
            log.debug("SSE emitter removed (error: {}). Active: {}", e.getMessage(), emitters.size());
        });

        emitters.add(emitter);
        log.info("SSE client subscribed. Active emitters: {}", emitters.size());

        // 연결 직후 heartbeat를 전송하여 클라이언트가 연결 성공을 즉시 인지하게 합니다.
        try {
            emitter.send(SseEmitter.event().name("connected").data("ok"));
        } catch (IOException e) {
            log.warn("Failed to send SSE connect event: {}", e.getMessage());
            emitters.remove(emitter);
        }

        return emitter;
    }

    // ==================== SSE 전송 ====================

    /**
     * ChannelInfo를 JSON으로 직렬화하여 모든 구독자에게 Push합니다.
     * ChannelEventListener의 각 핸들러에서 호출됩니다.
     *
     * @param channelInfo 전송할 채널 정보
     */
    public void pushChannelUpdate(ChannelInfo channelInfo) {
        if (emitters.isEmpty()) {
            return; // 구독자가 없으면 skip
        }

        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(channelInfo);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize ChannelInfo: {}", e.getMessage());
            return;
        }

        // 실패한 emitter를 제거하면서 전송
        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("channel-update")
                        .data(jsonPayload));
            } catch (IOException e) {
                log.debug("SSE send failed, marking emitter for removal: {}", e.getMessage());
                deadEmitters.add(emitter);
            }
        }
        emitters.removeAll(deadEmitters);

        log.debug("Pushed channel-update (type={}, channel={}) to {} clients",
                channelInfo.getEventType(), channelInfo.getChannel(), emitters.size());
    }
}
