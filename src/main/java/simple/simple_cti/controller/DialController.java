package simple.simple_cti.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import simple.simple_cti.ami.OutboundCallCommand;
import java.util.HashMap;
import java.util.Map;

@RestController
public class DialController {

    private final OutboundCallCommand outboundCallCommand;

    public DialController(OutboundCallCommand outboundCallCommand) {
        this.outboundCallCommand = outboundCallCommand;

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
}
