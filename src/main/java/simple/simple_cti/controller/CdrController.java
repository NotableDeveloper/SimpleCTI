package simple.simple_cti.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import simple.simple_cti.domain.Cdr;
import simple.simple_cti.service.CdrService;

import java.util.HashMap;
import java.util.Map;

/**
 * CDR(통화 이력) 조회 REST 컨트롤러.
 * 비즈니스 로직은 CdrService에 위임한다.
 */
@RestController
@RequestMapping("/api/cdr")
public class CdrController {

    private final CdrService cdrService;

    public CdrController(CdrService cdrService) {
        this.cdrService = cdrService;
    }

    /**
     * 번호(src 또는 dst)와 상태(disposition)로 필터링하여 통화 이력을 페이지 단위로 반환한다.
     *
     * @param number      발신·수신번호 검색어 (생략 시 전체)
     * @param disposition 상태 필터 — ANSWERED, NO ANSWER, BUSY, FAILED (생략 시 전체)
     * @param page        0-based 페이지 번호 (기본값: 0)
     * @return { "success": true, "data": [...], "totalElements": N, "totalPages": N, "page": N }
     */
    @GetMapping("/list")
    public Map<String, Object> list(
            @RequestParam(required = false, defaultValue = "") String number,
            @RequestParam(required = false, defaultValue = "") String disposition,
            @RequestParam(required = false, defaultValue = "0") int page
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Page<Cdr> result = cdrService.findByFilter(number, disposition, page);
            response.put("success", true);
            response.put("data", result.getContent());
            response.put("totalElements", result.getTotalElements());
            response.put("totalPages", result.getTotalPages());
            response.put("page", result.getNumber());
        } catch (Exception e) {
            response.put("success", false);
            response.put("data", null);
        }
        return response;
    }
}
