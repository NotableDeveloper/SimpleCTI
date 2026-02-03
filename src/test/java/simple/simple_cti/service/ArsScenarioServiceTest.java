package simple.simple_cti.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import simple.simple_cti.dto.ArsNodeDto;
import simple.simple_cti.dto.ArsScenarioDto;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ArsScenarioServiceTest {

    private ArsScenarioService arsScenarioService;
    private ObjectMapper objectMapper;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        // 테스트를 위해 임시 디렉토리를 사용하도록 서비스를 초기화하려 했으나, 
        // 현재 서비스는 STORAGE_DIR가 하드코딩되어 있으므로 실제 로컬 디렉토리를 사용하게 됩니다.
        // (실제 구현에서는 생성자 주입 등으로 경로를 유연하게 바꾸는 것이 좋습니다.)
        arsScenarioService = new ArsScenarioService(objectMapper);
    }

    @Test
    void saveAndLoadScenarioTest() {
        // Given
        ArsScenarioDto scenario = new ArsScenarioDto();
        scenario.setScenarioId("test_ars_001");
        scenario.setName("Test ARS");

        Map<String, ArsNodeDto> nodes = new HashMap<>();
        ArsNodeDto startNode = new ArsNodeDto();
        startNode.setId("start_node");
        startNode.setType("PLAY");
        startNode.setAudio("welcome.wav");
        startNode.setNext("end");
        nodes.put("start_node", startNode);

        scenario.setNodes(nodes);

        // When: 저장
        arsScenarioService.saveScenario(scenario);

        // Then: 파일 존재 확인
        File savedFile = new File("ars_scenarios/test_ars_001.json");
        assertTrue(savedFile.exists(), "Scenario file should be created");

        // When: 로드
        ArsScenarioDto loadedScenario = arsScenarioService.loadScenario("test_ars_001");

        // Then: 내용 검증
        assertEquals("test_ars_001", loadedScenario.getScenarioId());
        assertEquals("Test ARS", loadedScenario.getName());
        assertTrue(loadedScenario.getNodes().containsKey("start_node"));
        assertEquals("welcome.wav", loadedScenario.getNodes().get("start_node").getAudio());

        // Clean up (optional for local tests)
        savedFile.delete();
    }
}
