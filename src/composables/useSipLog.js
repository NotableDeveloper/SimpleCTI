// [Composition API]
import { ref, computed, nextTick } from 'vue';

/**
 * SIP 메시지 로그 수집, 파싱, 필터링, 자동 스크롤을 담당하는 composable입니다.
 *
 * @param {import('vue').Ref<HTMLElement|null>} containerRef - 로그 터미널 컨테이너 DOM ref
 * @returns {Object} SIP 로그 상태 및 조작 함수
 */
export function useSipLog(containerRef) {
  const sipLogs      = ref([]);   // { id, timestamp, direction, summary, firstLine, raw, expanded, isNew }
  const sipLogFilter = ref('all'); // 'all' | '1xx' | '2xx' | '3xx' | '4xx' | '5xx' | 'request'
  const sipAutoScroll    = ref(true);
  const sipLogIdCounter  = ref(0);
  const sendCount = ref(0);
  const recvCount = ref(0);

  // ==================== Computed ====================

  /** sipLogFilter에 따라 필터링된 로그 배열을 반환합니다. */
  const filteredSipLogs = computed(() => {
    if (sipLogFilter.value === 'all') return sipLogs.value;
    return sipLogs.value.filter(entry => matchesSipFilter(entry, sipLogFilter.value));
  });

  // ==================== SIP 로그 처리 ====================

  /**
   * SIP.js logConnector 콜백 핸들러입니다.
   * traceSip: true 옵션으로 인해 WebSocket 송수신 메시지가 이 콜백으로 전달됩니다.
   *
   * @param {string} level - 'log' | 'debug' | 'warn' | 'error'
   * @param {string} category - SIP.js 내부 로거 카테고리
   * @param {string|undefined} label - 선택적 레이블
   * @param {string} content - 로그 내용
   */
  function handleSipLogConnector(level, category, label, content) {
    if (typeof content !== 'string') return;

    // WebSocket 송신 메시지 감지
    // transport.js _send(): "Sending WebSocket message:\n\n<RAW SIP>\n"
    if (content.includes('Sending WebSocket message:')) {
      const rawSip = extractRawSip(content, 'Sending WebSocket message:');
      if (rawSip) addSipLog('send', rawSip);
      return;
    }

    // WebSocket 수신 메시지 감지
    // transport.js onWebSocketMessage(): "Received WebSocket text message:\n\n<RAW SIP>\n"
    if (content.includes('Received WebSocket') && content.includes('message:')) {
      const rawSip = extractRawSip(content, 'message:');
      if (rawSip) addSipLog('recv', rawSip);
      return;
    }
  }

  /**
   * 로그 내용 문자열에서 raw SIP 메시지 부분만 추출합니다.
   *
   * @param {string} content - 전체 로그 내용
   * @param {string} marker - 분리 기준 마커 문자열
   * @returns {string|null} 추출된 raw SIP 메시지 또는 null
   */
  function extractRawSip(content, marker) {
    const idx = content.indexOf(marker);
    if (idx === -1) return null;
    const after = content.slice(idx + marker.length).replace(/^\n+/, '').replace(/\n+$/, '').trim();
    // CRLF Keep Alive (\r\n)는 SIP 메시지가 아니므로 제외
    if (!after || /^(\r\n|\n)+$/.test(after)) return null;
    // SIP 메시지 형식: 첫 줄이 "SIP/2.0" 또는 메서드명으로 시작해야 함
    const firstLine = after.split(/\r?\n/)[0].trim();
    if (!firstLine) return null;
    if (!firstLine.startsWith('SIP/') && !/^[A-Z]+\s+sip/.test(firstLine) && !/^[A-Z]+\s+/.test(firstLine)) {
      return null;
    }
    return after;
  }

  /**
   * 파싱된 SIP 메시지를 sipLogs에 추가합니다.
   * 자동 스크롤이 활성화된 경우 로그 컨테이너를 맨 아래로 스크롤합니다.
   *
   * @param {string} direction - 'send' | 'recv'
   * @param {string} rawSip - raw SIP 메시지 문자열
   */
  function addSipLog(direction, rawSip) {
    const parsed = parseSipMessage(rawSip);
    if (!parsed) return;

    const now = new Date();
    const ts = now.toTimeString().slice(0, 8) + '.' + String(now.getMilliseconds()).padStart(3, '0');

    const entry = {
      id: ++sipLogIdCounter.value,
      timestamp: ts,
      direction,
      summary: parsed.summary,
      firstLine: parsed.firstLine,
      raw: rawSip,
      expanded: false,
      isNew: true
    };

    sipLogs.value.push(entry);

    if (direction === 'send') {
      sendCount.value++;
    } else {
      recvCount.value++;
    }

    // 새 항목 강조 애니메이션: 0.8초 후 isNew 플래그 해제
    setTimeout(() => { entry.isNew = false; }, 800);

    // 자동 스크롤
    nextTick(() => {
      if (containerRef && containerRef.value && sipAutoScroll.value) {
        containerRef.value.scrollTop = containerRef.value.scrollHeight;
      }
    });
  }

  /**
   * raw SIP 메시지 문자열을 파싱하여 summary와 firstLine을 반환합니다.
   *
   * - 응답: "SIP/2.0 200 OK" → summary: "200 OK", firstLine: 전체 첫 줄
   * - 요청: "INVITE sip:..." → summary: "INVITE", firstLine: 전체 첫 줄
   *
   * @param {string} rawSip - raw SIP 메시지
   * @returns {{ summary: string, firstLine: string } | null}
   */
  function parseSipMessage(rawSip) {
    const lines = rawSip.split(/\r?\n/);
    const firstLine = lines[0].trim();
    if (!firstLine) return null;

    let summary;

    if (firstLine.startsWith('SIP/2.0')) {
      // 응답 메시지: "SIP/2.0 200 OK"
      const match = firstLine.match(/^SIP\/2\.0\s+(\d{3})\s*(.*)/);
      if (match) {
        summary = match[2] ? `${match[1]} ${match[2]}` : match[1];
      } else {
        summary = firstLine;
      }
    } else {
      // 요청 메시지: "INVITE sip:..."
      const match = firstLine.match(/^([A-Z]+)\s+/);
      summary = match ? match[1] : firstLine.split(' ')[0];
    }

    return { summary, firstLine };
  }

  // ==================== 필터 ====================

  /**
   * 특정 필터에 해당하는 로그 건수를 반환합니다. (필터 칩 카운트 배지용)
   * @param {string} filter - '1xx' | '2xx' | '3xx' | '4xx' | '5xx' | 'request'
   * @returns {number}
   */
  function filterCount(filter) {
    return sipLogs.value.filter(entry => matchesSipFilter(entry, filter)).length;
  }

  /**
   * 로그 항목이 주어진 필터와 일치하는지 판단합니다.
   *
   * @param {Object} entry - sipLogs 항목
   * @param {string} filter - 필터 키
   * @returns {boolean}
   */
  function matchesSipFilter(entry, filter) {
    if (filter === 'all') return true;
    if (filter === 'request') {
      // 숫자로 시작하지 않는 항목 = 요청 메시지
      return !/^\d/.test(entry.summary);
    }
    // 응답 코드 계열 필터 (1xx / 2xx / 3xx / 4xx / 5xx)
    const responseClass = getSipResponseClass(entry.summary);
    return responseClass === filter;
  }

  /**
   * summary 문자열에서 응답 코드 계열을 반환합니다.
   * @param {string} summary - "200 OK", "401 Unauthorized" 등
   * @returns {string|null} '1xx' | '2xx' | '3xx' | '4xx' | '5xx' | null
   */
  function getSipResponseClass(summary) {
    if (!summary || !/^\d/.test(summary)) return null;
    const code = parseInt(summary, 10);
    if (code >= 100 && code < 200) return '1xx';
    if (code >= 200 && code < 300) return '2xx';
    if (code >= 300 && code < 400) return '3xx';
    if (code >= 400 && code < 500) return '4xx';
    if (code >= 500 && code < 600) return '5xx';
    return null;
  }

  /**
   * SIP 메시지 summary에 따라 로그 타입 CSS 클래스를 반환합니다.
   * @param {string} summary - "INVITE", "200 OK", "401 Unauthorized" 등
   * @returns {string} CSS 클래스명
   */
  function getSipTypeClass(summary) {
    if (!summary) return '';
    const methodMap = {
      'INVITE':    'type-invite',
      'REGISTER':  'type-register',
      'ACK':       'type-ack',
      'BYE':       'type-bye',
      'CANCEL':    'type-cancel',
      'OPTIONS':   'type-options',
      'PRACK':     'type-options',
      'UPDATE':    'type-options',
      'SUBSCRIBE': 'type-options',
      'NOTIFY':    'type-options',
      'REFER':     'type-options',
      'MESSAGE':   'type-options',
      'INFO':      'type-options',
      'PUBLISH':   'type-options'
    };
    if (methodMap[summary]) return methodMap[summary];
    // 응답 코드 계열 매핑 (예: "200 OK")
    const cls = getSipResponseClass(summary);
    if (cls) return 'type-' + cls;
    return '';
  }

  // ==================== 기타 ====================

  /** 로그 항목의 상세 영역 펼침/닫힘을 토글합니다. */
  function toggleDetail(entry) {
    entry.expanded = !entry.expanded;
  }

  /** 모든 SIP 로그를 초기화합니다. */
  function clearSipLogs() {
    sipLogs.value = [];
    sendCount.value = 0;
    recvCount.value = 0;
    sipLogIdCounter.value = 0;
  }

  return {
    sipLogs,
    sipLogFilter,
    sipAutoScroll,
    sendCount,
    recvCount,
    filteredSipLogs,
    handleSipLogConnector,
    addSipLog,
    toggleDetail,
    clearSipLogs,
    filterCount,
    getSipTypeClass
  };
}
