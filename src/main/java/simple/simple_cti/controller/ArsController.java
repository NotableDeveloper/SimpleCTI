package simple.simple_cti.controller;

import org.springframework.web.bind.annotation.*;
import simple.simple_cti.service.ArsScenarioService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ARS(자동응답시스템) 시나리오 편집 REST 컨트롤러.
 * 시나리오 및 노드 CRUD 기능을 제공한다.
 */
@RestController
@RequestMapping("/api/ars")
public class ArsController {

    private final ArsScenarioService arsScenarioService;

    public ArsController(ArsScenarioService arsScenarioService) {
        this.arsScenarioService = arsScenarioService;
    }

    @GetMapping("/scenarios")
    public Map<String, Object> listScenarios() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> data = arsScenarioService.getScenarios();
            response.put("success", true);
            response.put("data", data);
        } catch (Exception e) {
            response.put("success", false);
            response.put("data", null);
        }
        return response;
    }

    @PostMapping("/scenarios")
    public Map<String, Object> createScenario(
            @RequestParam(required = false, defaultValue = "") String name,
            @RequestParam(required = false, defaultValue = "") String description
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> data = arsScenarioService.createScenario(name, description);
            response.put("success", true);
            response.put("data", data);
        } catch (Exception e) {
            response.put("success", false);
            response.put("data", null);
        }
        return response;
    }

    @GetMapping("/scenarios/{id}")
    public Map<String, Object> getScenario(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> data = arsScenarioService.getScenario(id);
            response.put("success", true);
            response.put("data", data);
        } catch (Exception e) {
            response.put("success", false);
            response.put("data", null);
        }
        return response;
    }

    @PutMapping("/scenarios/{id}")
    public Map<String, Object> updateScenario(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "") String name,
            @RequestParam(required = false, defaultValue = "") String description
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> data = arsScenarioService.updateScenario(id, name, description);
            response.put("success", true);
            response.put("data", data);
        } catch (Exception e) {
            response.put("success", false);
            response.put("data", null);
        }
        return response;
    }

    @DeleteMapping("/scenarios/{id}")
    public Map<String, Object> deleteScenario(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            arsScenarioService.deleteScenario(id);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
        }
        return response;
    }

    @PostMapping("/scenarios/{scenarioId}/nodes")
    public Map<String, Object> addNode(
            @PathVariable Long scenarioId,
            @RequestParam(required = false, defaultValue = "GREETING") String nodeType,
            @RequestParam(required = false, defaultValue = "") String label,
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false, defaultValue = "") String digit
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> data = arsScenarioService.addNode(scenarioId, nodeType, label, parentId, digit);
            response.put("success", true);
            response.put("data", data);
        } catch (Exception e) {
            response.put("success", false);
            response.put("data", null);
        }
        return response;
    }

    @PostMapping("/scenarios/{id}/activate")
    public Map<String, Object> activateScenario(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            arsScenarioService.activateScenario(id);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
        }
        return response;
    }

    @PutMapping("/nodes/{nodeId}")
    public Map<String, Object> updateNode(
            @PathVariable Long nodeId,
            @RequestParam(required = false, defaultValue = "") String label,
            @RequestParam(required = false, defaultValue = "") String audioFile,
            @RequestParam(required = false, defaultValue = "") String transferTarget,
            @RequestParam(required = false, defaultValue = "") String queueName
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> fields = new HashMap<>();
            if (!label.isEmpty()) fields.put("label", label);
            if (!audioFile.isEmpty()) fields.put("audioFile", audioFile);
            if (!transferTarget.isEmpty()) fields.put("transferTarget", transferTarget);
            if (!queueName.isEmpty()) fields.put("queueName", queueName);

            Map<String, Object> data = arsScenarioService.updateNode(nodeId, fields);
            response.put("success", true);
            response.put("data", data);
        } catch (Exception e) {
            response.put("success", false);
            response.put("data", null);
        }
        return response;
    }

    @DeleteMapping("/nodes/{nodeId}")
    public Map<String, Object> deleteNode(@PathVariable Long nodeId) {
        Map<String, Object> response = new HashMap<>();
        try {
            arsScenarioService.deleteNode(nodeId);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
        }
        return response;
    }

    @PutMapping("/nodes/{nodeId}/key-mappings")
    public Map<String, Object> setKeyMappings(
            @PathVariable Long nodeId,
            @RequestBody List<Map<String, Object>> mappings
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            arsScenarioService.setKeyMappings(nodeId, mappings);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
        }
        return response;
    }
}
