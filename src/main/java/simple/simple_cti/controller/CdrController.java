package simple.simple_cti.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
     * 전체 통화 이력을 최신순으로 반환한다.
     *
     * @return { "success": true, "data": [...] }
     */
    @GetMapping("/list")
    public Map<String, Object> list() {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("success", true);
            response.put("data", cdrService.findAll());
        } catch (Exception e) {
            response.put("success", false);
            response.put("data", null);
        }
        return response;
    }
}
