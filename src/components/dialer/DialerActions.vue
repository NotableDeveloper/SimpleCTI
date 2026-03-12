<template>
  <!-- ==================== 녹음 토글 옵션 ==================== -->
  <div class="dialer-options">
    <label class="record-toggle" :class="{ disabled: callStatus !== CALL_STATUS.REGISTERED }">
      <input
        type="checkbox"
        :checked="recordingEnabled"
        @change="$emit('update:recordingEnabled', $event.target.checked)"
        :disabled="callStatus !== CALL_STATUS.REGISTERED"
      />
      <div class="toggle-track"></div>
      <span class="record-label" :class="{ 'record-label-active': recordingEnabled && callStatus === CALL_STATUS.REGISTERED }">
        <!-- 녹음 아이콘 -->
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="M12 15c1.66 0 3-1.34 3-3V6c0-1.66-1.34-3-3-3S9 4.34 9 6v6c0 1.66 1.34 3 3 3zm5.91-3c-.49 0-.9.36-.98.85C16.52 15.2 14.47 17 12 17s-4.52-1.8-4.93-4.15c-.08-.49-.49-.85-.98-.85-.61 0-1.09.54-1 1.14.49 3 2.89 5.35 5.91 5.78V21c0 .55.45 1 1 1s1-.45 1-1v-2.08c3.02-.43 5.42-2.78 5.91-5.78.1-.6-.39-1.14-1-1.14z"/>
        </svg>
        통화 녹음
      </span>
    </label>
  </div>

  <!-- ==================== 발신 / 종료 버튼 ==================== -->
  <div class="dialer-actions">

    <!-- 발신 버튼: Registered 또는 Idle 상태일 때 표시 -->
    <div v-if="callStatus === CALL_STATUS.REGISTERED || callStatus === CALL_STATUS.IDLE" class="action-btn-wrap">
      <button
        class="action-btn-circle btn-call"
        @click="$emit('dial')"
        :disabled="callStatus === CALL_STATUS.IDLE"
        :style="{ opacity: callStatus === CALL_STATUS.IDLE ? '0.5' : '1', cursor: callStatus === CALL_STATUS.IDLE ? 'not-allowed' : 'pointer' }"
        aria-label="발신"
      >
        <!-- 전화 발신 아이콘 -->
        <svg viewBox="0 0 24 24" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
          <path d="M6.6 10.8c1.4 2.8 3.8 5.1 6.6 6.6l2.2-2.2c.3-.3.7-.4 1-.2 1.1.4 2.3.6 3.6.6.6 0 1 .4 1 1V20c0 .6-.4 1-1 1-9.4 0-17-7.6-17-17 0-.6.4-1 1-1h3.5c.6 0 1 .4 1 1 0 1.3.2 2.5.6 3.6.1.3 0 .7-.2 1L6.6 10.8z"/>
        </svg>
      </button>
      <div class="btn-label">발신</div>
    </div>

    <!-- 종료 버튼: Ringing, Dialing, InCall 상태일 때 표시 -->
    <div v-else class="action-btn-wrap">
      <button
        class="action-btn-circle btn-hangup"
        @click="$emit('hangup')"
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
</template>

<script setup>
// 2. 내부 모듈 import
import { CALL_STATUS } from '@/composables/useSip';

// 3. defineProps / defineEmits
defineProps({
  callStatus:       { type: String,  required: true },
  recordingEnabled: { type: Boolean, required: true },
  isInProgress:     { type: Boolean, required: true }
});

defineEmits(['update:recordingEnabled', 'dial', 'hangup']);
</script>

<style scoped>
/* ==================== 녹음 토글 옵션 ==================== */

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

/* recordingEnabled가 true일 때 토글 트랙 색상 변경 */
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

/* ==================== 발신 / 종료 버튼 ==================== */

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
</style>
