package simple.simple_cti.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import simple.simple_cti.domain.Cdr;

import java.util.Optional;

/**
 * CDR Spring Data JPA Repository.
 * 통화 이력의 저장 및 조회를 담당한다.
 */
public interface CdrRepository extends JpaRepository<Cdr, Long> {

    /**
     * Asterisk 고유 통화 ID로 CDR을 조회한다. (중복 저장 방지 용도)
     */
    Optional<Cdr> findByUniqueid(String uniqueid);

    /**
     * 번호(src 또는 dst)와 상태(disposition)로 필터링하여 최신순 페이지 조회한다.
     *
     * @param number     발신·수신번호 검색어 (빈 문자열이면 전체 일치)
     * @param disposition 상태 필터 (빈 문자열이면 전체 일치)
     * @param pageable   페이지 정보
     * @return 필터링된 CDR 페이지
     */
    @Query("SELECT c FROM Cdr c " +
           "WHERE (LOWER(c.src) LIKE LOWER(CONCAT('%', :number, '%')) " +
           "    OR LOWER(c.dst) LIKE LOWER(CONCAT('%', :number, '%'))) " +
           "AND (:disposition = '' OR LOWER(c.disposition) = LOWER(:disposition)) " +
           "ORDER BY c.calldate DESC")
    Page<Cdr> findByFilter(
            @Param("number") String number,
            @Param("disposition") String disposition,
            Pageable pageable
    );
}
