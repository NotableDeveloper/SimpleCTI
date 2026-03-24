<template>
  <!-- ==================== 통화 이력 페이지 ==================== -->
  <main class="page-body">

    <!-- 요약 스트립 -->
    <div class="summary-strip">
      <div class="summary-chip">
        <span class="chip-dot primary"></span>
        <span class="chip-label">총 통화</span>
        <span>{{ totalElements }}건</span>
      </div>
      <div class="summary-chip">
        <span class="chip-dot success"></span>
        <span class="chip-label">응답</span>
        <span>{{ answeredCount }}건</span>
      </div>
      <div class="summary-chip">
        <span class="chip-dot error"></span>
        <span class="chip-label">미응답</span>
        <span>{{ unansweredCount }}건</span>
      </div>
      <div class="last-updated-bar" v-if="lastFetchedAt">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
             stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="10"/>
          <polyline points="12 6 12 12 16 14"/>
        </svg>
        Last Updated:&nbsp;<span class="time-value">{{ lastFetchedAt }}</span>
      </div>
    </div>

    <!-- 필터 바 -->
    <div class="cdr-filter-bar">
      <!-- 번호 검색 -->
      <div class="filter-input-wrap">
        <svg class="filter-input-icon" viewBox="0 0 24 24" fill="none"
             stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="11" cy="11" r="8"/>
          <line x1="21" y1="21" x2="16.65" y2="16.65"/>
        </svg>
        <input
          id="cdr-search-number"
          v-model="searchNumber"
          class="filter-input"
          type="text"
          placeholder="발신·수신 번호 검색..."
          @keydown.enter="applyFilter"
        />
      </div>

      <!-- 상태 드롭다운 -->
      <select id="cdr-search-disposition" v-model="searchDisposition" class="filter-select">
        <option
          v-for="opt in DISPOSITION_OPTIONS"
          :key="opt.value"
          :value="opt.value"
        >{{ opt.label }}</option>
      </select>

      <!-- 검색 버튼 -->
      <button id="cdr-search-btn" class="filter-search-btn" @click="applyFilter">
        검색
      </button>
    </div>

    <!-- 섹션 헤더 -->
    <div class="section-header">
      <span class="section-title">통화 이력</span>
      <span class="section-count">{{ totalElements }}건</span>
    </div>

    <!-- 로딩 -->
    <div v-if="loading" class="cdr-loading">
      <div class="cdr-spinner"></div>
      <span>불러오는 중...</span>
    </div>

    <!-- 에러 -->
    <div v-else-if="errorMessage" class="cdr-error">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
           stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="10"/>
        <line x1="12" y1="8" x2="12" y2="12"/>
        <line x1="12" y1="16" x2="12.01" y2="16"/>
      </svg>
      {{ errorMessage }}
    </div>

    <!-- 테이블 -->
    <div v-else class="cdr-table-wrap">
      <table class="cdr-table">
        <thead>
          <tr>
            <th>통화 일시</th>
            <th>발신번호</th>
            <th>수신번호</th>
            <th>상태</th>
            <th>통화시간</th>
            <th>과금시간</th>
            <th class="col-recording">녹음</th>
          </tr>
        </thead>
        <tbody>
          <!-- 데이터 없음 -->
          <tr v-if="records.length === 0">
            <td colspan="7" class="cdr-empty">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"
                   stroke-linecap="round" stroke-linejoin="round">
                <path d="M9 17H5a2 2 0 0 0-2 2v2h18v-2a2 2 0 0 0-2-2h-4"/>
                <path d="M12 3v10M9 10l3 3 3-3"/>
              </svg>
              조회된 통화 이력이 없습니다.
            </td>
          </tr>
          <!-- CDR 행 -->
          <tr v-for="cdr in records" :key="cdr.id" class="cdr-row">
            <td class="cell-calldate">{{ formatCalldate(cdr.calldate) }}</td>
            <td class="cell-number">{{ cdr.src || '—' }}</td>
            <td class="cell-number">{{ cdr.dst || '—' }}</td>
            <td>
              <span class="status-badge" :class="dispositionBadgeClass(cdr.disposition)">
                <span class="badge-dot"></span>
                {{ cdr.disposition || '—' }}
              </span>
            </td>
            <td class="cell-duration">{{ formatDuration(cdr.duration) }}</td>
            <td class="cell-duration">{{ formatDuration(cdr.billsec) }}</td>
            <td class="col-recording">
              <!-- 녹음 파일이 있을 때만 다운로드 버튼 활성 -->
              <button
                v-if="cdr.recordingFile"
                class="cdr-rec-btn"
                :title="cdr.recordingFile"
                @click="downloadRecording(cdr.recordingFile)"
              >
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                     stroke-linecap="round" stroke-linejoin="round">
                  <path d="M12 15c1.66 0 3-1.34 3-3V6c0-1.66-1.34-3-3-3S9 4.34 9 6v6c0 1.66 1.34 3 3 3z"/>
                  <path d="M19 10c0 3.87-3.13 7-7 7s-7-3.13-7-7"/>
                  <line x1="12" y1="19" x2="12" y2="23"/>
                  <line x1="8" y1="23" x2="16" y2="23"/>
                </svg>
              </button>
              <span v-else class="cdr-rec-none">—</span>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- 페이지네이션 -->
      <div class="cdr-pagination" v-if="totalElements > 0">
        <span class="pagination-info">{{ summaryText }}</span>
        <div class="pagination-buttons">
          <button
            id="cdr-prev-btn"
            class="pagination-btn"
            :disabled="!hasPrev"
            @click="prevPage"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"
                 stroke-linecap="round" stroke-linejoin="round">
              <polyline points="15 18 9 12 15 6"/>
            </svg>
          </button>
          <span class="pagination-page">{{ currentPage + 1 }} / {{ totalPages }}</span>
          <button
            id="cdr-next-btn"
            class="pagination-btn"
            :disabled="!hasNext"
            @click="nextPage"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"
                 stroke-linecap="round" stroke-linejoin="round">
              <polyline points="9 18 15 12 9 6"/>
            </svg>
          </button>
        </div>
      </div>
    </div>

  </main>
</template>

<script setup>
// 1. 외부 라이브러리 import
import { ref, computed, onMounted } from 'vue'

// 2. 내부 모듈 import
import { useCdr, DISPOSITION_OPTIONS } from '@/composables/useCdr'

// 4. composable 호출
const {
  records,
  totalElements,
  totalPages,
  currentPage,
  loading,
  errorMessage,
  searchNumber,
  searchDisposition,
  hasPrev,
  hasNext,
  summaryText,
  fetchCdr,
  applyFilter,
  prevPage,
  nextPage,
  formatDuration,
  formatCalldate,
  dispositionBadgeClass,
} = useCdr()

// 5. 로컬 state

/** 마지막 조회 시각 표시 문자열 */
const lastFetchedAt = ref('')

// 6. computed

/** 현재 페이지의 ANSWERED 건수 */
const answeredCount = computed(() =>
  records.value.filter(r => r.disposition?.toUpperCase() === 'ANSWERED').length
)

/** 현재 페이지의 미응답(NO ANSWER, BUSY, FAILED) 건수 */
const unansweredCount = computed(() =>
  records.value.filter(r => {
    const d = r.disposition?.toUpperCase() ?? ''
    return d === 'NO ANSWER' || d === 'BUSY' || d === 'FAILED'
  }).length
)

// 7. 일반 함수

/**
 * 녹음 파일 다운로드 요청을 전송한다.
 * 기존 Recordings.vue의 다운로드 방식을 따른다.
 *
 * @param {string} filename 녹음 파일명
 */
function downloadRecording(filename) {
  const url = `${process.env.VUE_APP_API_BASE_URL}/api/recordings/${encodeURIComponent(filename)}`
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
}

// 8. lifecycle hooks

onMounted(async () => {
  await fetchCdr(0)
  lastFetchedAt.value = new Date().toLocaleTimeString('en-GB', { hour12: false })
})
</script>

<style scoped>
/* ==================== 요약 스트립 ==================== */

.summary-strip {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;
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

.chip-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.chip-dot.primary { background: var(--color-primary); }
.chip-dot.success { background: var(--color-success); }
.chip-dot.error   { background: var(--color-error); }

.chip-label {
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

.time-value {
  font-weight: 600;
  color: var(--color-text-primary);
  font-variant-numeric: tabular-nums;
}

/* ==================== 필터 바 ==================== */

.cdr-filter-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 24px;
  align-items: center;
  flex-wrap: wrap;
}

.filter-input-wrap {
  position: relative;
  flex: 1;
  min-width: 200px;
}

.filter-input-icon {
  position: absolute;
  left: 10px;
  top: 50%;
  transform: translateY(-50%);
  width: 14px;
  height: 14px;
  color: var(--color-text-secondary);
  pointer-events: none;
}

.filter-input {
  width: 100%;
  padding: 8px 12px 8px 32px;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  font-size: 13px;
  background: var(--color-card);
  color: var(--color-text-primary);
  outline: none;
  transition: border-color 0.15s;
}

.filter-input:focus {
  border-color: var(--color-primary);
}

.filter-select {
  padding: 8px 12px;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  font-size: 13px;
  background: var(--color-card);
  color: var(--color-text-primary);
  outline: none;
  cursor: pointer;
  transition: border-color 0.15s;
  min-width: 140px;
}

.filter-select:focus {
  border-color: var(--color-primary);
}

.filter-search-btn {
  padding: 8px 20px;
  background: var(--color-primary);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
  white-space: nowrap;
}

.filter-search-btn:hover {
  background: var(--color-primary-hover);
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
}

.section-count {
  font-size: 12px;
  color: var(--color-text-secondary);
}

/* ==================== 로딩 / 에러 ==================== */

.cdr-loading {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 40px;
  justify-content: center;
  color: var(--color-text-secondary);
  font-size: 14px;
}

.cdr-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.cdr-error {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 20px;
  background: var(--color-error-bg);
  border: 1px solid var(--color-error-border);
  border-radius: 10px;
  color: var(--color-error);
  font-size: 13.5px;
}

.cdr-error svg {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

/* ==================== 테이블 ==================== */

.cdr-table-wrap {
  background: var(--color-card);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.cdr-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.cdr-table thead tr {
  background: #f8f9fb;
  border-bottom: 1px solid var(--color-border);
}

.cdr-table th {
  padding: 11px 16px;
  text-align: left;
  font-size: 11.5px;
  font-weight: 600;
  color: var(--color-text-secondary);
  letter-spacing: 0.3px;
  white-space: nowrap;
}

.cdr-table td {
  padding: 12px 16px;
  color: var(--color-text-primary);
  border-bottom: 1px solid var(--color-border);
  vertical-align: middle;
}

.cdr-row:last-child td {
  border-bottom: none;
}

.cdr-row:hover td {
  background: #f8f9fb;
}

.cell-calldate {
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
  font-size: 12.5px;
  color: var(--color-text-secondary);
  white-space: nowrap;
}

.cell-number {
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
  font-size: 12.5px;
  font-weight: 500;
}

.cell-duration {
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
  font-size: 12.5px;
  text-align: right;
  color: var(--color-text-secondary);
  white-space: nowrap;
}

.col-recording {
  text-align: center;
  width: 60px;
}

/* ==================== 녹음 버튼 ==================== */

.cdr-rec-btn {
  width: 30px;
  height: 30px;
  border-radius: 7px;
  border: 1px solid var(--color-border);
  background: var(--color-card);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--color-primary);
  transition: background 0.15s, border-color 0.15s;
}

.cdr-rec-btn:hover {
  background: #eff4ff;
  border-color: var(--color-primary);
}

.cdr-rec-btn svg {
  width: 14px;
  height: 14px;
}

.cdr-rec-none {
  color: var(--color-text-secondary);
  opacity: 0.5;
}

/* ==================== 데이터 없음 ==================== */

.cdr-empty {
  text-align: center;
  padding: 48px 20px;
  color: var(--color-text-secondary);
  font-size: 14px;
}

.cdr-empty svg {
  display: block;
  margin: 0 auto 12px;
  width: 32px;
  height: 32px;
  opacity: 0.35;
}

/* ==================== 페이지네이션 ==================== */

.cdr-pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  border-top: 1px solid var(--color-border);
  background: #f8f9fb;
}

.pagination-info {
  font-size: 12.5px;
  color: var(--color-text-secondary);
  font-variant-numeric: tabular-nums;
}

.pagination-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination-page {
  font-size: 12.5px;
  color: var(--color-text-primary);
  font-weight: 600;
  min-width: 48px;
  text-align: center;
}

.pagination-btn {
  width: 30px;
  height: 30px;
  border-radius: 7px;
  border: 1px solid var(--color-border);
  background: var(--color-card);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--color-text-secondary);
  transition: all 0.15s;
}

.pagination-btn:hover:not(:disabled) {
  border-color: var(--color-primary);
  color: var(--color-primary);
  background: #eff4ff;
}

.pagination-btn:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.pagination-btn svg {
  width: 13px;
  height: 13px;
}

/* ==================== 반응형 ==================== */

@media (max-width: 900px) {
  .cdr-filter-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-input-wrap {
    min-width: unset;
  }
}

@media (max-width: 720px) {
  .summary-strip {
    flex-wrap: wrap;
  }

  .last-updated-bar {
    margin-left: 0;
  }
}
</style>
