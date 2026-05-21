package simple.simple_cti.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import simple.simple_cti.domain.ArsScenario;

public interface ArsScenarioRepository extends JpaRepository<ArsScenario, Long> {
}
