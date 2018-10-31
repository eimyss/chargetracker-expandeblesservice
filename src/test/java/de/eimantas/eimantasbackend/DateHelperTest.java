package de.eimantas.eimantasbackend;

import de.eimantas.eimantasbackend.helpers.DateHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class DateHelperTest {


  @Before
  public void setup() {


  }


  @Test
  public void testIsInMonthWayBefore() {


    LocalDateTime today = LocalDateTime.now();
    LocalDateTime ago = today.minus(Period.ofMonths(3));

    boolean answer = DateHelper.isInMonth(6, ago.toInstant(ZoneOffset.UTC));
    assertThat(answer).isEqualTo(false);

  }


  @Test
  public void testIsInCurrentMonth() {

    LocalDateTime today = LocalDateTime.now();
    LocalDateTime ago = today.minus(Period.ofMonths(0));

    boolean answer = DateHelper.isInMonth(6, ago.toInstant(ZoneOffset.UTC));
    assertThat(answer).isEqualTo(false);

  }


  @Test
  public void testIsNotInMonth() {

    LocalDateTime today = LocalDateTime.now();
    LocalDateTime ago = today.minus(Period.ofMonths(9));

    boolean answer = DateHelper.isInMonth(6, ago.toInstant(ZoneOffset.UTC));
    assertThat(answer).isEqualTo(false);

  }

  @Test
  public void testIsInMonthGrenze() {

    LocalDateTime today = LocalDateTime.now();
    LocalDateTime ago = today.minus(Period.ofMonths(6));

    boolean answer = DateHelper.isInMonth(6, ago.toInstant(ZoneOffset.UTC));
    assertThat(answer).isEqualTo(false);

  }


}
