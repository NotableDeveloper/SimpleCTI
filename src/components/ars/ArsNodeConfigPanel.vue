<template>
  <div v-if="node" class="ars-node-config-panel">

    <!-- 노드 타입 및 라벨 -->
    <div class="config-section">
      <label>노드 유형</label>
      <div class="type-display">{{ NODE_TYPE_LABELS[node.nodeType] || node.nodeType }}</div>
    </div>

    <div class="config-section">
      <label>라벨</label>
      <input v-model="editForm.label" type="text" class="form-input" placeholder="노드 이름">
    </div>

    <!-- 노드 타입별 조건부 필드 -->
    <template v-if="node.nodeType === NODE_TYPES.GREETING || node.nodeType === NODE_TYPES.MENU">
      <div class="config-section">
        <label>오디오 파일</label>
        <input v-model="editForm.audioFile" type="text" class="form-input" placeholder="파일명">
      </div>
    </template>

    <template v-if="node.nodeType === NODE_TYPES.TRANSFER">
      <div class="config-section">
        <label>전달 대상 (내선번호)</label>
        <input v-model="editForm.transferTarget" type="text" class="form-input" placeholder="예: 1001">
      </div>
    </template>

    <template v-if="node.nodeType === NODE_TYPES.QUEUE">
      <div class="config-section">
        <label>큐 이름</label>
        <input v-model="editForm.queueName" type="text" class="form-input" placeholder="예: sales">
      </div>
    </template>

    <!-- 키 분기 설정 (GREETING, MENU만) -->
    <template v-if="node.nodeType === NODE_TYPES.GREETING || node.nodeType === NODE_TYPES.MENU">
      <div class="config-section key-mappings">
        <label>키 분기 설정</label>
        <div class="key-mapping-list">
          <div v-for="digit in ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9']" :key="digit" class="key-mapping-item">
            <span class="key-digit">{{ digit }}</span>
            <select v-model="editForm.keyMappings[digit]" class="form-select">
              <option value="">연결 안 함</option>
              <option v-for="child in allNodes" :key="child.id" :value="child.id">
                {{ child.label || `노드 ${child.id}` }}
              </option>
            </select>
          </div>
        </div>
      </div>
    </template>

    <!-- 저장 버튼 -->
    <div class="config-actions">
      <button @click="handleSave" class="btn btn-primary">저장</button>
      <button @click="handleDelete" class="btn btn-danger">삭제</button>
    </div>

  </div>
  <div v-else class="ars-node-config-panel empty">
    <p>노드를 선택해주세요</p>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { NODE_TYPES, NODE_TYPE_LABELS } from '@/composables/useArsScenario'

const props = defineProps({
  node: Object,
  allNodes: {
    type: Array,
    default: () => [],
  },
})

const emit = defineEmits(['update-node', 'delete-node'])

const editForm = ref({
  label: '',
  audioFile: '',
  transferTarget: '',
  queueName: '',
  keyMappings: {},
})

watch(
  () => props.node,
  (newNode) => {
    if (newNode) {
      editForm.value.label = newNode.label || ''
      editForm.value.audioFile = newNode.audioFile || ''
      editForm.value.transferTarget = newNode.transferTarget || ''
      editForm.value.queueName = newNode.queueName || ''

      // 키 매핑 초기화
      const mappings = {}
      for (let i = 0; i <= 9; i++) {
        mappings[String(i)] = ''
      }
      if (newNode.keyMappings && Array.isArray(newNode.keyMappings)) {
        newNode.keyMappings.forEach((km) => {
          mappings[km.digit] = km.childNodeId
        })
      }
      editForm.value.keyMappings = mappings
    }
  },
  { immediate: true, deep: true }
)

function handleSave() {
  emit('update-node', {
    nodeId: props.node.id,
    label: editForm.value.label,
    audioFile: editForm.value.audioFile,
    transferTarget: editForm.value.transferTarget,
    queueName: editForm.value.queueName,
    keyMappings: editForm.value.keyMappings,
  })
}

function handleDelete() {
  if (confirm('이 노드를 삭제하시겠습니까?')) {
    emit('delete-node', props.node.id)
  }
}
</script>

<style scoped>
.ars-node-config-panel {
  padding: 16px;
  background: var(--color-card);
  border-left: 1px solid var(--color-border);
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.ars-node-config-panel.empty {
  justify-content: center;
  align-items: center;
  color: var(--color-text-secondary);
  font-size: 14px;
}

.config-section {
  margin-bottom: 20px;
}

.config-section label {
  display: block;
  font-weight: 500;
  margin-bottom: 8px;
  color: var(--color-text-primary);
  font-size: 13px;
}

.type-display {
  padding: 8px 12px;
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  border-radius: 4px;
  color: var(--color-text-secondary);
  font-size: 13px;
}

.form-input,
.form-select {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  font-size: 13px;
  background: white;
  color: var(--color-text-primary);
}

.form-input:focus,
.form-select:focus {
  outline: none;
  border-color: var(--color-primary);
}

.key-mappings {
  border: 1px solid var(--color-border);
  border-radius: 4px;
  padding: 12px;
  background: var(--color-bg);
}

.key-mapping-list {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.key-mapping-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.key-digit {
  width: 30px;
  padding: 6px;
  background: var(--color-primary);
  color: white;
  border-radius: 4px;
  text-align: center;
  font-weight: 500;
  font-size: 12px;
}

.form-select {
  flex: 1;
  font-size: 12px;
}

.config-actions {
  display: flex;
  gap: 10px;
  margin-top: auto;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  flex: 1;
}

.btn-primary {
  background: var(--color-primary);
  color: white;
}

.btn-primary:hover {
  background: var(--color-primary-hover);
}

.btn-danger {
  background: var(--color-error);
  color: white;
}

.btn-danger:hover {
  opacity: 0.9;
}
</style>
