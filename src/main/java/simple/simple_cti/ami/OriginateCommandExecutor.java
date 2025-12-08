package simple.simple_cti.ami;

import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionState;
import org.asteriskjava.manager.action.OriginateAction;
import org.asteriskjava.manager.response.ManagerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class OriginateCommandExecutor {

    private static final Logger logger = LoggerFactory.getLogger(OriginateCommandExecutor.class);

    private final AmiConnectionManager amiConnectionManager;

    public OriginateCommandExecutor(AmiConnectionManager amiConnectionManager) {
        this.amiConnectionManager = amiConnectionManager;
    }

    /**
     * Executes an Asterisk Originate command.
     *
     * @param channel The channel to call (e.g., SIP/myphone).
     * @param context The context to connect to in the dialplan.
     * @param exten The extension to connect to in the dialplan.
     * @param priority The priority to connect to in the dialplan.
     * @param timeout The timeout for the originate call in milliseconds.
     * @param callerId The caller ID to set for the call.
     * @param variables A map of variables to set for the call.
     * @return The ManagerResponse from the originate command.
     * @throws IOException if the connection to Asterisk is lost.
     * @throws Exception for other general errors during command execution.
     */
    public ManagerResponse executeOriginate(
            String channel,
            String context,
            String exten,
            Integer priority,
            Long timeout,
            String callerId,
            Map<String, String> variables
    ) throws Exception {
        ManagerConnection connection = amiConnectionManager.getManagerConnection();

        if (connection == null || connection.getState() != ManagerConnectionState.CONNECTED) {
            logger.error("AMI connection is not established or connected.");
            throw new IOException("AMI connection is not established or connected.");
        }

        OriginateAction originateAction = new OriginateAction();
        originateAction.setChannel(channel);
        originateAction.setContext(context);
        originateAction.setExten(exten);
        if (priority != null) {
            originateAction.setPriority(priority);
        }
        if (timeout != null) {
            originateAction.setTimeout(timeout);
        }
        if (callerId != null && !callerId.isEmpty()) {
            originateAction.setCallerId(callerId);
        }
        if (variables != null && !variables.isEmpty()) {
            originateAction.setVariables(variables);
        }

        logger.info("Sending OriginateAction to Asterisk: {}", originateAction);
        ManagerResponse response = connection.sendAction(originateAction);
        logger.info("Received response for OriginateAction: {}", response);

        return response;
    }
}
