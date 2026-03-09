package simple.simple_cti.controller;

import org.asteriskjava.manager.action.HangupAction;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import simple.simple_cti.ami.AmiConnectionManager;
import simple.simple_cti.ami.OutboundCallCommand;
import simple.simple_cti.ami.RecordingEventListener;
import java.util.HashMap;
import java.util.Map;

@RestController
public class DialController {

    private final OutboundCallCommand outboundCallCommand;
    private final AmiConnectionManager amiConnectionManager;
    private final RecordingEventListener recordingEventListener;

    public DialController(OutboundCallCommand outboundCallCommand,
                          AmiConnectionManager amiConnectionManager,
                          RecordingEventListener recordingEventListener) {
        this.outboundCallCommand = outboundCallCommand;
        this.amiConnectionManager = amiConnectionManager;
        this.recordingEventListener = recordingEventListener;
    }

    @PostMapping("/originate")
    public Map<String, Object> originate(@RequestParam String targetNumber, @RequestParam(defaultValue = "false") boolean recordingEnabled) {
        Map<String, Object> response = new HashMap<>();
        try {
            outboundCallCommand.executeOriginate(targetNumber, recordingEnabled);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
        }
        return response;
    }

    @PostMapping("/api/hangup")
    public Map<String, Object> hangup() {
        Map<String, Object> response = new HashMap<>();
        String channel = recordingEventListener.getActiveAgentChannel();
        if (channel == null) {
            response.put("success", false);
            response.put("message", "No active channel");
            return response;
        }
        try {
            HangupAction action = new HangupAction(channel);
            amiConnectionManager.getManagerConnection().sendAction(action);
            response.put("success", true);
            response.put("channel", channel);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
}
