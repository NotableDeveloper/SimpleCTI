package simple.simple_cti.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import simple.simple_cti.domain.Cdr;
import simple.simple_cti.repository.CdrRepository;

import java.util.List;

/**
 * CDR(통화 이력) 저장 및 조회 비즈니스 서비스.
 * 저장 실패가 통화에 영향을 주지 않도록 예외를 전파하지 않는다.
 */
@Slf4j
@Service
public class CdrService {

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
            // uniqueid 중복 여부 사전 확인
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
     * 전체 CDR을 통화 시작 시각 기준 최신순으로 반환한다.
     *
     * @return CDR 목록 (최신순)
     */
    public List<Cdr> findAll() {
        return cdrRepository.findAllByOrderByCalldateDesc();
    }
}
