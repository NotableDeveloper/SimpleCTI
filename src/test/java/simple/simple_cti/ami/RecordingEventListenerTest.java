package simple.simple_cti.ami;

import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.action.GetVarAction;
import org.asteriskjava.manager.action.MixMonitorAction;
import org.asteriskjava.manager.event.BridgeEnterEvent;
import org.asteriskjava.manager.event.ManagerEvent;
import org.asteriskjava.manager.response.ManagerResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

/**
 * RecordingEventListener 단위 테스트
 *
 * [A방식 Mock 전략]
 * - AmiConnectionManager: 실제 객체 사용 (Spring context 없으므로 @PostConstruct 미실행)
 * - ManagerConnection: Mock (Asterisk 서버와의 경계 — 유일한 Mock 대상)
 * - BridgeEnterEvent: Mock (Asterisk에서 수신되는 외부 입력)
 *
 * [비동기 처리]
 * - onManagerEvent()는 내부적으로 ExecutorService를 통해 비동기 처리한다.
 * - 호출이 발생해야 하는 케이스: Mockito timeout()으로 최대 1초 대기 후 검증
 * - 호출이 발생하면 안 되는 케이스: Mockito after()로 충분한 시간 대기 후 검증
 */
@ExtendWith(MockitoExtension.class)
class RecordingEventListenerTest {

    @Mock
    private ManagerConnection managerConnection;

    private AmiConnectionManager amiConnectionManager;
    private RecordingEventListener listener;

    @BeforeEach
    void setUp() {
        amiConnectionManager = new AmiConnectionManager(null);
        ReflectionTestUtils.setField(amiConnectionManager, "managerConnection", managerConnection);

        listener = new RecordingEventListener(amiConnectionManager);
        ReflectionTestUtils.setField(listener, "agentAccount", "test-account");
    }

    @AfterEach
    void tearDown() {
        listener.cleanup();
    }

    // -------------------------------------------------------------------------
    // init()
    // -------------------------------------------------------------------------

    @Test
    void init_연결이_수립된_경우_이벤트_리스너로_등록된다() {
        // When
        listener.init();

        // Then
        verify(managerConnection).addEventListener(listener);
    }

    @Test
    void init_연결이_없는_경우_이벤트_리스너_등록을_건너뛴다() {
        // Given
        ReflectionTestUtils.setField(amiConnectionManager, "managerConnection", null);

        // When
        listener.init();

        // Then
        verify(managerConnection, never()).addEventListener(any());
    }

    // -------------------------------------------------------------------------
    // onManagerEvent()
    // -------------------------------------------------------------------------

    @Test
    void onManagerEvent_에이전트_채널의_BridgeEnterEvent이고_녹음_활성화_시_MixMonitorAction이_전송된다() throws Exception {
        // Given
        ManagerResponse varResponse = mock(ManagerResponse.class);
        when(varResponse.getAttribute("Value")).thenReturn("true");
        when(managerConnection.sendAction(isA(GetVarAction.class))).thenReturn(varResponse);
        when(managerConnection.sendAction(isA(MixMonitorAction.class))).thenReturn(new ManagerResponse());

        // When
        listener.onManagerEvent(bridgeEnterEvent("SIP/test-account-1234"));

        // Then: 비동기 실행 완료를 최대 1초 대기
        verify(managerConnection, timeout(1000)).sendAction(isA(MixMonitorAction.class));
    }

    @Test
    void onManagerEvent_에이전트_채널의_BridgeEnterEvent이고_녹음_비활성화_시_MixMonitorAction이_전송되지_않는다() throws Exception {
        // Given
        ManagerResponse varResponse = mock(ManagerResponse.class);
        when(varResponse.getAttribute("Value")).thenReturn("false");
        when(managerConnection.sendAction(isA(GetVarAction.class))).thenReturn(varResponse);

        // When
        listener.onManagerEvent(bridgeEnterEvent("SIP/test-account-1234"));

        // Then: GetVarAction 호출 완료를 먼저 대기한 후 MixMonitorAction은 전송되지 않았는지 검증
        verify(managerConnection, timeout(1000)).sendAction(isA(GetVarAction.class));
        verify(managerConnection, never()).sendAction(isA(MixMonitorAction.class));
    }

    @Test
    void onManagerEvent_에이전트_채널이_아닌_BridgeEnterEvent는_무시된다() throws Exception {
        // When
        listener.onManagerEvent(bridgeEnterEvent("SIP/other-account-5678"));

        // Then: 비동기 작업 자체가 제출되지 않으므로 일정 시간 후 미호출 검증
        verify(managerConnection, after(200).never()).sendAction(any());
    }

    @Test
    void onManagerEvent_BridgeEnterEvent가_아닌_이벤트는_무시된다() throws Exception {
        // When
        listener.onManagerEvent(mock(ManagerEvent.class));

        // Then
        verify(managerConnection, after(200).never()).sendAction(any());
    }

    @Test
    void onManagerEvent_G_RECORD_CALL_응답이_null이면_녹음하지_않는다() throws Exception {
        // Given
        when(managerConnection.sendAction(isA(GetVarAction.class))).thenReturn(null);

        // When
        listener.onManagerEvent(bridgeEnterEvent("SIP/test-account-1234"));

        // Then: GetVarAction 호출 완료를 먼저 대기한 후 MixMonitorAction은 전송되지 않았는지 검증
        verify(managerConnection, timeout(1000)).sendAction(isA(GetVarAction.class));
        verify(managerConnection, never()).sendAction(isA(MixMonitorAction.class));
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private BridgeEnterEvent bridgeEnterEvent(String channel) {
        BridgeEnterEvent event = mock(BridgeEnterEvent.class);
        when(event.getChannel()).thenReturn(channel);
        return event;
    }
}
