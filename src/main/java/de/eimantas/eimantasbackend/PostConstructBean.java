package de.eimantas.eimantasbackend;

import de.eimantas.eimantasbackend.entities.Booking;
import de.eimantas.eimantasbackend.entities.Expense;
import de.eimantas.eimantasbackend.entities.ExpenseCategory;
import de.eimantas.eimantasbackend.repo.BookingRepository;
import de.eimantas.eimantasbackend.repo.ExpenseRepository;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;

@Component
public class PostConstructBean implements ApplicationRunner {

  private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private Environment environment;

  @Autowired
  private BookingRepository bookingRepository;

  @Autowired
  private ExpenseRepository repository;

  private void preFillData() {

    logger.info("populating data");

    for (int i = 1; i < 11; i++) {
      Expense exp = new Expense();
      exp.setAccountId(1);
      exp.setUserId("ee9fb974-c2c2-45f8-b60e-c22d9f00273f");
      exp.setName("Generated");
      exp.setExpensable(true);
      exp.setExpensed((i % 2) == 0);
      exp.setBetrag(BigDecimal.TEN);
      exp.setCategory(ExpenseCategory.STEUER);
      exp.setOrt("Wiesbade");
      exp.setCreateDate(ZonedDateTime.now().minusMonths(i).toInstant());
      exp.setValid(true);
      exp.setPurpose("Generated by the system");
      Expense saved = repository.save(exp);
      logger.info("expense saved: " + saved.toString());
    }

    logger.info("Populating bookings");
    Booking booking = new Booking();
    booking.setName("populated");
    booking.setUserId("ee9fb974-c2c2-45f8-b60e-c22d9f00273f");
    booking.setProjectId(2);
    booking.setStartdate(LocalDateTime.now());
    booking.setEndDate(LocalDateTime.now());
    booking.setId(1);
    Booking b = bookingRepository.save(booking);

    logger.info("saved: " + b.toString());


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
