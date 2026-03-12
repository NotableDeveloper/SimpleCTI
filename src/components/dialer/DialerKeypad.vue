<template>
  <!-- ==================== 키패드 ==================== -->
  <div class="keypad-section">
    <div class="keypad-grid">
      <button
        v-for="key in keys"
        :key="key.digit"
        class="key-btn"
        :class="{ 'key-special': key.special }"
        @click="$emit('digit-pressed', key.digit)"
        :disabled="disabled"
        :aria-label="key.digit"
      >
        <span class="key-digit">{{ key.digit }}</span>
        <!-- 서브 레이블이 있는 경우에만 렌더링 -->
        <span v-if="key.sub" class="key-sub">{{ key.sub }}</span>
      </button>
    </div>
  </div>
</template>

<script setup>
// 5. 로컬 state: 키패드 배열 (변경이 없으므로 반응형 불필요)
const keys = [
  { digit: '1', sub: '\u00a0', special: false },
  { digit: '2', sub: 'ABC',    special: false },
  { digit: '3', sub: 'DEF',    special: false },
  { digit: '4', sub: 'GHI',    special: false },
  { digit: '5', sub: 'JKL',    special: false },
  { digit: '6', sub: 'MNO',    special: false },
  { digit: '7', sub: 'PQRS',   special: false },
  { digit: '8', sub: 'TUV',    special: false },
  { digit: '9', sub: 'WXYZ',   special: false },
  { digit: '*', sub: '',        special: true  },
  { digit: '0', sub: '+',       special: false },
  { digit: '#', sub: '',        special: true  },
];

// 3. defineProps / defineEmits
defineProps({
  disabled: { type: Boolean, default: false }
});

defineEmits(['digit-pressed']);
</script>

<style scoped>
/* ==================== 키패드 ==================== */

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
</style>
