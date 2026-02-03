package simple.simple_cti.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import simple.simple_cti.dto.ArsScenarioDto;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ArsScenarioService {

    private static final Logger logger = LoggerFactory.getLogger(ArsScenarioService.class);

    private final String STORAGE_DIR = "ars_scenarios";
    private final ObjectMapper objectMapper;

    public ArsScenarioService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        initStorage();
    }

    private void initStorage() {
        try {
            Path path = Paths.get(STORAGE_DIR);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                logger.info("Created storage directory: {}", path.toAbsolutePath());
            }
        } catch (IOException e) {
            logger.error("Could not initialize storage location", e);
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    public void saveScenario(ArsScenarioDto scenarioDto) {
        if (scenarioDto.getScenarioId() == null || scenarioDto.getScenarioId().isEmpty()) {
            logger.warn("Attempted to save scenario without ID");
            throw new IllegalArgumentException("Scenario ID is required");
        }

        try {
            File file = new File(STORAGE_DIR, scenarioDto.getScenarioId() + ".json");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, scenarioDto);
            logger.info("Saved ARS scenario: {} to {}", scenarioDto.getScenarioId(), file.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Failed to save scenario file for ID: {}", scenarioDto.getScenarioId(), e);
            throw new RuntimeException("Failed to save scenario file", e);
        }
    }

    public ArsScenarioDto loadScenario(String scenarioId) {
        if (scenarioId == null || scenarioId.isEmpty()) {
            logger.warn("Attempted to load scenario with empty ID");
            throw new IllegalArgumentException("Scenario ID is required");
        }

        try {
            File file = new File(STORAGE_DIR, scenarioId + ".json");
            if (!file.exists()) {
                logger.error("Scenario file not found: {}", scenarioId);
                throw new RuntimeException("Scenario file not found: " + scenarioId);
            }
            ArsScenarioDto scenario = objectMapper.readValue(file, ArsScenarioDto.class);
            logger.info("Loaded ARS scenario: {}", scenarioId);
            return scenario;
        } catch (IOException e) {
            logger.error("Failed to load scenario file for ID: {}", scenarioId, e);
            throw new RuntimeException("Failed to load scenario file", e);
        }
    }
}