package de.eimantas.eimantasbackend.messaging;

import de.eimantas.eimantasbackend.TestUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.Charset;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class ExpensesMessagingTest {


  private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

  private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
      MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

  private MockMvc mockMvc;

  @Autowired
  ExpensesSender expensesSender;

  @Autowired
  private WebApplicationContext webApplicationContext;


  @Before
  public void setup() throws Exception {

  }


  @Test
  @Ignore
  public void testQueue() throws Exception {

    expensesSender.createExpense(TestUtils.getExpenseDTO());

  }


}
