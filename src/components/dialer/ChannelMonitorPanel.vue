<template>
  <div class="channel-monitor-panel">

    <!-- ==================== 패널 헤더 ==================== -->
    <div class="monitor-header">
      <div class="monitor-header-left">
        <!-- 라이브 아이콘 -->
        <div class="monitor-icon" :class="{ 'monitor-icon--active': isBridged }">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"
               stroke-linecap="round" stroke-linejoin="round">
            <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.8 19.8 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6A19.8 19.8 0 0 1 2.12 4.18 2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"/>
          </svg>
        </div>
        <div class="monitor-title-group">
          <span class="monitor-title">채널 모니터</span>
          <span class="monitor-subtitle">실시간 통화 채널 상태</span>
        </div>
        <!-- SSE 연결 상태 배지 -->
        <span class="sse-badge" :class="sseConnected ? 'sse-badge--on' : 'sse-badge--off'">
          <span class="sse-dot"></span>
          {{ sseConnected ? 'LIVE' : 'OFF' }}
        </span>
      </div>
      <!-- 채널 맵 비우기 버튼 -->
      <button class="monitor-clear-btn" @click="clearChannels" title="채널 목록 초기화">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"
             stroke-linecap="round" stroke-linejoin="round">
          <polyline points="3 6 5 6 21 6"/>
          <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/>
        </svg>
        초기화
      </button>
    </div>
    <!-- /패널 헤더 -->

    <!-- ==================== 통화 연결 배너 ==================== -->
    <div v-if="isBridged" class="bridge-banner">
      <span class="bridge-dot"></span>
      <span>통화 연결됨</span>
      <span class="bridge-id">Bridge: {{ agentChannel?.bridgeUniqueId || customerChannel?.bridgeUniqueId }}</span>
    </div>

    <!-- ==================== 채널 정보 그리드 ==================== -->
    <div class="channel-grid">

      <!-- 고객 채널 카드 -->
      <div class="channel-card" :class="getCardClass(customerChannel)">
        <div class="channel-card-header">
          <div class="channel-role-badge role-customer">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
            고객
          </div>
          <span v-if="customerChannel" class="channel-state-badge"
                :class="getStateClass(customerChannel.channelStateDesc)">
            {{ getStateLabel(customerChannel.channelStateDesc) }}
          </span>
        </div>

        <div v-if="customerChannel" class="channel-card-body">
          <!-- 발신 번호 -->
          <div class="channel-field">
            <span class="field-label">번호</span>
            <span class="field-value field-number">{{ customerChannel.callerIdNum || '-' }}</span>
          </div>
          <!-- 채널명 -->
          <div class="channel-field">
            <span class="field-label">채널</span>
            <span class="field-value field-mono">{{ customerChannel.channel || '-' }}</span>
          </div>
          <!-- Unique ID -->
          <div class="channel-field">
            <span class="field-label">Unique ID</span>
            <span class="field-value field-mono field-dim">{{ customerChannel.uniqueId || '-' }}</span>
          </div>
          <!-- 연결 상대 -->
          <div class="channel-field" v-if="customerChannel.connectedLineNum">
            <span class="field-label">연결 번호</span>
            <span class="field-value">{{ customerChannel.connectedLineNum }}</span>
          </div>
        </div>

        <!-- 채널 없음 상태 -->
        <div v-else class="channel-empty">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"
               stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" y1="8" x2="12" y2="12"/>
            <line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
          <span>고객 채널 없음</span>
        </div>
      </div>
      <!-- /고객 채널 -->

      <!-- 연결 화살표 -->
      <div class="channel-connector" :class="{ 'channel-connector--active': isBridged }">
        <div class="connector-line"></div>
        <div class="connector-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
               stroke-linecap="round" stroke-linejoin="round">
            <path d="M8 3H5a2 2 0 0 0-2 2v3"/>
            <path d="M21 8V5a2 2 0 0 0-2-2h-3"/>
            <path d="M3 16v3a2 2 0 0 0 2 2h3"/>
            <path d="M16 21h3a2 2 0 0 0 2-2v-3"/>
          </svg>
        </div>
        <div class="connector-line"></div>
      </div>

      <!-- 상담사 채널 카드 -->
      <div class="channel-card" :class="getCardClass(agentChannel)">
        <div class="channel-card-header">
          <div class="channel-role-badge role-agent">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <path d="M3 18v-6a9 9 0 0 1 18 0v6"/>
              <path d="M21 19a2 2 0 0 1-2 2h-1a2 2 0 0 1-2-2v-3a2 2 0 0 1 2-2h3zM3 19a2 2 0 0 0 2 2h1a2 2 0 0 0 2-2v-3a2 2 0 0 0-2-2H3z"/>
            </svg>
            상담사
          </div>
          <span v-if="agentChannel" class="channel-state-badge"
                :class="getStateClass(agentChannel.channelStateDesc)">
            {{ getStateLabel(agentChannel.channelStateDesc) }}
          </span>
        </div>

        <div v-if="agentChannel" class="channel-card-body">
          <!-- 내선 번호 -->
          <div class="channel-field">
            <span class="field-label">내선</span>
            <span class="field-value field-number">{{ agentChannel.callerIdNum || '-' }}</span>
          </div>
          <!-- 채널명 -->
          <div class="channel-field">
            <span class="field-label">채널</span>
            <span class="field-value field-mono">{{ agentChannel.channel || '-' }}</span>
          </div>
          <!-- Unique ID -->
          <div class="channel-field">
            <span class="field-label">Unique ID</span>
            <span class="field-value field-mono field-dim">{{ agentChannel.uniqueId || '-' }}</span>
          </div>
          <!-- 연결 상대 -->
          <div class="channel-field" v-if="agentChannel.connectedLineNum">
            <span class="field-label">연결 번호</span>
            <span class="field-value">{{ agentChannel.connectedLineNum }}</span>
          </div>
        </div>

        <!-- 채널 없음 상태 -->
        <div v-else class="channel-empty">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"
               stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" y1="8" x2="12" y2="12"/>
            <line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
          <span>상담사 채널 없음</span>
        </div>
      </div>
      <!-- /상담사 채널 -->

    </div>
    <!-- /channel-grid -->

    <!-- ==================== 공통 채널 정보 (Linked ID) ==================== -->
    <div v-if="activeLinkedId" class="linked-id-bar">
      <span class="linked-id-label">Linked ID</span>
      <span class="linked-id-value">{{ activeLinkedId }}</span>
    </div>

    <!-- ==================== Raw 채널 목록 (디버그) ==================== -->
    <details class="raw-channels" v-if="Object.keys(channelMap).length > 0">
      <summary class="raw-channels-summary">
        전체 채널 ({{ Object.keys(channelMap).length }}개)
      </summary>
      <div class="raw-channel-list">
        <div
          v-for="(ch, uid) in channelMap"
          :key="uid"
          class="raw-channel-item"
        >
          <span class="raw-channel-type" :class="getStateClass(ch.channelStateDesc)">
            {{ ch.eventType }}
          </span>
          <span class="raw-channel-name">{{ ch.channel }}</span>
          <span class="raw-channel-state">{{ ch.channelStateDesc }}</span>
          <span class="raw-channel-uid">{{ ch.uniqueId }}</span>
        </div>
      </div>
    </details>

  </div>
</template>

<script setup>
import { useChannelMonitor } from '@/composables/useChannelMonitor';

const props = defineProps({
  /** application.properties의 outbound.account 값 (예: '5005') */
  agentAccount: {
    type: String,
    default: ''
  },
  /** SIP callStatus — InCall 진입 시 SSE 연결 */
  callStatus: {
    type: String,
    default: 'Idle'
  }
});

const {
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
} = useChannelMonitor(props.agentAccount);

// 컴포넌트 마운트 시 SSE 즉시 연결
connectSse();

// ==================== 유틸 ====================

/** 채널 상태에 따른 카드 CSS 클래스 반환 */
function getCardClass(channel) {
  if (!channel) return 'channel-card--empty';
  if (channel.bridgeUniqueId) return 'channel-card--bridged';
  if (['Ring', 'Ringing', 'Dialing'].includes(channel.channelStateDesc)) return 'channel-card--ringing';
  if (channel.channelStateDesc === 'Up') return 'channel-card--up';
  return '';
}

// 부모(Dialer.vue)에서 connectSse/disconnectSse를 제어할 수 있도록 노출
defineExpose({ connectSse, disconnectSse, clearChannels });
</script>

<style scoped>
/* ==================== 패널 래퍼 ==================== */

.channel-monitor-panel {
  background: var(--color-card);
  border: 1px solid var(--color-border);
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.07);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  gap: 0;
}

/* ==================== 패널 헤더 ==================== */

.monitor-header {
  padding: 14px 18px;
  background: var(--color-card);
  border-bottom: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-shrink: 0;
}

.monitor-header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.monitor-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: background 0.2s;
}

.monitor-icon--active {
  background: #ecfdf5;
}

.monitor-icon svg {
  width: 15px;
  height: 15px;
  color: #6b7280;
  transition: color 0.2s;
}

.monitor-icon--active svg {
  color: #059669;
}

.monitor-title-group {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.monitor-title {
  font-size: 13.5px;
  font-weight: 700;
  color: var(--color-text-primary);
}

.monitor-subtitle {
  font-size: 11px;
  color: var(--color-text-secondary);
}

/* SSE 배지 */
.sse-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 3px 8px;
  border-radius: 20px;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.sse-badge--on {
  background: #ecfdf5;
  color: #065f46;
  border: 1px solid #a7f3d0;
}

.sse-badge--off {
  background: #f3f4f6;
  color: #9ca3af;
  border: 1px solid #e5e7eb;
}

.sse-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: currentColor;
  animation: ssePulse 1.4s ease-in-out infinite;
}

.sse-badge--off .sse-dot {
  animation: none;
}

@keyframes ssePulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

/* 초기화 버튼 */
.monitor-clear-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 5px 11px;
  border-radius: 7px;
  border: 1px solid var(--color-border);
  background: #f9fafb;
  color: var(--color-text-secondary);
  font-size: 11.5px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
  font-family: inherit;
}

.monitor-clear-btn:hover {
  border-color: var(--color-error-border, #fca5a5);
  color: var(--color-error, #dc2626);
  background: var(--color-error-bg, #fef2f2);
}

.monitor-clear-btn svg {
  width: 11px;
  height: 11px;
}

/* ==================== 브리지 배너 ==================== */

.bridge-banner {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 18px;
  background: linear-gradient(90deg, #ecfdf5, #f0fdf4);
  border-bottom: 1px solid #a7f3d0;
  font-size: 12px;
  font-weight: 600;
  color: #065f46;
}

.bridge-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #10b981;
  animation: ssePulse 1.2s ease-in-out infinite;
}

.bridge-id {
  margin-left: auto;
  font-size: 10px;
  font-weight: 500;
  color: #6ee7b7;
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
  background: rgba(0,0,0,0.05);
  padding: 2px 6px;
  border-radius: 4px;
}

/* ==================== 채널 그리드 ==================== */

.channel-grid {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  gap: 0;
  padding: 16px;
  align-items: stretch;
}

/* ==================== 채널 카드 ==================== */

.channel-card {
  border: 1.5px solid var(--color-border);
  border-radius: 12px;
  overflow: hidden;
  transition: border-color 0.2s, box-shadow 0.2s;
  background: #fafbfc;
  min-height: 140px;
  display: flex;
  flex-direction: column;
}

.channel-card--bridged {
  border-color: #10b981;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.12);
  background: #f0fdf4;
}

.channel-card--ringing {
  border-color: #f59e0b;
  box-shadow: 0 0 0 3px rgba(245, 158, 11, 0.1);
  background: #fffbeb;
}

.channel-card--up {
  border-color: #6366f1;
  background: #f5f3ff;
}

.channel-card--empty {
  border-color: var(--color-border);
  background: #f9fafb;
}

/* 카드 헤더 */
.channel-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-bottom: 1px solid var(--color-border);
  background: rgba(255, 255, 255, 0.7);
}

.channel-role-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 11.5px;
  font-weight: 700;
  padding: 3px 8px;
  border-radius: 6px;
}

.channel-role-badge svg {
  width: 13px;
  height: 13px;
}

.role-customer {
  background: #eff6ff;
  color: #1d4ed8;
}

.role-agent {
  background: #f5f3ff;
  color: #6d28d9;
}

/* 상태 배지 */
.channel-state-badge {
  font-size: 10.5px;
  font-weight: 700;
  padding: 2px 7px;
  border-radius: 10px;
}

.state-up {
  background: #ecfdf5;
  color: #065f46;
  border: 1px solid #a7f3d0;
}

.state-ringing {
  background: #fffbeb;
  color: #92400e;
  border: 1px solid #fde68a;
}

.state-down {
  background: #fef2f2;
  color: #991b1b;
  border: 1px solid #fca5a5;
}

.state-other {
  background: #f3f4f6;
  color: #374151;
  border: 1px solid #e5e7eb;
}

.state-unknown {
  background: #f3f4f6;
  color: #9ca3af;
  border: 1px solid #e5e7eb;
}

/* 카드 본문 */
.channel-card-body {
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  gap: 7px;
  flex: 1;
}

.channel-field {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.field-label {
  font-size: 10px;
  font-weight: 600;
  color: var(--color-text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.4px;
  width: 54px;
  flex-shrink: 0;
}

.field-value {
  font-size: 12px;
  color: var(--color-text-primary);
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  min-width: 0;
}

.field-number {
  font-size: 13.5px;
  font-weight: 700;
  color: var(--color-text-primary);
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
}

.field-mono {
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
  font-size: 10.5px;
}

.field-dim {
  color: var(--color-text-secondary);
}

/* 채널 없음 */
.channel-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 7px;
  padding: 20px;
  color: #9ca3af;
}

.channel-empty svg {
  width: 24px;
  height: 24px;
  opacity: 0.3;
}

.channel-empty span {
  font-size: 11.5px;
  opacity: 0.6;
}

/* ==================== 채널 연결 표시 ==================== */

.channel-connector {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 48px;
  gap: 4px;
  padding: 0 4px;
}

.connector-line {
  flex: 1;
  width: 2px;
  background: var(--color-border);
  border-radius: 1px;
  transition: background 0.2s;
}

.channel-connector--active .connector-line {
  background: linear-gradient(180deg, #10b981, #059669);
}

.connector-icon {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: #f3f4f6;
  border: 1.5px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.2s;
}

.channel-connector--active .connector-icon {
  background: #ecfdf5;
  border-color: #10b981;
}

.connector-icon svg {
  width: 14px;
  height: 14px;
  color: #9ca3af;
  transition: color 0.2s;
}

.channel-connector--active .connector-icon svg {
  color: #059669;
}

/* ==================== Linked ID 바 ==================== */

.linked-id-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 18px;
  background: #f9fafb;
  border-top: 1px solid var(--color-border);
  font-size: 11px;
}

.linked-id-label {
  font-size: 10px;
  font-weight: 700;
  color: var(--color-text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.linked-id-value {
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
  font-size: 11px;
  color: var(--color-text-primary);
  font-weight: 600;
}

/* ==================== Raw 채널 목록 ==================== */

.raw-channels {
  border-top: 1px solid var(--color-border);
}

.raw-channels-summary {
  padding: 8px 18px;
  font-size: 11px;
  font-weight: 600;
  color: var(--color-text-secondary);
  cursor: pointer;
  background: #f9fafb;
  user-select: none;
  list-style: none;
}

.raw-channels-summary::-webkit-details-marker { display: none; }

.raw-channels-summary:hover {
  color: var(--color-text-primary);
}

.raw-channel-list {
  background: #f3f4f6;
  padding: 6px 0;
  max-height: 120px;
  overflow-y: auto;
}

.raw-channel-item {
  display: grid;
  grid-template-columns: 90px 1fr 80px 100px;
  gap: 8px;
  padding: 4px 18px;
  font-size: 10.5px;
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
  border-bottom: 1px solid rgba(0,0,0,0.04);
  align-items: center;
}

.raw-channel-type {
  font-weight: 700;
  font-size: 9.5px;
  padding: 1px 5px;
  border-radius: 4px;
  text-align: center;
}

.raw-channel-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--color-text-primary);
}

.raw-channel-state {
  color: var(--color-text-secondary);
}

.raw-channel-uid {
  color: #9ca3af;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
