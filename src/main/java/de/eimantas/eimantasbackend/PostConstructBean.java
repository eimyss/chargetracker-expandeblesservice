package de.eimantas.eimantasbackend;

import de.eimantas.eimantasbackend.service.SecurityService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

import static org.springframework.transaction.annotation.Isolation.READ_UNCOMMITTED;

@Component
public class PostConstructBean implements ApplicationRunner {

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

    private String testUserName = "test";

    @Autowired
    private Environment environment;

    @Autowired
    private SecurityService securityService;

    //@PostConstruct
    //  public void init() {
    //  }

    private void preFillData() {

    }

    // we allow read stuff that is not commited, because by generation of subsequent entities, it comes to id collision
    @Override
    @Transactional(isolation = READ_UNCOMMITTED)
    public void run(ApplicationArguments args) throws Exception {

        logger.info("Starting expenses backend controller");
        logger.info("eureka server: " + environment.getProperty("spring.application.name"));
        logger.info("active profiles: " + Arrays.asList(environment.getActiveProfiles()).toString());
        logger.info("default profiles: " + Arrays.asList(environment.getDefaultProfiles()).toString());
        logger.info("sonstige info: " + environment.toString());
        logger.info("allowed Profiles: " + environment.getProperty("spring.profiles"));

        if (environment.getProperty("spring.profiles") != null) {
            if (environment.getProperty("spring.profiles").contains("populate")) {
                logger.info("Stuff will be populated!");
                preFillData();
            }
        } else {
            logger.info("Profile doesnt populate data");
        }
    }
}
