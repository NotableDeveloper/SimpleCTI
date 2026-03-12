<template>
  <!-- 페이지 전용 하위 상단 바 (App.vue 글로벌 topbar 아래 sticky 위치) -->
  <header class="app-topbar">
    <div class="topbar-title">
      <span class="topbar-heading">{{ title }}</span>
      <span class="topbar-breadcrumb">Simple CTI &rsaquo; {{ breadcrumb }}</span>
    </div>
    <div class="topbar-actions">
      <!-- 페이지별 액션 버튼을 slot으로 주입 -->
      <slot name="actions" />
    </div>
  </header>
</template>

<script setup>
// 3. defineProps
defineProps({
  title:      { type: String, required: true },
  breadcrumb: { type: String, required: true }
});
</script>

<style scoped>
/* ==================== 페이지 전용 하위 topbar ==================== */

/*
 * App.vue 글로벌 topbar(top: 0, height: 60px) 아래에 sticky 위치합니다.
 * App.vue topbar 높이가 변경되면 top 값도 함께 수정해야 합니다.
 */
.app-topbar {
  background: var(--color-card);
  border-bottom: 1px solid var(--color-border);
  padding: 0 32px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  top: 60px;
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
</style>
