<template>
  <!-- ==================== 전화번호 입력 영역 ==================== -->
  <div class="dialer-display">
    <div class="display-label">전화번호 입력</div>
    <div class="display-input-row">
      <input
        type="tel"
        class="display-input"
        :value="modelValue"
        @input="$emit('update:modelValue', $event.target.value)"
        placeholder="번호를 입력하세요"
        maxlength="20"
        autocomplete="off"
        inputmode="numeric"
        :disabled="isInProgress"
        @keyup.enter="$emit('submit')"
      />
      <button
        class="backspace-btn"
        @click="$emit('backspace')"
        :disabled="isInProgress"
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

  <!-- ==================== InCall 상태 바 ==================== -->
  <div class="incall-info" :class="{ visible: isInProgress }">
    <div class="incall-left">
      <span class="incall-pulse-dot"></span>
      <span class="incall-number">{{ modelValue || '--' }}</span>
      <span v-if="callStatus === CALL_STATUS.RINGING || callStatus === CALL_STATUS.DIALING" class="incall-connecting">연결 중...</span>
    </div>
    <span v-if="callStatus === CALL_STATUS.IN_CALL" class="incall-status-label">통화 중</span>
  </div>

  <!-- ==================== 타이머 바 ==================== -->
  <div class="incall-timer-bar" :class="{ visible: callStatus === CALL_STATUS.IN_CALL }">
    <svg class="incall-clock-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
      <circle cx="12" cy="12" r="9"/>
      <polyline points="12 7 12 12 15.5 15.5"/>
    </svg>
    <span class="incall-timer">{{ formattedElapsed }}</span>
  </div>
</template>

<script setup>
// 1. 외부 라이브러리 import
import { computed } from 'vue';

// 2. 내부 모듈 import
import { CALL_STATUS } from '@/composables/useSip';

// 3. defineProps / defineEmits
const props = defineProps({
  modelValue:     { type: String,  required: true },
  callStatus:     { type: String,  required: true },
  isInProgress:   { type: Boolean, required: true },
  elapsedSeconds: { type: Number,  required: true }
});

defineEmits(['update:modelValue', 'backspace', 'submit']);

// 6. computed: 경과 초를 MM:SS 형식으로 변환
const formattedElapsed = computed(() => {
  const m = Math.floor(props.elapsedSeconds / 60).toString().padStart(2, '0');
  const s = (props.elapsedSeconds % 60).toString().padStart(2, '0');
  return `${m}:${s}`;
});
</script>

<style scoped>
/* ==================== 전화번호 입력 영역 ==================== */

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

/* ==================== 통화 중 상태 바 ==================== */

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

.incall-left {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

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

.incall-connecting {
  font-size: 12px;
  font-weight: 500;
  color: #b45309;
  font-style: italic;
}

.incall-status-label {
  font-size: 12px;
  font-weight: 600;
  color: #7c3aed;
  letter-spacing: 0.3px;
  flex-shrink: 0;
}

/* ==================== 타이머 바 ==================== */

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
</style>
