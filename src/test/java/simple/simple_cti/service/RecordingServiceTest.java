package simple.simple_cti.service;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RecordingService 단위 테스트
 *
 * [A방식 Mock 전략]
 * - RecordingService: 실제 객체 — 경로 조합, 디렉터리 필터링 로직이 실제로 실행됨
 * - JSch/Session/ChannelSftp: MockedConstruction — SFTP 서버와의 경계 (유일한 외부 시스템 Mock)
 */
@ExtendWith(MockitoExtension.class)
class RecordingServiceTest {

    private RecordingService recordingService;
    private Session mockSession;
    private ChannelSftp mockChannelSftp;

    @BeforeEach
    void setUp() throws Exception {
        recordingService = new RecordingService();
        ReflectionTestUtils.setField(recordingService, "host", "localhost");
        ReflectionTestUtils.setField(recordingService, "port", 22);
        ReflectionTestUtils.setField(recordingService, "username", "test-user");
        ReflectionTestUtils.setField(recordingService, "password", "test-password");
        ReflectionTestUtils.setField(recordingService, "basePath", "/tmp/recordings/");

        mockSession = mock(Session.class);
        mockChannelSftp = mock(ChannelSftp.class);
        when(mockSession.openChannel("sftp")).thenReturn(mockChannelSftp);
    }

    private MockedConstruction<JSch> mockJsch() {
        return Mockito.mockConstruction(JSch.class, (mock, context) ->
                when(mock.getSession(anyString(), anyString(), anyInt())).thenReturn(mockSession));
    }

    // -------------------------------------------------------------------------
    // downloadFile()
    // -------------------------------------------------------------------------

    @Test
    void downloadFile_SFTP_연결_성공시_지정된_경로에서_파일을_다운로드한다() throws Exception {
        // Given
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(mockChannelSftp).get(anyString(), any(OutputStream.class));

        try (MockedConstruction<JSch> ignored = mockJsch()) {
            // When
            recordingService.downloadFile("rec-20241201-120000.wav", new ByteArrayOutputStream());

            // Then: 실제 서비스 로직이 조합한 전체 경로가 SFTP에 전달되는지 검증
            verify(mockChannelSftp).get(pathCaptor.capture(), any(OutputStream.class));
            assertEquals("/tmp/recordings/rec-20241201-120000.wav", pathCaptor.getValue());
        }
    }

    @Test
    void downloadFile_basePath가_슬래시로_끝나지_않아도_올바른_경로로_다운로드한다() throws Exception {
        // Given: trailing slash 없는 basePath 설정
        ReflectionTestUtils.setField(recordingService, "basePath", "/tmp/recordings");
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(mockChannelSftp).get(anyString(), any(OutputStream.class));

        try (MockedConstruction<JSch> ignored = mockJsch()) {
            // When
            recordingService.downloadFile("rec-20241201-120000.wav", new ByteArrayOutputStream());

            // Then: 서비스가 슬래시를 보완하여 올바른 경로를 조합하는지 검증
            verify(mockChannelSftp).get(pathCaptor.capture(), any(OutputStream.class));
            assertEquals("/tmp/recordings/rec-20241201-120000.wav", pathCaptor.getValue());
        }
    }

    @Test
    void downloadFile_SFTP_실패시_예외를_재전파한다() throws Exception {
        // Given
        doThrow(new SftpException(2, "No such file")).when(mockChannelSftp)
                .get(anyString(), any(OutputStream.class));

        try (MockedConstruction<JSch> ignored = mockJsch()) {
            // When & Then
            assertThrows(SftpException.class,
                    () -> recordingService.downloadFile("missing.wav", new ByteArrayOutputStream()));
        }
    }

    @Test
    void downloadFile_완료_후_SFTP_채널과_세션이_종료된다() throws Exception {
        // Given
        doNothing().when(mockChannelSftp).get(anyString(), any(OutputStream.class));

        try (MockedConstruction<JSch> ignored = mockJsch()) {
            // When
            recordingService.downloadFile("rec-20241201-120000.wav", new ByteArrayOutputStream());

            // Then: finally 블록에서 리소스가 반드시 해제되는지 검증
            verify(mockChannelSftp).disconnect();
            verify(mockSession).disconnect();
        }
    }

    // -------------------------------------------------------------------------
    // listRecordings()
    // -------------------------------------------------------------------------

    @Test
    void listRecordings_SFTP_조회_성공시_파일_목록을_반환한다() throws Exception {
        // Given
        Vector<ChannelSftp.LsEntry> entries = new Vector<>();
        entries.add(lsEntry("rec-20241201-120000.wav", false));
        entries.add(lsEntry("rec-20241201-130000.wav", false));
        when(mockChannelSftp.ls(anyString())).thenReturn(entries);

        try (MockedConstruction<JSch> ignored = mockJsch()) {
            // When
            List<String> result = recordingService.listRecordings();

            // Then
            assertEquals(2, result.size());
            assertTrue(result.contains("rec-20241201-120000.wav"));
            assertTrue(result.contains("rec-20241201-130000.wav"));
        }
    }

    @Test
    void listRecordings_디렉터리_항목은_목록에서_제외된다() throws Exception {
        // Given
        Vector<ChannelSftp.LsEntry> entries = new Vector<>();
        entries.add(lsEntry("rec-20241201-120000.wav", false));
        entries.add(lsEntry("subdir", true));
        when(mockChannelSftp.ls(anyString())).thenReturn(entries);

        try (MockedConstruction<JSch> ignored = mockJsch()) {
            // When
            List<String> result = recordingService.listRecordings();

            // Then
            assertEquals(1, result.size());
            assertFalse(result.contains("subdir"));
        }
    }

    @Test
    void listRecordings_SFTP_실패시_예외를_재전파한다() throws Exception {
        // Given
        doThrow(new SftpException(2, "Permission denied")).when(mockChannelSftp).ls(anyString());

        try (MockedConstruction<JSch> ignored = mockJsch()) {
            // When & Then
            assertThrows(SftpException.class, () -> recordingService.listRecordings());
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
        // 디렉터리 항목은 getFilename()이 호출되지 않으므로 stubbing하지 않는다
        if (!isDir) {
            when(entry.getFilename()).thenReturn(filename);
        }
        return entry;
    }
}
