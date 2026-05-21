package simple.simple_cti.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ARS 노드 JPA Entity.
 * 통화 흐름 중 한 단계를 나타낸다.
 * 테이블: ars_node
 */
@Entity
@Table(name = "ars_node")
public class ArsNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scenario_id", nullable = false)
    private Long scenarioId;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "node_type", nullable = false, length = 20)
    private String nodeType;

    @Column(name = "label", length = 200)
    private String label;

    @Column(name = "audio_file", length = 200)
    private String audioFile;

    @Column(name = "transfer_target", length = 50)
    private String transferTarget;

    @Column(name = "queue_name", length = 100)
    private String queueName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public ArsNode() {
    }

    public Long getId() {
        return id;
    }

    public Long getScenarioId() {
        return scenarioId;
    }

    public void setScenarioId(Long scenarioId) {
        this.scenarioId = scenarioId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getAudioFile() {
        return audioFile;
    }

    public void setAudioFile(String audioFile) {
        this.audioFile = audioFile;
    }

    public String getTransferTarget() {
        return transferTarget;
    }

    public void setTransferTarget(String transferTarget) {
        this.transferTarget = transferTarget;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
