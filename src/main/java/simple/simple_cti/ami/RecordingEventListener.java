package simple.simple_cti.ami;

import jakarta.annotation.PostConstruct;
import org.asteriskjava.manager.ManagerEventListener;
import org.asteriskjava.manager.action.MixMonitorAction;
import org.asteriskjava.manager.event.BridgeEnterEvent;
import org.asteriskjava.manager.event.ManagerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class RecordingEventListener implements ManagerEventListener {
    private static final Logger logger = LoggerFactory.getLogger(RecordingEventListener.class);
    private final AmiConnectionManager amiConnectionManager;

    @Value("${outbound.account}")
    private String agentAccount;

    public RecordingEventListener(AmiConnectionManager amiConnectionManager) {
        this.amiConnectionManager = amiConnectionManager;
    }

    @PostConstruct
    public void init() {
        if (amiConnectionManager.getManagerConnection() != null) {
            amiConnectionManager.getManagerConnection().addEventListener(this);
            logger.info("RecordingEventListener registered to AmiConnectionManager.");
        } else {
            logger.warn("AmiConnectionManager connection is null. RecordingEventListener NOT registered.");
        }
    }

    @Override
    public void onManagerEvent(ManagerEvent event) {
        if (event instanceof BridgeEnterEvent) {
            BridgeEnterEvent bridgeEvent = (BridgeEnterEvent) event;
            String channel = bridgeEvent.getChannel();
            
            // Check if the channel belongs to our agent
            // Typically channel names are like "PJSIP/100-0000001" where "100" is the account
            if (channel != null && agentAccount != null && channel.contains(agentAccount)) {
                startRecording(channel);
            }
        }
    }

    private void startRecording(String channel) {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
            // Create a filename: rec-TIMESTAMP-CHANNEL.wav
            // Sanitize channel name for filename
            String safeChannel = channel.replaceAll("[^a-zA-Z0-9.-]", "_");
            String filename = "rec-" + timestamp + "-" + safeChannel + ".wav";

            MixMonitorAction mixMonitorAction = new MixMonitorAction(channel, filename);
            logger.info("Starting recording for channel {}: {}", channel, filename);
            
            amiConnectionManager.getManagerConnection().sendAction(mixMonitorAction);
            
        } catch (Exception e) {
            logger.error("Failed to start recording for channel {}", channel, e);
        }
    }
}
