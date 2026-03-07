package simple.simple_cti.controller;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import simple.simple_cti.service.RecordingService;

import java.io.OutputStream;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * RecordingController 슬라이스 테스트
 *
 * [A방식 Mock 전략]
 * - RecordingService: 실제 객체 (@Import) — 경로 조합 등 서비스 로직이 실제로 실행됨
 * - JSch/Session/ChannelSftp: MockedConstruction — SFTP 서버와의 경계 (유일한 외부 시스템 Mock)
 */
@WebMvcTest(RecordingController.class)
@Import(RecordingService.class)
class RecordingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private Session mockSession;
    private ChannelSftp mockChannelSftp;

    @BeforeEach
    void setUp() throws Exception {
        mockSession = mock(Session.class);
        mockChannelSftp = mock(ChannelSftp.class);
        when(mockSession.openChannel("sftp")).thenReturn(mockChannelSftp);
    }

    private MockedConstruction<JSch> mockJsch() {
        return Mockito.mockConstruction(JSch.class, (mock, context) ->
                when(mock.getSession(anyString(), anyString(), anyInt())).thenReturn(mockSession));
    }

    // -------------------------------------------------------------------------
    // GET /api/recordings/list
    // -------------------------------------------------------------------------

    @Test
    void listRecordings_SFTP_조회_성공시_파일명_목록을_반환한다() throws Exception {
        // Given
        Vector<ChannelSftp.LsEntry> entries = new Vector<>();
        entries.add(lsEntry("rec-20241201-120000.wav", false));
        entries.add(lsEntry("rec-20241201-130000.wav", false));
        when(mockChannelSftp.ls(anyString())).thenReturn(entries);

        try (MockedConstruction<JSch> ignored = mockJsch()) {
            // When & Then
            mockMvc.perform(get("/api/recordings/list"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0]").value("rec-20241201-120000.wav"))
                    .andExpect(jsonPath("$[1]").value("rec-20241201-130000.wav"));
        }
    }

    @Test
    void listRecordings_SFTP_연결_실패시_RuntimeException이_발생한다() throws Exception {
        // Given
        doThrow(new RuntimeException("Connection refused")).when(mockSession).connect();

        try (MockedConstruction<JSch> ignored = mockJsch()) {
            // When & Then
            // @WebMvcTest 환경에서는 에러 페이지 렌더링이 없어 MockMvc가 예외를 직접 전파한다.
            // 실제 운영 환경에서는 Spring Boot의 에러 처리로 인해 500이 반환된다.
            Exception thrown = assertThrows(Exception.class,
                    () -> mockMvc.perform(get("/api/recordings/list")));
            Throwable cause = thrown.getCause() != null ? thrown.getCause() : thrown;
            assertInstanceOf(RuntimeException.class, cause);
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/recordings/{filename}
    // -------------------------------------------------------------------------

    @Test
    void downloadRecording_SFTP_다운로드_성공시_wav_헤더와_함께_200을_반환한다() throws Exception {
        // Given: sftpChannel.get()은 void 메서드이므로 기본 mock(아무것도 안 함)으로 충분
        doNothing().when(mockChannelSftp).get(anyString(), any(OutputStream.class));

        try (MockedConstruction<JSch> ignored = mockJsch()) {
            // When & Then
            mockMvc.perform(get("/api/recordings/rec-20241201-120000.wav"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", "audio/wav"))
                    .andExpect(header().string("Content-Disposition",
                            "attachment; filename=\"rec-20241201-120000.wav\""));
        }
    }

    @Test
    void downloadRecording_SFTP_다운로드_실패시_500과_에러_메시지를_반환한다() throws Exception {
        // Given
        doThrow(new SftpException(2, "No such file")).when(mockChannelSftp)
                .get(anyString(), any(OutputStream.class));

        try (MockedConstruction<JSch> ignored = mockJsch()) {
            // When & Then
            mockMvc.perform(get("/api/recordings/rec-20241201-120000.wav"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Error downloading file")));
        }
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private ChannelSftp.LsEntry lsEntry(String filename, boolean isDir) {
        ChannelSftp.LsEntry entry = mock(ChannelSftp.LsEntry.class);
        SftpATTRS attrs = mock(SftpATTRS.class);
        when(attrs.isDir()).thenReturn(isDir);
        when(entry.getAttrs()).thenReturn(attrs);
        when(entry.getFilename()).thenReturn(filename);
        return entry;
    }
}
