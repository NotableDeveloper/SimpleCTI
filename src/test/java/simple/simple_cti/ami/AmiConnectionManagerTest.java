package simple.simple_cti.ami;

import org.asteriskjava.AsteriskVersion;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AmiConnectionManager 단위 테스트
 *
 * [A방식 Mock 전략]
 * - AmiConnectionManager: 실제 객체 — 내부 로직(null 체크, 상태 위임 등)이 실제로 실행됨
 * - ManagerConnection: Mock (Asterisk 서버와의 경계 — 유일한 Mock 대상)
 *
 * [참고]
 * - connect()는 ManagerConnectionFactory를 내부 생성하고 실패 시 System.exit()을 호출하므로 테스트하지 않는다.
 * - disconnect(), getConnectionState(), getAsteriskVersion()의 null 분기 및 위임 동작만 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class AmiConnectionManagerTest {

    @Mock
    private ManagerConnection managerConnection;

    private AmiConnectionManager amiConnectionManager;

    @BeforeEach
    void setUp() {
        amiConnectionManager = new AmiConnectionManager(null);
        ReflectionTestUtils.setField(amiConnectionManager, "host", "asterisk-host");
        ReflectionTestUtils.setField(amiConnectionManager, "username", "admin");
    }

    // -------------------------------------------------------------------------
    // getConnectionState()
    // -------------------------------------------------------------------------

    @Test
    void getConnectionState_연결이_없으면_DISCONNECTED를_반환한다() {
        // managerConnection 필드가 null인 초기 상태
        assertEquals(ManagerConnectionState.DISCONNECTED, amiConnectionManager.getConnectionState());
    }

    @Test
    void getConnectionState_연결이_수립된_경우_ManagerConnection의_상태를_반환한다() {
        // Given
        ReflectionTestUtils.setField(amiConnectionManager, "managerConnection", managerConnection);
        when(managerConnection.getState()).thenReturn(ManagerConnectionState.CONNECTED);

        // When & Then
        assertEquals(ManagerConnectionState.CONNECTED, amiConnectionManager.getConnectionState());
    }

    // -------------------------------------------------------------------------
    // getAsteriskVersion()
    // -------------------------------------------------------------------------

    @Test
    void getAsteriskVersion_연결이_없으면_NA를_반환한다() {
        assertEquals("N/A", amiConnectionManager.getAsteriskVersion());
    }

    @Test
    void getAsteriskVersion_연결된_경우_ManagerConnection의_버전_문자열을_반환한다() {
        // Given
        ReflectionTestUtils.setField(amiConnectionManager, "managerConnection", managerConnection);
        when(managerConnection.getVersion()).thenReturn(AsteriskVersion.ASTERISK_18);

        // When & Then
        assertEquals(AsteriskVersion.ASTERISK_18.toString(), amiConnectionManager.getAsteriskVersion());
    }

    // -------------------------------------------------------------------------
    // disconnect()
    // -------------------------------------------------------------------------

    @Test
    void disconnect_연결이_없으면_logoff를_호출하지_않는다() {
        // managerConnection 필드가 null인 초기 상태 — 예외 없이 완료되어야 함
        assertDoesNotThrow(() -> amiConnectionManager.disconnect());
        verify(managerConnection, never()).logoff();
    }

    @Test
    void disconnect_연결이_CONNECTED_상태이면_logoff를_호출한다() {
        // Given
        ReflectionTestUtils.setField(amiConnectionManager, "managerConnection", managerConnection);
        when(managerConnection.getState()).thenReturn(ManagerConnectionState.CONNECTED);

        // When
        amiConnectionManager.disconnect();

        // Then
        verify(managerConnection).logoff();
    }

    @Test
    void disconnect_연결이_이미_DISCONNECTED_상태이면_logoff를_호출하지_않는다() {
        // Given
        ReflectionTestUtils.setField(amiConnectionManager, "managerConnection", managerConnection);
        when(managerConnection.getState()).thenReturn(ManagerConnectionState.DISCONNECTED);

        // When
        amiConnectionManager.disconnect();

        // Then
        verify(managerConnection, never()).logoff();
    }

    // -------------------------------------------------------------------------
    // getHost() / getUsername()
    // -------------------------------------------------------------------------

    @Test
    void getHost_설정된_호스트를_반환한다() {
        assertEquals("asterisk-host", amiConnectionManager.getHost());
    }

    @Test
    void getUsername_설정된_사용자명을_반환한다() {
        assertEquals("admin", amiConnectionManager.getUsername());
    }
}
