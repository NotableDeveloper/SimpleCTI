<template>
  <div class="channel-monitor-panel">

    <!-- ==================== 패널 헤더 ==================== -->
    <div class="monitor-header">
      <div class="monitor-header-left">
        <div class="monitor-icon" :class="{ 'monitor-icon--active': isBridged }">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"
               stroke-linecap="round" stroke-linejoin="round">
            <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.8 19.8 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6A19.8 19.8 0 0 1 2.12 4.18 2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"/>
          </svg>
        </div>
        <div class="monitor-title-group">
          <span class="monitor-title">채널 모니터</span>
          <span class="monitor-subtitle">실시간 연결 상태</span>
        </div>
      </div>
      <!-- SSE 배지 및 초기화 버튼 -->
      <div class="monitor-header-right">
        <span class="sse-badge" :class="sseConnected ? 'sse-badge--on' : 'sse-badge--off'">
          {{ sseConnected ? 'LIVE' : 'OFF' }}
        </span>
        <button class="monitor-clear-btn" @click="clearChannels">초기화</button>
      </div>
    </div>

    <!-- ==================== 메인 콘텐츠 (세로 배치) ==================== -->
    <div class="monitor-content">

      <!-- [1] 고객 채널 카드 (TOP) -->
      <div class="channel-card customer-card" :class="getCardClass(customerChannel)">
        <div class="role-tag role-customer">고객 (External)</div>
        <div v-if="customerChannel" class="card-inner">
          <div class="main-info">
            <span class="number">{{ customerChannel.callerIdNum || '-' }}</span>
            <span class="state-label" :class="getStateClass(customerChannel.channelStateDesc)">
              {{ getStateLabel(customerChannel.channelStateDesc) }}
            </span>
          </div>
          <div class="sub-info">
            <span class="channel-name">{{ customerChannel.channel }}</span>
          </div>
        </div>
        <div v-else class="card-empty">고객 채널 대기 중...</div>
      </div>

      <!-- [2] 연결 커넥터 (VERTICAL) -->
      <div class="v-connector" :class="{ 'v-connector--active': isBridged }">
        <div class="v-line"></div>
        <div class="v-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <polyline points="7 13 12 18 17 13"/><polyline points="7 6 12 11 17 6"/>
          </svg>
        </div>
        <div class="v-line"></div>
      </div>

      <!-- [3] 상담사 채널 카드 (BOTTOM) -->
      <div class="channel-card agent-card" :class="getCardClass(agentChannel)">
        <div class="role-tag role-agent">상담사 (Agent)</div>
        <div v-if="agentChannel" class="card-inner">
          <div class="main-info">
            <span class="number">{{ agentChannel.callerIdNum || '-' }}</span>
            <span class="state-label" :class="getStateClass(agentChannel.channelStateDesc)">
              {{ getStateLabel(agentChannel.channelStateDesc) }}
            </span>
          </div>
          <div class="sub-info">
            <span class="channel-name">{{ agentChannel.channel }}</span>
          </div>
        </div>
        <div v-else class="card-empty">상담사 채널 대기 중...</div>
      </div>

    </div>

    <!-- ==================== 푸터 (통화 상세 정보) ==================== -->
    <div v-if="activeLinkedId" class="monitor-footer">
      <div class="footer-row">
        <span class="footer-label">Linked ID</span>
        <span class="footer-value">{{ activeLinkedId }}</span>
      </div>
      <div v-if="isBridged" class="footer-row">
        <span class="footer-label">Bridge ID</span>
        <span class="footer-value">{{ agentChannel?.bridgeUniqueId || customerChannel?.bridgeUniqueId }}</span>
      </div>
    </div>

  </div>
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue';
import { useChannelMonitor } from '@/composables/useChannelMonitor';

const props = defineProps({
  agentAccount: { type: String, default: '5005' },
  callStatus: { type: String, default: 'Idle' }
});

const {
  sseConnected,
  activeLinkedId,
  agentChannel,
  customerChannel,
  isBridged,
  connectSse,
  disconnectSse,
  clearChannels,
  getStateLabel,
  getStateClass,
} = useChannelMonitor(props.agentAccount);

onMounted(() => {
  connectSse();
});

onUnmounted(() => {
  disconnectSse();
});

function getCardClass(channel) {
  if (!channel) return 'card--empty';
  if (channel.bridgeUniqueId) return 'card--bridged';
  if (channel.channelStateDesc === 'Up') return 'card--up';
  return 'card--active';
}
</script>

<style scoped>
.channel-monitor-panel {
  background: #fdfdfd;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  height: 100%;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

/* 헤더 */
.monitor-header {
  padding: 12px 16px;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  background: white;
  border-radius: 16px 16px 0 0;
}
.monitor-header-left { display: flex; align-items: center; gap: 10px; }
.monitor-icon { width: 32px; height: 32px; background: #f1f5f9; border-radius: 8px; display: flex; align-items: center; justify-content: center; }
.monitor-icon--active { background: #dcfce7; color: #10b981; }
.monitor-icon svg { width: 16px; height: 16px; }
.monitor-title { font-size: 14px; font-weight: 800; color: #1e293b; display: block; }
.monitor-subtitle { font-size: 11px; color: #64748b; }
.monitor-header-right { display: flex; align-items: center; gap: 8px; }

.sse-badge { font-size: 10px; font-weight: 700; padding: 2px 8px; border-radius: 10px; }
.sse-badge--on { background: #dcfce7; color: #15803d; }
.sse-badge--off { background: #f1f5f9; color: #94a3b8; }
.monitor-clear-btn { font-size: 11px; color: #94a3b8; border: none; background: none; cursor: pointer; }
.monitor-clear-btn:hover { color: #ef4444; }

/* 메인 콘텐츠 영역 */
.monitor-content {
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

/* 공통 카드 스타일 */
.channel-card {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 12px;
  position: relative;
  background: white;
  transition: all 0.2s;
}
.card--empty { background: #f8fafc; border-style: dashed; }
.card--bridged { border-color: #10b981; background: #f0fdf4; box-shadow: 0 0 10px rgba(16, 185, 129, 0.05); }
.card--up { border-color: #6366f1; background: #f5f3ff; }

.role-tag {
  position: absolute;
  top: -8px;
  left: 12px;
  font-size: 9px;
  font-weight: 800;
  padding: 1px 6px;
  border-radius: 4px;
  text-transform: uppercase;
}
.role-customer { background: #3b82f6; color: white; }
.role-agent { background: #8b5cf6; color: white; }

.card-inner { display: flex; flex-direction: column; gap: 4px; }
.main-info { display: flex; justify-content: space-between; align-items: center; }
.number { font-size: 16px; font-weight: 800; font-family: monospace; color: #1e293b; }
.state-label { font-size: 10px; font-weight: 700; padding: 2px 6px; border-radius: 4px; }
.state-up { background: #dcfce7; color: #15803d; }
.state-ringing { background: #fef9c3; color: #854d0e; }
.sub-info { font-size: 10px; color: #64748b; font-family: monospace; }

.card-empty { height: 40px; display: flex; align-items: center; justify-content: center; font-size: 12px; color: #94a3b8; font-style: italic; }

/* 세로 커넥터 */
.v-connector {
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 40px;
}
.v-line { width: 2px; flex: 1; background: #e2e8f0; }
.v-connector--active .v-line { background: #10b981; }
.v-icon { width: 22px; height: 22px; border: 1px solid #e2e8f0; border-radius: 50%; display: flex; align-items: center; justify-content: center; background: white; margin: 2px 0; }
.v-connector--active .v-icon { border-color: #10b981; color: #10b981; }
.v-icon svg { width: 14px; height: 14px; }

/* 푸터 */
.monitor-footer {
  padding: 10px 16px;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
  border-radius: 0 0 16px 16px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.footer-row { display: flex; justify-content: space-between; font-size: 10px; }
.footer-label { color: #64748b; font-weight: 600; }
.footer-value { color: #1e293b; font-family: monospace; font-weight: 600; }
</style>
