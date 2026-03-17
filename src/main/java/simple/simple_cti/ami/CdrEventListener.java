package simple.simple_cti.ami;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.asteriskjava.manager.ManagerEventListener;
import org.asteriskjava.manager.event.CdrEvent;
import org.asteriskjava.manager.event.ManagerEvent;
import org.springframework.stereotype.Component;
import simple.simple_cti.domain.Cdr;
import simple.simple_cti.service.CdrService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Asterisk AMI CdrEvent를 수신하여 통화 이력을 MariaDB에 저장하는 컴포넌트.
 * RecordingEventListener와 역할을 분리하여 CDR 저장만을 전담한다.
 */
@Slf4j
@Component
public class CdrEventListener implements ManagerEventListener {

    private final AmiConnectionManager amiConnectionManager;
    private final CdrService cdrService;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public CdrEventListener(AmiConnectionManager amiConnectionManager, CdrService cdrService) {
        this.amiConnectionManager = amiConnectionManager;
        this.cdrService = cdrService;
    }

    @PostConstruct
    public void init() {
        if (amiConnectionManager.getManagerConnection() != null) {
            amiConnectionManager.getManagerConnection().addEventListener(this);
            log.info("CdrEventListener registered to AmiConnectionManager.");
        } else {
            log.warn("AmiConnectionManager connection is null. CdrEventListener NOT registered.");
        }
    }

    @PreDestroy
    public void cleanup() {
        executorService.shutdown();
    }

    @Override
    public void onManagerEvent(ManagerEvent event) {
        if (event instanceof CdrEvent) {
            CdrEvent cdrEvent = (CdrEvent) event;
            log.debug("CdrEvent received for uniqueid {}", cdrEvent.getUniqueId());
            executorService.submit(() -> handleCdrEvent(cdrEvent));
        }
    }

    /**
     * CdrEvent를 Cdr Entity로 변환하여 저장한다.
     * 저장 실패가 통화에 영향을 주지 않도록 예외를 CdrService 레이어에서 처리한다.
     *
     * @param event Asterisk AMI CdrEvent
     */
    private void handleCdrEvent(CdrEvent event) {
        log.info("Handling CdrEvent for uniqueid {}, src={}, dst={}", event.getUniqueId(), event.getSrc(), event.getDestination());

        Cdr cdr = new Cdr();
        cdr.setAccountcode(event.getAccountCode());
        cdr.setSrc(event.getSrc() != null ? event.getSrc() : "");
        cdr.setDst(event.getDestination() != null ? event.getDestination() : "");
        cdr.setDcontext(event.getDestinationContext());
        cdr.setClid(event.getCallerId());
        cdr.setChannel(event.getChannel());
        cdr.setDstchannel(event.getDestinationChannel());
        cdr.setLastapp(event.getLastApplication());
        cdr.setLastdata(event.getLastData());
        cdr.setCalldate(toLocalDateTime(event.getStartTimeAsDate()));
        cdr.setAnswer(toLocalDateTime(event.getAnswerTimeAsDate()));
        cdr.setEnd(toLocalDateTime(event.getEndTimeAsDate()));
        cdr.setDuration(event.getDuration() != null ? event.getDuration() : 0);
        cdr.setBillsec(event.getBillableSeconds() != null ? event.getBillableSeconds() : 0);
        cdr.setDisposition(event.getDisposition());
        cdr.setAmaflags(event.getAmaFlags());
        cdr.setUniqueid(event.getUniqueId());
        cdr.setUserfield(event.getUserField());
        cdr.setRecordingFile(event.getRecordfile());
        cdr.setCreatedAt(LocalDateTime.now());

        cdrService.save(cdr);
    }

    /**
     * java.util.Date를 LocalDateTime으로 변환한다.
     * null이면 null을 반환한다.
     *
     * @param date 변환할 Date 객체
     * @returns LocalDateTime 또는 null
     */
    private LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
