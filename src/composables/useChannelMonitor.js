console.log('[DEBUG] useChannelMonitor.js Source Code Evaluated');

// [Composition API]
import { ref, computed, onUnmounted } from 'vue';

/**
 * Asterisk 채널 실시간 모니터링 composable.
 *
 * 백엔드 SSE(/api/channel-events)에 EventSource로 구독하여
 * 채널 이벤트(NewChannel/Hangup/BridgeEnter/BridgeLeave)를 수신합니다.
 *
 * @param {string} agentAccount - application.properties의 outbound.account 값 (예: '5005')
 */
export function useChannelMonitor(agentAccount) {
  // ==================== 상태 ====================

  const eventSource = ref(null);
  const sseConnected = ref(false);
  const channelMap = ref({});

  // ==================== Computed ====================

  /** 현재 통화 세션의 linkedId. 브리지가 활성화된 채널에서 추출합니다. */
  const activeLinkedId = computed(() => {
    const channels = Object.values(channelMap.value);
    const bridged = channels.find(c => c.bridgeUniqueId);
    if (bridged) {
      console.log('[ChannelMonitor] Found active linkedId from bridged channel:', bridged.linkedId);
      return bridged.linkedId;
    }
    const last = channels.length > 0 ? channels[channels.length - 1].linkedId : null;
    return last;
  });

  /** 상담사 채널 정보 (channel 필드에 agentAccount 포함 여부로 판단) */
  const agentChannel = computed(() => {
    if (!agentAccount) return null;
    const found = Object.values(channelMap.value).find(c =>
      c.channel && (c.channel.includes(agentAccount) || (c.callerIdNum === agentAccount))
    );
    if (found) console.log('[ChannelMonitor] Agent channel matched:', found.channel);
    return found || null;
  });

  /** 고객 채널 정보 (같은 linkedId를 공유하지만 상담사 고유ID가 아닌 채널) */
  const customerChannel = computed(() => {
    const linkedId = activeLinkedId.value;
    const agentUniqueId = agentChannel.value?.uniqueId;
    if (!linkedId) return null;
    const found = Object.values(channelMap.value).find(c =>
      c.linkedId === linkedId && c.uniqueId !== agentUniqueId
    );
    if (found) console.log('[ChannelMonitor] Customer channel matched:', found.channel);
    return found || null;
  });

  const isBridged = computed(() =>
    !!(agentChannel.value?.bridgeUniqueId || customerChannel.value?.bridgeUniqueId)
  );

  // ==================== SSE 연결 ====================

  function connectSse() {
    if (eventSource.value) return;

    console.log('[ChannelMonitor] Connecting to http://localhost:8090/api/channel-events ...');
    // 백엔드 직접 연결 (CORS 허용됨)
    eventSource.value = new EventSource('http://localhost:8090/api/channel-events');

    eventSource.value.addEventListener('connected', () => {
      sseConnected.value = true;
      console.log('[ChannelMonitor] SSE connected (Status: LIVE)');
      fetchSnapshot();
    });

    eventSource.value.addEventListener('channel-update', (event) => {
      try {
        const info = JSON.parse(event.data);
        console.log('[ChannelMonitor] Received Update:', info.eventType, info.channel, 'LinkedId:', info.linkedId);
        applyChannelUpdate(info);
      } catch (e) {
        console.warn('[ChannelMonitor] Data parse error:', e);
      }
    });

    eventSource.value.onerror = (e) => {
      console.warn('[ChannelMonitor] SSE error, possibly disconnected:', e);
      sseConnected.value = false;
    };
  }

  function disconnectSse() {
    if (eventSource.value) {
      eventSource.value.close();
      eventSource.value = null;
      sseConnected.value = false;
      channelMap.value = {}; // 상태 초기화
      console.log('[ChannelMonitor] SSE disconnected and state cleared');
    }
  }

  // ==================== 채널 맵 관리 ====================

  function applyChannelUpdate(info) {
    if (!info || !info.uniqueId) return;

    const newMap = { ...channelMap.value };
    if (info.eventType === 'Hangup') {
      delete newMap[info.uniqueId];
    } else {
      newMap[info.uniqueId] = { ...(newMap[info.uniqueId] || {}), ...info };
    }
    channelMap.value = newMap;
    console.log('[ChannelMonitor] Active Channels count:', Object.keys(channelMap.value).length);
  }

  async function fetchSnapshot() {
    try {
      const res = await fetch('/api/channels');
      if (!res.ok) return;
      const channels = await res.json();
      const map = {};
      channels.forEach(c => { map[c.uniqueId] = c; });
      channelMap.value = map;
      console.log('[ChannelMonitor] Snapshot loaded:', channels.length, 'active channels');
    } catch (e) {
      console.warn('[ChannelMonitor] Failed to fetch snapshot:', e);
    }
  }

  function clearChannels() {
    channelMap.value = {};
  }

  onUnmounted(() => {
    disconnectSse();
  });

  // ==================== 유틸리티 (UI 표시용) ====================

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

  function getStateClass(stateDesc) {
    if (!stateDesc) return 'state-unknown';
    if (stateDesc === 'Up') return 'state-up';
    if (['Ring', 'Ringing', 'Dialing'].includes(stateDesc)) return 'state-ringing';
    if (['Hangup', 'Down', 'BridgeLeave'].includes(stateDesc)) return 'state-down';
    return 'state-other';
  }

  return {
    sseConnected,
    channelMap,
    activeLinkedId,
    agentChannel,
    customerChannel,
    isBridged,
    connectSse,
    disconnectSse,
    clearChannels,
    getStateLabel,
    getStateClass,
  };
}
