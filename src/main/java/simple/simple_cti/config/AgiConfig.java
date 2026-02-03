package simple.simple_cti.config;

import org.asteriskjava.fastagi.AgiServerThread;
import org.asteriskjava.fastagi.MappingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import simple.simple_cti.agi.ArsAgiScript;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Configuration
public class AgiConfig {

    private static final Logger logger = LoggerFactory.getLogger(AgiConfig.class);

    private final ArsAgiScript arsAgiScript;
    private AgiServerThread agiServerThread;

    public AgiConfig(ArsAgiScript arsAgiScript) {
        this.arsAgiScript = arsAgiScript;
    }

    @PostConstruct
    public void startAgiServer() {
        org.asteriskjava.fastagi.DefaultAgiServer agiServer = new org.asteriskjava.fastagi.DefaultAgiServer();
        
        MappingStrategy mappingStrategy = (request, channel) -> arsAgiScript;
        
        agiServer.setMappingStrategy(mappingStrategy);
        agiServer.setPort(4573);
        
        agiServerThread = new AgiServerThread(agiServer);
        agiServerThread.setDaemon(true);
        agiServerThread.startup();
        
        logger.info("FastAGI Server started on port 4573");
    }

    @PreDestroy
    public void stopAgiServer() {
        if (agiServerThread != null) {
            agiServerThread.shutdown();
            logger.info("FastAGI Server stopped");
        }
    }
}