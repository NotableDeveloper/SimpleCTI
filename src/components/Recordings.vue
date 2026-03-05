<template>
  <div class="recordings-page">

    <!-- ==================== 상단 바 ==================== -->
    <header class="topbar">
      <div class="topbar-title">
        <span class="topbar-heading">녹음 파일 관리</span>
        <span class="topbar-breadcrumb">Simple CTI &rsaquo; 녹음 파일</span>
      </div>
      <div class="topbar-actions">
        <!-- 새로고침 버튼: loadRecordings 호출, 클릭 시 아이콘 회전 애니메이션 -->
        <button
          class="refresh-btn"
          :class="{ spinning: isRefreshing }"
          @click="loadRecordings"
        >
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <polyline points="23 4 23 10 17 10"/>
            <polyline points="1 20 1 14 7 14"/>
            <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
          </svg>
          새로고침
        </button>
      </div>
    </header>

    <!-- ==================== 페이지 본문 ==================== -->
    <main class="recordings-page-body">

      <!-- 통계 strip: 총 파일 수, 선택된 파일 수 실시간 표시 -->
      <div class="stats-strip">
        <!-- 전체 파일 수 chip -->
        <div class="stat-chip">
          <div class="stat-chip-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
            </svg>
          </div>
          <div class="stat-chip-body">
            <span class="stat-chip-value">{{ recordings.length }}</span>
            <span class="stat-chip-label">전체 파일</span>
          </div>
        </div>

        <!-- 선택된 파일 수 chip -->
        <div class="stat-chip">
          <div class="stat-chip-icon green">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="20 6 9 17 4 12"/>
            </svg>
          </div>
          <div class="stat-chip-body">
            <span class="stat-chip-value">{{ selectedRecordings.length }}</span>
            <span class="stat-chip-label">선택됨</span>
          </div>
        </div>

        <!-- 파일 형식 chip -->
        <div class="stat-chip">
          <div class="stat-chip-icon orange">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 15c1.66 0 3-1.34 3-3V6c0-1.66-1.34-3-3-3S9 4.34 9 6v6c0 1.66 1.34 3 3 3z"/>
              <path d="M17.91 12c-.49 0-.9.36-.98.85C16.52 15.2 14.47 17 12 17s-4.52-1.8-4.93-4.15c-.08-.49-.49-.85-.98-.85-.61 0-1.09.54-1 1.14.49 3 2.89 5.35 5.91 5.78V21c0 .55.45 1 1 1s1-.45 1-1v-2.08c3.02-.43 5.42-2.78 5.91-5.78.1-.6-.39-1.14-1-1.14z"/>
            </svg>
          </div>
          <div class="stat-chip-body">
            <span class="stat-chip-value">WAV</span>
            <span class="stat-chip-label">파일 형식</span>
          </div>
        </div>
      </div>

      <!-- 테이블 툴바: 전체 선택 체크박스 + 선택 건수 + 선택 다운로드 버튼 -->
      <div class="table-toolbar">
        <div class="toolbar-left">
          <span class="section-title">파일 목록</span>
          <span class="selection-info" :class="{ 'has-selection': selectedRecordings.length > 0 }">
            {{ selectedRecordings.length }}개 선택됨
          </span>
        </div>
        <div class="toolbar-right">
          <!-- 선택된 파일이 없으면 disabled -->
          <button
            class="btn btn-primary"
            :disabled="selectedRecordings.length === 0"
            @click="downloadSelected"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
              <polyline points="7 10 12 15 17 10"/>
              <line x1="12" y1="15" x2="12" y2="3"/>
            </svg>
            선택 다운로드
          </button>
        </div>
      </div>

      <!-- 파일 테이블 카드 -->
      <div class="table-card">

        <!-- 파일이 있을 때: 테이블 렌더링 -->
        <template v-if="parsedRecordings.length > 0">
          <table class="rec-table">
            <thead>
              <tr>
                <th class="col-checkbox">
                  <!-- 전체 선택 체크박스: computed getter/setter로 제어 -->
                  <input
                    type="checkbox"
                    class="custom-checkbox"
                    :checked="isAllSelected"
                    :indeterminate.prop="isIndeterminate"
                    @change="isAllSelected = $event.target.checked"
                  />
                </th>
                <th>파일명</th>
                <th class="col-ext">형식</th>
                <th class="col-datetime">날짜 / 시간</th>
                <th class="col-action">다운로드</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="item in parsedRecordings"
                :key="item.filename"
                :class="{ 'row-selected': selectedRecordings.includes(item.filename) }"
              >
                <!-- 체크박스 열 -->
                <td class="col-checkbox">
                  <input
                    type="checkbox"
                    class="custom-checkbox"
                    :value="item.filename"
                    v-model="selectedRecordings"
                  />
                </td>

                <!-- 파일명 열 (monospace) -->
                <td>
                  <div class="filename-cell">
                    <div class="file-icon-wrap">
                      <!-- 오디오 파일 아이콘 -->
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M9 18V5l12-2v13"/>
                        <circle cx="6" cy="18" r="3"/>
                        <circle cx="18" cy="16" r="3"/>
                      </svg>
                    </div>
                    <span class="filename-text">{{ item.filename }}</span>
                  </div>
                </td>

                <!-- 확장자 뱃지 열 -->
                <td class="col-ext">
                  <span class="ext-badge wav">WAV</span>
                </td>

                <!-- 날짜/시간 열: 파일명 파싱 결과 표시 -->
                <td class="col-datetime">
                  <div class="datetime-cell">
                    <span class="datetime-date">{{ item.datetime.date }}</span>
                    <span class="datetime-time">{{ item.datetime.time }}</span>
                  </div>
                </td>

                <!-- 개별 다운로드 버튼 열 -->
                <td class="col-action">
                  <button class="btn-icon-sm" @click="downloadFile(item.filename)">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                      <polyline points="7 10 12 15 17 10"/>
                      <line x1="12" y1="15" x2="12" y2="3"/>
                    </svg>
                    다운로드
                  </button>
                </td>
              </tr>
            </tbody>
          </table>

          <!-- 테이블 푸터: 총 파일 수 + 선택된 파일 수 -->
          <div class="table-footer">
            <span class="table-footer-info">
              총 <strong>{{ recordings.length }}</strong>개 파일 &bull; <strong>{{ selectedRecordings.length }}</strong>개 선택됨
            </span>
            <span class="table-footer-info">
              마지막 조회: <strong>{{ lastUpdated || '--:--:--' }}</strong>
            </span>
          </div>
        </template>

        <!-- 빈 상태 UI: recordings 배열이 비어있을 때 표시 -->
        <div v-else class="empty-state">
          <div class="empty-icon-wrap">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 15c1.66 0 3-1.34 3-3V6c0-1.66-1.34-3-3-3S9 4.34 9 6v6c0 1.66 1.34 3 3 3z"/>
              <path d="M17.91 12c-.49 0-.9.36-.98.85C16.52 15.2 14.47 17 12 17s-4.52-1.8-4.93-4.15c-.08-.49-.49-.85-.98-.85-.61 0-1.09.54-1 1.14.49 3 2.89 5.35 5.91 5.78V21c0 .55.45 1 1 1s1-.45 1-1v-2.08c3.02-.43 5.42-2.78 5.91-5.78.1-.6-.39-1.14-1-1.14z"/>
            </svg>
          </div>
          <div class="empty-title">녹음 파일이 없습니다</div>
          <div class="empty-desc">
            통화 녹음이 활성화된 상태에서 발신 통화를 진행하면<br>
            이 목록에 파일이 표시됩니다.
          </div>
          <button class="btn btn-outline" @click="loadRecordings">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="23 4 23 10 17 10"/>
              <polyline points="1 20 1 14 7 14"/>
              <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
            </svg>
            새로고침
          </button>
        </div>

      </div>
      <!-- /table-card -->

    </main>
    <!-- /recordings-page-body -->

  </div>
  <!-- /recordings-page -->
</template>

<script>
export default {
  name: 'RecordingsPage',
  emits: ['back'],
  data() {
    return {
      /** @type {string[]} 서버에서 가져온 파일명 배열 (내림차순 정렬) */
      recordings: [],
      /** @type {string[]} 체크박스로 선택된 파일명 배열 */
      selectedRecordings: [],
      /** @type {boolean} 새로고침 버튼 스피닝 애니메이션 진행 여부 */
      isRefreshing: false,
      /** @type {string} 마지막 목록 조회 시간 (HH:MM:SS) */
      lastUpdated: ''
    }
  },
  computed: {
    /**
     * 전체 선택 체크박스의 checked 상태를 get/set으로 제어합니다.
     * get: 모든 파일이 선택되었을 때 true
     * set: true이면 전체 선택, false이면 전체 해제
     */
    isAllSelected: {
      get() {
        return this.recordings.length > 0 && this.selectedRecordings.length === this.recordings.length;
      },
      set(value) {
        this.selectedRecordings = value ? [...this.recordings] : [];
      }
    },

    /**
     * 일부만 선택된 상태(indeterminate)를 반환합니다.
     * 전체 선택도 아니고 전체 해제도 아닐 때 true입니다.
     * @returns {boolean}
     */
    isIndeterminate() {
      return this.selectedRecordings.length > 0 && this.selectedRecordings.length < this.recordings.length;
    },

    /**
     * recordings 배열을 파싱하여 { filename, datetime } 객체 배열로 변환합니다.
     * @returns {{ filename: string, datetime: { date: string, time: string } }[]}
     */
    parsedRecordings() {
      return this.recordings.map(filename => ({
        filename,
        datetime: this.parseFileDatetime(filename)
      }));
    }
  },
  methods: {
    /**
     * 서버에서 녹음 파일 목록을 가져옵니다.
     * GET /api/recordings/list 호출 후 내림차순 정렬하여 recordings에 저장합니다.
     * 새로고침 버튼의 스피닝 애니메이션은 isRefreshing 플래그로 제어합니다.
     */
    async loadRecordings() {
      // 이미 로딩 중이면 중복 호출 방지
      if (this.isRefreshing) return;

      this.isRefreshing = true;

      try {
        const response = await fetch('/api/recordings/list');
        if (!response.ok) {
          throw new Error(`서버 응답 오류: ${response.status}`);
        }
        const files = await response.json();
        // 내림차순 정렬: 최신 파일이 상단에 표시
        this.recordings = files.sort().reverse();
        // 선택 상태 초기화: 목록 갱신 시 기존 선택이 유효하지 않을 수 있음
        this.selectedRecordings = [];
        // 마지막 조회 시간 갱신
        this.lastUpdated = this.getCurrentTimeString();
      } catch (error) {
        console.error('녹음 파일 목록 로드 오류:', error);
      } finally {
        // 애니메이션이 눈에 보이도록 최소 700ms 유지
        setTimeout(() => {
          this.isRefreshing = false;
        }, 700);
      }
    },

    /**
     * 선택된 녹음 파일들을 각각 개별 다운로드합니다.
     * Dialer.vue의 downloadSelected 로직과 동일한 방식으로 동작합니다.
     */
    downloadSelected() {
      if (this.selectedRecordings.length === 0) return;

      this.selectedRecordings.forEach(filename => {
        this.downloadFile(filename);
      });
    },

    /**
     * 단일 파일을 다운로드합니다.
     * 동적으로 <a> 요소를 생성하여 클릭 후 즉시 제거합니다.
     * @param {string} filename - 다운로드할 파일명
     */
    downloadFile(filename) {
      const link = document.createElement('a');
      link.href = '/api/recordings/' + encodeURIComponent(filename);
      link.download = filename;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    },

    /**
     * 파일명에서 날짜와 시간을 파싱합니다.
     * 지원 형식: YYYYMMDD-HHmmss.wav (예: 20240315-143022.wav → 2024-03-15 / 14:30:22)
     * 형식이 맞지 않으면 date와 time 모두 파일명을 그대로 반환합니다.
     * @param {string} filename - 파싱할 파일명
     * @returns {{ date: string, time: string }}
     */
    parseFileDatetime(filename) {
      // 확장자 제거 후 날짜-시간 분리
      const base = filename.replace(/\.wav$/i, '');
      const parts = base.split('-');

      // YYYYMMDD-HHmmss 형식 검증: 하이픈으로 분리된 두 파트가 각각 8자리, 6자리여야 함
      if (parts.length < 2) {
        return { date: filename, time: '' };
      }

      const datePart = parts[0];
      const timePart = parts[1];

      if (datePart.length !== 8 || timePart.length !== 6 || !/^\d+$/.test(datePart) || !/^\d+$/.test(timePart)) {
        return { date: filename, time: '' };
      }

      const date = `${datePart.slice(0, 4)}-${datePart.slice(4, 6)}-${datePart.slice(6, 8)}`;
      const time = `${timePart.slice(0, 2)}:${timePart.slice(2, 4)}:${timePart.slice(4, 6)}`;

      return { date, time };
    },

    /**
     * 현재 시각을 HH:MM:SS 형식의 문자열로 반환합니다.
     * @returns {string}
     */
    getCurrentTimeString() {
      const now = new Date();
      const hh = String(now.getHours()).padStart(2, '0');
      const mm = String(now.getMinutes()).padStart(2, '0');
      const ss = String(now.getSeconds()).padStart(2, '0');
      return `${hh}:${mm}:${ss}`;
    }
  },
  mounted() {
    // 컴포넌트 마운트 시 녹음 파일 목록 자동 로드
    this.loadRecordings();
  }
}
</script>

<style scoped>
/*
 * Recordings.vue 전용 스타일
 * App.vue의 :root CSS 변수를 그대로 참조합니다. 중복 선언하지 않습니다.
 */

/* ==================== 페이지 래퍼 ==================== */

/*
 * App.vue의 .page-body--recordings (padding: 24px 32px) 내에 렌더링되므로,
 * 음수 마진으로 부모 패딩을 상쇄하여 topbar가 full-width sticky로 동작하도록 합니다.
 * Dialer.vue의 .dialer-page와 동일한 패턴입니다.
 */
.recordings-page {
  margin: -24px -32px;
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 60px); /* 60px = App.vue topbar 높이 */
}

/* ==================== topbar ==================== */

.topbar {
  background: var(--color-card);
  border-bottom: 1px solid var(--color-border);
  padding: 0 32px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  top: 60px; /* App.vue topbar 아래에 위치 */
  z-index: 40;
}

.topbar-title {
  display: flex;
  flex-direction: column;
}

.topbar-heading {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-text-primary);
  line-height: 1.3;
}

.topbar-breadcrumb {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.topbar-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* ==================== 새로고침 버튼 ==================== */

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 7px 14px;
  border-radius: 7px;
  border: 1px solid var(--color-border);
  background: var(--color-card);
  color: var(--color-text-secondary);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
  font-family: inherit;
}

.refresh-btn:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
  background: #f0f5ff;
}

.refresh-btn svg {
  width: 14px;
  height: 14px;
  transition: transform 0.7s ease;
}

/* spinning 클래스가 붙어있는 동안 아이콘이 360도 회전 */
.refresh-btn.spinning svg {
  animation: spin-once 0.7s ease-in-out;
}

@keyframes spin-once {
  from { transform: rotate(0deg); }
  to   { transform: rotate(360deg); }
}

/* ==================== 페이지 본문 ==================== */

.recordings-page-body {
  padding: 28px 32px;
  flex: 1;
}

/* ==================== 통계 strip ==================== */

.stats-strip {
  display: flex;
  gap: 14px;
  margin-bottom: 24px;
}

.stat-chip {
  display: flex;
  align-items: center;
  gap: 10px;
  background: var(--color-card);
  border: 1px solid var(--color-border);
  border-radius: 10px;
  padding: 12px 18px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  min-width: 0;
}

.stat-chip-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: #eff4ff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-chip-icon svg {
  width: 18px;
  height: 18px;
  color: var(--color-primary);
}

/* 선택됨 chip 아이콘: 녹색 계열 */
.stat-chip-icon.green {
  background: var(--color-success-bg);
}

.stat-chip-icon.green svg {
  color: var(--color-success);
}

/* 파일 형식 chip 아이콘: 주황색 계열 */
.stat-chip-icon.orange {
  background: #fff4e6;
}

.stat-chip-icon.orange svg {
  color: #e07a10;
}

.stat-chip-body {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.stat-chip-value {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-text-primary);
  font-variant-numeric: tabular-nums;
  line-height: 1.2;
}

.stat-chip-label {
  font-size: 11.5px;
  color: var(--color-text-secondary);
  font-weight: 500;
}

/* ==================== 테이블 툴바 ==================== */

.table-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  gap: 12px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.section-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--color-text-primary);
  letter-spacing: 0.2px;
}

/* 선택 건수 뱃지 */
.selection-info {
  font-size: 12.5px;
  color: var(--color-text-secondary);
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  border-radius: 20px;
  padding: 3px 10px;
  font-variant-numeric: tabular-nums;
  transition: all 0.2s;
}

/* 선택이 있을 때: 파란색 강조 */
.selection-info.has-selection {
  background: #eff4ff;
  border-color: #c3d5fd;
  color: var(--color-primary);
  font-weight: 600;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* ==================== 버튼 ==================== */

.btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
  border: none;
  white-space: nowrap;
  font-family: inherit;
}

.btn svg {
  width: 14px;
  height: 14px;
  flex-shrink: 0;
}

/* 선택 다운로드 버튼 (primary) */
.btn-primary {
  background: var(--color-primary);
  color: #ffffff;
  border: 1px solid transparent;
}

.btn-primary:hover:not(:disabled) {
  background: var(--color-primary-hover);
}

.btn-primary:disabled {
  background: #c5d5fb;
  color: #8aabf5;
  cursor: not-allowed;
}

/* 빈 상태 새로고침 버튼 (outline) */
.btn-outline {
  background: var(--color-card);
  color: var(--color-text-secondary);
  border: 1px solid var(--color-border);
}

.btn-outline:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
  background: #f0f5ff;
}

/* 개별 다운로드 버튼 (소형) */
.btn-icon-sm {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  padding: 5px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
  border: 1px solid var(--color-border);
  background: var(--color-card);
  color: var(--color-text-secondary);
  white-space: nowrap;
  font-family: inherit;
}

.btn-icon-sm svg {
  width: 13px;
  height: 13px;
}

.btn-icon-sm:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
  background: #f0f5ff;
}

/* ==================== 테이블 카드 ==================== */

.table-card {
  background: var(--color-card);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.rec-table {
  width: 100%;
  border-collapse: collapse;
}

.rec-table thead {
  background: #f8f9fc;
  border-bottom: 1px solid var(--color-border);
}

.rec-table thead th {
  padding: 12px 16px;
  font-size: 11.5px;
  font-weight: 700;
  color: var(--color-text-secondary);
  text-align: left;
  letter-spacing: 0.4px;
  text-transform: uppercase;
}

.rec-table thead th.col-checkbox {
  width: 44px;
  text-align: center;
}

.rec-table thead th.col-ext {
  width: 80px;
}

.rec-table thead th.col-datetime {
  width: 180px;
}

.rec-table thead th.col-action {
  width: 110px;
  text-align: center;
}

.rec-table tbody tr {
  border-bottom: 1px solid #f0f2f5;
  transition: background 0.1s;
}

.rec-table tbody tr:last-child {
  border-bottom: none;
}

.rec-table tbody tr:hover {
  background: #f8f9fc;
}

/* 선택된 행: 파란색 배경 강조 */
.rec-table tbody tr.row-selected {
  background: #f0f5ff;
}

.rec-table tbody tr.row-selected:hover {
  background: #e8f0fe;
}

.rec-table tbody td {
  padding: 13px 16px;
  font-size: 13px;
  color: var(--color-text-primary);
  vertical-align: middle;
}

.rec-table tbody td.col-checkbox {
  text-align: center;
}

.rec-table tbody td.col-action {
  text-align: center;
}

/* ==================== 커스텀 체크박스 ==================== */

.custom-checkbox {
  appearance: none;
  -webkit-appearance: none;
  width: 16px;
  height: 16px;
  border: 2px solid var(--color-border);
  border-radius: 4px;
  background: #ffffff;
  cursor: pointer;
  transition: all 0.15s;
  position: relative;
  vertical-align: middle;
  flex-shrink: 0;
}

.custom-checkbox:hover {
  border-color: var(--color-primary);
}

.custom-checkbox:checked {
  background: var(--color-primary);
  border-color: var(--color-primary);
}

/* 체크 표시 (::after 가상 요소) */
.custom-checkbox:checked::after {
  content: '';
  position: absolute;
  top: 2px;
  left: 4px;
  width: 4px;
  height: 7px;
  border: 2px solid #ffffff;
  border-top: none;
  border-left: none;
  transform: rotate(45deg);
}

/* indeterminate 상태: 파란 배경 + 가로선 */
.custom-checkbox:indeterminate {
  background: var(--color-primary);
  border-color: var(--color-primary);
}

.custom-checkbox:indeterminate::after {
  content: '';
  position: absolute;
  top: 5px;
  left: 2px;
  width: 8px;
  height: 2px;
  background: #ffffff;
  border-radius: 1px;
}

/* ==================== 파일명 셀 ==================== */

.filename-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.file-icon-wrap {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: #fff4e6;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.file-icon-wrap svg {
  width: 16px;
  height: 16px;
  color: #e07a10;
}

/* 파일명 텍스트: monospace 폰트 적용 */
.filename-text {
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
  font-size: 12.5px;
  font-weight: 500;
  color: var(--color-text-primary);
}

/* ==================== 확장자 뱃지 ==================== */

.ext-badge {
  display: inline-flex;
  align-items: center;
  padding: 3px 9px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

.ext-badge.wav {
  background: #fff4e6;
  color: #c05e00;
  border: 1px solid #fdd5a2;
}

/* ==================== 날짜/시간 셀 ==================== */

.datetime-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.datetime-date {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-primary);
  font-variant-numeric: tabular-nums;
}

.datetime-time {
  font-size: 11.5px;
  color: var(--color-text-secondary);
  font-variant-numeric: tabular-nums;
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
}

/* ==================== 테이블 푸터 ==================== */

.table-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-top: 1px solid var(--color-border);
  background: #f8f9fc;
}

.table-footer-info {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.table-footer-info strong {
  font-weight: 700;
  color: var(--color-text-primary);
}

/* ==================== 빈 상태 UI ==================== */

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 72px 32px;
  text-align: center;
  gap: 16px;
}

.empty-icon-wrap {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-icon-wrap svg {
  width: 36px;
  height: 36px;
  color: #9ca3af;
}

.empty-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--color-text-primary);
}

.empty-desc {
  font-size: 13px;
  color: var(--color-text-secondary);
  line-height: 1.6;
  max-width: 320px;
}

/* ==================== 반응형 ==================== */

@media (max-width: 900px) {
  .stats-strip {
    flex-wrap: wrap;
  }

  .stat-chip {
    flex: 1;
    min-width: calc(50% - 7px);
  }
}

@media (max-width: 720px) {
  .recordings-page-body {
    padding: 20px 16px;
  }

  .topbar {
    padding: 0 16px;
  }

  /* 좁은 화면에서 날짜/확장자 열 숨김 */
  .col-ext,
  .col-datetime {
    display: none;
  }
}
</style>
