package simple.simple_cti.domain;

import jakarta.persistence.*;

/**
 * ARS 키 매핑 JPA Entity.
 * 노드에서 DTMF 키 입력에 따른 자식 노드 분기를 정의한다.
 * 테이블: ars_key_mapping
 */
@Entity
@Table(name = "ars_key_mapping")
public class ArsKeyMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "node_id", nullable = false)
    private Long nodeId;

    @Column(name = "digit", nullable = false, length = 5)
    private String digit;

    @Column(name = "child_node_id", nullable = false)
    private Long childNodeId;

    public ArsKeyMapping() {
    }

    public Long getId() {
        return id;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public String getDigit() {
        return digit;
    }

    public void setDigit(String digit) {
        this.digit = digit;
    }

    public Long getChildNodeId() {
        return childNodeId;
    }

    public void setChildNodeId(Long childNodeId) {
        this.childNodeId = childNodeId;
    }
}
