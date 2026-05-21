package simple.simple_cti.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import simple.simple_cti.domain.ArsNode;

import java.util.List;

public interface ArsNodeRepository extends JpaRepository<ArsNode, Long> {
    List<ArsNode> findByScenarioId(Long scenarioId);
}
