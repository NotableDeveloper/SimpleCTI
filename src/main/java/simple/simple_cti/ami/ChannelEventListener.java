package simple.simple_cti.ami;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.asteriskjava.manager.ManagerEventListener;
import org.asteriskjava.manager.event.*;
import org.springframework.stereotype.Component;
import simple.simple_cti.dto.ChannelInfo;
import simple.simple_cti.service.ChannelMonitorService;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Asterisk AMI 채널 이벤트를 수신하여 실시간 채널 상태를 관리하는 컴포넌트.
 *
 * 구독 이벤트:
 *   - NewChannelEvent  : 채널 생성 시 채널 맵에 등록
 *   - HangupEvent      : 채널 종료 시 채널 맵에서 제거
 *   - BridgeEnterEvent : 통화 연결(브리지 진입) 시 BridgeId 설정
 *   - BridgeLeaveEvent : 브리지 이탈 시 BridgeId 초기화
 *
 * asteriskjava 3.41.0 메서드명 (javap 확인):
 *   - NewChannelEvent.getLinkedid()     (소문자 i)
 *   - HangupEvent.getLinkedId()         (대문자 I)
 *   - BridgeEnterEvent.getLinkedId()    (대문자 I)
 *   - AbstractBridgeEvent.getBridgeUniqueId(), getBridgeNumChannels()
 */
@Slf4j
@Component
public class ChannelEventListener implements ManagerEventListener {

    private final AmiConnectionManager amiConnectionManager;
    private final ChannelMonitorService channelMonitorService;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * 현재 활성 채널 맵 (key: Uniqueid, value: ChannelInfo)
     * ConcurrentHashMap으로 멀티스레드 안전성을 보장합니다.
     */
    private final ConcurrentHashMap<String, ChannelInfo> channelMap = new ConcurrentHashMap<>();

    public ChannelEventListener(AmiConnectionManager amiConnectionManager,
                                ChannelMonitorService channelMonitorService) {
        this.amiConnectionManager = amiConnectionManager;
        this.channelMonitorService = channelMonitorService;
    }

    @PostConstruct
    public void init() {
        if (amiConnectionManager.getManagerConnection() != null) {
            amiConnectionManager.getManagerConnection().addEventListener(this);
            log.info("ChannelEventListener registered to AmiConnectionManager.");
        } else {
            log.warn("AmiConnectionManager connection is null. ChannelEventListener NOT registered.");
        }
    }

    @PreDestroy
    public void cleanup() {
        executorService.shutdown();
    }

    @Override
    public void onManagerEvent(ManagerEvent event) {
        if (event instanceof NewChannelEvent) {
            executorService.submit(() -> handleNewChannel((NewChannelEvent) event));
        } else if (event instanceof HangupEvent) {
            executorService.submit(() -> handleHangup((HangupEvent) event));
        } else if (event instanceof BridgeEnterEvent) {
            executorService.submit(() -> handleBridgeEnter((BridgeEnterEvent) event));
        } else if (event instanceof BridgeLeaveEvent) {
            executorService.submit(() -> handleBridgeLeave((BridgeLeaveEvent) event));
        }
    }

    // ==================== 이벤트 핸들러 ====================

    /**
     * 새 채널이 생성되면 채널 맵에 등록 후 SSE Push.
     * NewChannelEvent는 getLinkedid() (소문자 i)를 사용합니다.
     */
    private void handleNewChannel(NewChannelEvent event) {
        String uniqueId = event.getUniqueId();
        if (uniqueId == null) return;

        ChannelInfo info = new ChannelInfo();
        info.setUniqueId(uniqueId);
        info.setLinkedId(event.getLinkedid()); // NewChannelEvent: getLinkedid() - 소문자 i
        info.setChannel(event.getChannel());
        info.setCallerIdNum(event.getCallerIdNum());
        info.setCallerIdName(event.getCallerIdName());
        info.setConnectedLineNum(event.getConnectedLineNum());
        info.setConnectedLineName(event.getConnectedLineName());
        if (event.getChannelState() != null) {
            info.setChannelState(event.getChannelState().toString());
        }
        info.setChannelStateDesc(event.getChannelStateDesc());
        info.setContext(event.getContext());
        info.setExten(event.getExten());
        info.setEventType("NewChannel");

        channelMap.put(uniqueId, info);
        log.debug("NewChannel: {} (state={})", event.getChannel(), event.getChannelStateDesc());
        channelMonitorService.pushChannelUpdate(info);
    }

    /**
     * 채널이 종료되면 채널 맵에서 제거 후 SSE Push.
     * HangupEvent는 getLinkedId() (대문자 I)를 사용합니다.
     */
    private void handleHangup(HangupEvent event) {
        String uniqueId = event.getUniqueId();
        if (uniqueId == null) return;

        ChannelInfo info = channelMap.remove(uniqueId);
        if (info == null) {
            // 맵에 없는 경우에도 종료 이벤트는 전달
            info = new ChannelInfo();
            info.setUniqueId(uniqueId);
            info.setLinkedId(event.getLinkedId()); // HangupEvent: getLinkedId() - 대문자 I
            info.setChannel(event.getChannel());
            info.setCallerIdNum(event.getCallerIdNum());
        }
        info.setChannelStateDesc("Hangup");
        info.setEventType("Hangup");
        info.setCause(event.getCause());
        info.setCauseTxt(event.getCauseTxt());

        log.debug("Hangup: {} cause={} ({})", event.getChannel(), event.getCause(), event.getCauseTxt());
        channelMonitorService.pushChannelUpdate(info);
    }

    /**
     * 채널이 브리지에 진입하면 BridgeUniqueid를 설정 후 SSE Push.
     * 이 시점이 고객↔상담사 통화가 실제로 연결된 상태입니다.
     * BridgeEnterEvent는 AbstractBridgeEvent를 상속하여
     * getBridgeUniqueId(), getBridgeNumChannels()를 제공합니다.
     */
    private void handleBridgeEnter(BridgeEnterEvent event) {
        String uniqueId = event.getUniqueId();
        if (uniqueId == null) return;

        // 채널 맵에서 조회하거나 새로 생성
        ChannelInfo info = channelMap.computeIfAbsent(uniqueId, id -> {
            ChannelInfo newInfo = new ChannelInfo();
            newInfo.setUniqueId(id);
            newInfo.setLinkedId(event.getLinkedId()); // BridgeEnterEvent: getLinkedId() - 대문자 I
            newInfo.setChannel(event.getChannel());
            newInfo.setCallerIdNum(event.getCallerIdNum());
            newInfo.setCallerIdName(event.getCallerIdName());
            newInfo.setConnectedLineNum(event.getConnectedLineNum());
            newInfo.setConnectedLineName(event.getConnectedLineName());
            return newInfo;
        });

        info.setBridgeUniqueId(event.getBridgeUniqueId());
        info.setChannelStateDesc("Up");
        info.setEventType("BridgeEnter");
        info.setBridgeNumChannels(event.getBridgeNumChannels());

        log.info("BridgeEnter: {} bridgeId={} numChannels={}", event.getChannel(), info.getBridgeUniqueId(), info.getBridgeNumChannels());
        channelMonitorService.pushChannelUpdate(info);
    }

    /**
     * 채널이 브리지에서 이탈하면 BridgeId를 초기화 후 SSE Push.
     * BridgeLeaveEvent는 getLinkedId() (대문자 I)를 사용합니다.
     */
    private void handleBridgeLeave(BridgeLeaveEvent event) {
        String uniqueId = event.getUniqueId();
        if (uniqueId == null) return;

        ChannelInfo info = channelMap.get(uniqueId);
        if (info != null) {
            log.debug("BridgeLeave: {} bridgeId={}", event.getChannel(), event.getBridgeUniqueId());
            info.setBridgeUniqueId(null);
            info.setChannelStateDesc("BridgeLeave");
            info.setEventType("BridgeLeave");
            channelMonitorService.pushChannelUpdate(info);
        }
    }

    // ==================== 공개 조회 ====================

    /** 현재 활성 채널 맵의 스냅샷을 반환합니다 (ChannelController에서 사용). */
    public ConcurrentHashMap<String, ChannelInfo> getChannelMap() {
        return channelMap;
    }
}
