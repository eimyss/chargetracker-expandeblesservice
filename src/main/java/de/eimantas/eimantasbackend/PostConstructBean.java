package de.eimantas.eimantasbackend;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class PostConstructBean implements ApplicationRunner {

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private Environment environment;

    private void preFillData() {

    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        logger.info("Starting expenses backend controller");
        logger.info("eureka server: "
                + environment.getProperty("spring.application.name"));
        logger.info("active profiles: "
                + Arrays.asList(environment.getActiveProfiles()).toString());
        logger.info("default profiles: "
                + Arrays.asList(environment.getDefaultProfiles()).toString());
        logger.info("sonstige info: "
                + environment.toString());
        logger.info("allowed Profiles: "
                + environment.getProperty("spring.profiles"));

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
