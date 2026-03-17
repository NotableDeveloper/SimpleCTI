package simple.simple_cti.ami;

import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.event.CdrEvent;
import org.asteriskjava.manager.event.HangupEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import simple.simple_cti.domain.Cdr;
import simple.simple_cti.service.CdrService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * CdrEventListener 단위 테스트
 *
 * [A방식 Mock 전략]
 * - CdrEventListener: 실제 객체 — CdrEvent 감지 및 Cdr 변환 로직이 실제로 실행됨
 * - AmiConnectionManager: Mockito.mock() — @PostConstruct 리스너 등록 검증 및 ManagerConnection 제어
 * - ManagerConnection: Mockito.mock() — AMI 경계
 * - CdrService: Mockito.mock() — DB 저장 경계
 *
 * 비동기(executorService.submit) 처리 검증: Mockito timeout(1000)으로 최대 1초 대기
 */
@ExtendWith(MockitoExtension.class)
class CdrEventListenerTest {

    private AmiConnectionManager amiConnectionManager;
    private CdrService cdrService;
    private ManagerConnection managerConnection;
    private CdrEventListener cdrEventListener;

    @BeforeEach
    void setUp() {
        amiConnectionManager = mock(AmiConnectionManager.class);
        cdrService = mock(CdrService.class);
        managerConnection = mock(ManagerConnection.class);
        cdrEventListener = new CdrEventListener(amiConnectionManager, cdrService);
    }

    // -------------------------------------------------------------------------
    // init()
    // -------------------------------------------------------------------------

    @Test
    void init_AMI가_연결된_상태이면_리스너를_등록한다() {
        // Given
        when(amiConnectionManager.getManagerConnection()).thenReturn(managerConnection);

        // When
        cdrEventListener.init();

        // Then
        verify(managerConnection).addEventListener(cdrEventListener);
    }

    @Test
    void init_AMI_연결이_null이면_리스너를_등록하지_않는다() {
        // Given
        when(amiConnectionManager.getManagerConnection()).thenReturn(null);

        // When & Then: 예외 없이 완료되어야 함
        assertDoesNotThrow(() -> cdrEventListener.init());
        verify(managerConnection, never()).addEventListener(any());
    }

    // -------------------------------------------------------------------------
    // onManagerEvent()
    // -------------------------------------------------------------------------

    @Test
    void onManagerEvent_CdrEvent_수신시_cdrService_save를_비동기로_호출한다() {
        // Given
        CdrEvent event = buildCdrEvent("1234567890.10", "01012345678", "01098765432");

        // When
        cdrEventListener.onManagerEvent(event);

        // Then: 비동기 실행 완료를 최대 1초 대기
        verify(cdrService, timeout(1000)).save(any(Cdr.class));
    }

    @Test
    void onManagerEvent_CdrEvent_수신시_src와_dst가_올바르게_매핑된다() {
        // Given
        CdrEvent event = buildCdrEvent("1234567890.20", "01011111111", "01022222222");
        ArgumentCaptor<Cdr> cdrCaptor = ArgumentCaptor.forClass(Cdr.class);

        // When
        cdrEventListener.onManagerEvent(event);

        // Then: 비동기 실행 후 캡처된 Cdr의 필드 검증
        verify(cdrService, timeout(1000)).save(cdrCaptor.capture());
        Cdr saved = cdrCaptor.getValue();
        assertEquals("01011111111", saved.getSrc());
        assertEquals("01022222222", saved.getDst());
        assertEquals("1234567890.20", saved.getUniqueid());
    }

    @Test
    void onManagerEvent_CdrEvent_수신시_disposition이_올바르게_매핑된다() {
        // Given
        CdrEvent event = buildCdrEvent("1234567890.30", "src", "dst");
        event.setDisposition(CdrEvent.DISPOSITION_ANSWERED);
        ArgumentCaptor<Cdr> cdrCaptor = ArgumentCaptor.forClass(Cdr.class);

        // When
        cdrEventListener.onManagerEvent(event);

        // Then
        verify(cdrService, timeout(1000)).save(cdrCaptor.capture());
        assertEquals(CdrEvent.DISPOSITION_ANSWERED, cdrCaptor.getValue().getDisposition());
    }

    @Test
    void onManagerEvent_CdrEvent가_아닌_이벤트는_무시한다() {
        // Given: HangupEvent는 CdrEventListener가 처리하지 않음
        HangupEvent hangupEvent = new HangupEvent(this);

        // When
        cdrEventListener.onManagerEvent(hangupEvent);

        // Then: cdrService.save() 미호출
        verify(cdrService, never()).save(any());
    }

    @Test
    void onManagerEvent_src가_null이면_빈_문자열로_대체한다() {
        // Given
        CdrEvent event = buildCdrEvent("1234567890.40", null, "01099999999");
        ArgumentCaptor<Cdr> cdrCaptor = ArgumentCaptor.forClass(Cdr.class);

        // When
        cdrEventListener.onManagerEvent(event);

        // Then
        verify(cdrService, timeout(1000)).save(cdrCaptor.capture());
        assertEquals("", cdrCaptor.getValue().getSrc());
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private CdrEvent buildCdrEvent(String uniqueId, String src, String dst) {
        CdrEvent event = new CdrEvent(this);
        event.setUniqueId(uniqueId);
        event.setSrc(src);
        event.setDestination(dst);
        event.setDuration(60);
        event.setBillableSeconds(45);
        event.setDisposition(CdrEvent.DISPOSITION_ANSWERED);
        return event;
    }
}
