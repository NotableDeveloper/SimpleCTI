// [Composition API]
import { ref, computed } from 'vue';
import { UserAgent, Registerer, Inviter, SessionState } from 'sip.js';

/**
 * callStatus가 가질 수 있는 상태 상수입니다.
 * 모든 컴포넌트와 composable에서 이 상수를 통해 상태를 참조합니다.
 */
export const CALL_STATUS = {
  IDLE:        'Idle',
  REGISTERING: 'Registering',
  REGISTERED:  'Registered',
  RINGING:     'Ringing',
  DIALING:     'Dialing',
  IN_CALL:     'InCall',
};

/**
 * SIP UA 초기화, 등록, 발신·수신·종료, 통화 타이머를 담당하는 composable입니다.
 *
 * @param {import('vue').Ref<HTMLAudioElement|null>} remoteAudioRef - 원격 오디오 재생 DOM ref
 * @param {Function} logConnector - SIP.js logConnector 콜백 (useSipLog의 handleSipLogConnector)
 * @returns {Object} SIP 상태 및 조작 함수
 */
export function useSip(remoteAudioRef, logConnector) {
  // ==================== 상태 ====================

  // 통화 시간 타이머
  const timer          = ref(null); // setInterval 핸들러 (clearInterval 용)
  const elapsedSeconds = ref(0);    // 경과 초

  // SIP / WebRTC 상태
  const userAgent   = ref(null);
  const registerer  = ref(null);
  const session     = ref(null);
  const callStatus  = ref(CALL_STATUS.IDLE); // CALL_STATUS 상수 참조

  // SIP 설정: process.env는 이 파일에서만 읽습니다.
  const sipConfig = {
    server:      process.env.VUE_APP_SIP_SERVER,
    uri:         process.env.VUE_APP_SIP_URI,
    password:    process.env.VUE_APP_SIP_PASSWORD,
    displayName: process.env.VUE_APP_SIP_DISPLAY_NAME,
  };

  // ==================== Computed ====================

  /** 통화 진행 중 여부 (InCall | Ringing | Dialing) */
  const isInProgress = computed(() =>
    [CALL_STATUS.IN_CALL, CALL_STATUS.RINGING, CALL_STATUS.DIALING].includes(callStatus.value)
  );

  // ==================== 타이머 ====================

  /** 통화 타이머를 시작합니다. (SessionState.Established 시 호출) */
  function startCallTimer() {
    elapsedSeconds.value = 0;
    timer.value = setInterval(() => { elapsedSeconds.value++; }, 1000);
  }

  /** 통화 타이머를 정지하고 초기화합니다. */
  function stopCallTimer() {
    clearInterval(timer.value);
    timer.value = null;
    elapsedSeconds.value = 0;
  }

  /**
   * 경과 초를 MM:SS 형식으로 변환합니다.
   * @returns {string} "MM:SS" 형식의 시간 문자열
   */
  function formattedElapsed() {
    const m = Math.floor(elapsedSeconds.value / 60).toString().padStart(2, '0');
    const s = (elapsedSeconds.value % 60).toString().padStart(2, '0');
    return `${m}:${s}`;
  }

  // ==================== SIP 초기화 ====================

  /**
   * SIP UserAgent를 초기화하고 서버에 등록합니다.
   * logConnector 옵션으로 SIP 내부 로그를 수집하고,
   * transportOptions.traceSip: true 로 WebSocket raw 메시지를 로거로 라우팅합니다.
   */
  async function initSip() {
    const transportOptions = {
      server: sipConfig.server,
      // traceSip: true 설정 시 _send()와 onWebSocketMessage()에서
      // raw SIP 메시지가 logger.log()로 흘러 logConnector로 전달됩니다.
      traceSip: true
    };

    const uri = UserAgent.makeURI(sipConfig.uri);
    if (!uri) {
      console.error('Invalid URI');
      return;
    }

    userAgent.value = new UserAgent({
      uri,
      transportOptions,
      authorizationUsername: uri.user,
      authorizationPassword: sipConfig.password,
      displayName: sipConfig.displayName,
      delegate: {
        onInvite: (invitation) => {
          console.log('수신 전화...');
          handleIncomingCall(invitation);
        }
      },
      // SIP.js 내부 로그를 가로채는 커넥터
      logConnector: logConnector || null
    });

    try {
      await userAgent.value.start();
      console.log('UserAgent 시작됨');
      callStatus.value = CALL_STATUS.REGISTERING;

      registerer.value = new Registerer(userAgent.value);
      await registerer.value.register();
      callStatus.value = CALL_STATUS.REGISTERED;
      console.log('SIP 서버에 등록 완료');
    } catch (error) {
      console.error('UserAgent 시작 또는 등록 실패', error);
      callStatus.value = CALL_STATUS.IDLE;
    }
  }

  // ==================== 통화 처리 ====================

  /**
   * 수신 전화를 처리합니다.
   * @param {Object} invitation - SIP 초대 객체
   */
  function handleIncomingCall(invitation) {
    session.value = invitation;
    callStatus.value = CALL_STATUS.RINGING;
    if (confirm(`${invitation.remoteIdentity.uri.user}에서 전화가 왔습니다. 받으시겠습니까?`)) {
      answerCall();
    } else {
      invitation.reject();
      callStatus.value = CALL_STATUS.REGISTERED;
    }
  }

  /** 수신 전화를 받습니다. */
  async function answerCall() {
    if (!session.value) return;

    const options = {
      sessionDescriptionHandlerOptions: {
        constraints: { audio: true, video: false }
      }
    };

    setupSessionDelegate(session.value);
    await session.value.accept(options);
    callStatus.value = CALL_STATUS.IN_CALL;
  }

  /**
   * 세션 상태 변화 리스너와 오디오 스트림 설정을 등록합니다.
   * @param {Object} sess - SIP 세션 객체
   */
  function setupSessionDelegate(sess) {
    sess.stateChange.addListener((newState) => {
      console.log('세션 상태:', newState);
      if (newState === SessionState.Established) {
        callStatus.value = CALL_STATUS.IN_CALL;
        startCallTimer(); // 통화 연결 시 타이머 시작
      } else if (newState === SessionState.Terminated) {
        stopCallTimer(); // 통화 종료 시 타이머 정지
        session.value = null;
        callStatus.value = CALL_STATUS.REGISTERED;
      }
    });

    sess.delegate = {
      onSessionDescriptionHandler: (sdh) => {
        const remoteStream = new MediaStream();
        sdh.peerConnection.getReceivers().forEach((receiver) => {
          if (receiver.track) remoteStream.addTrack(receiver.track);
        });
        if (remoteAudioRef && remoteAudioRef.value) {
          remoteAudioRef.value.srcObject = remoteStream;
          remoteAudioRef.value.play().catch(e => console.warn('오디오 재생 중단:', e));
        }
      }
    };
  }

  /** 발신 통화를 시작합니다. */
  async function dial(targetNumber) {
    if (!targetNumber) {
      alert('전화번호를 입력해주세요.');
      return;
    }

    if (!userAgent.value || callStatus.value === CALL_STATUS.IDLE) {
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

    const targetURI = UserAgent.makeURI(`sip:${targetNumber}@${sipConfig.uri.split('@')[1]}`);
    if (!targetURI) {
      alert('유효하지 않은 전화번호입니다.');
      return;
    }

    session.value = new Inviter(userAgent.value, targetURI);
    setupSessionDelegate(session.value);

    const options = {
      sessionDescriptionHandlerOptions: {
        constraints: { audio: true, video: false }
      }
    };

    try {
      callStatus.value = CALL_STATUS.DIALING;
      await session.value.invite(options);
      console.log('발신 중...');
    } catch (error) {
      console.error('발신 실패', error);
      alert('발신에 실패했습니다.');
      callStatus.value = CALL_STATUS.REGISTERED;
    }
  }

  /** 통화를 종료합니다. */
  async function hangup() {
    if (!session.value) return;

    const state = session.value.state;

    if (state === SessionState.Terminated || state === SessionState.Terminating) {
      session.value = null;
      callStatus.value = CALL_STATUS.REGISTERED;
      return;
    }

    try {
      if (session.value instanceof Inviter && state === SessionState.Establishing) {
        await session.value.cancel();
      } else {
        await session.value.bye();
      }
    } catch (e) {
      console.warn('통화 종료 오류:', e);
    } finally {
      // 고객 채널이 남아있는 경우를 대비해 AMI HangupAction 추가 전송
      fetch('/api/hangup', { method: 'POST' }).catch(err => console.warn('AMI hangup 요청 오류:', err));
      stopCallTimer();
      session.value = null;
      callStatus.value = CALL_STATUS.REGISTERED;
    }
  }

  // ==================== UI 헬퍼 ====================

  /**
   * callStatus 값에 따라 SIP 상태 배지의 CSS 클래스를 반환합니다.
   * @param {string} status - CALL_STATUS 값
   * @returns {string} 배지 클래스명
   */
  function getSipBadgeClass(status) {
    switch (status) {
      case CALL_STATUS.REGISTERED:  return 'badge-success';
      case CALL_STATUS.REGISTERING: return 'badge-warning';
      case CALL_STATUS.DIALING:     return 'badge-warning';
      case CALL_STATUS.RINGING:     return 'badge-warning';
      case CALL_STATUS.IN_CALL:     return 'badge-incall';
      case CALL_STATUS.IDLE:
      default:                      return 'badge-offline';
    }
  }

  /**
   * callStatus 값에 따라 표시할 레이블을 반환합니다.
   * @param {string} status - CALL_STATUS 값
   * @returns {string} 표시 레이블
   */
  function getSipStatusLabel(status) {
    switch (status) {
      case CALL_STATUS.IDLE:        return 'Idle';
      case CALL_STATUS.REGISTERING: return 'Registering';
      case CALL_STATUS.REGISTERED:  return 'Registered';
      case CALL_STATUS.DIALING:     return 'Dialing';
      case CALL_STATUS.RINGING:     return 'Ringing';
      case CALL_STATUS.IN_CALL:     return 'In Call';
      default:                      return status;
    }
  }

  // ==================== 정리 ====================

  /** UA 등록 해제 및 정지 (beforeUnmount 시 호출) */
  async function destroySip() {
    stopCallTimer(); // 메모리 누수 방지: 인터벌 정리
    if (registerer.value) {
      try { await registerer.value.unregister(); } catch (e) { /* 무시 */ }
    }
    if (userAgent.value) {
      try { await userAgent.value.stop(); } catch (e) { /* 무시 */ }
    }
  }

  return {
    callStatus,
    elapsedSeconds,
    isInProgress,
    sipConfig,    // 템플릿에서 sipConfig.uri 참조 시 필요
    initSip,
    destroySip,
    dial,
    hangup,
    formattedElapsed,
    startCallTimer,
    stopCallTimer,
    getSipBadgeClass,
    getSipStatusLabel
  };
}
