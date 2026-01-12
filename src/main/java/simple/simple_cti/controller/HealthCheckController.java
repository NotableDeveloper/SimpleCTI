package simple.simple_cti.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import simple.simple_cti.ami.AmiConnectionManager;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthCheckController {

    private final AmiConnectionManager amiConnectionManager;

    public HealthCheckController(AmiConnectionManager amiConnectionManager) {
        this.amiConnectionManager = amiConnectionManager;
    }

    @GetMapping
    public Map<String, Object> checkAppHealth() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", System.currentTimeMillis());
        return status;
    }

    @GetMapping("/asterisk")
    public Map<String, Object> checkAsteriskHealth() {
        Map<String, Object> status = new HashMap<>();
        status.put("host", amiConnectionManager.getHost());
        status.put("username", amiConnectionManager.getUsername());
        status.put("connectionState", amiConnectionManager.getConnectionState());
        status.put("asteriskVersion", amiConnectionManager.getAsteriskVersion());
        return status;
    }
}
