package simple.simple_cti.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import simple.simple_cti.domain.Cdr;
import simple.simple_cti.repository.CdrRepository;

/**
 * CDR(통화 이력) 저장 및 조회 비즈니스 서비스.
 * 저장 실패가 통화에 영향을 주지 않도록 예외를 전파하지 않는다.
 */
@Slf4j
@Service
public class CdrService {

    /** 한 페이지당 표시할 CDR 건수 */
    private static final int PAGE_SIZE = 20;

    private final CdrRepository cdrRepository;

    public CdrService(CdrRepository cdrRepository) {
        this.cdrRepository = cdrRepository;
    }

    /**
     * CDR을 MariaDB에 저장한다.
     * uniqueid 중복 시 WARN 로그 후 무시한다. 저장 실패는 예외로 전파하지 않는다.
     *
     * @param cdr 저장할 CDR 객체
     */
    public void save(Cdr cdr) {
        try {
            if (cdrRepository.findByUniqueid(cdr.getUniqueid()).isPresent()) {
                log.warn("CDR with uniqueid {} already exists. Skipping.", cdr.getUniqueid());
                return;
            }
            cdrRepository.save(cdr);
            log.info("CDR saved for uniqueid {}, src={}, dst={}", cdr.getUniqueid(), cdr.getSrc(), cdr.getDst());
        } catch (Exception e) {
            log.error("Failed to save CDR for uniqueid {}", cdr.getUniqueid(), e);
        }
    }

    /**
     * 번호(src 또는 dst)와 상태(disposition)로 필터링하여 최신순 페이지를 반환한다.
     * number 또는 disposition이 null이면 빈 문자열로 처리하여 전체 조회한다.
     *
     * @param number      발신·수신번호 검색어 (null 또는 빈 문자열이면 전체)
     * @param disposition 상태 필터 (null 또는 빈 문자열이면 전체)
     * @param page        0-based 페이지 번호
     * @return 필터링된 CDR 페이지
     */
    public Page<Cdr> findByFilter(String number, String disposition, int page) {
        String safeNumber      = (number      != null) ? number.trim()      : "";
        String safeDisposition = (disposition != null) ? disposition.trim() : "";
        log.debug("CDR filter query: number='{}', disposition='{}', page={}", safeNumber, safeDisposition, page);
        return cdrRepository.findByFilter(safeNumber, safeDisposition, PageRequest.of(page, PAGE_SIZE));
    }
}
