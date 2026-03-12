// [Composition API]
import { ref, computed } from 'vue';

/**
 * 녹음 파일 목록 조회, 선택, 다운로드, 파일명 파싱을 담당하는 composable입니다.
 * @returns {Object} 녹음 파일 관련 상태 및 조작 함수
 */
export function useRecordings() {
  // ==================== 상태 ====================

  const recordings          = ref([]);  // 서버에서 가져온 파일명 배열 (내림차순 정렬)
  const selectedRecordings  = ref([]);  // 체크박스로 선택된 파일명 배열
  const isRefreshing        = ref(false); // 새로고침 버튼 스피닝 애니메이션 진행 여부
  const lastUpdated         = ref('');  // 마지막 목록 조회 시간 (HH:MM:SS)

  // ==================== Computed ====================

  /**
   * 전체 선택 체크박스의 checked 상태를 제어하는 writable computed입니다.
   * get: 모든 파일이 선택되었을 때 true
   * set: true이면 전체 선택, false이면 전체 해제
   */
  const isAllSelected = computed({
    get: () => recordings.value.length > 0 && selectedRecordings.value.length === recordings.value.length,
    set: (value) => {
      selectedRecordings.value = value ? [...recordings.value] : [];
    }
  });

  /**
   * 일부만 선택된 상태(indeterminate)를 반환합니다.
   * @returns {boolean}
   */
  const isIndeterminate = computed(() =>
    selectedRecordings.value.length > 0 && selectedRecordings.value.length < recordings.value.length
  );

  /**
   * recordings 배열을 파싱하여 { filename, datetime } 객체 배열로 변환합니다.
   * @returns {{ filename: string, datetime: { date: string, time: string } }[]}
   */
  const parsedRecordings = computed(() =>
    recordings.value.map(filename => ({
      filename,
      datetime: parseFileDatetime(filename)
    }))
  );

  // ==================== 함수 ====================

  /**
   * 서버에서 녹음 파일 목록을 가져옵니다.
   * GET /api/recordings/list 호출 후 내림차순 정렬하여 recordings에 저장합니다.
   */
  async function loadRecordings() {
    // 이미 로딩 중이면 중복 호출 방지
    if (isRefreshing.value) return;

    isRefreshing.value = true;

    try {
      const response = await fetch('/api/recordings/list');
      if (!response.ok) {
        throw new Error(`서버 응답 오류: ${response.status}`);
      }
      const files = await response.json();
      // 내림차순 정렬: 최신 파일이 상단에 표시
      recordings.value = files.sort().reverse();
      // 선택 상태 초기화: 목록 갱신 시 기존 선택이 유효하지 않을 수 있음
      selectedRecordings.value = [];
      // 마지막 조회 시간 갱신
      lastUpdated.value = getCurrentTimeString();
    } catch (error) {
      console.error('녹음 파일 목록 로드 오류:', error);
    } finally {
      // 애니메이션이 눈에 보이도록 최소 700ms 유지
      setTimeout(() => { isRefreshing.value = false; }, 700);
    }
  }

  /**
   * 단일 파일을 다운로드합니다.
   * 동적으로 <a> 요소를 생성하여 클릭 후 즉시 제거합니다.
   * @param {string} filename - 다운로드할 파일명
   */
  function downloadFile(filename) {
    const link = document.createElement('a');
    link.href = '/api/recordings/' + encodeURIComponent(filename);
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }

  /**
   * 선택된 녹음 파일들을 각각 개별 다운로드합니다.
   */
  function downloadSelected() {
    if (selectedRecordings.value.length === 0) return;
    selectedRecordings.value.forEach(filename => downloadFile(filename));
  }

  /**
   * 파일명에서 날짜와 시간을 파싱합니다.
   * 지원 형식: YYYYMMDD-HHmmss.wav (예: 20240315-143022.wav → 2024-03-15 / 14:30:22)
   * 형식이 맞지 않으면 date에 파일명 그대로, time은 빈 문자열을 반환합니다.
   * @param {string} filename - 파싱할 파일명
   * @returns {{ date: string, time: string }}
   */
  function parseFileDatetime(filename) {
    const base = filename.replace(/\.wav$/i, '');
    const parts = base.split('-');

    if (parts.length < 2) {
      return { date: filename, time: '' };
    }

    const datePart = parts[0];
    const timePart = parts[1];

    if (datePart.length !== 8 || timePart.length !== 6 || !/^\d+$/.test(datePart) || !/^\d+$/.test(timePart)) {
      return { date: filename, time: '' };
    }

    const date = `${datePart.slice(0, 4)}-${datePart.slice(4, 6)}-${datePart.slice(6, 8)}`;
    const time = `${timePart.slice(0, 2)}:${timePart.slice(2, 4)}:${timePart.slice(4, 6)}`;

    return { date, time };
  }

  /**
   * 현재 시각을 HH:MM:SS 형식의 문자열로 반환합니다.
   * @returns {string}
   */
  function getCurrentTimeString() {
    const now = new Date();
    const hh = String(now.getHours()).padStart(2, '0');
    const mm = String(now.getMinutes()).padStart(2, '0');
    const ss = String(now.getSeconds()).padStart(2, '0');
    return `${hh}:${mm}:${ss}`;
  }

  return {
    recordings,
    selectedRecordings,
    isRefreshing,
    lastUpdated,
    isAllSelected,
    isIndeterminate,
    parsedRecordings,
    loadRecordings,
    downloadFile,
    downloadSelected
  };
}
