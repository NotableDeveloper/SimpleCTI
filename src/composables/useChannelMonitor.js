// [Composition API]
import { ref, computed, onUnmounted } from 'vue';

/**
 * Asterisk 채널 실시간 모니터링 composable.
 *
 * 백엔드 SSE(/api/channel-events)에 EventSource로 구독하여
 * 채널 이벤트(NewChannel/Hangup/BridgeEnter/BridgeLeave)를 수신합니다.
 *
 * linkedId를 기준으로 고객 채널과 상담사 채널을 분류합니다:
 *   - 상담사 채널: channel 이름에 agentAccount가 포함된 채널
 *   - 고객 채널  : 동일 linkedId를 가진 나머지 채널
 *
 * @param {string} agentAccount - application.properties의 outbound.account 값 (예: '5005')
 * @returns {Object} 채널 모니터링 상태 및 제어 함수
 */
export function useChannelMonitor(agentAccount) {
  // ==================== 상태 ====================

  /** EventSource 인스턴스 */
  const eventSource = ref(null);

  /** SSE 연결 상태 */
  const sseConnected = ref(false);

  /**
   * 현재 활성 채널 맵.
   * key: uniqueId, value: ChannelInfo 객체
   */
  const channelMap = ref({});

  // ==================== Computed ====================

  /** 현재 통화 세션의 linkedId. 브리지가 활성화된 채널에서 추출합니다. */
  const activeLinkedId = computed(() => {
    const channels = Object.values(channelMap.value);
    // BridgeEnter 이벤트가 있는 채널(bridgeUniqueId 존재) 우선 탐색
    const bridged = channels.find(c => c.bridgeUniqueId);
    if (bridged) return bridged.linkedId;
    // 없으면 가장 최근 채널의 linkedId 반환
    return channels.length > 0 ? channels[channels.length - 1].linkedId : null;
  });

  /**
   * 상담사 채널 정보.
   * channel 이름에 agentAccount가 포함된 채널입니다.
   */
  const agentChannel = computed(() => {
    if (!agentAccount) return null;
    return Object.values(channelMap.value).find(c =>
      c.channel && c.channel.includes(agentAccount)
    ) || null;
  });

  /**
   * 고객 채널 정보.
   * activeLinkedId를 공유하지만 agentChannel이 아닌 채널입니다.
   */
  const customerChannel = computed(() => {
    const linkedId = activeLinkedId.value;
    const agentUniqueId = agentChannel.value?.uniqueId;
    if (!linkedId) return null;
    return Object.values(channelMap.value).find(c =>
      c.linkedId === linkedId && c.uniqueId !== agentUniqueId
    ) || null;
  });

  /** 현재 통화가 브리지(연결)된 상태인지 여부 */
  const isBridged = computed(() =>
    !!(agentChannel.value?.bridgeUniqueId || customerChannel.value?.bridgeUniqueId)
  );

  // ==================== SSE 연결 ====================

  /**
   * SSE 스트림에 구독합니다.
   * callStatus가 IDLE이 아닐 때 (SIP 등록 후) 호출하세요.
   * 컴포넌트 mount 또는 SIP 등록 완료 시 호출합니다.
   */
  function connectSse() {
    if (eventSource.value) return; // 이미 연결됨

    // 상대 경로 사용: Vue dev server 프록시(/api → localhost:8090)를 경유합니다.
    // 절대 URL(http://localhost:8090/...) 사용 시 CORS 오류가 발생할 수 있습니다.
    eventSource.value = new EventSource('/api/channel-events');

    // SSE 연결 성공 (connected 이벤트)
    eventSource.value.addEventListener('connected', () => {
      sseConnected.value = true;
      console.log('[ChannelMonitor] SSE connected');
      // 초기 스냅샷 조회 (이미 진행 중인 통화 반영)
      fetchSnapshot();
    });

    // 채널 업데이트 이벤트 수신
    eventSource.value.addEventListener('channel-update', (event) => {
      try {
        const info = JSON.parse(event.data);
        applyChannelUpdate(info);
      } catch (e) {
        console.warn('[ChannelMonitor] Failed to parse channel-update:', e);
      }
    });

    // EventSource 기본 에러 핸들러
    eventSource.value.onerror = (e) => {
      console.warn('[ChannelMonitor] SSE error:', e);
      sseConnected.value = false;
    };
  }

  /**
   * SSE 연결을 끊습니다.
   * 컴포넌트 beforeUnmount 또는 SIP 로그아웃 시 호출합니다.
   */
  function disconnectSse() {
    if (eventSource.value) {
      eventSource.value.close();
      eventSource.value = null;
      sseConnected.value = false;
      console.log('[ChannelMonitor] SSE disconnected');
    }
  }

  // ==================== 채널 맵 관리 ====================

  /**
   * 수신한 ChannelInfo를 channelMap에 반영합니다.
   * Hangup 이벤트: 채널 맵에서 제거
   * 그 외: 채널 맵에 upsert
   */
  function applyChannelUpdate(info) {
    if (!info || !info.uniqueId) return;

    if (info.eventType === 'Hangup') {
      const updated = { ...channelMap.value };
      delete updated[info.uniqueId];
      channelMap.value = updated;
    } else {
      channelMap.value = {
        ...channelMap.value,
        [info.uniqueId]: { ...channelMap.value[info.uniqueId], ...info }
      };
    }
  }

  /**
   * 페이지 첫 로드 시 현재 활성 채널 스냅샷을 가져옵니다.
   * 이미 진행 중인 통화가 있어도 화면에 표시될 수 있도록 합니다.
   */
  async function fetchSnapshot() {
    try {
      // 상대 경로로 요청 (프록시 경유)
      const res = await fetch('/api/channels');
      if (!res.ok) return;
      const channels = await res.json();
      const map = {};
      channels.forEach(c => { map[c.uniqueId] = c; });
      channelMap.value = map;
      console.log('[ChannelMonitor] Snapshot loaded:', channels.length, 'channels');
    } catch (e) {
      console.warn('[ChannelMonitor] Failed to fetch snapshot:', e);
    }
  }

  /** 채널 맵 전체를 초기화합니다. */
  function clearChannels() {
    channelMap.value = {};
  }

  // ==================== 라이프사이클 ====================

  // 컴포넌트 언마운트 시 SSE 자동 해제
  onUnmounted(() => {
    disconnectSse();
  });

  // ==================== 유틸리티 ====================

  /**
   * ChannelStateDesc 값을 한국어 레이블로 변환합니다.
   * @param {string} stateDesc
   * @returns {string}
   */
  function getStateLabel(stateDesc) {
    const labels = {
      'Down':        '비활성',
      'OffHook':     '오프훅',
      'Dialing':     '발신 중',
      'Ring':        '링백',
      'Ringing':     '링잉',
      'Up':          '통화 중',
      'Busy':        '통화 중',
      'Hangup':      '통화 종료',
      'BridgeLeave': '연결 해제',
    };
    return labels[stateDesc] || stateDesc || '-';
  }

  /**
   * ChannelStateDesc 값에 따른 CSS 클래스를 반환합니다.
   * @param {string} stateDesc
   * @returns {string}
   */
  function getStateClass(stateDesc) {
    if (!stateDesc) return 'state-unknown';
    if (stateDesc === 'Up') return 'state-up';
    if (['Ring', 'Ringing', 'Dialing'].includes(stateDesc)) return 'state-ringing';
    if (['Hangup', 'Down', 'BridgeLeave'].includes(stateDesc)) return 'state-down';
    return 'state-other';
  }

  return {
    // 상태
    sseConnected,
    channelMap,
    // computed
    activeLinkedId,
    agentChannel,
    customerChannel,
    isBridged,
    // 함수
    connectSse,
    disconnectSse,
    clearChannels,
    getStateLabel,
    getStateClass,
  };
}
