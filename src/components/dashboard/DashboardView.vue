<template>
  <!-- ==================== 대시보드 뷰 ==================== -->
  <main class="page-body">

    <!-- 요약 스트립 -->
    <div class="summary-strip">
      <!-- Application Server 상태 chip -->
      <div class="summary-chip">
        <span
          class="chip-dot"
          :class="appStatus.status === 'UP' ? 'success' : (appStatus.status === 'CHECKING...' ? 'warning' : 'error')"
        ></span>
        <span class="chip-label">Application Server</span>
        <span>{{ appStatus.status }}</span>
      </div>
      <!-- Asterisk 상태 chip -->
      <div class="summary-chip">
        <span
          class="chip-dot"
          :class="asteriskStatus.connectionState === 'CONNECTED' ? 'success' : (asteriskStatus.connectionState === 'UNKNOWN' ? 'warning' : 'error')"
        ></span>
        <span class="chip-label">Asterisk</span>
        <span>{{ asteriskStatus.connectionState }}</span>
      </div>
      <!-- Last Updated -->
      <div class="last-updated-bar">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="10"/>
          <polyline points="12 6 12 12 16 14"/>
        </svg>
        Last Updated:&nbsp;<span class="time-value">{{ formatTime(lastUpdated) || '--:--:--' }}</span>
      </div>
    </div>

    <!-- 연결 상태 섹션 헤더 -->
    <div class="section-header">
      <span class="section-title">연결 상태</span>
      <span class="section-count">2개 서비스</span>
    </div>

    <!-- 상태 카드 그리드 -->
    <div class="cards-grid">

      <!-- Application Server 카드 -->
      <div class="status-card">
        <div class="card-header">
          <div class="card-header-left">
            <div class="card-icon-wrap blue">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <rect x="2" y="3" width="20" height="14" rx="2"/>
                <line x1="8" y1="21" x2="16" y2="21"/>
                <line x1="12" y1="17" x2="12" y2="21"/>
              </svg>
            </div>
            <div class="card-title-group">
              <span class="card-title">Application Server</span>
              <span class="card-subtitle">Spring Boot Backend</span>
            </div>
          </div>
          <!-- 상태 배지: App.vue 전역 클래스 사용 -->
          <span class="status-badge" :class="getStatusClass(appStatus.status)">
            <span class="badge-dot" :class="appStatus.status === 'UP' ? 'pulse' : ''"></span>
            {{ appStatus.status }}
          </span>
        </div>
        <div class="card-body">
          <div class="card-row">
            <span class="card-row-label">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/>
              </svg>
              Port
            </span>
            <span class="card-row-value mono">:8090</span>
          </div>
          <div class="card-divider"></div>
          <div class="card-row">
            <span class="card-row-label">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="16 18 22 12 16 6"/>
                <polyline points="8 6 2 12 8 18"/>
              </svg>
              Status
            </span>
            <span class="card-row-value mono">{{ appStatus.status === 'UP' ? '200 OK' : appStatus.status }}</span>
          </div>
        </div>
      </div>

      <!-- Asterisk Connection 카드 -->
      <div class="status-card">
        <div class="card-header">
          <div class="card-header-left">
            <div class="card-icon-wrap purple">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <path d="M6.6 10.8c1.4 2.8 3.8 5.1 6.6 6.6l2.2-2.2c.3-.3.7-.4 1-.2 1.1.4 2.3.6 3.6.6.6 0 1 .4 1 1V20c0 .6-.4 1-1 1-9.4 0-17-7.6-17-17 0-.6.4-1 1-1h3.5c.6 0 1 .4 1 1 0 1.3.2 2.5.6 3.6.1.3 0 .7-.2 1L6.6 10.8z"/>
              </svg>
            </div>
            <div class="card-title-group">
              <span class="card-title">Asterisk Connection</span>
              <span class="card-subtitle">PBX Server (AMI)</span>
            </div>
          </div>
          <!-- 연결 상태 배지: App.vue 전역 클래스 사용 -->
          <span class="status-badge" :class="getConnectionClass(asteriskStatus.connectionState)">
            <span class="badge-dot" :class="asteriskStatus.connectionState === 'CONNECTED' ? 'pulse' : ''"></span>
            {{ asteriskStatus.connectionState }}
          </span>
        </div>
        <div class="card-body">
          <div class="card-row">
            <span class="card-row-label">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="2" y="2" width="20" height="8" rx="2"/>
                <rect x="2" y="14" width="20" height="8" rx="2"/>
                <line x1="6" y1="6" x2="6.01" y2="6"/>
                <line x1="6" y1="18" x2="6.01" y2="18"/>
              </svg>
              Host
            </span>
            <span class="card-row-value mono">{{ asteriskStatus.host }}</span>
          </div>
          <div class="card-divider"></div>
          <div class="card-row">
            <span class="card-row-label">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
              User
            </span>
            <span class="card-row-value mono">{{ asteriskStatus.username }}</span>
          </div>
          <div class="card-row">
            <span class="card-row-label">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="3"/>
                <path d="M12 1v4M12 19v4M4.22 4.22l2.83 2.83M16.95 16.95l2.83 2.83M1 12h4M19 12h4M4.22 19.78l2.83-2.83M16.95 7.05l2.83-2.83"/>
              </svg>
              Version
            </span>
            <span class="card-row-value mono">{{ asteriskStatus.asteriskVersion }}</span>
          </div>
          <div class="card-divider"></div>
          <div class="card-row">
            <span class="card-row-label">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M1 6v16l7-4 8 4 7-4V2l-7 4-8-4-7 4z"/>
                <line x1="8" y1="2" x2="8" y2="18"/>
                <line x1="16" y1="6" x2="16" y2="22"/>
              </svg>
              State
            </span>
            <span class="status-badge" :class="getConnectionClass(asteriskStatus.connectionState)" style="font-size: 11px; padding: 3px 8px;">
              <span class="badge-dot" :class="asteriskStatus.connectionState === 'CONNECTED' ? 'pulse' : ''"></span>
              {{ asteriskStatus.connectionState }}
            </span>
          </div>
        </div>
      </div>

    </div>

    <!-- 빠른 이동 섹션 -->
    <div class="quick-action-section">
      <div class="section-header">
        <span class="section-title">빠른 이동</span>
      </div>
      <div class="action-cards-row">

        <!-- 수동 콜 빠른 이동 카드 -->
        <button type="button" class="action-card" @click="$emit('navigate', 'dial')">
          <div class="action-card-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M6.6 10.8c1.4 2.8 3.8 5.1 6.6 6.6l2.2-2.2c.3-.3.7-.4 1-.2 1.1.4 2.3.6 3.6.6.6 0 1 .4 1 1V20c0 .6-.4 1-1 1-9.4 0-17-7.6-17-17 0-.6.4-1 1-1h3.5c.6 0 1 .4 1 1 0 1.3.2 2.5.6 3.6.1.3 0 .7-.2 1L6.6 10.8z"/>
            </svg>
          </div>
          <div class="action-card-text">
            <div class="action-card-title">수동 콜 (다이얼러)</div>
            <div class="action-card-desc">번호를 입력하여 발신 통화를 시작합니다</div>
          </div>
          <div class="action-card-arrow">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="9 18 15 12 9 6"/>
            </svg>
          </div>
        </button>

        <!-- 녹음 파일 관리 빠른 이동 카드 -->
        <button type="button" class="action-card" @click="$emit('navigate', 'recordings')">
          <div class="action-card-icon green-bg">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 15c1.66 0 3-1.34 3-3V6c0-1.66-1.34-3-3-3S9 4.34 9 6v6c0 1.66 1.34 3 3 3zm5.91-3c-.49 0-.9.36-.98.85C16.52 15.2 14.47 17 12 17s-4.52-1.8-4.93-4.15c-.08-.49-.49-.85-.98-.85-.61 0-1.09.54-1 1.14.49 3 2.89 5.35 5.91 5.78V21c0 .55.45 1 1 1s1-.45 1-1v-2.08c3.02-.43 5.42-2.78 5.91-5.78.1-.6-.39-1.14-1-1.14z"/>
            </svg>
          </div>
          <div class="action-card-text">
            <div class="action-card-title">녹음 파일 관리</div>
            <div class="action-card-desc">저장된 통화 녹음 파일을 조회하고 다운로드합니다</div>
          </div>
          <div class="action-card-arrow">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="9 18 15 12 9 6"/>
            </svg>
          </div>
        </button>

      </div>
    </div>

  </main>
</template>

<script setup>
// 3. defineProps / defineEmits
defineProps({
  appStatus: {
    type: Object,
    required: true
    // { status: String, timestamp: Date|null }
  },
  asteriskStatus: {
    type: Object,
    required: true
    // { host, username, connectionState, asteriskVersion }
  },
  lastUpdated: {
    type: Date,
    default: null
  }
});

defineEmits(['navigate']);

// 7. 일반 함수

/**
 * Application Server 상태 값에 따라 status-badge CSS 클래스를 반환합니다.
 * App.vue 전역 스타일에 정의된 badge-* 클래스를 사용합니다.
 * @param {string} status - 'UP' | 'DOWN' | 'OFFLINE' | 'CHECKING...'
 * @returns {string}
 */
function getStatusClass(status) {
  if (status === 'UP') return 'badge-success';
  if (status === 'CHECKING...') return 'badge-warning';
  return 'badge-error';
}

/**
 * Asterisk 연결 상태 값에 따라 status-badge CSS 클래스를 반환합니다.
 * App.vue 전역 스타일에 정의된 badge-* 클래스를 사용합니다.
 * @param {string} state - 'CONNECTED' | 'UNKNOWN' | 'ERROR' | 'OFFLINE'
 * @returns {string}
 */
function getConnectionClass(state) {
  if (state === 'CONNECTED') return 'badge-success';
  if (state === 'UNKNOWN') return 'badge-warning';
  return 'badge-error';
}

/**
 * Date 객체를 HH:MM:SS 형식의 시간 문자열로 변환합니다.
 * @param {Date|null} timestamp
 * @returns {string}
 */
function formatTime(timestamp) {
  if (!timestamp) return '';
  return new Date(timestamp).toLocaleTimeString('en-GB', { hour12: false });
}
</script>

<style scoped>
/* ==================== 요약 스트립 ==================== */

.summary-strip {
  display: flex;
  gap: 16px;
  margin-bottom: 28px;
}

.summary-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--color-card);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 10px 16px;
  font-size: 13px;
  color: var(--color-text-secondary);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.summary-chip .chip-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.chip-dot.success { background: var(--color-success); }
.chip-dot.error   { background: var(--color-error); }
.chip-dot.warning { background: var(--color-warning); }

.summary-chip .chip-label {
  font-weight: 500;
  color: var(--color-text-primary);
}

.last-updated-bar {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--color-text-secondary);
  background: var(--color-card);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 10px 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.last-updated-bar svg {
  width: 13px;
  height: 13px;
  opacity: 0.6;
}

.last-updated-bar .time-value {
  font-weight: 600;
  color: var(--color-text-primary);
  font-variant-numeric: tabular-nums;
}

/* ==================== 섹션 헤더 ==================== */

.section-header {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 16px;
}

.section-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--color-text-primary);
  letter-spacing: 0.2px;
}

.section-count {
  font-size: 12px;
  color: var(--color-text-secondary);
}

/* ==================== 상태 카드 ==================== */

.cards-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 28px;
}

.status-card {
  background: var(--color-card);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  transition: box-shadow 0.2s, transform 0.2s;
}

.status-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transform: translateY(-1px);
}

.card-header {
  padding: 18px 20px 14px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  border-bottom: 1px solid var(--color-border);
}

.card-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.card-icon-wrap {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.card-icon-wrap.blue {
  background: #eff4ff;
}

.card-icon-wrap.purple {
  background: #f3f0ff;
}

.card-icon-wrap svg {
  width: 20px;
  height: 20px;
}

.card-icon-wrap.blue svg   { color: #4a7cf7; }
.card-icon-wrap.purple svg { color: #7c3aed; }

.card-title-group {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.card-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--color-text-primary);
}

.card-subtitle {
  font-size: 11.5px;
  color: var(--color-text-secondary);
}

.card-body {
  padding: 16px 20px 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.card-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-row-label {
  font-size: 12px;
  color: var(--color-text-secondary);
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
}

.card-row-label svg {
  width: 13px;
  height: 13px;
  opacity: 0.6;
}

.card-row-value {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-primary);
  font-variant-numeric: tabular-nums;
}

.card-row-value.mono {
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
  font-size: 12.5px;
  color: var(--color-text-secondary);
  font-weight: 500;
}

.card-divider {
  height: 1px;
  background: var(--color-border);
  margin: 2px 0;
}

/* ==================== 빠른 이동 카드 ==================== */

.quick-action-section {
  margin-top: 4px;
}

.action-cards-row {
  display: flex;
  gap: 16px;
}

.action-card {
  flex: 1;
  background: var(--color-card);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  text-decoration: none;
  color: inherit;
  transition: box-shadow 0.2s, transform 0.2s, border-color 0.2s;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  text-align: left;
  font-family: inherit;
}

.action-card:hover:not(.action-card--disabled) {
  box-shadow: 0 4px 16px rgba(74, 124, 247, 0.14);
  transform: translateY(-1px);
  border-color: #c3d5fd;
}

/* 미구현 기능 카드: 클릭 불가 */
.action-card--disabled {
  opacity: 0.6;
  cursor: default;
}

.action-card-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: #eff4ff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.action-card-icon svg {
  width: 24px;
  height: 24px;
  color: var(--color-primary);
}

.action-card-icon.green-bg {
  background: var(--color-success-bg);
}

.action-card-icon.green-bg svg {
  color: var(--color-success);
}

.action-card-text {
  flex: 1;
}

.action-card-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: 3px;
}

.action-card-desc {
  font-size: 12px;
  color: var(--color-text-secondary);
  line-height: 1.4;
}

.action-card-arrow {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--color-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: background 0.15s;
}

.action-card:hover:not(.action-card--disabled) .action-card-arrow {
  background: #dce8fd;
}

.action-card-arrow svg {
  width: 14px;
  height: 14px;
  color: var(--color-text-secondary);
}

/* ==================== 반응형 ==================== */

@media (max-width: 900px) {
  .cards-grid {
    grid-template-columns: 1fr;
  }

  .action-cards-row {
    flex-direction: column;
  }
}

@media (max-width: 720px) {
  .summary-strip {
    flex-wrap: wrap;
  }
}
</style>
