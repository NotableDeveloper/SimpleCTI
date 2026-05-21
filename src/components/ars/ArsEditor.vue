<template>
  <main class="ars-editor-page">

    <!-- 상단 툴바 -->
    <div class="ars-toolbar">
      <select v-model="selectedScenarioId" @change="handleScenarioChange" class="scenario-select">
        <option value="">— 시나리오를 선택하세요 —</option>
        <option value="new">+ 새 시나리오 만들기</option>
        <option v-for="scenario in scenarios" :key="scenario.id" :value="scenario.id">
          {{ scenario.name }}
          <span v-if="scenario.isActive" class="active-marker">(활성)</span>
        </option>
      </select>

      <div class="toolbar-actions">
        <button @click="handleActivate" class="btn btn-success" v-if="currentScenario">
          Asterisk 적용
        </button>
      </div>
    </div>

    <!-- 에러 메시지 -->
    <div v-if="errorMessage" class="error-banner">
      {{ errorMessage }}
    </div>

    <!-- 3단 레이아웃 -->
    <div v-if="currentScenario" class="ars-layout">

      <!-- 좌측: 노드 팔레트 -->
      <div class="ars-palette">
        <div class="palette-header">노드 추가</div>
        <div class="palette-buttons">
          <button v-for="(label, type) in nodeTypeOptions" :key="type"
                  @click="handleAddNode(type)" class="btn-node-type">
            {{ label }}
          </button>
        </div>
      </div>

      <!-- 중앙: 노드 트리 뷰 -->
      <div class="ars-canvas">
        <div v-if="loading" class="loading">로딩 중...</div>
        <div v-else-if="nodeTree.length === 0" class="empty">
          노드를 추가하세요
        </div>
        <ArsNodeTree
          v-else
          :nodes="nodeTree"
          :selected-node-id="selectedNode?.id"
          @select-node="handleSelectNode"
          @delete-node="handleDeleteNode"
        />
      </div>

      <!-- 우측: 노드 속성 편집 패널 -->
      <div class="ars-config-panel">
        <ArsNodeConfigPanel
          :node="selectedNode"
          :all-nodes="nodes"
          @update-node="handleUpdateNode"
          @delete-node="handleDeleteNode"
        />
      </div>

    </div>

    <!-- 시나리오 미선택 상태 -->
    <div v-else class="ars-empty">
      <p>시나리오를 선택하거나 새로 만들어보세요</p>
    </div>

  </main>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useArsScenario, NODE_TYPES, NODE_TYPE_LABELS } from '@/composables/useArsScenario'
import ArsNodeTree from './ArsNodeTree.vue'
import ArsNodeConfigPanel from './ArsNodeConfigPanel.vue'

const {
  scenarios,
  currentScenario,
  selectedNode,
  nodes,
  loading,
  errorMessage,
  fetchScenarios,
  fetchScenario,
  createScenario,
  addNode,
  updateNode,
  deleteNode,
  setKeyMappings,
  activateScenario,
} = useArsScenario()

const selectedScenarioId = ref('')
const nodeTree = ref([])

const nodeTypeOptions = {
  GREETING: NODE_TYPE_LABELS.GREETING,
  MENU: NODE_TYPE_LABELS.MENU,
  TRANSFER: NODE_TYPE_LABELS.TRANSFER,
  QUEUE: NODE_TYPE_LABELS.QUEUE,
  HANGUP: NODE_TYPE_LABELS.HANGUP,
}

onMounted(() => {
  fetchScenarios()
})

async function handleScenarioChange() {
  if (selectedScenarioId.value === 'new') {
    const name = prompt('새 시나리오 이름을 입력하세요:')
    if (name) {
      await createScenario(name, '')
      if (currentScenario.value) {
        selectedScenarioId.value = currentScenario.value.id
        nodeTree.value = currentScenario.value.nodes || []
      }
    } else {
      selectedScenarioId.value = ''
    }
  } else if (selectedScenarioId.value) {
    await fetchScenario(parseInt(selectedScenarioId.value))
    if (currentScenario.value) {
      nodeTree.value = currentScenario.value.nodes || []
    }
  } else {
    nodeTree.value = []
  }
}

async function handleAddNode(nodeType) {
  let parentId = null

  // 선택된 노드가 있으면 그것을 부모로, 없으면 루트 노드를 부모로
  if (selectedNode.value) {
    if (nodeType === NODE_TYPES.TRANSFER || nodeType === NODE_TYPES.QUEUE || nodeType === NODE_TYPES.HANGUP) {
      parentId = selectedNode.value.id
    } else {
      parentId = selectedNode.value.id
    }
  } else if (currentScenario.value?.rootNodeId) {
    parentId = currentScenario.value.rootNodeId
  }

  const label = prompt(`${nodeTypeOptions[nodeType]} 노드의 이름을 입력하세요:`) || nodeTypeOptions[nodeType]

  if (currentScenario.value) {
    await addNode(currentScenario.value.id, nodeType, label, parentId)
    await fetchScenario(currentScenario.value.id)
    nodeTree.value = currentScenario.value.nodes || []
  }
}

function handleSelectNode(node) {
  selectedNode.value = node
}

async function handleUpdateNode(updateData) {
  const { nodeId, label, audioFile, transferTarget, queueName, keyMappings } = updateData

  const fields = {}
  if (label !== undefined) fields.label = label
  if (audioFile !== undefined) fields.audioFile = audioFile
  if (transferTarget !== undefined) fields.transferTarget = transferTarget
  if (queueName !== undefined) fields.queueName = queueName

  // 먼저 노드 정보 업데이트
  if (Object.keys(fields).length > 0) {
    await updateNode(nodeId, fields)
  }

  // 키 매핑이 있으면 업데이트
  if (keyMappings && Object.keys(keyMappings).length > 0) {
    const mappings = Object.entries(keyMappings)
      .filter(([, childNodeId]) => childNodeId)
      .map(([digit, childNodeId]) => ({
        digit,
        childNodeId: typeof childNodeId === 'string' ? parseInt(childNodeId) : childNodeId,
      }))

    if (mappings.length > 0) {
      await setKeyMappings(nodeId, mappings)
    }
  }

  // 현재 시나리오 다시 로드
  if (currentScenario.value) {
    await fetchScenario(currentScenario.value.id)
    nodeTree.value = currentScenario.value.nodes || []
  }
}

async function handleDeleteNode(nodeId) {
  if (confirm('이 노드를 삭제하시겠습니까?')) {
    await deleteNode(nodeId)
    if (currentScenario.value) {
      await fetchScenario(currentScenario.value.id)
      nodeTree.value = currentScenario.value.nodes || []
    }
  }
}

async function handleActivate() {
  if (currentScenario.value && confirm(`"${currentScenario.value.name}" 시나리오를 Asterisk에 적용하시겠습니까?`)) {
    await activateScenario(currentScenario.value.id)
    await fetchScenarios()
    selectedScenarioId.value = currentScenario.value.id
  }
}
</script>

<style scoped>
.ars-editor-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--color-bg);
}

.ars-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: white;
  border-bottom: 1px solid var(--color-border);
  flex-wrap: wrap;
}

.scenario-select {
  padding: 8px 12px;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  font-size: 13px;
  background: white;
  flex: 1;
  min-width: 200px;
}

.toolbar-actions {
  display: flex;
  gap: 8px;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-success {
  background: var(--color-success);
  color: white;
}

.btn-success:hover {
  opacity: 0.9;
}

.error-banner {
  padding: 10px 16px;
  background: #ffe8e8;
  color: var(--color-error);
  font-size: 13px;
  border-bottom: 1px solid var(--color-border);
}

.ars-layout {
  display: flex;
  flex: 1;
  gap: 0;
  overflow: hidden;
}

.ars-palette {
  width: 150px;
  padding: 12px;
  background: white;
  border-right: 1px solid var(--color-border);
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.palette-header {
  font-weight: 600;
  font-size: 12px;
  color: var(--color-text-primary);
  text-transform: uppercase;
  padding: 8px 0;
  border-bottom: 1px solid var(--color-border);
  margin-bottom: 8px;
}

.palette-buttons {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.btn-node-type {
  padding: 8px 10px;
  background: var(--color-primary);
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  white-space: normal;
  line-height: 1.3;
}

.btn-node-type:hover {
  background: var(--color-primary-hover);
}

.ars-canvas {
  flex: 1;
  background: white;
  overflow-y: auto;
  border-right: 1px solid var(--color-border);
  position: relative;
}

.ars-canvas.empty,
.ars-canvas .loading {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--color-text-secondary);
  font-size: 14px;
}

.ars-config-panel {
  width: 280px;
  background: var(--color-card);
  border-left: 1px solid var(--color-border);
  overflow-y: auto;
}

.ars-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
  color: var(--color-text-secondary);
  font-size: 14px;
}

/* 반응형 */
@media (max-width: 1024px) {
  .ars-palette {
    width: 120px;
  }

  .ars-config-panel {
    width: 200px;
  }
}

@media (max-width: 768px) {
  .ars-layout {
    flex-direction: column;
  }

  .ars-palette,
  .ars-config-panel {
    width: 100%;
    border: none;
    border-top: 1px solid var(--color-border);
    max-height: 200px;
  }
}
</style>
