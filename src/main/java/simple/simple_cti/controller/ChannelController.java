package simple.simple_cti.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import simple.simple_cti.service.ChannelMonitorService;

@RestController
@RequestMapping("/api")
@CrossOrigin("*") // 프론트엔드 직접 연결 허용
public class ChannelController {

    private static final Logger logger = LoggerFactory.getLogger(ChannelController.class);
    private final ChannelMonitorService monitorService;

    public ChannelController(ChannelMonitorService monitorService) {
        this.monitorService = monitorService;
    }

    /**
     * 실시간 채널 이벤트를 위한 SSE 구독 엔드포인트.
     */
    @GetMapping(value = "/channel-events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        logger.info("[ChannelController] New SSE subscription requested for channel monitoring.");
        return monitorService.subscribe();
    }
}
