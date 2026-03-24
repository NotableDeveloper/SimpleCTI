import { ref, computed } from 'vue'

// ─────────────────────────────────────────────────────────────────────────────
// 상수
// ─────────────────────────────────────────────────────────────────────────────

/** disposition 필터 옵션 */
export const DISPOSITION_OPTIONS = [
  { label: '전체', value: '' },
  { label: 'ANSWERED', value: 'ANSWERED' },
  { label: 'NO ANSWER', value: 'NO ANSWER' },
  { label: 'BUSY', value: 'BUSY' },
  { label: 'FAILED', value: 'FAILED' },
]

// ─────────────────────────────────────────────────────────────────────────────
// composable
// ─────────────────────────────────────────────────────────────────────────────

/**
 * CDR(통화 이력) API 호출 및 상태 관리 composable.
 * GET /api/cdr/list?number=&disposition=&page= 를 호출한다.
 */
export function useCdr() {
  // 5. 로컬 ref / reactive state

  /** CDR 목록 (현재 페이지) */
  const records = ref([])
  /** 전체 CDR 건수 */
  const totalElements = ref(0)
  /** 전체 페이지 수 */
  const totalPages = ref(0)
  /** 현재 페이지 (0-based) */
  const currentPage = ref(0)
  /** 로딩 중 여부 */
  const loading = ref(false)
  /** 에러 메시지 */
  const errorMessage = ref('')

  /** 번호 검색어 */
  const searchNumber = ref('')
  /** 상태 필터 ('' = 전체) */
  const searchDisposition = ref('')

  // 6. computed

  /** 이전 페이지 이동 가능 여부 */
  const hasPrev = computed(() => currentPage.value > 0)
  /** 다음 페이지 이동 가능 여부 */
  const hasNext = computed(() => currentPage.value < totalPages.value - 1)

  /**
   * 요약 통계: 현재 조회된 records 기준이 아닌 전체 건수 표시를 위해
   * 필터 없는 상태로 별도 집계하지 않고 totalElements만 사용한다.
   */
  const summaryText = computed(() => {
    const from = totalElements.value === 0 ? 0 : currentPage.value * 20 + 1
    const to   = Math.min((currentPage.value + 1) * 20, totalElements.value)
    return `${from}–${to} / 총 ${totalElements.value}건`
  })

  // 7. 일반 함수

  /**
   * CDR 목록을 API에서 가져온다.
   * 필터는 현재 searchNumber, searchDisposition 상태를 사용한다.
   *
   * @param {number} page 0-based 페이지 번호 (기본값: currentPage)
   */
  async function fetchCdr(page = currentPage.value) {
    loading.value = true
    errorMessage.value = ''
    try {
      const params = new URLSearchParams({
        number: searchNumber.value.trim(),
        disposition: searchDisposition.value,
        page: String(page),
      })
      const response = await fetch(
        `${process.env.VUE_APP_API_BASE_URL}/api/cdr/list?${params}`
      )
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`)
      }
      const data = await response.json()
      if (data.success) {
        records.value       = data.data ?? []
        totalElements.value = data.totalElements ?? 0
        totalPages.value    = data.totalPages ?? 0
        currentPage.value   = data.page ?? 0
      } else {
        throw new Error('API returned success: false')
      }
    } catch (err) {
      errorMessage.value = '통화 이력을 불러오지 못했습니다.'
      console.error('[useCdr] fetchCdr error:', err)
    } finally {
      loading.value = false
    }
  }

  /** 필터를 적용하고 첫 페이지부터 다시 조회한다. */
  function applyFilter() {
    fetchCdr(0)
  }

  /** 이전 페이지로 이동한다. */
  function prevPage() {
    if (hasPrev.value) fetchCdr(currentPage.value - 1)
  }

  /** 다음 페이지로 이동한다. */
  function nextPage() {
    if (hasNext.value) fetchCdr(currentPage.value + 1)
  }

  /**
   * duration(초)를 "MM:SS" 또는 "H:MM:SS" 형식으로 변환한다.
   *
   * @param {number|null} seconds
   * @returns {string}
   */
  function formatDuration(seconds) {
    if (!seconds && seconds !== 0) return '—'
    const h = Math.floor(seconds / 3600)
    const m = Math.floor((seconds % 3600) / 60)
    const s = seconds % 60
    if (h > 0) {
      return `${h}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
    }
    return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
  }

  /**
   * calldate(ISO String 또는 LocalDateTime 배열) 를 "YYYY-MM-DD HH:mm:ss" 로 변환한다.
   * Spring Boot는 LocalDateTime을 배열 [yr, mo, day, hr, min, sec] 로 직렬화하므로 양 형식을 처리한다.
   *
   * @param {string|number[]} calldate
   * @returns {string}
   */
  function formatCalldate(calldate) {
    if (!calldate) return '—'
    let d
    if (Array.isArray(calldate)) {
      // [year, month(1-based), day, hour, minute, second, nano]
      const [yr, mo, day, hr, min, sec] = calldate
      d = new Date(yr, mo - 1, day, hr, min, sec)
    } else {
      d = new Date(calldate)
    }
    if (isNaN(d.getTime())) return String(calldate)
    const pad = (n) => String(n).padStart(2, '0')
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ` +
           `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
  }

  /**
   * disposition 값에 따라 뱃지 CSS 클래스를 반환한다.
   * App.vue 전역 스타일의 badge-* 클래스를 사용한다.
   *
   * @param {string} disposition
   * @returns {string}
   */
  function dispositionBadgeClass(disposition) {
    if (!disposition) return 'badge-offline'
    const upper = disposition.toUpperCase()
    if (upper === 'ANSWERED')  return 'badge-success'
    if (upper === 'NO ANSWER') return 'badge-error'
    if (upper === 'BUSY')      return 'badge-warning'
    if (upper === 'FAILED')    return 'badge-error'
    return 'badge-offline'
  }

  return {
    // state
    records,
    totalElements,
    totalPages,
    currentPage,
    loading,
    errorMessage,
    searchNumber,
    searchDisposition,
    // computed
    hasPrev,
    hasNext,
    summaryText,
    // functions
    fetchCdr,
    applyFilter,
    prevPage,
    nextPage,
    formatDuration,
    formatCalldate,
    dispositionBadgeClass,
  }
}
