package simple.simple_cti.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import simple.simple_cti.domain.ArsKeyMapping;

import java.util.List;

public interface ArsKeyMappingRepository extends JpaRepository<ArsKeyMapping, Long> {
    List<ArsKeyMapping> findByNodeId(Long nodeId);
    void deleteByNodeId(Long nodeId);
}
