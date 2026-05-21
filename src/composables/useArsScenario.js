import { ref } from 'vue'

// 노드 타입 상수
export const NODE_TYPES = {
  GREETING: 'GREETING',
  MENU: 'MENU',
  TRANSFER: 'TRANSFER',
  QUEUE: 'QUEUE',
  HANGUP: 'HANGUP',
}

export const NODE_TYPE_LABELS = {
  GREETING: '음성 안내',
  MENU: '메뉴',
  TRANSFER: '내선 전달',
  QUEUE: '대기열',
  HANGUP: '통화 종료',
}

/**
 * ARS 시나리오 관리 composable.
 * API 호출 및 상태 관리를 담당한다.
 */
export function useArsScenario() {
  // ==================== 상태 ====================

  const scenarios = ref([])
  const currentScenario = ref(null)
  const selectedNode = ref(null)
  const nodes = ref([])
  const loading = ref(false)
  const errorMessage = ref('')

  // ==================== 함수 ====================

  async function fetchScenarios() {
    loading.value = true
    errorMessage.value = ''
    try {
      const response = await fetch(`${process.env.VUE_APP_API_BASE_URL}/api/ars/scenarios`)
      if (!response.ok) throw new Error(`HTTP ${response.status}`)
      const data = await response.json()
      if (data.success) {
        scenarios.value = data.data ?? []
      } else {
        throw new Error('API returned success: false')
      }
    } catch (err) {
      errorMessage.value = 'ARS 시나리오를 불러오지 못했습니다.'
      console.error('[useArsScenario] fetchScenarios error:', err)
    } finally {
      loading.value = false
    }
  }

  async function createScenario(name, description = '') {
    loading.value = true
    errorMessage.value = ''
    try {
      const params = new URLSearchParams({ name, description })
      const response = await fetch(
        `${process.env.VUE_APP_API_BASE_URL}/api/ars/scenarios?${params}`,
        { method: 'POST' }
      )
      if (!response.ok) throw new Error(`HTTP ${response.status}`)
      const data = await response.json()
      if (data.success) {
        await fetchScenarios()
        currentScenario.value = data.data
        selectedNode.value = null
        return data.data
      } else {
        throw new Error('Failed to create scenario')
      }
    } catch (err) {
      errorMessage.value = '시나리오 생성에 실패했습니다.'
      console.error('[useArsScenario] createScenario error:', err)
    } finally {
      loading.value = false
    }
  }

  async function fetchScenario(scenarioId) {
    loading.value = true
    errorMessage.value = ''
    try {
      const response = await fetch(
        `${process.env.VUE_APP_API_BASE_URL}/api/ars/scenarios/${scenarioId}`
      )
      if (!response.ok) throw new Error(`HTTP ${response.status}`)
      const data = await response.json()
      if (data.success) {
        currentScenario.value = data.data
        nodes.value = flattenNodes(data.data.nodes ?? [])
        selectedNode.value = null
        return data.data
      } else {
        throw new Error('Failed to fetch scenario')
      }
    } catch (err) {
      errorMessage.value = '시나리오를 불러오지 못했습니다.'
      console.error('[useArsScenario] fetchScenario error:', err)
    } finally {
      loading.value = false
    }
  }

  async function updateScenario(scenarioId, name, description) {
    loading.value = true
    errorMessage.value = ''
    try {
      const params = new URLSearchParams({ name, description })
      const response = await fetch(
        `${process.env.VUE_APP_API_BASE_URL}/api/ars/scenarios/${scenarioId}?${params}`,
        { method: 'PUT' }
      )
      if (!response.ok) throw new Error(`HTTP ${response.status}`)
      const data = await response.json()
      if (data.success) {
        currentScenario.value = data.data
        return data.data
      } else {
        throw new Error('Failed to update scenario')
      }
    } catch (err) {
      errorMessage.value = '시나리오 수정에 실패했습니다.'
      console.error('[useArsScenario] updateScenario error:', err)
    } finally {
      loading.value = false
    }
  }

  async function deleteScenario(scenarioId) {
    loading.value = true
    errorMessage.value = ''
    try {
      const response = await fetch(
        `${process.env.VUE_APP_API_BASE_URL}/api/ars/scenarios/${scenarioId}`,
        { method: 'DELETE' }
      )
      if (!response.ok) throw new Error(`HTTP ${response.status}`)
      const data = await response.json()
      if (data.success) {
        await fetchScenarios()
        currentScenario.value = null
        nodes.value = []
        selectedNode.value = null
        return true
      } else {
        throw new Error('Failed to delete scenario')
      }
    } catch (err) {
      errorMessage.value = '시나리오 삭제에 실패했습니다.'
      console.error('[useArsScenario] deleteScenario error:', err)
    } finally {
      loading.value = false
    }
  }

  async function addNode(scenarioId, nodeType, label, parentId = null, digit = '') {
    loading.value = true
    errorMessage.value = ''
    try {
      const params = new URLSearchParams({ nodeType, label, digit })
      if (parentId !== null) params.append('parentId', parentId)

      const response = await fetch(
        `${process.env.VUE_APP_API_BASE_URL}/api/ars/scenarios/${scenarioId}/nodes?${params}`,
        { method: 'POST' }
      )
      if (!response.ok) throw new Error(`HTTP ${response.status}`)
      const data = await response.json()
      if (data.success) {
        await fetchScenario(scenarioId)
        return data.data
      } else {
        throw new Error('Failed to add node')
      }
    } catch (err) {
      errorMessage.value = '노드 추가에 실패했습니다.'
      console.error('[useArsScenario] addNode error:', err)
    } finally {
      loading.value = false
    }
  }

  async function updateNode(nodeId, fields) {
    loading.value = true
    errorMessage.value = ''
    try {
      const params = new URLSearchParams()
      if (fields.label !== undefined) params.append('label', fields.label)
      if (fields.audioFile !== undefined) params.append('audioFile', fields.audioFile)
      if (fields.transferTarget !== undefined) params.append('transferTarget', fields.transferTarget)
      if (fields.queueName !== undefined) params.append('queueName', fields.queueName)

      const response = await fetch(
        `${process.env.VUE_APP_API_BASE_URL}/api/ars/nodes/${nodeId}?${params}`,
        { method: 'PUT' }
      )
      if (!response.ok) throw new Error(`HTTP ${response.status}`)
      const data = await response.json()
      if (data.success) {
        // 현재 시나리오 다시 로드
        if (currentScenario.value && currentScenario.value.id) {
          await fetchScenario(currentScenario.value.id)
        }
        return data.data
      } else {
        throw new Error('Failed to update node')
      }
    } catch (err) {
      errorMessage.value = '노드 수정에 실패했습니다.'
      console.error('[useArsScenario] updateNode error:', err)
    } finally {
      loading.value = false
    }
  }

  async function deleteNode(nodeId) {
    loading.value = true
    errorMessage.value = ''
    try {
      const response = await fetch(
        `${process.env.VUE_APP_API_BASE_URL}/api/ars/nodes/${nodeId}`,
        { method: 'DELETE' }
      )
      if (!response.ok) throw new Error(`HTTP ${response.status}`)
      const data = await response.json()
      if (data.success) {
        // 현재 시나리오 다시 로드
        if (currentScenario.value && currentScenario.value.id) {
          await fetchScenario(currentScenario.value.id)
        }
        selectedNode.value = null
        return true
      } else {
        throw new Error('Failed to delete node')
      }
    } catch (err) {
      errorMessage.value = '노드 삭제에 실패했습니다.'
      console.error('[useArsScenario] deleteNode error:', err)
    } finally {
      loading.value = false
    }
  }

  async function setKeyMappings(nodeId, mappings) {
    loading.value = true
    errorMessage.value = ''
    try {
      const response = await fetch(
        `${process.env.VUE_APP_API_BASE_URL}/api/ars/nodes/${nodeId}/key-mappings`,
        {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(mappings),
        }
      )
      if (!response.ok) throw new Error(`HTTP ${response.status}`)
      const data = await response.json()
      if (data.success) {
        // 현재 시나리오 다시 로드
        if (currentScenario.value && currentScenario.value.id) {
          await fetchScenario(currentScenario.value.id)
        }
        return true
      } else {
        throw new Error('Failed to set key mappings')
      }
    } catch (err) {
      errorMessage.value = '키 매핑 설정에 실패했습니다.'
      console.error('[useArsScenario] setKeyMappings error:', err)
    } finally {
      loading.value = false
    }
  }

  async function activateScenario(scenarioId) {
    loading.value = true
    errorMessage.value = ''
    try {
      const response = await fetch(
        `${process.env.VUE_APP_API_BASE_URL}/api/ars/scenarios/${scenarioId}/activate`,
        { method: 'POST' }
      )
      if (!response.ok) throw new Error(`HTTP ${response.status}`)
      const data = await response.json()
      if (data.success) {
        await fetchScenarios()
        return true
      } else {
        throw new Error('Failed to activate scenario')
      }
    } catch (err) {
      errorMessage.value = '시나리오 활성화에 실패했습니다.'
      console.error('[useArsScenario] activateScenario error:', err)
    } finally {
      loading.value = false
    }
  }

  // Helper 함수: 트리를 평탄화하여 모든 노드를 배열로 반환
  function flattenNodes(tree, result = []) {
    for (const node of tree) {
      result.push(node)
      if (node.children && node.children.length > 0) {
        flattenNodes(node.children, result)
      }
    }
    return result
  }

  return {
    // state
    scenarios,
    currentScenario,
    selectedNode,
    nodes,
    loading,
    errorMessage,
    // functions
    fetchScenarios,
    createScenario,
    fetchScenario,
    updateScenario,
    deleteScenario,
    addNode,
    updateNode,
    deleteNode,
    setKeyMappings,
    activateScenario,
  }
}
