<!-- [Composition API - <script setup>] -->
<template>
  <div class="sip-log-panel">

    <!-- ==================== 패널 헤더 ==================== -->
    <div class="log-panel-header">
      <div class="log-panel-header-left">
        <!-- 터미널 아이콘 -->
        <div class="log-panel-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="4 17 10 11 4 5"/>
            <line x1="12" y1="19" x2="20" y2="19"/>
          </svg>
        </div>
        <div class="log-panel-title-group">
          <span class="log-panel-title">SIP 메시지 로그</span>
          <span class="log-panel-subtitle">실시간 SIP 시그널링 모니터</span>
        </div>
        <!-- 총 로그 건수 배지 -->
        <span class="log-count-badge">{{ sipLogs.length }}</span>
      </div>

      <div class="log-panel-header-right">
        <!-- 자동 스크롤 토글 -->
        <label class="autoscroll-toggle" title="자동 스크롤">
          <input type="checkbox" v-model="sipAutoScroll" />
          <div class="autoscroll-toggle-track"></div>
          <span class="autoscroll-label" :class="{ 'autoscroll-label-on': sipAutoScroll }">자동 스크롤</span>
        </label>

        <!-- 로그 초기화 버튼 -->
        <button class="log-clear-btn" @click="clearSipLogs" title="로그 지우기">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="3 6 5 6 21 6"/>
            <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/>
            <path d="M10 11v6M14 11v6"/>
            <path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/>
          </svg>
          지우기
        </button>
      </div>
    </div>
    <!-- /패널 헤더 -->

    <!-- ==================== 필터 바 ==================== -->
    <div class="log-filter-bar">
      <span class="filter-label">필터</span>

      <button class="filter-chip" :class="{ active: sipLogFilter === 'all' }" data-filter="all" @click="sipLogFilter = 'all'">
        All
        <span class="filter-chip-count">{{ sipLogs.length }}</span>
      </button>

      <button class="filter-chip" :class="{ active: sipLogFilter === '1xx' }" data-filter="1xx" @click="sipLogFilter = '1xx'">
        <span class="filter-chip-dot" style="background: #e3b341;"></span>
        1xx
        <span class="filter-chip-count">{{ filterCount('1xx') }}</span>
      </button>

      <button class="filter-chip" :class="{ active: sipLogFilter === '2xx' }" data-filter="2xx" @click="sipLogFilter = '2xx'">
        <span class="filter-chip-dot" style="background: #56d364;"></span>
        2xx
        <span class="filter-chip-count">{{ filterCount('2xx') }}</span>
      </button>

      <button class="filter-chip" :class="{ active: sipLogFilter === '3xx' }" data-filter="3xx" @click="sipLogFilter = '3xx'">
        <span class="filter-chip-dot" style="background: #a8daff;"></span>
        3xx
        <span class="filter-chip-count">{{ filterCount('3xx') }}</span>
      </button>

      <button class="filter-chip" :class="{ active: sipLogFilter === '4xx' }" data-filter="4xx" @click="sipLogFilter = '4xx'">
        <span class="filter-chip-dot" style="background: #ff7b72;"></span>
        4xx
        <span class="filter-chip-count">{{ filterCount('4xx') }}</span>
      </button>

      <button class="filter-chip" :class="{ active: sipLogFilter === '5xx' }" data-filter="5xx" @click="sipLogFilter = '5xx'">
        <span class="filter-chip-dot" style="background: #f85149;"></span>
        5xx
        <span class="filter-chip-count">{{ filterCount('5xx') }}</span>
      </button>

      <button class="filter-chip" :class="{ active: sipLogFilter === 'request' }" data-filter="request" @click="sipLogFilter = 'request'">
        <span class="filter-chip-dot" style="background: #d2a8ff;"></span>
        Request
        <span class="filter-chip-count">{{ filterCount('request') }}</span>
      </button>
    </div>
    <!-- /필터 바 -->

    <!-- ==================== 로그 터미널 영역 ==================== -->
    <div class="log-terminal" ref="containerRef">

      <!-- 빈 상태 (초기) -->
      <div v-if="sipLogs.length === 0" class="log-empty-state">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="4 17 10 11 4 5"/>
          <line x1="12" y1="19" x2="20" y2="19"/>
        </svg>
        <p>SIP 메시지가 없습니다.<br>통화를 시작하면 실시간으로 로그가 표시됩니다.</p>
      </div>

      <!-- 필터 결과 없음 상태 -->
      <div v-else-if="filteredSipLogs.length === 0" class="log-filter-empty">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="11" cy="11" r="8"/>
          <line x1="21" y1="21" x2="16.65" y2="16.65"/>
          <line x1="8" y1="11" x2="14" y2="11"/>
        </svg>
        <p>선택한 필터에 해당하는 메시지가 없습니다.</p>
      </div>

      <!-- 로그 항목 목록 -->
      <template v-else>
        <div
          v-for="entry in filteredSipLogs"
          :key="entry.id"
          class="log-entry expandable"
          :class="{ 'new-entry': entry.isNew }"
          @click="toggleDetail(entry)"
        >
          <!-- 타임스탬프 -->
          <span class="log-ts">{{ entry.timestamp }}</span>
          <!-- 방향 화살표 -->
          <span class="log-dir" :class="entry.direction">
            {{ entry.direction === 'send' ? '&#8593;' : '&#8595;' }}
          </span>
          <!-- 메시지 요약 -->
          <span class="log-msg" :class="getSipTypeClass(entry.summary)">{{ entry.firstLine }}</span>
        </div>
        <!-- 상세 펼침 영역 -->
        <template v-for="entry in filteredSipLogs" :key="'detail-' + entry.id">
          <div v-if="entry.expanded" class="log-detail expanded">
            <pre>{{ entry.raw }}</pre>
          </div>
        </template>
      </template>

    </div>
    <!-- /로그 터미널 영역 -->

    <!-- ==================== 패널 푸터 ==================== -->
    <div class="log-panel-footer">
      <div class="log-footer-left">
        <span class="log-stat">
          <span class="log-stat-dot send"></span>
          <span>송신</span>
          <span class="log-stat-num">{{ sendCount }}</span>
        </span>
        <span class="log-stat">
          <span class="log-stat-dot recv"></span>
          <span>수신</span>
          <span class="log-stat-num">{{ recvCount }}</span>
        </span>
        <!-- 필터 활성 시 결과 건수 표시 -->
        <span v-if="sipLogFilter !== 'all'" class="log-stat-filtered">
          <span>필터 결과</span>
          <span class="log-stat-filtered-num">{{ filteredSipLogs.length }}</span>
          <span>건</span>
        </span>
      </div>
      <div class="log-footer-right">
        <!-- 실시간 인디케이터: SIP 등록 후 표시 -->
        <span v-if="callStatus !== 'Idle'" class="live-indicator">
          <span class="live-dot"></span>
          LIVE
        </span>
      </div>
    </div>
    <!-- /패널 푸터 -->

  </div>
  <!-- /sip-log-panel -->
</template>

<script setup>
// 1. 외부 라이브러리 import
import { ref } from 'vue';

// 2. 내부 모듈 import
import { useSipLog } from '@/composables/useSipLog';

// 3. defineProps
defineProps({
  callStatus: {
    type: String,
    required: true
  }
});

// 4. composable 호출
const containerRef = ref(null);
const {
  sipLogs,
  sipLogFilter,
  sipAutoScroll,
  sendCount,
  recvCount,
  filteredSipLogs,
  handleSipLogConnector,
  toggleDetail,
  clearSipLogs,
  filterCount,
  getSipTypeClass
} = useSipLog(containerRef);

// Dialer.vue가 logConnector를 useSip에 전달할 수 있도록 노출합니다.
defineExpose({ handleSipLogConnector });
</script>

<style scoped>
/* ==================== SIP 로그 패널 ==================== */

.sip-log-panel {
  background: var(--color-card);
  border: 1px solid var(--color-border);
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.07);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-height: 560px;
  max-height: calc(100vh - 180px);
}

/* --- 패널 헤더 --- */

.log-panel-header {
  padding: 14px 18px;
  background: var(--color-card);
  border-bottom: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-shrink: 0;
}

.log-panel-header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.log-panel-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: #0d1117;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.log-panel-icon svg {
  width: 15px;
  height: 15px;
  color: #58a6ff;
}

.log-panel-title-group {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.log-panel-title {
  font-size: 13.5px;
  font-weight: 700;
  color: var(--color-text-primary);
}

.log-panel-subtitle {
  font-size: 11px;
  color: var(--color-text-secondary);
}

.log-panel-header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 로그 카운트 배지 */
.log-count-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: 10px;
  background: #f3f4f6;
  color: var(--color-text-secondary);
  font-size: 10.5px;
  font-weight: 700;
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
}

/* 자동 스크롤 토글 */
.autoscroll-toggle {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  user-select: none;
  padding: 5px 10px;
  border-radius: 7px;
  border: 1px solid var(--color-border);
  background: #f9fafb;
  transition: all 0.15s;
}

.autoscroll-toggle:hover {
  border-color: var(--color-primary);
  background: #f0f5ff;
}

.autoscroll-toggle input[type="checkbox"] {
  display: none;
}

.autoscroll-toggle-track {
  width: 28px;
  height: 16px;
  border-radius: 8px;
  background: #d1d5db;
  position: relative;
  transition: background 0.2s;
  flex-shrink: 0;
}

.autoscroll-toggle-track::after {
  content: '';
  position: absolute;
  top: 2px;
  left: 2px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #ffffff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
  transition: transform 0.2s;
}

.autoscroll-toggle input:checked + .autoscroll-toggle-track {
  background: var(--color-primary);
}

.autoscroll-toggle input:checked + .autoscroll-toggle-track::after {
  transform: translateX(12px);
}

.autoscroll-label {
  font-size: 11.5px;
  font-weight: 500;
  color: var(--color-text-secondary);
  white-space: nowrap;
}

.autoscroll-label-on {
  color: var(--color-primary);
}

/* 로그 초기화 버튼 */
.log-clear-btn {
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
  white-space: nowrap;
  font-family: inherit;
}

.log-clear-btn:hover {
  border-color: var(--color-error-border);
  color: var(--color-error);
  background: var(--color-error-bg);
}

.log-clear-btn svg {
  width: 12px;
  height: 12px;
}

/* --- 필터 바 --- */

.log-filter-bar {
  padding: 10px 18px;
  background: #fafbfc;
  border-bottom: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
  flex-shrink: 0;
}

.filter-label {
  font-size: 10.5px;
  font-weight: 600;
  color: var(--color-text-secondary);
  letter-spacing: 0.5px;
  text-transform: uppercase;
  margin-right: 2px;
  white-space: nowrap;
}

.filter-chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 10px;
  border-radius: 12px;
  border: 1px solid var(--color-border);
  background: #ffffff;
  font-size: 11px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.12s;
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
  user-select: none;
  white-space: nowrap;
}

.filter-chip:hover {
  border-color: #b3c4fd;
  background: #f0f5ff;
}

.filter-chip.active {
  border-color: transparent;
}

.filter-chip-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}

.filter-chip-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 16px;
  height: 15px;
  padding: 0 4px;
  border-radius: 7px;
  background: rgba(0, 0, 0, 0.08);
  font-size: 9.5px;
  font-weight: 700;
  line-height: 1;
}

/* 필터 칩 활성 색상 */
.filter-chip[data-filter="all"].active {
  background: #374151;
  border-color: #374151;
  color: #ffffff;
}
.filter-chip[data-filter="all"].active .filter-chip-count {
  background: rgba(255, 255, 255, 0.2);
  color: #ffffff;
}

.filter-chip[data-filter="1xx"].active {
  background: rgba(227, 179, 65, 0.15);
  border-color: rgba(227, 179, 65, 0.5);
  color: #c98a00;
}
.filter-chip[data-filter="1xx"].active .filter-chip-count {
  background: rgba(227, 179, 65, 0.25);
  color: #c98a00;
}

.filter-chip[data-filter="2xx"].active {
  background: rgba(86, 211, 100, 0.15);
  border-color: rgba(86, 211, 100, 0.5);
  color: #1a7f37;
}
.filter-chip[data-filter="2xx"].active .filter-chip-count {
  background: rgba(86, 211, 100, 0.25);
  color: #1a7f37;
}

.filter-chip[data-filter="3xx"].active {
  background: rgba(168, 218, 255, 0.2);
  border-color: rgba(168, 218, 255, 0.6);
  color: #2471a3;
}
.filter-chip[data-filter="3xx"].active .filter-chip-count {
  background: rgba(168, 218, 255, 0.3);
  color: #2471a3;
}

.filter-chip[data-filter="4xx"].active {
  background: rgba(255, 123, 114, 0.15);
  border-color: rgba(255, 123, 114, 0.5);
  color: #b91c1c;
}
.filter-chip[data-filter="4xx"].active .filter-chip-count {
  background: rgba(255, 123, 114, 0.25);
  color: #b91c1c;
}

.filter-chip[data-filter="5xx"].active {
  background: rgba(248, 81, 73, 0.15);
  border-color: rgba(248, 81, 73, 0.5);
  color: #991b1b;
}
.filter-chip[data-filter="5xx"].active .filter-chip-count {
  background: rgba(248, 81, 73, 0.25);
  color: #991b1b;
}

.filter-chip[data-filter="request"].active {
  background: rgba(210, 168, 255, 0.15);
  border-color: rgba(210, 168, 255, 0.5);
  color: #6d28d9;
}
.filter-chip[data-filter="request"].active .filter-chip-count {
  background: rgba(210, 168, 255, 0.25);
  color: #6d28d9;
}

/* --- 로그 터미널 영역 --- */

.log-terminal {
  flex: 1;
  overflow-y: auto;
  background: #0d1117;
  font-family: 'SF Mono', 'Fira Code', 'Consolas', 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.5;
  padding: 8px 0;
  min-height: 0;
}

/* 스크롤바 스타일 */
.log-terminal::-webkit-scrollbar { width: 6px; }
.log-terminal::-webkit-scrollbar-track { background: transparent; }
.log-terminal::-webkit-scrollbar-thumb { background: rgba(255, 255, 255, 0.12); border-radius: 3px; }
.log-terminal::-webkit-scrollbar-thumb:hover { background: rgba(255, 255, 255, 0.2); }

/* 빈 상태 */
.log-empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  height: 100%;
  min-height: 200px;
  color: #586069;
  padding: 40px 20px;
}

.log-empty-state svg {
  width: 32px;
  height: 32px;
  opacity: 0.3;
}

.log-empty-state p {
  font-size: 12px;
  opacity: 0.5;
  text-align: center;
  line-height: 1.6;
}

/* 필터 결과 없음 */
.log-filter-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 32px 20px;
  color: #586069;
}

.log-filter-empty svg {
  width: 24px;
  height: 24px;
  opacity: 0.25;
}

.log-filter-empty p {
  font-size: 11.5px;
  opacity: 0.5;
  text-align: center;
}

/* --- 로그 항목 --- */

.log-entry {
  display: grid;
  grid-template-columns: 78px 18px 1fr;
  gap: 0 8px;
  align-items: baseline;
  padding: 3px 14px;
  border-bottom: 1px solid transparent;
  transition: background 0.1s;
  cursor: default;
}

.log-entry:hover { background: rgba(255, 255, 255, 0.03); }
.log-entry.expandable { cursor: pointer; }
.log-entry.expandable:hover .log-msg {
  text-decoration: underline;
  text-underline-offset: 2px;
  text-decoration-color: rgba(255, 255, 255, 0.2);
}

/* 새 항목 강조 애니메이션 */
@keyframes logEntryFlash {
  0%   { background: rgba(88, 166, 255, 0.1); }
  100% { background: transparent; }
}

.log-entry.new-entry { animation: logEntryFlash 0.8s ease-out forwards; }

/* 타임스탬프 */
.log-ts {
  font-size: 10.5px;
  color: #6e7681;
  white-space: nowrap;
  letter-spacing: 0.2px;
  user-select: none;
}

/* 방향 화살표 */
.log-dir {
  font-size: 11px;
  font-weight: 700;
  user-select: none;
  display: flex;
  align-items: center;
}

.log-dir.send { color: #58a6ff; }
.log-dir.recv { color: #56d364; }

/* 메시지 본문 요약 */
.log-msg {
  color: #c9d1d9;
  font-size: 11.5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  min-width: 0;
}

/* 상세 펼침 영역 */
.log-detail {
  display: none;
  padding: 4px 14px 8px 36px;
  background: rgba(255, 255, 255, 0.02);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.log-detail.expanded { display: block; }

.log-detail pre {
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
  font-size: 10.5px;
  color: #8b949e;
  white-space: pre-wrap;
  word-break: break-all;
  line-height: 1.6;
}

/* --- 패널 푸터 --- */

.log-panel-footer {
  padding: 7px 18px;
  background: #0d1117;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-shrink: 0;
}

.log-footer-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.log-stat {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 10.5px;
  color: #586069;
}

.log-stat-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
}

.log-stat-dot.send { background: #58a6ff; }
.log-stat-dot.recv { background: #56d364; }

.log-stat-num {
  color: #c9d1d9;
  font-weight: 600;
}

/* 필터 결과 건수 */
.log-stat-filtered {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 10.5px;
  color: #586069;
  padding-left: 10px;
  border-left: 1px solid rgba(255, 255, 255, 0.08);
}

.log-stat-filtered-num {
  color: #e3b341;
  font-weight: 600;
}

.log-footer-right {
  font-size: 10.5px;
  color: #586069;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 실시간 인디케이터 */
@keyframes livePulse {
  0%, 100% { opacity: 1; }
  50%       { opacity: 0.3; }
}

.live-indicator {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 10.5px;
  font-weight: 600;
  color: #56d364;
}

.live-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #56d364;
  animation: livePulse 1.2s ease-in-out infinite;
}
</style>
