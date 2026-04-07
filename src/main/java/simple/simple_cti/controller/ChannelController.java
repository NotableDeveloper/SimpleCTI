package simple.simple_cti.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import simple.simple_cti.ami.ChannelEventListener;
import simple.simple_cti.dto.ChannelInfo;
import simple.simple_cti.service.ChannelMonitorService;

import java.util.Collection;

/**
 * 채널 모니터링 관련 REST / SSE 엔드포인트.
 *
 * GET /api/channel-events  : SSE 스트림 구독 (Vue의 EventSource가 연결)
 * GET /api/channels         : 현재 활성 채널 스냅샷 반환 (페이지 첫 로드 시 사용)
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class ChannelController {

    private final ChannelMonitorService channelMonitorService;
    private final ChannelEventListener channelEventListener;

    public ChannelController(ChannelMonitorService channelMonitorService,
                             ChannelEventListener channelEventListener) {
        this.channelMonitorService = channelMonitorService;
        this.channelEventListener = channelEventListener;
    }

    /**
     * Vue 프론트엔드의 EventSource가 이 엔드포인트에 연결합니다.
     * 채널 이벤트(NewChannel, Hangup, BridgeEnter, BridgeLeave)가 발생할 때마다
     * 'channel-update' 이름의 SSE 이벤트를 JSON으로 전송합니다.
     *
     * produces = text/event-stream : SSE 프로토콜에 필수
     */
    @GetMapping(value = "/channel-events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter channelEvents() {
        log.info("New SSE subscription requested for channel monitoring.");
        return channelMonitorService.subscribe();
    }

    /**
     * 현재 활성 채널 목록 스냅샷을 반환합니다.
     * Vue 화면이 처음 마운트될 때 호출하여 이미 진행 중인 통화를 표시합니다.
     *
     * @return 현재 활성 ChannelInfo 목록
     */
    @GetMapping("/channels")
    public Collection<ChannelInfo> getActiveChannels() {
        Collection<ChannelInfo> channels = channelEventListener.getChannelMap().values();
        log.debug("Returning {} active channels.", channels.size());
        return channels;
    }
}
