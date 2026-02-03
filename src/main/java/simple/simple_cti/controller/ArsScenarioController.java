package simple.simple_cti.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import simple.simple_cti.dto.ArsScenarioDto;
import simple.simple_cti.service.ArsScenarioService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ars/scenario")
public class ArsScenarioController {

    private static final Logger logger = LoggerFactory.getLogger(ArsScenarioController.class);

    private final ArsScenarioService arsScenarioService;

    public ArsScenarioController(ArsScenarioService arsScenarioService) {
        this.arsScenarioService = arsScenarioService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createScenario(@RequestBody ArsScenarioDto scenarioDto) {
        logger.info("Received request to save scenario: {}", scenarioDto.getScenarioId());
        
        arsScenarioService.saveScenario(scenarioDto);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Scenario saved successfully");
        response.put("scenarioId", scenarioDto.getScenarioId());
        
        return ResponseEntity.ok(response);
    }
}