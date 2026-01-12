package simple.simple_cti.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import simple.simple_cti.service.RecordingService;

import java.io.IOException;

@RestController
@RequestMapping("/api/recordings")
public class RecordingController {

    private final RecordingService recordingService;

    public RecordingController(RecordingService recordingService) {
        this.recordingService = recordingService;
    }

    @GetMapping("/{filename}")
    public void downloadRecording(@PathVariable String filename, HttpServletResponse response) {
        try {
            // Set content type and attachment header
            response.setContentType("audio/wav");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
            
            recordingService.downloadFile(filename, response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                response.getWriter().write("Error downloading file: " + e.getMessage());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    @GetMapping("/list")
    public java.util.List<String> listRecordings() {
        try {
            return recordingService.listRecordings();
        } catch (Exception e) {
            throw new RuntimeException("Failed to list recordings", e);
        }
    }
}
