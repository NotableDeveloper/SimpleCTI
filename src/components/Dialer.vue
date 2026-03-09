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

      <!-- SIP 상태 배너 -->
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

      <!-- 다이얼러 카드 -->
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
              :disabled="callStatus === 'InCall' || callStatus === 'Ringing'"
              @keyup.enter="dial"
            />
            <button
              class="backspace-btn"
              @click="backspace"
              :disabled="callStatus === 'InCall' || callStatus === 'Ringing'"
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
        <div class="incall-info" :class="{ visible: callStatus === 'InCall' || callStatus === 'Ringing' }">
          <div class="incall-left">
            <span class="incall-pulse-dot"></span>
            <span class="incall-number">{{ targetNumber || '--' }}</span>
            <span v-if="callStatus === 'Ringing'" class="incall-connecting">연결 중...</span>
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
            <button class="key-btn" @click="appendDigit('1')" :disabled="callStatus === 'InCall' || callStatus === 'Ringing'" aria-label="1">
              <span class="key-digit">1</span>
              <span class="key-sub">&nbsp;</span>
            </button>
            <button class="key-btn" @click="appendDigit('2')" :disabled="callStatus === 'InCall' || callStatus === 'Ringing'" aria-label="2">
              <span class="key-digit">2</span>
              <span class="key-sub">ABC</span>
            </button>
            <button class="key-btn" @click="appendDigit('3')" :disabled="callStatus === 'InCall' || callStatus === 'Ringing'" aria-label="3">
              <span class="key-digit">3</span>
              <span class="key-sub">DEF</span>
            </button>
            <!-- 2행 -->
            <button class="key-btn" @click="appendDigit('4')" :disabled="callStatus === 'InCall' || callStatus === 'Ringing'" aria-label="4">
              <span class="key-digit">4</span>
              <span class="key-sub">GHI</span>
            </button>
            <button class="key-btn" @click="appendDigit('5')" :disabled="callStatus === 'InCall' || callStatus === 'Ringing'" aria-label="5">
              <span class="key-digit">5</span>
              <span class="key-sub">JKL</span>
            </button>
            <button class="key-btn" @click="appendDigit('6')" :disabled="callStatus === 'InCall' || callStatus === 'Ringing'" aria-label="6">
              <span class="key-digit">6</span>
              <span class="key-sub">MNO</span>
            </button>
            <!-- 3행 -->
            <button class="key-btn" @click="appendDigit('7')" :disabled="callStatus === 'InCall' || callStatus === 'Ringing'" aria-label="7">
              <span class="key-digit">7</span>
              <span class="key-sub">PQRS</span>
            </button>
            <button class="key-btn" @click="appendDigit('8')" :disabled="callStatus === 'InCall' || callStatus === 'Ringing'" aria-label="8">
              <span class="key-digit">8</span>
              <span class="key-sub">TUV</span>
            </button>
            <button class="key-btn" @click="appendDigit('9')" :disabled="callStatus === 'InCall' || callStatus === 'Ringing'" aria-label="9">
              <span class="key-digit">9</span>
              <span class="key-sub">WXYZ</span>
            </button>
            <!-- 4행 -->
            <button class="key-btn key-special" @click="appendDigit('*')" :disabled="callStatus === 'InCall' || callStatus === 'Ringing'" aria-label="*">
              <span class="key-digit">*</span>
            </button>
            <button class="key-btn" @click="appendDigit('0')" :disabled="callStatus === 'InCall' || callStatus === 'Ringing'" aria-label="0">
              <span class="key-digit">0</span>
              <span class="key-sub">+</span>
            </button>
            <button class="key-btn key-special" @click="appendDigit('#')" :disabled="callStatus === 'InCall' || callStatus === 'Ringing'" aria-label="#">
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
      callStatus: 'Idle', // Idle, Registering, Registered, Ringing, InCall
      sipConfig: {
        server: process.env.VUE_APP_SIP_SERVER,
        uri: process.env.VUE_APP_SIP_URI,
        password: process.env.VUE_APP_SIP_PASSWORD,
        displayName: process.env.VUE_APP_SIP_DISPLAY_NAME
      }
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
     */
    async initSip() {
      const transportOptions = {
        server: this.sipConfig.server
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
        await this.session.invite(options);
        this.callStatus = 'Ringing';
        console.log('발신 중...');
      } catch (error) {
        console.error('발신 실패', error);
        alert('발신에 실패했습니다.');
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
  align-items: center;
}

/* ==================== SIP 상태 배너 ==================== */

.sip-status-banner {
  width: 100%;
  max-width: 480px;
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

/* ==================== 다이얼러 카드 ==================== */

.dialer-card {
  width: 100%;
  max-width: 400px;
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

/* ==================== 반응형 ==================== */

@media (max-width: 480px) {
  .dialer-card {
    max-width: 100%;
  }

  .sip-status-banner {
    max-width: 100%;
  }

  .dialer-page-body {
    padding: 20px 16px;
  }

  .topbar {
    padding: 0 16px;
  }
}
</style>
