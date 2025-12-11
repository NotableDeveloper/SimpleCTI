package simple.simple_cti.ami;


import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionState;
import org.asteriskjava.manager.action.OriginateAction;
import org.asteriskjava.manager.response.ManagerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class OriginateCommandExecutor {
    private static final Logger logger = LoggerFactory.getLogger(OriginateCommandExecutor.class);

    private final AmiConnectionManager amiConnectionManager;

    @Value("${outbound.channel}")
    private String Channel;

    @Value("${outbound.gateway}")
    private String Gateway;

    @Value("${outbound.context}")
    private String Context;

    @Value("${outbound.main_number}")
    private String MainNumber;

    @Value("${outbound.account}")
    private String Account;

    public OriginateCommandExecutor(AmiConnectionManager amiConnectionManager) {
        this.amiConnectionManager = amiConnectionManager;
    }

    public ManagerResponse executeOriginate(String targetNumber) throws Exception {
        ManagerConnection connection = amiConnectionManager.getManagerConnection();

        if (connection == null || connection.getState() != ManagerConnectionState.CONNECTED) {
            logger.error("AMI connection is not established or connected.");
            throw new IOException("AMI connection is not established or connected.");
        }

        String channel = Channel + targetNumber + Gateway;

        OriginateAction originateAction = new OriginateAction();
        originateAction.setChannel(channel);
        originateAction.setContext(Context);
        originateAction.setExten(targetNumber);
        originateAction.setPriority(1);
        originateAction.setTimeout(30000L);
        originateAction.setCallerId(MainNumber);
        originateAction.setAccount(Account);

        logger.info("Sending OriginateAction to Asterisk: {}", originateAction);
        ManagerResponse response = connection.sendAction(originateAction);
        logger.info("Received response for OriginateAction: {}", response);

        return response;
    }
}
