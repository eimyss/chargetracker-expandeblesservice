package de.eimantas.eimantasbackend.service;


import de.eimantas.eimantasbackend.entities.converter.EntitiesConverter;
import de.eimantas.eimantasbackend.entities.dto.CSVExpenseDTO;
import de.eimantas.eimantasbackend.entities.dto.ExpenseDTO;
import de.eimantas.eimantasbackend.processing.FileProcessor;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ProcessingServiceTest {
  private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

  @Inject
  private FileProcessor dataProcessor;

  private MockMvc mockMvc;

  @Autowired
  EntitiesConverter entitiesConverter;

  @Autowired
  private WebApplicationContext webApplicationContext;


  @Before
  public void setup() throws Exception {

    this.mockMvc = webAppContextSetup(webApplicationContext).build();
  }


  @Test
  public void dateFormatParse() throws Exception {


    String date = "24.08.2018";
    SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");

    Date datum = df.parse(date);


    assertThat(datum).isNotNull();
    logger.info("Date: " + datum.toString());

  }

  @Test
  @Ignore
  public void testReadData() throws Exception {

    List<CSVExpenseDTO> dtos = dataProcessor.loadObjectList(CSVExpenseDTO.class, "data-small.csv");

    assertThat(dtos).isNotNull();
    logger.info("size: " + dtos.size());

  }


  @Test
  @Ignore
  public void testProcessReadData() throws Exception {

    List<CSVExpenseDTO> dtos = dataProcessor.loadObjectList(CSVExpenseDTO.class, "data-small.csv");

    assertThat(dtos).isNotNull();
    logger.info("size: " + dtos.size());

  }

  @Test
  @Ignore
  public void testConvertProcessReadData() throws Exception {

    List<CSVExpenseDTO> dtos = dataProcessor.loadObjectList(CSVExpenseDTO.class, "data-small.csv");

    assertThat(dtos).isNotNull();
    logger.info("size: " + dtos.size());

    List<ExpenseDTO> dtosconverted = entitiesConverter.convertCSVExpenses(dtos);

    assertThat(dtosconverted).isNotNull();
    logger.info(" dtosconverted size: " + dtosconverted.size());

    assertThat(dtosconverted.size()).isEqualTo(dtos.size());

    dtosconverted.forEach(dto -> {
      dto.setAccountId(1L);
      dto.setUserId("1L");
      dto.setExpensable(true);
      dto.setCategory("IMPORTED");
    });

  }

}
