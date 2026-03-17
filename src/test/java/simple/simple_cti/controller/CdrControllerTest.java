package simple.simple_cti.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import simple.simple_cti.ami.CdrEventListener;
import simple.simple_cti.ami.RecordingEventListener;
import simple.simple_cti.ami.AmiConnectionManager;
import simple.simple_cti.domain.Cdr;
import simple.simple_cti.service.CdrService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CdrController 슬라이스 테스트
 *
 * [A방식 Mock 전략]
 * - CdrController: 실제 객체 (@WebMvcTest) — HTTP 요청 처리 흐름이 실제로 실행됨
 * - CdrService: @MockitoBean — DB 조회 경계
 * - AmiConnectionManager, RecordingEventListener, CdrEventListener: @MockitoBean — 슬라이스 컨텍스트 구성 필요
 */
@WebMvcTest(CdrController.class)
class CdrControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CdrService cdrService;

    @MockitoBean
    private AmiConnectionManager amiConnectionManager;

    @MockitoBean
    private RecordingEventListener recordingEventListener;

    @MockitoBean
    private CdrEventListener cdrEventListener;

    @Test
    void list_CDR이_존재하면_success_true와_목록을_반환한다() throws Exception {
        // Given
        Cdr cdr = buildCdr("1234567890.1", "01012345678", "01098765432");
        when(cdrService.findAll()).thenReturn(List.of(cdr));

        // When & Then
        mockMvc.perform(get("/api/cdr/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].uniqueid").value("1234567890.1"));
    }

    @Test
    void list_CDR이_없으면_success_true와_빈_배열을_반환한다() throws Exception {
        // Given
        when(cdrService.findAll()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/cdr/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void list_service_예외_발생시_success_false를_반환한다() throws Exception {
        // Given
        when(cdrService.findAll()).thenThrow(new RuntimeException("DB error"));

        // When & Then
        mockMvc.perform(get("/api/cdr/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private Cdr buildCdr(String uniqueid, String src, String dst) {
        Cdr cdr = new Cdr();
        cdr.setUniqueid(uniqueid);
        cdr.setSrc(src);
        cdr.setDst(dst);
        cdr.setCalldate(LocalDateTime.now());
        return cdr;
    }
}
