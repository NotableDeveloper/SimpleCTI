<template>
  <div class="dialer-page">

    <!-- ==================== 상단 바 ==================== -->
    <header class="topbar">
      <div class="topbar-title">
        <span class="topbar-heading">수동 콜</span>
        <span class="topbar-breadcrumb">Simple CTI &rsaquo; 수동 콜</span>
      </div>
      <div class="topbar-actions">
        <!-- 대시보드로 돌아가기 버튼 -->
        <button class="back-btn" @click="$emit('back')">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="15 18 9 12 15 6"/>
          </svg>
          대시보드로 돌아가기
        </button>
      </div>
    </header>

    <!-- ==================== 페이지 본문 ==================== -->
    <main class="dialer-page-body">

      <!-- SIP 상태 배너 (풀 너비) -->
      <div class="sip-status-banner">
        <div class="sip-status-card">
          <div class="sip-status-left">
            <!-- SIP 전화 아이콘 -->
            <div class="sip-icon-wrap">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <path d="M6.6 10.8c1.4 2.8 3.8 5.1 6.6 6.6l2.2-2.2c.3-.3.7-.4 1-.2 1.1.4 2.3.6 3.6.6.6 0 1 .4 1 1V20c0 .6-.4 1-1 1-9.4 0-17-7.6-17-17 0-.6.4-1 1-1h3.5c.6 0 1 .4 1 1 0 1.3.2 2.5.6 3.6.1.3 0 .7-.2 1L6.6 10.8z"/>
              </svg>
            </div>
            <div class="sip-label-group">
              <span class="sip-label">SIP 계정</span>
              <!-- URI에서 사용자 파트만 추출하여 표시 -->
              <span class="sip-ext">{{ sipConfig.uri ? sipConfig.uri.split('@')[0].replace('sip:', '') : '--' }}</span>
            </div>
          </div>
          <!-- callStatus에 따른 배지 클래스 동적 바인딩 -->
          <span class="status-badge" :class="getSipBadgeClass(callStatus)">
            <span class="badge-dot" :class="{ pulse: callStatus !== 'Idle' }"></span>
            {{ getSipStatusLabel(callStatus) }}
          </span>
        </div>
      </div>

      <!-- 2단 레이아웃: 다이얼러(왼쪽) + SIP 로그(오른쪽) -->
      <div class="dual-layout">

        <!-- ===== 왼쪽: 다이얼러 카드 ===== -->
        <div class="dialer-card">

          <!-- 전화번호 입력 영역 -->
          <div class="dialer-display">
            <div class="display-label">전화번호 입력</div>
            <div class="display-input-row">
              <input
                type="tel"
                class="display-input"
                v-model="targetNumber"
                placeholder="번호를 입력하세요"
                maxlength="20"
                autocomplete="off"
                inputmode="numeric"
                :disabled="callStatus === 'InCall' || callStatus === 'Ringing' || callStatus === 'Dialing'"
                @keyup.enter="dial"
              />
              <button
                class="backspace-btn"
                @click="backspace"
                :disabled="callStatus === 'InCall' || callStatus === 'Ringing' || callStatus === 'Dialing'"
                aria-label="한 자리 지우기"
                title="한 자리 지우기"
              >
                <!-- 백스페이스 아이콘 -->
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M21 4H8l-7 8 7 8h13a2 2 0 0 0 2-2V6a2 2 0 0 0-2-2z"/>
                  <line x1="18" y1="9" x2="12" y2="15"/>
                  <line x1="12" y1="9" x2="18" y2="15"/>
                </svg>
              </button>
            </div>
          </div>

          <!-- InCall 상태 바 -->
          <div class="incall-info" :class="{ visible: callStatus === 'InCall' || callStatus === 'Ringing' || callStatus === 'Dialing' }">
            <div class="incall-left">
              <span class="incall-pulse-dot"></span>
              <span class="incall-number">{{ targetNumber || '--' }}</span>
              <span v-if="callStatus === 'Ringing' || callStatus === 'Dialing'" class="incall-connecting">연결 중...</span>
            </div>
            <span v-if="callStatus === 'InCall'" class="incall-status-label">통화 중</span>
          </div>

          <!-- 타이머 바: InCall 상태일 때만 표시 -->
          <div class="incall-timer-bar" :class="{ visible: callStatus === 'InCall' }">
            <svg class="incall-clock-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
              <circle cx="12" cy="12" r="9"/>
              <polyline points="12 7 12 12 15.5 15.5"/>
            </svg>
            <span class="incall-timer">{{ formattedElapsed() }}</span>
          </div>

          <!-- 키패드 -->
          <div class="keypad-section">
            <div class="keypad-grid">
              <!-- 1행 -->
              <button class="key-btn" @click="appendDigit('1')" :disabled="callStatus === 'InCall' || callStatus === 'Ringing' || callStatus === 'Dialing'" aria-label="1">
                <span class="key-digit">1</span>
                <span class="key-sub">&nbsp;</span>
              </button>
              <button class="key-btn" @click="appendDigit('2')" :disabled="callStatus === 'InCall' || callStatus === 'Ringing' || callStatus === 'Dialing'" aria-label="2">
                <span class="key-digit">2</span>
                <span class="key-sub">ABC</span>
              </button>
              <button class="key-btn" @click="appendDigit('3')" :disabled="callStatus === 'InCall' || callStatus === 'Ringing' || callStatus === 'Dialing'" aria-label="3">
                <span class="key-digit">3</span>
                <span class="key-sub">DEF</span>
              </button>
              <!-- 2행 -->
              <button class="key-btn" @click="appendDigit('4')" :disabled="callStatus === 'InCall' || callStatus === 'Ringing' || callStatus === 'Dialing'" aria-label="4">
                <span class="key-digit">4</span>
                <span class="key-sub">GHI</span>
              </button>
              <button class="key-btn" @click="appendDigit('5')" :disabled="callStatus === 'InCall' || callStatus === 'Ringing' || callStatus === 'Dialing'" aria-label="5">
                <span class="key-digit">5</span>
                <span class="key-sub">JKL</span>
              </button>
              <button class="key-btn" @click="appendDigit('6')" :disabled="callStatus === 'InCall' || callStatus === 'Ringing' || callStatus === 'Dialing'" aria-label="6">
                <span class="key-digit">6</span>
                <span class="key-sub">MNO</span>
              </button>
              <!-- 3행 -->
              <button class="key-btn" @click="appendDigit('7')" :disabled="callStatus === 'InCall' || callStatus === 'Ringing' || callStatus === 'Dialing'" aria-label="7">
                <span class="key-digit">7</span>
                <span class="key-sub">PQRS</span>
              </button>
              <button class="key-btn" @click="appendDigit('8')" :disabled="callStatus === 'InCall' || callStatus === 'Ringing' || callStatus === 'Dialing'" aria-label="8">
                <span class="key-digit">8</span>
                <span class="key-sub">TUV</span>
              </button>
              <button class="key-btn" @click="appendDigit('9')" :disabled="callStatus === 'InCall' || callStatus === 'Ringing' || callStatus === 'Dialing'" aria-label="9">
                <span class="key-digit">9</span>
                <span class="key-sub">WXYZ</span>
              </button>
              <!-- 4행 -->
              <button class="key-btn key-special" @click="appendDigit('*')" :disabled="callStatus === 'InCall' || callStatus === 'Ringing' || callStatus === 'Dialing'" aria-label="*">
                <span class="key-digit">*</span>
              </button>
              <button class="key-btn" @click="appendDigit('0')" :disabled="callStatus === 'InCall' || callStatus === 'Ringing' || callStatus === 'Dialing'" aria-label="0">
                <span class="key-digit">0</span>
                <span class="key-sub">+</span>
              </button>
              <button class="key-btn key-special" @click="appendDigit('#')" :disabled="callStatus === 'InCall' || callStatus === 'Ringing' || callStatus === 'Dialing'" aria-label="#">
                <span class="key-digit">#</span>
              </button>
            </div>
          </div>

          <!-- 녹음 토글 옵션 -->
          <div class="dialer-options">
            <label class="record-toggle" :class="{ disabled: callStatus !== 'Registered' }">
              <input
                type="checkbox"
                v-model="recordingEnabled"
                :disabled="callStatus !== 'Registered'"
              />
              <div class="toggle-track"></div>
              <span class="record-label" :class="{ 'record-label-active': recordingEnabled && callStatus === 'Registered' }">
                <!-- 녹음 아이콘 -->
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M12 15c1.66 0 3-1.34 3-3V6c0-1.66-1.34-3-3-3S9 4.34 9 6v6c0 1.66 1.34 3 3 3zm5.91-3c-.49 0-.9.36-.98.85C16.52 15.2 14.47 17 12 17s-4.52-1.8-4.93-4.15c-.08-.49-.49-.85-.98-.85-.61 0-1.09.54-1 1.14.49 3 2.89 5.35 5.91 5.78V21c0 .55.45 1 1 1s1-.45 1-1v-2.08c3.02-.43 5.42-2.78 5.91-5.78.1-.6-.39-1.14-1-1.14z"/>
                </svg>
                통화 녹음
              </span>
            </label>
          </div>

          <!-- 발신 / 종료 버튼 -->
          <div class="dialer-actions">

            <!-- 발신 버튼: Registered 또는 Idle 상태일 때 표시 -->
            <div v-if="callStatus === 'Registered' || callStatus === 'Idle'" class="action-btn-wrap">
              <button
                class="action-btn-circle btn-call"
                @click="dial"
                :disabled="callStatus === 'Idle'"
                :style="{ opacity: callStatus === 'Idle' ? '0.5' : '1', cursor: callStatus === 'Idle' ? 'not-allowed' : 'pointer' }"
                aria-label="발신"
              >
                <!-- 전화 발신 아이콘 -->
                <svg viewBox="0 0 24 24" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                  <path d="M6.6 10.8c1.4 2.8 3.8 5.1 6.6 6.6l2.2-2.2c.3-.3.7-.4 1-.2 1.1.4 2.3.6 3.6.6.6 0 1 .4 1 1V20c0 .6-.4 1-1 1-9.4 0-17-7.6-17-17 0-.6.4-1 1-1h3.5c.6 0 1 .4 1 1 0 1.3.2 2.5.6 3.6.1.3 0 .7-.2 1L6.6 10.8z"/>
                </svg>
              </button>
              <div class="btn-label">발신</div>
            </div>

            <!-- 종료 버튼: Ringing 또는 InCall 상태일 때 표시 -->
            <div v-else class="action-btn-wrap">
              <button
                class="action-btn-circle btn-hangup"
                @click="hangup"
                aria-label="종료"
              >
                <!-- 전화 종료 아이콘 (135도 회전) -->
                <svg viewBox="0 0 24 24" fill="currentColor" xmlns="http://www.w3.org/2000/svg" style="transform: rotate(135deg);">
                  <path d="M6.6 10.8c1.4 2.8 3.8 5.1 6.6 6.6l2.2-2.2c.3-.3.7-.4 1-.2 1.1.4 2.3.6 3.6.6.6 0 1 .4 1 1V20c0 .6-.4 1-1 1-9.4 0-17-7.6-17-17 0-.6.4-1 1-1h3.5c.6 0 1 .4 1 1 0 1.3.2 2.5.6 3.6.1.3 0 .7-.2 1L6.6 10.8z"/>
                </svg>
              </button>
              <div class="btn-label">종료</div>
            </div>

          </div>
          <!-- /dialer-actions -->

        </div>
        <!-- /dialer-card -->

        <!-- ===== 오른쪽: SIP 로그 패널 ===== -->
        <div class="sip-log-panel">

          <!-- 패널 헤더 -->
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

          <!-- 필터 바 -->
          <div class="log-filter-bar">
            <span class="filter-label">필터</span>

            <button
              class="filter-chip"
              :class="{ active: sipLogFilter === 'all' }"
              data-filter="all"
              @click="sipLogFilter = 'all'"
            >
              All
              <span class="filter-chip-count">{{ sipLogs.length }}</span>
            </button>

            <button
              class="filter-chip"
              :class="{ active: sipLogFilter === '1xx' }"
              data-filter="1xx"
              @click="sipLogFilter = '1xx'"
            >
              <span class="filter-chip-dot" style="background: #e3b341;"></span>
              1xx
              <span class="filter-chip-count">{{ filterCount('1xx') }}</span>
            </button>

            <button
              class="filter-chip"
              :class="{ active: sipLogFilter === '2xx' }"
              data-filter="2xx"
              @click="sipLogFilter = '2xx'"
            >
              <span class="filter-chip-dot" style="background: #56d364;"></span>
              2xx
              <span class="filter-chip-count">{{ filterCount('2xx') }}</span>
            </button>

            <button
              class="filter-chip"
              :class="{ active: sipLogFilter === '3xx' }"
              data-filter="3xx"
              @click="sipLogFilter = '3xx'"
            >
              <span class="filter-chip-dot" style="background: #a8daff;"></span>
              3xx
              <span class="filter-chip-count">{{ filterCount('3xx') }}</span>
            </button>

            <button
              class="filter-chip"
              :class="{ active: sipLogFilter === '4xx' }"
              data-filter="4xx"
              @click="sipLogFilter = '4xx'"
            >
              <span class="filter-chip-dot" style="background: #ff7b72;"></span>
              4xx
              <span class="filter-chip-count">{{ filterCount('4xx') }}</span>
            </button>

            <button
              class="filter-chip"
              :class="{ active: sipLogFilter === '5xx' }"
              data-filter="5xx"
              @click="sipLogFilter = '5xx'"
            >
              <span class="filter-chip-dot" style="background: #f85149;"></span>
              5xx
              <span class="filter-chip-count">{{ filterCount('5xx') }}</span>
            </button>

            <button
              class="filter-chip"
              :class="{ active: sipLogFilter === 'request' }"
              data-filter="request"
              @click="sipLogFilter = 'request'"
            >
              <span class="filter-chip-dot" style="background: #d2a8ff;"></span>
              Request
              <span class="filter-chip-count">{{ filterCount('request') }}</span>
            </button>
          </div>

          <!-- 로그 터미널 영역 -->
          <div class="log-terminal" ref="sipLogContainer">

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
                <!-- 메시지 타입 -->
                <span class="log-type" :class="getSipTypeClass(entry.summary)">{{ entry.summary }}</span>
                <!-- 메시지 요약 -->
                <span class="log-msg">{{ entry.firstLine }}</span>
              </div>
              <!-- 상세 펼침 영역 -->
              <template v-for="entry in filteredSipLogs" :key="'detail-' + entry.id">
                <div v-if="entry.expanded" class="log-detail expanded">
                  <pre>{{ entry.raw }}</pre>
                </div>
              </template>
            </template>

          </div>

          <!-- 패널 푸터 (통계 상태바) -->
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

        </div>
        <!-- /sip-log-panel -->

      </div>
      <!-- /dual-layout -->

    </main>
    <!-- /dialer-page-body -->

    <!-- WebRTC 원격 오디오 (숨김 요소) -->
    <audio ref="remoteAudio" autoplay></audio>

  </div>
  <!-- /dialer-page -->
</template>

<script>
import { UserAgent, Registerer, Inviter, SessionState } from 'sip.js';

export default {
  name: 'DialerPage',
  emits: ['back'],
  data() {
    return {
      targetNumber: '',
      recordingEnabled: false,
      recordings: [],
      selectedRecordings: [],
      keys: ['1', '2', '3', '4', '5', '6', '7', '8', '9', '*', '0', '#'],

      // 통화 시간 타이머 상태
      timer: null,         // setInterval 핸들러 (clearInterval 용)
      elapsedSeconds: 0,   // 경과 초

      // SIP / WebRTC 상태
      userAgent: null,
      registerer: null,
      session: null,
      callStatus: 'Idle', // Idle, Registering, Registered, Ringing, Dialing, InCall
      sipConfig: {
        server: process.env.VUE_APP_SIP_SERVER,
        uri: process.env.VUE_APP_SIP_URI,
        password: process.env.VUE_APP_SIP_PASSWORD,
        displayName: process.env.VUE_APP_SIP_DISPLAY_NAME
      },

      // SIP 로그 상태
      sipLogs: [],         // { id, timestamp, direction, summary, firstLine, raw, expanded, isNew }
      sipLogFilter: 'all', // 'all' | '1xx' | '2xx' | '3xx' | '4xx' | '5xx' | 'request'
      sipAutoScroll: true,
      sipLogIdCounter: 0,
      sendCount: 0,
      recvCount: 0
    }
  },
  computed: {
    /**
     * sipLogFilter에 따라 필터링된 로그 배열을 반환합니다.
     * @returns {Array} 필터링된 sipLogs 항목 배열
     */
    filteredSipLogs() {
      if (this.sipLogFilter === 'all') {
        return this.sipLogs;
      }
      return this.sipLogs.filter(entry => this.matchesSipFilter(entry, this.sipLogFilter));
    }
  },
  methods: {
    /**
     * 경과 초를 MM:SS 형식으로 변환합니다.
     * @returns {string} "MM:SS" 형식의 시간 문자열
     */
    formattedElapsed() {
      const m = Math.floor(this.elapsedSeconds / 60).toString().padStart(2, '0');
      const s = (this.elapsedSeconds % 60).toString().padStart(2, '0');
      return `${m}:${s}`;
    },

    /**
     * 통화 타이머를 시작합니다. (SessionState.Established 시 호출)
     */
    startCallTimer() {
      this.elapsedSeconds = 0;
      this.timer = setInterval(() => { this.elapsedSeconds++; }, 1000);
    },

    /**
     * 통화 타이머를 정지하고 초기화합니다.
     */
    stopCallTimer() {
      clearInterval(this.timer);
      this.timer = null;
      this.elapsedSeconds = 0;
    },

    /**
     * SIP UserAgent를 초기화하고 서버에 등록합니다.
     * logConnector 옵션으로 SIP 내부 로그를 수집하고,
     * transportOptions.traceSip: true 로 WebSocket raw 메시지를 로거로 라우팅합니다.
     */
    async initSip() {
      const transportOptions = {
        server: this.sipConfig.server,
        // traceSip: true 설정 시 _send() 와 onWebSocketMessage() 에서
        // raw SIP 메시지가 logger.log()로 흘러 logConnector로 전달됩니다.
        traceSip: true
      };

      const uri = UserAgent.makeURI(this.sipConfig.uri);
      if (!uri) {
        console.error('Invalid URI');
        return;
      }

      this.userAgent = new UserAgent({
        uri: uri,
        transportOptions: transportOptions,
        authorizationUsername: uri.user,
        authorizationPassword: this.sipConfig.password,
        displayName: this.sipConfig.displayName,
        delegate: {
          onInvite: (invitation) => {
            console.log('수신 전화...');
            this.handleIncomingCall(invitation);
          }
        },
        // SIP.js 내부 로그를 가로채는 커넥터
        // level: 'log'|'debug'|'warn'|'error', category: 로거 카테고리
        // label: 선택적 레이블, content: 로그 내용 문자열
        logConnector: (level, category, label, content) => {
          this.handleSipLogConnector(level, category, label, content);
        }
      });

      try {
        await this.userAgent.start();
        console.log('UserAgent 시작됨');
        this.callStatus = 'Registering';

        this.registerer = new Registerer(this.userAgent);
        await this.registerer.register();
        this.callStatus = 'Registered';
        console.log('SIP 서버에 등록 완료');
      } catch (error) {
        console.error('UserAgent 시작 또는 등록 실패', error);
        this.callStatus = 'Idle';
      }
    },

    /**
     * SIP.js logConnector 콜백 핸들러입니다.
     * traceSip: true 옵션으로 인해 WebSocket 송수신 메시지가 이 콜백으로 전달됩니다.
     * "Sending WebSocket message:" / "Received WebSocket message:" 패턴을 감지하여
     * raw SIP 메시지를 파싱합니다.
     *
     * @param {string} level - 'log' | 'debug' | 'warn' | 'error'
     * @param {string} category - SIP.js 내부 로거 카테고리 (예: 'sip.Transport')
     * @param {string|undefined} label - 선택적 레이블
     * @param {string} content - 로그 내용
     */
    handleSipLogConnector(level, category, label, content) {
      if (typeof content !== 'string') return;

      // WebSocket 송신 메시지 감지
      // transport.js _send(): "Sending WebSocket message:\n\n<RAW SIP>\n"
      if (content.includes('Sending WebSocket message:')) {
        const rawSip = this.extractRawSip(content, 'Sending WebSocket message:');
        if (rawSip) {
          this.addSipLog('send', rawSip);
        }
        return;
      }

      // WebSocket 수신 메시지 감지 (텍스트)
      // transport.js onWebSocketMessage(): "Received WebSocket text message:\n\n<RAW SIP>\n"
      // 또는 바이너리: "Received WebSocket binary message:\n\n<RAW SIP>\n"
      if (content.includes('Received WebSocket') && content.includes('message:')) {
        const rawSip = this.extractRawSip(content, 'message:');
        if (rawSip) {
          this.addSipLog('recv', rawSip);
        }
        return;
      }
    },

    /**
     * 로그 내용 문자열에서 raw SIP 메시지 부분만 추출합니다.
     * "Sending WebSocket message:\n\n<SIP>\n" 형태에서 <SIP> 부분을 추출합니다.
     *
     * @param {string} content - 전체 로그 내용
     * @param {string} marker - 분리 기준 마커 문자열
     * @returns {string|null} 추출된 raw SIP 메시지 또는 null
     */
    extractRawSip(content, marker) {
      const idx = content.indexOf(marker);
      if (idx === -1) return null;
      const after = content.slice(idx + marker.length).replace(/^\n+/, '').replace(/\n+$/, '').trim();
      // CRLF Keep Alive (\r\n) 는 SIP 메시지가 아니므로 제외
      if (!after || /^(\r\n|\n)+$/.test(after)) return null;
      // SIP 메시지 형식: 첫 줄이 "SIP/2.0" 또는 메서드명으로 시작해야 함
      const firstLine = after.split(/\r?\n/)[0].trim();
      if (!firstLine) return null;
      if (!firstLine.startsWith('SIP/') && !/^[A-Z]+\s+sip/.test(firstLine) && !/^[A-Z]+\s+/.test(firstLine)) {
        return null;
      }
      return after;
    },

    /**
     * 파싱된 SIP 메시지를 sipLogs에 추가합니다.
     * 자동 스크롤이 활성화된 경우 로그 컨테이너를 맨 아래로 스크롤합니다.
     *
     * @param {string} direction - 'send' | 'recv'
     * @param {string} rawSip - raw SIP 메시지 문자열
     */
    addSipLog(direction, rawSip) {
      const parsed = this.parseSipMessage(rawSip);
      if (!parsed) return;

      const now = new Date();
      const ts = now.toTimeString().slice(0, 8) + '.' + String(now.getMilliseconds()).padStart(3, '0');

      const entry = {
        id: ++this.sipLogIdCounter,
        timestamp: ts,
        direction: direction,
        summary: parsed.summary,
        firstLine: parsed.firstLine,
        raw: rawSip,
        expanded: false,
        isNew: true
      };

      this.sipLogs.push(entry);

      if (direction === 'send') {
        this.sendCount++;
      } else {
        this.recvCount++;
      }

      // 새 항목 강조 애니메이션: 0.8초 후 isNew 플래그 해제
      setTimeout(() => {
        entry.isNew = false;
      }, 800);

      // 자동 스크롤
      this.$nextTick(() => {
        const el = this.$refs.sipLogContainer;
        if (el && this.sipAutoScroll) {
          el.scrollTop = el.scrollHeight;
        }
      });
    },

    /**
     * raw SIP 메시지 문자열을 파싱하여 summary와 firstLine을 반환합니다.
     *
     * - 응답: "SIP/2.0 200 OK" -> summary: "200 OK", firstLine: 전체 첫 줄
     * - 요청: "INVITE sip:..." -> summary: "INVITE", firstLine: 전체 첫 줄
     *
     * @param {string} rawSip - raw SIP 메시지
     * @returns {{ summary: string, firstLine: string } | null}
     */
    parseSipMessage(rawSip) {
      const lines = rawSip.split(/\r?\n/);
      const firstLine = lines[0].trim();
      if (!firstLine) return null;

      let summary;

      if (firstLine.startsWith('SIP/2.0')) {
        // 응답 메시지: "SIP/2.0 200 OK"
        const match = firstLine.match(/^SIP\/2\.0\s+(\d{3})\s*(.*)/);
        if (match) {
          summary = match[2] ? `${match[1]} ${match[2]}` : match[1];
        } else {
          summary = firstLine;
        }
      } else {
        // 요청 메시지: "INVITE sip:..."
        const match = firstLine.match(/^([A-Z]+)\s+/);
        summary = match ? match[1] : firstLine.split(' ')[0];
      }

      return { summary, firstLine };
    },

    /**
     * 로그 항목의 상세 영역 펼침/닫힘을 토글합니다.
     * @param {Object} entry - sipLogs 항목
     */
    toggleDetail(entry) {
      entry.expanded = !entry.expanded;
    },

    /**
     * 모든 SIP 로그를 초기화합니다.
     */
    clearSipLogs() {
      this.sipLogs = [];
      this.sendCount = 0;
      this.recvCount = 0;
      this.sipLogIdCounter = 0;
    },

    /**
     * 특정 필터에 해당하는 로그 건수를 반환합니다. (필터 칩 카운트 배지용)
     * @param {string} filter - '1xx' | '2xx' | '3xx' | '4xx' | '5xx' | 'request'
     * @returns {number}
     */
    filterCount(filter) {
      return this.sipLogs.filter(entry => this.matchesSipFilter(entry, filter)).length;
    },

    /**
     * 로그 항목이 주어진 필터와 일치하는지 판단합니다.
     *
     * @param {Object} entry - sipLogs 항목
     * @param {string} filter - 필터 키
     * @returns {boolean}
     */
    matchesSipFilter(entry, filter) {
      if (filter === 'all') return true;
      if (filter === 'request') {
        // 숫자로 시작하지 않는 항목 = 요청 메시지
        return !/^\d/.test(entry.summary);
      }
      // 응답 코드 계열 필터 (1xx / 2xx / 3xx / 4xx / 5xx)
      const responseClass = this.getSipResponseClass(entry.summary);
      return responseClass === filter;
    },

    /**
     * summary 문자열에서 응답 코드 계열을 반환합니다.
     * @param {string} summary - "200 OK", "401 Unauthorized" 등
     * @returns {string|null} '1xx' | '2xx' | '3xx' | '4xx' | '5xx' | null
     */
    getSipResponseClass(summary) {
      if (!summary || !/^\d/.test(summary)) return null;
      const code = parseInt(summary, 10);
      if (code >= 100 && code < 200) return '1xx';
      if (code >= 200 && code < 300) return '2xx';
      if (code >= 300 && code < 400) return '3xx';
      if (code >= 400 && code < 500) return '4xx';
      if (code >= 500 && code < 600) return '5xx';
      return null;
    },

    /**
     * SIP 메시지 summary에 따라 로그 타입 CSS 클래스를 반환합니다.
     * @param {string} summary - "INVITE", "200 OK", "401 Unauthorized" 등
     * @returns {string} CSS 클래스명
     */
    getSipTypeClass(summary) {
      if (!summary) return '';
      const methodMap = {
        'INVITE':   'type-invite',
        'REGISTER': 'type-register',
        'ACK':      'type-ack',
        'BYE':      'type-bye',
        'CANCEL':   'type-cancel',
        'OPTIONS':  'type-options',
        'PRACK':    'type-options',
        'UPDATE':   'type-options',
        'SUBSCRIBE':'type-options',
        'NOTIFY':   'type-options',
        'REFER':    'type-options',
        'MESSAGE':  'type-options',
        'INFO':     'type-options',
        'PUBLISH':  'type-options'
      };
      // 요청 메서드 직접 매핑
      if (methodMap[summary]) return methodMap[summary];
      // 응답 코드 계열 매핑 (예: "200 OK")
      const cls = this.getSipResponseClass(summary);
      if (cls) return 'type-' + cls;
      return '';
    },

    /**
     * 수신 전화를 처리합니다.
     * @param {Object} invitation - SIP 초대 객체
     */
    handleIncomingCall(invitation) {
      this.session = invitation;
      this.callStatus = 'Ringing';
      if (confirm(`${invitation.remoteIdentity.uri.user}에서 전화가 왔습니다. 받으시겠습니까?`)) {
        this.answerCall();
      } else {
        invitation.reject();
        this.callStatus = 'Registered';
      }
    },

    /**
     * 수신 전화를 받습니다.
     */
    async answerCall() {
      if (!this.session) return;

      const options = {
        sessionDescriptionHandlerOptions: {
          constraints: { audio: true, video: false }
        }
      };

      this.setupSessionDelegate(this.session);
      await this.session.accept(options);
      this.callStatus = 'InCall';
    },

    /**
     * 세션 상태 변화 리스너와 오디오 스트림 설정을 등록합니다.
     * @param {Object} session - SIP 세션 객체
     */
    setupSessionDelegate(session) {
      session.stateChange.addListener((newState) => {
        console.log('세션 상태:', newState);
        if (newState === SessionState.Established) {
          this.callStatus = 'InCall';
          this.startCallTimer(); // 통화 연결 시 타이머 시작
        } else if (newState === SessionState.Terminated) {
          this.stopCallTimer(); // 통화 종료 시 타이머 정지
          this.session = null;
          this.callStatus = 'Registered';
        }
      });

      session.delegate = {
        onSessionDescriptionHandler: (sdh) => {
          const remoteStream = new MediaStream();
          sdh.peerConnection.getReceivers().forEach((receiver) => {
            if (receiver.track) {
              remoteStream.addTrack(receiver.track);
            }
          });
          this.$refs.remoteAudio.srcObject = remoteStream;
          this.$refs.remoteAudio.play().catch(e => console.warn('오디오 재생 중단:', e));
        }
      };
    },

    /**
     * 키패드 입력: targetNumber에 숫자/기호를 추가합니다.
     * @param {string} digit - 입력할 숫자 또는 기호
     */
    appendDigit(digit) {
      this.targetNumber += digit;
    },

    /**
     * 입력된 전화번호의 마지막 자리를 삭제합니다.
     */
    backspace() {
      this.targetNumber = this.targetNumber.slice(0, -1);
    },

    /**
     * 발신 통화를 시작합니다.
     */
    async dial() {
      if (!this.targetNumber) {
        alert('전화번호를 입력해주세요.');
        return;
      }

      if (!this.userAgent || this.callStatus === 'Idle') {
        alert('SIP이 등록되지 않았습니다. 설정을 확인해주세요.');
        return;
      }

      try {
        await navigator.mediaDevices.getUserMedia({ audio: true });
      } catch (e) {
        if (e.name === 'NotFoundError' || e.name === 'DevicesNotFoundError') {
          alert('마이크를 찾을 수 없습니다. 오디오 입력 장치를 연결하거나 브라우저 권한을 확인해주세요.');
        } else if (e.name === 'NotAllowedError' || e.name === 'PermissionDeniedError') {
          alert('마이크 접근이 거부되었습니다. 브라우저에서 마이크 권한을 허용해주세요.');
        } else {
          alert('마이크 접근 중 오류가 발생했습니다: ' + e.message);
        }
        return;
      }

      const targetURI = UserAgent.makeURI(`sip:${this.targetNumber}@${this.sipConfig.uri.split('@')[1]}`);
      if (!targetURI) {
        alert('유효하지 않은 전화번호입니다.');
        return;
      }

      this.session = new Inviter(this.userAgent, targetURI);
      this.setupSessionDelegate(this.session);

      const options = {
        sessionDescriptionHandlerOptions: {
          constraints: { audio: true, video: false }
        }
      };

      try {
        this.callStatus = 'Dialing';
        await this.session.invite(options);
        console.log('발신 중...');
      } catch (error) {
        console.error('발신 실패', error);
        alert('발신에 실패했습니다.');
        this.callStatus = 'Registered';
      }
    },

    /**
     * 통화를 종료합니다.
     */
    async hangup() {
      if (!this.session) return;

      const state = this.session.state;

      if (state === SessionState.Terminated || state === SessionState.Terminating) {
        this.session = null;
        this.callStatus = 'Registered';
        return;
      }

      try {
        if (this.session instanceof Inviter && state === SessionState.Establishing) {
          await this.session.cancel();
        } else {
          await this.session.bye();
        }
      } catch (e) {
        console.warn('통화 종료 오류:', e);
      } finally {
        // 고객 채널이 남아있는 경우를 대비해 AMI HangupAction 추가 전송
        fetch('/api/hangup', { method: 'POST' }).catch(err => console.warn('AMI hangup 요청 오류:', err));
        this.stopCallTimer();
        this.session = null;
        this.callStatus = 'Registered';
      }
    },

    /**
     * 서버에서 녹음 파일 목록을 가져옵니다.
     */
    async loadRecordings() {
      try {
        const response = await fetch('/api/recordings/list');
        const files = await response.json();
        this.recordings = files.sort().reverse();
      } catch (error) {
        console.error('녹음 파일 목록 로드 오류:', error);
      }
    },

    /**
     * 선택된 녹음 파일들을 다운로드합니다.
     */
    downloadSelected() {
      if (this.selectedRecordings.length === 0) {
        alert('다운로드할 녹음 파일을 선택해주세요.');
        return;
      }

      this.selectedRecordings.forEach(filename => {
        const link = document.createElement('a');
        link.href = '/api/recordings/' + encodeURIComponent(filename);
        link.download = filename;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
      });
    },

    /**
     * callStatus 값에 따라 SIP 상태 배지의 CSS 클래스를 반환합니다.
     * @param {string} status - 'Idle' | 'Registering' | 'Registered' | 'Ringing' | 'InCall'
     * @returns {string} 배지 클래스명
     */
    getSipBadgeClass(status) {
      switch (status) {
        case 'Registered':  return 'badge-success';
        case 'Registering': return 'badge-warning';
        case 'Dialing':     return 'badge-warning';
        case 'Ringing':     return 'badge-warning';
        case 'InCall':      return 'badge-incall';
        case 'Idle':
        default:            return 'badge-offline';
      }
    },

    /**
     * callStatus 값에 따라 표시할 한국어 레이블을 반환합니다.
     * @param {string} status - callStatus 값
     * @returns {string} 표시 레이블
     */
    getSipStatusLabel(status) {
      switch (status) {
        case 'Idle':        return 'Idle';
        case 'Registering': return 'Registering';
        case 'Registered':  return 'Registered';
        case 'Dialing':     return 'Dialing';
        case 'Ringing':     return 'Ringing';
        case 'InCall':      return 'In Call';
        default:            return status;
      }
    }
  },
  mounted() {
    this.initSip();
  },
  beforeUnmount() {
    this.stopCallTimer(); // 메모리 누수 방지: 인터벌 정리
    if (this.registerer) {
      this.registerer.unregister();
    }
    if (this.userAgent) {
      this.userAgent.stop();
    }
  }
}
</script>

<style scoped>
/*
 * Dialer.vue 전용 스타일
 * App.vue의 :root CSS 변수를 그대로 참조합니다. 중복 선언하지 않습니다.
 */

/* ==================== SIP 로그 패널 전용 CSS 변수 ==================== */
/*
 * <style scoped>에서 :root 변수는 App.vue에 있으나, 로그 패널 전용 변수는
 * 여기에 별도 선언합니다. scoped 특성상 :root에 직접 추가 불가하므로
 * 클래스 선택자로 스코프를 제한합니다.
 */

/* ==================== 페이지 래퍼 ==================== */

/*
 * App.vue의 .page-body--dialer (padding: 24px 32px) 내에 렌더링되므로,
 * 음수 마진으로 부모 패딩을 상쇄하여 topbar가 full-width sticky로 동작하도록 합니다.
 */
.dialer-page {
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

.back-btn {
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
  text-decoration: none;
  font-family: inherit;
}

.back-btn:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
  background: #f0f5ff;
}

.back-btn svg {
  width: 14px;
  height: 14px;
}

/* ==================== 페이지 본문 ==================== */

.dialer-page-body {
  padding: 28px 32px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* ==================== SIP 상태 배너 ==================== */

.sip-status-banner {
  width: 100%;
  margin-bottom: 20px;
}

.sip-status-card {
  background: var(--color-card);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 14px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.sip-status-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.sip-icon-wrap {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  background: #eff4ff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.sip-icon-wrap svg {
  width: 18px;
  height: 18px;
  color: var(--color-primary);
}

.sip-label-group {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.sip-label {
  font-size: 12px;
  color: var(--color-text-secondary);
  font-weight: 500;
}

.sip-ext {
  font-size: 13.5px;
  font-weight: 700;
  color: var(--color-text-primary);
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
}

/* ==================== 상태 배지 ==================== */

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 11.5px;
  font-weight: 600;
  letter-spacing: 0.3px;
}

.status-badge .badge-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

/* badge-success, badge-warning, badge-offline, badge-error는 App.vue 전역에 정의되어 있으므로 여기서는 badge-incall만 추가 정의 */
.badge-incall {
  background: #fdf0ff;
  color: #7c3aed;
  border: 1px solid #ddd1f7;
}

.badge-incall .badge-dot {
  background: #7c3aed;
}

/* pulse 애니메이션은 App.vue 전역에 정의되어 있으므로 여기서 중복 선언하지 않음 */

/* ==================== 2단 레이아웃 ==================== */

.dual-layout {
  display: grid;
  grid-template-columns: 400px 1fr;
  gap: 24px;
  align-items: start;
  flex: 1;
}

/* ==================== 다이얼러 카드 ==================== */

.dialer-card {
  width: 100%;
  background: var(--color-card);
  border: 1px solid var(--color-border);
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.07);
  overflow: hidden;
}

/* --- 전화번호 입력 --- */

.dialer-display {
  padding: 24px 24px 18px;
  border-bottom: 1px solid var(--color-border);
  background: linear-gradient(135deg, #f8faff 0%, #f0f4ff 100%);
}

.display-label {
  font-size: 11px;
  font-weight: 600;
  color: var(--color-text-secondary);
  letter-spacing: 0.8px;
  text-transform: uppercase;
  margin-bottom: 10px;
}

.display-input-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.display-input {
  flex: 1;
  border: 1.5px solid var(--color-border);
  border-radius: 10px;
  padding: 11px 16px;
  font-size: 22px;
  font-weight: 700;
  color: var(--color-text-primary);
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
  letter-spacing: 2px;
  background: #ffffff;
  outline: none;
  transition: border-color 0.15s, box-shadow 0.15s;
  text-align: center;
}

.display-input::placeholder {
  color: #c4c9d4;
  font-size: 16px;
  letter-spacing: 1px;
  font-weight: 400;
}

.display-input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(74, 124, 247, 0.12);
}

.display-input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.backspace-btn {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  border: 1.5px solid var(--color-border);
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--color-text-secondary);
  transition: all 0.15s;
  flex-shrink: 0;
}

.backspace-btn:hover:not(:disabled) {
  border-color: var(--color-error-border);
  color: var(--color-error);
  background: var(--color-error-bg);
}

.backspace-btn:active:not(:disabled) {
  transform: scale(0.94);
}

.backspace-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.backspace-btn svg {
  width: 18px;
  height: 18px;
}

/* --- 통화 중 상태 바 --- */

.incall-info {
  display: none;
  padding: 11px 24px;
  background: linear-gradient(135deg, #faf5ff 0%, #f5edff 100%);
  border-top: 1px solid #ddd1f7;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.incall-info.visible {
  display: flex;
}

/* 왼쪽 그룹 */
.incall-left {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

/* 펄스 도트 */
.incall-pulse-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #7c3aed;
  flex-shrink: 0;
  animation: incallPulse 1.5s ease-in-out infinite;
}

@keyframes incallPulse {
  0%, 100% { opacity: 1; transform: scale(1); box-shadow: 0 0 0 0 rgba(124, 58, 237, 0.4); }
  50%       { opacity: 0.7; transform: scale(0.88); box-shadow: 0 0 0 4px rgba(124, 58, 237, 0); }
}

/* 전화번호 */
.incall-number {
  font-size: 13px;
  font-weight: 700;
  color: #5b21b6;
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
  letter-spacing: 0.5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Ringing 상태 "연결 중..." */
.incall-connecting {
  font-size: 12px;
  font-weight: 500;
  color: #b45309;
  font-style: italic;
}

/* InCall 상태 "통화 중" 레이블 */
.incall-status-label {
  font-size: 12px;
  font-weight: 600;
  color: #7c3aed;
  letter-spacing: 0.3px;
  flex-shrink: 0;
}

/* 타이머 바 (incall-info 아래 별도 행) */
.incall-timer-bar {
  display: none;
  padding: 10px 24px 11px;
  background: #f3eaff;
  border-top: 1px solid #e4d4f9;
  border-bottom: 1.5px solid #ddd1f7;
  align-items: center;
  justify-content: center;
  gap: 7px;
}

.incall-timer-bar.visible {
  display: flex;
}

.incall-clock-icon {
  width: 15px;
  height: 15px;
  color: #7c3aed;
  opacity: 0.7;
  flex-shrink: 0;
}

.incall-timer {
  font-size: 18px;
  font-weight: 700;
  color: #6d28d9;
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
  font-variant-numeric: tabular-nums;
  letter-spacing: 2px;
  min-width: 4ch;
  text-align: center;
}

/* --- 키패드 --- */

.keypad-section {
  padding: 20px 24px;
}

.keypad-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.key-btn {
  aspect-ratio: 1 / 0.82;
  border-radius: 12px;
  border: 1.5px solid var(--color-border);
  background: #ffffff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.12s;
  user-select: none;
  gap: 1px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.key-btn:hover:not(:disabled) {
  border-color: #c3d5fd;
  background: #f4f8ff;
  box-shadow: 0 2px 8px rgba(74, 124, 247, 0.12);
}

.key-btn:active:not(:disabled) {
  transform: scale(0.92);
  background: #e8f0ff;
  border-color: var(--color-primary);
}

.key-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.key-digit {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-text-primary);
  line-height: 1;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}

.key-sub {
  font-size: 8.5px;
  font-weight: 500;
  color: var(--color-text-secondary);
  letter-spacing: 1px;
  text-transform: uppercase;
  line-height: 1;
}

.key-btn.key-special .key-digit {
  font-size: 18px;
  color: var(--color-text-secondary);
}

/* --- 녹음 토글 옵션 --- */

.dialer-options {
  padding: 0 24px 18px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.record-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  user-select: none;
  flex: 1;
}

.record-toggle input[type="checkbox"] {
  display: none;
}

.toggle-track {
  width: 36px;
  height: 20px;
  border-radius: 10px;
  background: #d1d5db;
  position: relative;
  transition: background 0.2s;
  flex-shrink: 0;
}

.toggle-track::after {
  content: '';
  position: absolute;
  top: 3px;
  left: 3px;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: #ffffff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.2);
  transition: transform 0.2s;
}

/* v-model로 바인딩된 recordingEnabled가 true일 때 토글 트랙 색상 변경 */
.record-toggle input:checked + .toggle-track {
  background: var(--color-success);
}

.record-toggle input:checked + .toggle-track::after {
  transform: translateX(16px);
}

.record-toggle.disabled {
  opacity: 0.4;
  pointer-events: none;
}

.record-label {
  font-size: 12.5px;
  font-weight: 500;
  color: var(--color-text-secondary);
  display: flex;
  align-items: center;
  gap: 5px;
}

.record-label svg {
  width: 13px;
  height: 13px;
}

.record-label-active {
  color: var(--color-success);
  font-weight: 600;
}

/* --- 발신 / 종료 버튼 --- */

.dialer-actions {
  padding: 0 24px 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
}

.action-btn-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.action-btn-circle {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.15s;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.15);
  flex-shrink: 0;
}

.action-btn-circle svg {
  width: 26px;
  height: 26px;
}

.btn-call {
  background: var(--color-success);
  color: #ffffff;
}

.btn-call:hover:not(:disabled) {
  background: #218838;
  box-shadow: 0 6px 20px rgba(40, 167, 69, 0.4);
  transform: translateY(-1px) scale(1.04);
}

.btn-call:active:not(:disabled) {
  transform: scale(0.94);
}

.btn-hangup {
  background: var(--color-error);
  color: #ffffff;
}

.btn-hangup:hover {
  background: #c82333;
  box-shadow: 0 6px 20px rgba(220, 53, 69, 0.4);
  transform: translateY(-1px) scale(1.04);
}

.btn-hangup:active {
  transform: scale(0.94);
}

.btn-label {
  font-size: 11px;
  font-weight: 600;
  color: var(--color-text-secondary);
  text-align: center;
  margin-top: 6px;
  letter-spacing: 0.3px;
}

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
.log-terminal::-webkit-scrollbar {
  width: 6px;
}

.log-terminal::-webkit-scrollbar-track {
  background: transparent;
}

.log-terminal::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.12);
  border-radius: 3px;
}

.log-terminal::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.2);
}

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
  grid-template-columns: 78px 20px 80px 1fr;
  gap: 0;
  align-items: baseline;
  padding: 3px 14px;
  border-bottom: 1px solid transparent;
  transition: background 0.1s;
  cursor: default;
}

.log-entry:hover {
  background: rgba(255, 255, 255, 0.03);
}

.log-entry.expandable {
  cursor: pointer;
}

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

.log-entry.new-entry {
  animation: logEntryFlash 0.8s ease-out forwards;
}

/* 타임스탬프 */
.log-ts {
  font-size: 10.5px;
  color: #6e7681;
  white-space: nowrap;
  letter-spacing: 0.2px;
  padding-right: 8px;
  user-select: none;
}

/* 방향 화살표 */
.log-dir {
  font-size: 11px;
  font-weight: 700;
  padding-right: 6px;
  user-select: none;
  display: flex;
  align-items: center;
}

.log-dir.send {
  color: #58a6ff;
}

.log-dir.recv {
  color: #56d364;
}

/* 메시지 타입 배지 */
.log-type {
  font-size: 11px;
  font-weight: 700;
  white-space: nowrap;
  padding-right: 10px;
  letter-spacing: 0.3px;
}

.type-invite   { color: #79c0ff; }
.type-register { color: #56d364; }
.type-ack      { color: #a8daff; }
.type-bye      { color: #ff7b72; }
.type-cancel   { color: #ffa657; }
.type-options  { color: #d2a8ff; }
.type-1xx      { color: #e3b341; }
.type-2xx      { color: #56d364; }
.type-3xx      { color: #a8daff; }
.type-4xx      { color: #ff7b72; }
.type-5xx      { color: #f85149; }

/* 메시지 본문 요약 */
.log-msg {
  color: #c9d1d9;
  font-size: 11.5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 상세 펼침 영역 */
.log-detail {
  display: none;
  padding: 4px 14px 8px 112px;
  background: rgba(255, 255, 255, 0.02);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.log-detail.expanded {
  display: block;
}

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
  50% { opacity: 0.3; }
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

/* ==================== 반응형 ==================== */

/* 1024px 이하: 2단 레이아웃 -> 1단 세로 배치 */
@media (max-width: 1024px) {
  .dual-layout {
    grid-template-columns: 1fr;
  }

  .sip-log-panel {
    max-height: 480px;
  }

  .dialer-card {
    max-width: 480px;
    margin: 0 auto;
  }
}

@media (max-width: 480px) {
  .dialer-page-body {
    padding: 20px 16px;
  }

  .topbar {
    padding: 0 16px;
  }

  .dialer-card {
    max-width: 100%;
  }
}
</style>
