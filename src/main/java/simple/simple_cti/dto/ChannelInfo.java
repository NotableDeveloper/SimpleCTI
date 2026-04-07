package simple.simple_cti.dto;

import lombok.Data;

/**
 * Asterisk AMI 채널 정보를 담는 DTO.
 * ChannelEventListener에서 이벤트를 수신하면 이 객체로 변환하여
 * ChannelMonitorService를 통해 SSE로 Vue 프론트에 전달됩니다.
 */
@Data
public class ChannelInfo {

    // ==================== 채널 기본 정보 ====================

    /** 채널 고유 ID (추적의 기준 키). 예: 1712345678.1 */
    private String uniqueId;

    /**
     * 연결된 채널 패밀리의 최초 채널 ID.
     * 고객 채널과 상담사 채널이 동일한 linkedId를 가지므로
     * 이 값으로 두 채널을 묶어서 관리합니다.
     */
    private String linkedId;

    /** 채널 이름. 예: PJSIP/1001-00000001 */
    private String channel;

    /** 발신자 번호 */
    private String callerIdNum;

    /** 발신자 이름 */
    private String callerIdName;

    /** 연결된 상대방 번호 */
    private String connectedLineNum;

    /** 연결된 상대방 이름 */
    private String connectedLineName;

    // ==================== 채널 상태 ====================

    /**
     * 채널 상태 숫자 코드.
     * 0=Down, 1=Rsrvd, 2=OffHook, 3=Dialing, 4=Ring, 5=Ringing, 6=Up, 7=Busy
     */
    private String channelState;

    /**
     * 채널 상태 텍스트.
     * Down / OffHook / Dialing / Ring / Ringing / Up / Busy / Hangup / BridgeLeave
     */
    private String channelStateDesc;

    /** 다이얼플랜 컨텍스트 */
    private String context;

    /** 다이얼된 내선번호 */
    private String exten;

    // ==================== 브리지 정보 ====================

    /**
     * 현재 참여 중인 브리지의 고유 ID.
     * BridgeEnter 이벤트에서 설정되며 BridgeLeave/Hangup 시 null로 초기화됩니다.
     * 이 값이 존재하면 통화가 실제로 연결(Up)된 상태입니다.
     */
    private String bridgeUniqueId;

    /** 브리지 내 현재 채널 수 */
    private Integer bridgeNumChannels;

    // ==================== 종료 정보 ====================

    /** 종료 원인 코드 (Q.850 표준). 예: 16 = Normal Clearing */
    private Integer cause;

    /** 종료 원인 텍스트. 예: "Normal Clearing" */
    private String causeTxt;

    // ==================== 메타 ====================

    /**
     * 이 DTO를 생성한 이벤트 타입.
     * NewChannel / Hangup / BridgeEnter / BridgeLeave
     */
    private String eventType;
}
