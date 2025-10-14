package simple.simple_cti.ami;


import org.asteriskjava.live.ManagerCommunicationException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionState;
import org.asteriskjava.manager.TimeoutException;
import org.asteriskjava.manager.action.CommandAction;
import org.asteriskjava.manager.response.CommandResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class AmiService {
    private static final Logger logger = LoggerFactory.getLogger(AmiService.class);
    private final AmiConnectionManager connectionManager;

    public AmiService(AmiConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public void printDialplan() {
        try {
            String dialplan = getDialplan();
            logger.info("--- DIALPLAN START ---\n{}\n--- DIALPLAN END ---", dialplan);
        } catch (Exception e) {
            logger.error("Error while getting and printing dialplan", e);
        }
    }

    private String getDialplan() throws IOException, TimeoutException, ManagerCommunicationException {
        ManagerConnection managerConnection = connectionManager.getManagerConnection();

        if (managerConnection == null || managerConnection.getState() != ManagerConnectionState.CONNECTED) {
            logger.warn("Cannot get dialplan, not connected to Asterisk. State: {}", connectionManager.getConnectionState());
            return "Not connected to Asterisk.";
        }

        logger.info("Sending CommandAction('dialplan show') to fetch dialplan...");
        // Use CommandAction as an alternative way to get the dialplan
        CommandAction commandAction = new CommandAction("dialplan show");
        CommandResponse commandResponse = (CommandResponse) managerConnection.sendAction(commandAction);

        if (commandResponse.getResult() == null || commandResponse.getResult().isEmpty()) {
            logger.warn("Command 'dialplan show' executed, but returned no output.");
            return "Dialplan is empty or user lacks 'command' permission.";
        }

        // The result is a list of strings, join them together.
        return String.join("\n", commandResponse.getResult());
    }

    public ManagerConnectionState getConnectionState() {
        return connectionManager.getConnectionState();
    }
}