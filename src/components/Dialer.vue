<template>
  <div class="dialer-page">

    <!-- ==================== 상단 바 ==================== -->
    <AppTopbar title="수동 콜" breadcrumb="수동 콜">
      <template #actions>
        <!-- 대시보드로 돌아가기 버튼 -->
        <button class="back-btn" @click="$emit('back')">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="15 18 9 12 15 6"/>
          </svg>
          대시보드로 돌아가기
        </button>
      </template>
    </AppTopbar>

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
          <DialerDisplay
            v-model="targetNumber"
            :callStatus="callStatus"
            :isInProgress="isInProgress"
            :elapsedSeconds="elapsedSeconds"
            @backspace="backspace"
            @submit="dial(targetNumber)"
          />

          <!-- 키패드 -->
          <DialerKeypad :disabled="isInProgress" @digit-pressed="appendDigit" />

          <!-- 녹음 토글 + 발신/종료 버튼 -->
          <DialerActions
            :callStatus="callStatus"
            :recordingEnabled="recordingEnabled"
            :isInProgress="isInProgress"
            @update:recordingEnabled="recordingEnabled = $event"
            @dial="dial(targetNumber)"
            @hangup="hangup"
          />

        </div>
        <!-- /dialer-card -->

        <!-- ===== 오른쪽: SIP 로그 패널 ===== -->
        <SipLogPanel ref="sipLogPanel" :call-status="callStatus" />

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
import { ref } from 'vue';
import { useSip } from '@/composables/useSip';
import SipLogPanel from './dialer/SipLogPanel.vue';
import DialerDisplay from './dialer/DialerDisplay.vue';
import DialerKeypad from './dialer/DialerKeypad.vue';
import DialerActions from './dialer/DialerActions.vue';
import AppTopbar from '@/components/common/AppTopbar.vue';

export default {
  name: 'DialerPage',
  emits: ['back'],
  components: { SipLogPanel, DialerDisplay, DialerKeypad, DialerActions, AppTopbar },
  setup() {
    // SipLogPanel 컴포넌트 ref: handleSipLogConnector 접근용
    const sipLogPanel = ref(null);

    // logConnector는 SipLogPanel이 마운트된 이후부터 동작합니다.
    const logConnector = (level, category, label, content) => {
      sipLogPanel.value?.handleSipLogConnector(level, category, label, content);
    };

    // useSip에 오디오 ref와 logConnector를 전달합니다.
    const remoteAudio = ref(null);
    const sip = useSip(remoteAudio, logConnector);

    return { sipLogPanel, remoteAudio, ...sip };
  },
  data() {
    return {
      targetNumber: '',
      recordingEnabled: false
    }
  },
  methods: {
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
    }
  },
  mounted() {
    this.initSip();
  },
  beforeUnmount() {
    this.destroySip();
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

/* ==================== 상태 배지 (badge-incall 추가 정의) ==================== */

/* badge-success, badge-warning, badge-offline, badge-error는 App.vue 전역에 정의되어 있으므로
   여기서는 badge-incall만 추가 정의합니다. */
.badge-incall {
  background: #fdf0ff;
  color: #7c3aed;
  border: 1px solid #ddd1f7;
}

.badge-incall .badge-dot {
  background: #7c3aed;
}

/* pulse 애니메이션은 App.vue 전역에 정의되어 있으므로 여기서 중복 선언하지 않습니다. */

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

/* ==================== 대시보드로 돌아가기 버튼 ==================== */

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

/* ==================== 반응형 ==================== */

/* 1024px 이하: 2단 레이아웃 -> 1단 세로 배치 */
@media (max-width: 1024px) {
  .dual-layout {
    grid-template-columns: 1fr;
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

  .dialer-card {
    max-width: 100%;
  }
}
</style>
