package simple.simple_cti.controller;

import org.asteriskjava.manager.ManagerConnectionState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import simple.simple_cti.ami.AmiConnectionManager;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * HealthCheckController 슬라이스 테스트
 *
 * [A방식 Mock 전략]
 * - AmiConnectionManager: @MockitoBean — @PostConstruct(connect()) 실행 방지 및 상태 제어
 */
@WebMvcTest(HealthCheckController.class)
class HealthCheckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AmiConnectionManager amiConnectionManager;

    // -------------------------------------------------------------------------
    // GET /api/health
    // -------------------------------------------------------------------------

    @Test
    void checkAppHealth_항상_status_UP과_timestamp를_반환한다() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.timestamp").isNumber());
    }

    // -------------------------------------------------------------------------
    // GET /api/health/asterisk
    // -------------------------------------------------------------------------

    @Test
    void checkAsteriskHealth_연결된_경우_호스트_사용자_상태_버전을_반환한다() throws Exception {
        // Given
        when(amiConnectionManager.getHost()).thenReturn("asterisk-host");
        when(amiConnectionManager.getUsername()).thenReturn("admin");
        when(amiConnectionManager.getConnectionState()).thenReturn(ManagerConnectionState.CONNECTED);
        when(amiConnectionManager.getAsteriskVersion()).thenReturn("1.6");

        // When & Then
        mockMvc.perform(get("/api/health/asterisk"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.host").value("asterisk-host"))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.connectionState").value("CONNECTED"))
                .andExpect(jsonPath("$.asteriskVersion").value("1.6"));
    }

    @Test
    void checkAsteriskHealth_연결이_없는_경우_DISCONNECTED_상태와_NA_버전을_반환한다() throws Exception {
        // Given
        when(amiConnectionManager.getHost()).thenReturn("asterisk-host");
        when(amiConnectionManager.getUsername()).thenReturn("admin");
        when(amiConnectionManager.getConnectionState()).thenReturn(ManagerConnectionState.DISCONNECTED);
        when(amiConnectionManager.getAsteriskVersion()).thenReturn("N/A");

        // When & Then
        mockMvc.perform(get("/api/health/asterisk"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connectionState").value("DISCONNECTED"))
                .andExpect(jsonPath("$.asteriskVersion").value("N/A"));
    }
}