package simple.simple_cti.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import simple.simple_cti.domain.Cdr;

import java.util.List;
import java.util.Optional;

/**
 * CDR Spring Data JPA Repository.
 * 통화 이력의 저장 및 조회를 담당한다.
 */
public interface CdrRepository extends JpaRepository<Cdr, Long> {

    /**
     * 전체 CDR을 통화 시작 시각 기준 최신순으로 조회한다.
     */
    List<Cdr> findAllByOrderByCalldateDesc();

    /**
     * Asterisk 고유 통화 ID로 CDR을 조회한다. (중복 저장 방지 용도)
     */
    Optional<Cdr> findByUniqueid(String uniqueid);
}
