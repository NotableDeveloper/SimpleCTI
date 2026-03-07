package simple.simple_cti.ami;

import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionState;
import org.asteriskjava.manager.action.OriginateAction;
import org.asteriskjava.manager.response.ManagerResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * OutboundCallCommand 단위 테스트
 *
 * [A방식 Mock 전략]
 * - AmiConnectionManager: 실제 객체 사용 (Spring context 없으므로 @PostConstruct 미실행)
 * - ManagerConnection: Mock (Asterisk 서버와의 경계 — 유일한 Mock 대상)
 */
@ExtendWith(MockitoExtension.class)
class OutboundCallCommandTest {

    @Mock
    private ManagerConnection managerConnection;

    private AmiConnectionManager amiConnectionManager;
    private OutboundCallCommand outboundCallCommand;

    @BeforeEach
    void setUp() {
        amiConnectionManager = new AmiConnectionManager(null);
        ReflectionTestUtils.setField(amiConnectionManager, "managerConnection", managerConnection);

        outboundCallCommand = new OutboundCallCommand(amiConnectionManager);
        ReflectionTestUtils.setField(outboundCallCommand, "Channel", "SIP/");
        ReflectionTestUtils.setField(outboundCallCommand, "Gateway", "@my-gateway");
        ReflectionTestUtils.setField(outboundCallCommand, "Context", "from-internal");
        ReflectionTestUtils.setField(outboundCallCommand, "MainNumber", "1000");
        ReflectionTestUtils.setField(outboundCallCommand, "Account", "test-account");
    }

    @Test
    void executeOriginate_연결된_상태에서_발신하면_성공_응답을_반환한다() throws Exception {
        // Given
        ManagerResponse expectedResponse = new ManagerResponse();
        expectedResponse.setResponse("Success");
        when(managerConnection.getState()).thenReturn(ManagerConnectionState.CONNECTED);
        when(managerConnection.sendAction(any(OriginateAction.class))).thenReturn(expectedResponse);

        // When
        ManagerResponse response = outboundCallCommand.executeOriginate("01012345678", false);

        // Then
        assertNotNull(response);
        assertEquals("Success", response.getResponse());
    }

    @Test
    void executeOriginate_녹음_활성화_시_Asterisk에_녹음_변수가_전달된다() throws Exception {
        // Given
        when(managerConnection.getState()).thenReturn(ManagerConnectionState.CONNECTED);
        when(managerConnection.sendAction(any(OriginateAction.class))).thenReturn(new ManagerResponse());
        ArgumentCaptor<OriginateAction> captor = ArgumentCaptor.forClass(OriginateAction.class);

        // When
        outboundCallCommand.executeOriginate("01012345678", true);

        // Then
        verify(managerConnection).sendAction(captor.capture());
        assertEquals("true", captor.getValue().getVariables().get("G_RECORD_CALL"));
    }

    @Test
    void executeOriginate_연결이_없으면_예외가_발생한다() {
        // Given
        ReflectionTestUtils.setField(amiConnectionManager, "managerConnection", null);

        // When & Then
        IOException exception = assertThrows(IOException.class,
                () -> outboundCallCommand.executeOriginate("01012345678", false));
        assertEquals("AMI connection is not established or connected.", exception.getMessage());
    }

    @Test
    void executeOriginate_연결이_끊긴_상태면_예외가_발생한다() {
        // Given
        when(managerConnection.getState()).thenReturn(ManagerConnectionState.DISCONNECTED);

        // When & Then
        IOException exception = assertThrows(IOException.class,
                () -> outboundCallCommand.executeOriginate("01012345678", false));
        assertEquals("AMI connection is not established or connected.", exception.getMessage());
    }
}
