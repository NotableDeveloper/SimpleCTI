package simple.simple_cti.ami;

import org.asteriskjava.manager.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;

@Component
public class AmiConnectionManager {
    private final Logger logger = LoggerFactory.getLogger(AmiConnectionManager.class);
    private ManagerConnection managerConnection;

    @Value("${asterisk.host}")
    private String host;

    @Value("${asterisk.username}")
    private String username;

    @Value("${asterisk.password}")
    private String password;

    @PostConstruct
    public void connect() {
        try {
            ManagerConnectionFactory factory = new ManagerConnectionFactory(host, username, password);
            this.managerConnection = factory.createManagerConnection();
            managerConnection.login();
            logger.info("AMI connection process initiated by AmiConnectionManager.");
        } catch (IOException | AuthenticationFailedException | TimeoutException | IllegalStateException e) {
            logger.error("AmiConnectionManager failed to connect to Asterisk", e);
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void disconnect() {
        if (this.managerConnection != null && this.managerConnection.getState() != ManagerConnectionState.DISCONNECTED) {
            this.managerConnection.logoff();
        }
    }

    public ManagerConnection getManagerConnection() {
        return managerConnection;
    }

    public ManagerConnectionState getConnectionState() {
        if (this.managerConnection == null) {
            return ManagerConnectionState.DISCONNECTED;
        }
        return this.managerConnection.getState();
    }
}