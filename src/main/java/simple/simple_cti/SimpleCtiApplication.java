package simple.simple_cti;

import org.asteriskjava.manager.ManagerConnectionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import simple.simple_cti.ami.AmiService;

@SpringBootApplication
public class SimpleCtiApplication {

    private static final Logger logger = LoggerFactory.getLogger(SimpleCtiApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SimpleCtiApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLine_runner(AmiService amiService) {
        return args -> {
            // Give the connection a moment to establish before checking its state
            logger.info("Waiting 1 second for AMI connection to establish...");
            Thread.sleep(1000);

            amiService.printDialplan();
        };
    }
}