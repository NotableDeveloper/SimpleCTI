package simple.simple_cti.controller;

import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionState;
import org.asteriskjava.manager.response.ManagerResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import simple.simple_cti.ami.AmiConnectionManager;
import simple.simple_cti.ami.OutboundCallCommand;
import simple.simple_cti.ami.RecordingEventListener;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

/**
 * DialController 슬라이스 테스트
 *
 * [A방식 Mock 전략]
 * - OutboundCallCommand: 실제 객체 (@Import) — 채널 조합 및 Action 구성 로직이 실제로 실행됨
 * - AmiConnectionManager: @MockitoBean — @PostConstruct(connect()) 실행 방지 및 ManagerConnection 제어
 * - ManagerConnection: Mockito.mock() — Asterisk 서버와의 경계 (유일한 외부 시스템 Mock)
 */
@WebMvcTest(DialController.class)
@Import(OutboundCallCommand.class)
class DialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AmiConnectionManager amiConnectionManager;

    @MockitoBean
    private RecordingEventListener recordingEventListener;

    private ManagerConnection managerConnection;

    @BeforeEach
    void setUp() throws Exception {
        managerConnection = Mockito.mock(ManagerConnection.class);
    }

    @Test
    void originate_AMI가_연결된_상태에서_발신하면_success_true를_반환한다() throws Exception {
        // Given
        when(amiConnectionManager.getManagerConnection()).thenReturn(managerConnection);
        when(managerConnection.getState()).thenReturn(ManagerConnectionState.CONNECTED);
        when(managerConnection.sendAction(any())).thenReturn(new ManagerResponse());

        // When & Then
        mockMvc.perform(post("/originate")
                        .param("targetNumber", "01012345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void originate_recordingEnabled_true로_발신하면_success_true를_반환한다() throws Exception {
        // Given
        when(amiConnectionManager.getManagerConnection()).thenReturn(managerConnection);
        when(managerConnection.getState()).thenReturn(ManagerConnectionState.CONNECTED);
        when(managerConnection.sendAction(any())).thenReturn(new ManagerResponse());

        // When & Then
        mockMvc.perform(post("/originate")
                        .param("targetNumber", "01012345678")
                        .param("recordingEnabled", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void originate_AMI가_미연결_상태이면_success_false를_반환한다() throws Exception {
        // Given: getManagerConnection()이 null 반환 → OutboundCallCommand가 IOException을 던짐
        when(amiConnectionManager.getManagerConnection()).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/originate")
                        .param("targetNumber", "01012345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void originate_targetNumber_파라미터가_없으면_400을_반환한다() throws Exception {
        // When & Then
        mockMvc.perform(post("/originate"))
                .andExpect(status().isBadRequest());
    }
}
