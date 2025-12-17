package simple.simple_cti.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import simple.simple_cti.ami.OutboundCallCommand;
import org.asteriskjava.manager.response.ManagerResponse;

import java.util.HashMap;
import java.util.Map;

@RestController
public class DialController {

    private final OutboundCallCommand outboundCallCommand;

    public DialController(OutboundCallCommand outboundCallCommand) {
        this.outboundCallCommand = outboundCallCommand;
    }

    @PostMapping("/originate")
    @ResponseBody
    public Map<String, Object> originate(@RequestParam String targetNumber) {
        Map<String, Object> response = new HashMap<>();
        try {
            ManagerResponse managerResponse = outboundCallCommand.executeOriginate(targetNumber);

            response.put("success", true);
            response.put("message", "Call initiated successfully!");
            response.put("amiResponse", managerResponse.getResponse());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to initiate call: " + e.getMessage());
        }
        return response;
    }
}
