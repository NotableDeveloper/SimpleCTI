<template>
  <div class="ars-node-tree">
    <div v-for="node in visibleNodes" :key="node.id" class="node-item">
      <!-- 노드 헤더 -->
      <div
        :class="['node-header', { selected: selectedNodeId === node.id }]"
        @click="selectNode(node)"
      >
        <!-- 확장/축소 토글 -->
        <span v-if="node.children && node.children.length > 0" class="expand-toggle">
          <span v-if="expandedNodeIds.has(node.id)" @click.stop="toggleExpand(node.id)">▼</span>
          <span v-else @click.stop="toggleExpand(node.id)">▶</span>
        </span>
        <span v-else class="expand-toggle"></span>

        <!-- 노드 정보 -->
        <span class="node-label">{{ node.label || `노드 ${node.id}` }}</span>
        <span :class="['node-type-badge', `type-${node.nodeType.toLowerCase()}`]">
          {{ NODE_TYPE_LABELS[node.nodeType] || node.nodeType }}
        </span>

        <!-- 삭제 버튼 -->
        <button
          v-if="node.parentId !== null"
          class="btn-delete"
          @click.stop="deleteNode(node.id)"
          title="삭제"
        >
          ✕
        </button>
      </div>

      <!-- 자식 노드 (재귀) -->
      <div v-if="node.children && node.children.length > 0 && expandedNodeIds.has(node.id)" class="children">
        <ArsNodeTree
          :nodes="node.children"
          :selected-node-id="selectedNodeId"
          @select-node="selectNode"
          @delete-node="deleteNode"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { NODE_TYPE_LABELS } from '@/composables/useArsScenario'

const props = defineProps({
  nodes: {
    type: Array,
    default: () => [],
  },
  selectedNodeId: {
    type: Number,
    default: null,
  },
})

const emit = defineEmits(['select-node', 'delete-node'])

const expandedNodeIds = ref(new Set())

const visibleNodes = computed(() => props.nodes)

function toggleExpand(nodeId) {
  if (expandedNodeIds.value.has(nodeId)) {
    expandedNodeIds.value.delete(nodeId)
  } else {
    expandedNodeIds.value.add(nodeId)
  }
}

function selectNode(node) {
  emit('select-node', node)
}

function deleteNode(nodeId) {
  emit('delete-node', nodeId)
}
</script>

<style scoped>
.ars-node-tree {
  padding: 12px;
}

.node-item {
  margin-bottom: 8px;
}

.node-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 13px;
}

.node-header:hover {
  background: #f5f5f5;
  border-color: var(--color-primary);
}

.node-header.selected {
  background: var(--color-primary);
  color: white;
  border-color: var(--color-primary);
}

.expand-toggle {
  display: inline-block;
  width: 20px;
  text-align: center;
  color: var(--color-text-secondary);
  user-select: none;
  cursor: pointer;
  font-size: 11px;
}

.node-header.selected .expand-toggle {
  color: white;
}

.node-label {
  flex: 1;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.node-type-badge {
  display: inline-block;
  padding: 3px 8px;
  border-radius: 3px;
  font-size: 11px;
  font-weight: 500;
  white-space: nowrap;
}

.type-greeting {
  background: #d4edff;
  color: #0052cc;
}

.node-header.selected .type-greeting {
  background: rgba(255, 255, 255, 0.3);
  color: white;
}

.type-menu {
  background: #fff1d9;
  color: #8b3e00;
}

.node-header.selected .type-menu {
  background: rgba(255, 255, 255, 0.3);
  color: white;
}

.type-transfer {
  background: #dff0de;
  color: #216e4e;
}

.node-header.selected .type-transfer {
  background: rgba(255, 255, 255, 0.3);
  color: white;
}

.type-queue {
  background: #ffe8e6;
  color: #932f1d;
}

.node-header.selected .type-queue {
  background: rgba(255, 255, 255, 0.3);
  color: white;
}

.type-hangup {
  background: #f0f0f0;
  color: #666;
}

.node-header.selected .type-hangup {
  background: rgba(255, 255, 255, 0.3);
  color: white;
}

.btn-delete {
  background: none;
  border: none;
  color: var(--color-error);
  cursor: pointer;
  font-size: 14px;
  padding: 0 4px;
  transition: all 0.2s;
}

.btn-delete:hover {
  color: #c00;
}

.node-header.selected .btn-delete {
  color: rgba(255, 255, 255, 0.8);
}

.children {
  margin-left: 20px;
  margin-top: 8px;
  padding-left: 12px;
  border-left: 2px solid var(--color-border);
}
</style>
