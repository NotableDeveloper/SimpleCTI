package simple.simple_cti.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import simple.simple_cti.domain.ArsKeyMapping;
import simple.simple_cti.domain.ArsNode;
import simple.simple_cti.domain.ArsScenario;
import simple.simple_cti.repository.ArsKeyMappingRepository;
import simple.simple_cti.repository.ArsNodeRepository;
import simple.simple_cti.repository.ArsScenarioRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ARS 시나리오 관리 서비스.
 * 시나리오와 노드의 CRUD, 트리 조합, 키매핑 관리를 담당한다.
 */
@Slf4j
@Service
public class ArsScenarioService {

    private final ArsScenarioRepository scenarioRepository;
    private final ArsNodeRepository nodeRepository;
    private final ArsKeyMappingRepository keyMappingRepository;

    public ArsScenarioService(ArsScenarioRepository scenarioRepository,
                             ArsNodeRepository nodeRepository,
                             ArsKeyMappingRepository keyMappingRepository) {
        this.scenarioRepository = scenarioRepository;
        this.nodeRepository = nodeRepository;
        this.keyMappingRepository = keyMappingRepository;
    }

    public List<Map<String, Object>> getScenarios() {
        return scenarioRepository.findAll().stream()
                .map(this::entityToMap)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getScenario(Long id) {
        ArsScenario scenario = scenarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Scenario not found: " + id));

        Map<String, Object> result = entityToMap(scenario);
        List<ArsNode> nodes = nodeRepository.findByScenarioId(id);

        if (scenario.getRootNodeId() != null) {
            List<Map<String, Object>> tree = buildNodeTree(scenario.getRootNodeId(), nodes);
            result.put("nodes", tree);
        } else {
            result.put("nodes", new ArrayList<>());
        }

        return result;
    }

    @Transactional
    public Map<String, Object> createScenario(String name, String description) {
        try {
            ArsScenario scenario = new ArsScenario();
            scenario.setName(name);
            scenario.setDescription(description);
            scenario.setIsActive(false);
            scenario.setCreatedAt(LocalDateTime.now());

            ArsScenario saved = scenarioRepository.save(scenario);

            // 루트 노드 자동 생성 (GREETING)
            ArsNode rootNode = new ArsNode();
            rootNode.setScenarioId(saved.getId());
            rootNode.setParentId(null);
            rootNode.setNodeType("GREETING");
            rootNode.setLabel("루트 음성 안내");
            rootNode.setCreatedAt(LocalDateTime.now());
            ArsNode savedRoot = nodeRepository.save(rootNode);

            // 루트 노드 ID 업데이트
            saved.setRootNodeId(savedRoot.getId());
            scenarioRepository.save(saved);

            log.info("Scenario created: id={}, name={}", saved.getId(), name);
            return entityToMap(saved);
        } catch (Exception e) {
            log.error("Failed to create scenario: {}", name, e);
            throw new RuntimeException("Failed to create scenario", e);
        }
    }

    @Transactional
    public Map<String, Object> updateScenario(Long id, String name, String description) {
        try {
            ArsScenario scenario = scenarioRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Scenario not found: " + id));

            scenario.setName(name);
            scenario.setDescription(description);
            ArsScenario updated = scenarioRepository.save(scenario);

            log.info("Scenario updated: id={}, name={}", id, name);
            return entityToMap(updated);
        } catch (Exception e) {
            log.error("Failed to update scenario: {}", id, e);
            throw new RuntimeException("Failed to update scenario", e);
        }
    }

    @Transactional
    public void deleteScenario(Long id) {
        try {
            List<ArsNode> nodes = nodeRepository.findByScenarioId(id);
            for (ArsNode node : nodes) {
                deleteNodeCascade(node.getId());
            }
            scenarioRepository.deleteById(id);
            log.info("Scenario deleted: id={}", id);
        } catch (Exception e) {
            log.error("Failed to delete scenario: {}", id, e);
            throw new RuntimeException("Failed to delete scenario", e);
        }
    }

    @Transactional
    public Map<String, Object> addNode(Long scenarioId, String nodeType, String label,
                                       Long parentId, String digit) {
        try {
            ArsScenario scenario = scenarioRepository.findById(scenarioId)
                    .orElseThrow(() -> new IllegalArgumentException("Scenario not found: " + scenarioId));

            ArsNode node = new ArsNode();
            node.setScenarioId(scenarioId);
            node.setParentId(parentId);
            node.setNodeType(nodeType);
            node.setLabel(label);
            node.setCreatedAt(LocalDateTime.now());
            ArsNode savedNode = nodeRepository.save(node);

            // 부모 노드와 키 매핑 자동 생성
            if (parentId != null && digit != null && !digit.isEmpty()) {
                ArsKeyMapping keyMapping = new ArsKeyMapping();
                keyMapping.setNodeId(parentId);
                keyMapping.setDigit(digit);
                keyMapping.setChildNodeId(savedNode.getId());
                keyMappingRepository.save(keyMapping);
            }

            log.info("Node added: scenario={}, nodeType={}, id={}", scenarioId, nodeType, savedNode.getId());
            return nodeToMap(savedNode);
        } catch (Exception e) {
            log.error("Failed to add node: scenarioId={}, nodeType={}", scenarioId, nodeType, e);
            throw new RuntimeException("Failed to add node", e);
        }
    }

    @Transactional
    public Map<String, Object> updateNode(Long nodeId, Map<String, Object> fields) {
        try {
            ArsNode node = nodeRepository.findById(nodeId)
                    .orElseThrow(() -> new IllegalArgumentException("Node not found: " + nodeId));

            if (fields.containsKey("label")) {
                node.setLabel((String) fields.get("label"));
            }
            if (fields.containsKey("audioFile")) {
                node.setAudioFile((String) fields.get("audioFile"));
            }
            if (fields.containsKey("transferTarget")) {
                node.setTransferTarget((String) fields.get("transferTarget"));
            }
            if (fields.containsKey("queueName")) {
                node.setQueueName((String) fields.get("queueName"));
            }

            ArsNode updated = nodeRepository.save(node);
            log.info("Node updated: id={}, label={}", nodeId, node.getLabel());
            return nodeToMap(updated);
        } catch (Exception e) {
            log.error("Failed to update node: {}", nodeId, e);
            throw new RuntimeException("Failed to update node", e);
        }
    }

    @Transactional
    public void deleteNode(Long nodeId) {
        try {
            deleteNodeCascade(nodeId);
            log.info("Node deleted: id={}", nodeId);
        } catch (Exception e) {
            log.error("Failed to delete node: {}", nodeId, e);
            throw new RuntimeException("Failed to delete node", e);
        }
    }

    @Transactional
    public void setKeyMappings(Long nodeId, List<Map<String, Object>> mappings) {
        try {
            keyMappingRepository.deleteByNodeId(nodeId);

            for (Map<String, Object> mapping : mappings) {
                String digit = (String) mapping.get("digit");
                Long childNodeId = ((Number) mapping.get("childNodeId")).longValue();

                ArsKeyMapping keyMapping = new ArsKeyMapping();
                keyMapping.setNodeId(nodeId);
                keyMapping.setDigit(digit);
                keyMapping.setChildNodeId(childNodeId);
                keyMappingRepository.save(keyMapping);
            }

            log.debug("Key mappings set for node {}: {} mappings", nodeId, mappings.size());
        } catch (Exception e) {
            log.error("Failed to set key mappings for node: {}", nodeId, e);
            throw new RuntimeException("Failed to set key mappings", e);
        }
    }

    @Transactional
    public void activateScenario(Long id) {
        try {
            // 모든 시나리오 비활성화
            scenarioRepository.findAll().forEach(s -> {
                s.setIsActive(false);
                scenarioRepository.save(s);
            });

            // 해당 시나리오 활성화
            ArsScenario scenario = scenarioRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Scenario not found: " + id));
            scenario.setIsActive(true);
            scenarioRepository.save(scenario);

            log.info("Scenario activated: id={}", id);
        } catch (Exception e) {
            log.error("Failed to activate scenario: {}", id, e);
            throw new RuntimeException("Failed to activate scenario", e);
        }
    }

    // Helper methods

    private void deleteNodeCascade(Long nodeId) {
        ArsNode node = nodeRepository.findById(nodeId).orElse(null);
        if (node == null) return;

        // 자식 노드 재귀 삭제
        List<ArsNode> children = nodeRepository.findByScenarioId(node.getScenarioId())
                .stream()
                .filter(n -> nodeId.equals(n.getParentId()))
                .collect(Collectors.toList());

        for (ArsNode child : children) {
            deleteNodeCascade(child.getId());
        }

        // 키매핑 삭제
        keyMappingRepository.deleteByNodeId(nodeId);

        // 노드 삭제
        nodeRepository.deleteById(nodeId);
    }

    private List<Map<String, Object>> buildNodeTree(Long parentId, List<ArsNode> allNodes) {
        List<Map<String, Object>> result = new ArrayList<>();

        List<ArsNode> children = allNodes.stream()
                .filter(n -> parentId.equals(n.getParentId()))
                .collect(Collectors.toList());

        for (ArsNode child : children) {
            Map<String, Object> nodeMap = nodeToMap(child);

            // 키매핑 조회
            List<ArsKeyMapping> keyMappings = keyMappingRepository.findByNodeId(child.getId());
            List<Map<String, Object>> mappings = keyMappings.stream()
                    .map(km -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("digit", km.getDigit());
                        m.put("childNodeId", km.getChildNodeId());
                        return m;
                    })
                    .collect(Collectors.toList());
            nodeMap.put("keyMappings", mappings);

            // 자식 노드 재귀
            List<Map<String, Object>> childrenTree = buildNodeTree(child.getId(), allNodes);
            nodeMap.put("children", childrenTree);

            result.add(nodeMap);
        }

        return result;
    }

    private Map<String, Object> entityToMap(ArsScenario scenario) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", scenario.getId());
        map.put("name", scenario.getName());
        map.put("description", scenario.getDescription());
        map.put("rootNodeId", scenario.getRootNodeId());
        map.put("isActive", scenario.getIsActive());
        map.put("createdAt", scenario.getCreatedAt());
        return map;
    }

    private Map<String, Object> nodeToMap(ArsNode node) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", node.getId());
        map.put("scenarioId", node.getScenarioId());
        map.put("parentId", node.getParentId());
        map.put("nodeType", node.getNodeType());
        map.put("label", node.getLabel());
        map.put("audioFile", node.getAudioFile());
        map.put("transferTarget", node.getTransferTarget());
        map.put("queueName", node.getQueueName());
        map.put("createdAt", node.getCreatedAt());
        return map;
    }
}
