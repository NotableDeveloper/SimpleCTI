package simple.simple_cti.agi;

import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import simple.simple_cti.dto.ArsNodeDto;
import simple.simple_cti.dto.ArsScenarioDto;
import simple.simple_cti.service.ArsScenarioService;

import java.util.Map;

@Component
public class ArsAgiScript extends BaseAgiScript {

    private static final Logger logger = LoggerFactory.getLogger(ArsAgiScript.class);

    private final ArsScenarioService arsScenarioService;

    public ArsAgiScript(ArsScenarioService arsScenarioService) {
        this.arsScenarioService = arsScenarioService;
    }

    @Override
    public void service(AgiRequest request, AgiChannel channel) throws AgiException {
        String scenarioId = getVariable("ARS_SCENARIO_ID");
        if (scenarioId == null || scenarioId.isEmpty()) {
            verbose("No scenario ID provided. Exiting ARS.", 1);
            logger.warn("ARS request received without scenario ID. Caller: {}", request.getCallerIdNumber());
            return;
        }

        verbose("Starting ARS Scenario: " + scenarioId, 1);
        logger.info("Starting ARS Scenario: {} for Caller: {}", scenarioId, request.getCallerIdNumber());

        try {
            ArsScenarioDto scenario = arsScenarioService.loadScenario(scenarioId);
            Map<String, ArsNodeDto> nodes = scenario.getNodes();
            
            if (nodes == null || !nodes.containsKey("start_node")) {
                verbose("Invalid scenario: " + scenarioId + " (Missing start_node)", 1);
                logger.error("Invalid scenario: {} (Missing start_node)", scenarioId);
                return;
            }

            String currentNodeId = "start_node";
            
            while (currentNodeId != null && nodes.containsKey(currentNodeId)) {
                ArsNodeDto node = nodes.get(currentNodeId);
                verbose("Executing Node: " + currentNodeId + " (" + node.getType() + ")", 2);
                logger.debug("Executing Node: {} Type: {}", currentNodeId, node.getType());

                if ("PLAY".equalsIgnoreCase(node.getType())) {
                    currentNodeId = executePlayNode(node);
                } else if ("MENU".equalsIgnoreCase(node.getType())) {
                    currentNodeId = executeMenuNode(node);
                } else if ("TRANSFER".equalsIgnoreCase(node.getType())) {
                    executeTransferNode(node);
                    currentNodeId = null;
                } else {
                    verbose("Unknown node type: " + node.getType(), 1);
                    logger.warn("Unknown node type encountered: {}", node.getType());
                    currentNodeId = null;
                }
            }
            
            verbose("ARS Scenario finished.", 1);
            logger.info("ARS Scenario finished: {}", scenarioId);

        } catch (Exception e) {
            verbose("Error executing ARS scenario: " + e.getMessage(), 1);
            logger.error("Error executing ARS scenario: {}", scenarioId, e);
        }
    }

    private String executePlayNode(ArsNodeDto node) throws AgiException {
        if (node.getAudio() != null) {
            streamFile(removeExtension(node.getAudio()));
        }
        return node.getNext();
    }

    private String executeMenuNode(ArsNodeDto node) throws AgiException {
        String audio = removeExtension(node.getAudio());
        long timeout = node.getTimeout() != null ? (long) node.getTimeout() * 1000 : 5000L;
        int maxAttempts = node.getMaxAttempts() != null ? node.getMaxAttempts() : 3;
        
        for (int i = 0; i < maxAttempts; i++) {
            String input = getData(audio, timeout, 1);
            
            if (input != null && !input.isEmpty()) {
                if (node.getRoutes() != null && node.getRoutes().containsKey(input)) {
                    return node.getRoutes().get(input);
                }
                
                if (i == maxAttempts - 1 && node.getInvalid() != null) {
                    return node.getInvalid();
                }
            } else {
                if (i == maxAttempts - 1 && node.getNoInput() != null) {
                    return node.getNoInput();
                }
            }
        }
        
        return null;
    }

    private void executeTransferNode(ArsNodeDto node) throws AgiException {
        if (node.getDestination() != null) {
            exec("Dial", node.getDestination());
        }
    }
    
    private String removeExtension(String filename) {
        if (filename == null) return null;
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            return filename.substring(0, lastDot);
        }
        return filename;
    }
}