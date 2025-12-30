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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboundCallCommandTest {

    @Mock
    private AmiConnectionManager amiConnectionManager;

    @Mock
    private ManagerConnection managerConnection;

    private OutboundCallCommand outboundCallCommand;

    @BeforeEach
    void setUp() {
        outboundCallCommand = new OutboundCallCommand(amiConnectionManager);
        
        // Inject properties using ReflectionTestUtils
        ReflectionTestUtils.setField(outboundCallCommand, "Channel", "SIP/");
        ReflectionTestUtils.setField(outboundCallCommand, "Gateway", "@my-gateway");
        ReflectionTestUtils.setField(outboundCallCommand, "Context", "from-internal");
        ReflectionTestUtils.setField(outboundCallCommand, "MainNumber", "1000");
        ReflectionTestUtils.setField(outboundCallCommand, "Account", "test-account");
    }

    @Test
    void executeOriginate_ShouldSendAction_WhenConnected() throws Exception {
        // Arrange
        String targetNumber = "01012345678";
        ManagerResponse expectedResponse = new ManagerResponse();
        expectedResponse.setResponse("Success");

        when(amiConnectionManager.getManagerConnection()).thenReturn(managerConnection);
        when(managerConnection.getState()).thenReturn(ManagerConnectionState.CONNECTED);
        when(managerConnection.sendAction(any(OriginateAction.class))).thenReturn(expectedResponse);

        // Act
        ManagerResponse response = outboundCallCommand.executeOriginate(targetNumber);

        // Assert
        assertNotNull(response);
        assertEquals("Success", response.getResponse());

        ArgumentCaptor<OriginateAction> captor = ArgumentCaptor.forClass(OriginateAction.class);
        verify(managerConnection).sendAction(captor.capture());

        OriginateAction sentAction = captor.getValue();
        assertEquals("SIP/test-account", sentAction.getChannel());
        assertEquals("Dial", sentAction.getApplication());
        assertEquals("SIP/01012345678@my-gateway", sentAction.getData());
        assertEquals(30000L, sentAction.getTimeout());
        assertEquals("1000", sentAction.getCallerId());
        assertEquals("test-account", sentAction.getAccount());
        assertTrue(sentAction.getAsync());
    }

    @Test
    void executeOriginate_ShouldThrowException_WhenConnectionIsNull() {
        // Arrange
        when(amiConnectionManager.getManagerConnection()).thenReturn(null);

        // Act & Assert
        IOException exception = assertThrows(IOException.class, () -> 
            outboundCallCommand.executeOriginate("01012345678")
        );
        assertEquals("AMI connection is not established or connected.", exception.getMessage());
    }

    @Test
    void executeOriginate_ShouldThrowException_WhenConnectionIsNotConnected() {
        // Arrange
        when(amiConnectionManager.getManagerConnection()).thenReturn(managerConnection);
        when(managerConnection.getState()).thenReturn(ManagerConnectionState.DISCONNECTED);

        // Act & Assert
        IOException exception = assertThrows(IOException.class, () -> 
            outboundCallCommand.executeOriginate("01012345678")
        );
        assertEquals("AMI connection is not established or connected.", exception.getMessage());
    }
}
