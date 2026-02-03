package simple.simple_cti.dto;

import java.util.Map;

public class ArsScenarioDto {
    private String scenarioId;
    private String name;
    private Map<String, ArsNodeDto> nodes;

    public String getScenarioId() { return scenarioId; }
    public void setScenarioId(String scenarioId) { this.scenarioId = scenarioId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Map<String, ArsNodeDto> getNodes() { return nodes; }
    public void setNodes(Map<String, ArsNodeDto> nodes) { this.nodes = nodes; }
}