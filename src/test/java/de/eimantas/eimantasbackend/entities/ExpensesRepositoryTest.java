package de.eimantas.eimantasbackend.entities;

import com.google.common.collect.Iterables;
import de.eimantas.eimantasbackend.repo.ExpenseRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ExpensesRepositoryTest {

  private MockMvc mockMvc;
  private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

  @SuppressWarnings("rawtypes")
  private HttpMessageConverter mappingJackson2HttpMessageConverter;

  private List<Expense> expensesList = new ArrayList<>();

  @Autowired
  private ExpenseRepository expensesRepository;

  private Principal mockPrincipal;

  // @Autowired
  // private ExpenseController controller;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @Autowired
  void setConverters(HttpMessageConverter<?>[] converters) {

    this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream()
        .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny().orElse(null);

    assertNotNull("the JSON message converter must not be null", this.mappingJackson2HttpMessageConverter);
  }

  @Before
  public void setup() throws Exception {
    this.mockMvc = webAppContextSetup(webApplicationContext).build();

    expensesRepository.deleteAll();

    Expense exp = new Expense();
    exp.setName("test");
    exp.setCategory(ExpenseCategory.DIESEL);
    exp.setBetrag(BigDecimal.TEN);
    exp.setOrt("Test Ort");
    exp.setCreateDate(Instant.now().minus(1, ChronoUnit.DAYS));
    exp.setAccountId(1L);
    exp.setUserId("1L");

    this.expensesList.add(exp);
    // this.expensesList.add(expensesRepository.save(exp));

    Expense exp2 = new Expense();
    exp2.setName("test-2");
    exp2.setCategory(ExpenseCategory.ESSEN);
    exp2.setBetrag(BigDecimal.TEN);
    exp2.setOrt("Bingen");
    exp2.setCreateDate(Instant.now().minus(40, ChronoUnit.DAYS));
    exp2.setAccountId(1L);
    exp2.setUserId("1L");

    this.expensesList.add(exp2);
    expensesRepository.saveAll(expensesList);


    mockPrincipal = Mockito.mock(KeycloakAuthenticationToken.class);
    Mockito.when(mockPrincipal.getName()).thenReturn("test@test.de");
  }


  @Test
  public void contexLoads() throws Exception {
    assertThat(expensesRepository).isNotNull();
  }

  @Test
  public void readSingleExpense() throws Exception {

    logger.info("eimantas testing for expense: " + this.expensesList.get(0).toString());
    mockMvc.perform(get("/expenses/" + this.expensesList.get(0).getId())).andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE + ";charset=UTF-8"))
        .andExpect(jsonPath("$.id", is(this.expensesList.get(0).getId().intValue())))
        .andExpect(jsonPath("$.name", is("test"))).andExpect(jsonPath("$.category", is("DIESEL")));
  }

  @Test
  public void getExpensesBetweenDatesSource() throws Exception {
    List<Expense> mapstream = Collections.emptyList();


    try (Stream<Expense> stream = expensesRepository.findExpensesInPeriod(Instant.now().minus(30, ChronoUnit.DAYS), Instant.now())) {
      mapstream = stream.collect(Collectors.toList());
    }

    assertThat(mapstream.size()).isEqualTo(1);

  }


  @Test
  public void getExpensesInPeriodForAccount() throws Exception {
    List<Expense> mapstream = Collections.emptyList();


    logger.info("dates: " + Instant.now().minus(30, ChronoUnit.DAYS) + " and: " + Instant.now());
    try (Stream<Expense> stream = expensesRepository.findExpensesInPeriodForAccount(1L, Instant.now().minus(30, ChronoUnit.DAYS), Instant.now())) {
      mapstream = stream.collect(Collectors.toList());
    }

    assertThat(mapstream.size()).isEqualTo(1);

  }

  @Test
  public void getExpensesBetweenDatesSourceGlobally() throws Exception {

    Iterable<Expense> list = this.expensesRepository.findAll();
    logger.info("eimantas repo count:" + Iterables.size(list));
    assertThat(Iterables.size(list)).isEqualTo(2);
    list.forEach((expense) -> logger.info(expense.getCreateDate().toString()));

    List<Expense> listfound = expensesRepository
        .findExpensesInPeriodGlobaly(Instant.now().minus(30, ChronoUnit.DAYS), Instant.now());

    assertThat(listfound.size()).isEqualTo(1);

  }

  @Test
  public void getExpensesBetweenDatesSourceByName() throws Exception {

    List<Expense> listfound = expensesRepository.findByCreateDateBetween(Instant.now().minus(30, ChronoUnit.DAYS),
        Instant.now());

    assertThat(listfound.size()).isEqualTo(1);

  }

  @Test
  public void readExpenses() throws Exception {

    Principal mockPrincipal = Mockito.mock(Principal.class);
    Authentication auth = Mockito.mock(Authentication.class);
    HashMap<String, Object> details = new HashMap<>();
    details.put("email", (String) "test@test.de");
    Mockito.when(auth.getDetails()).thenReturn(details);
    Mockito.when(auth.getPrincipal()).thenReturn("test");
    // given(controller.principal).willReturn(allEmployees);
    mockMvc.perform(get("/expenses").principal(mockPrincipal)).andExpect(status().isOk())
        .andDo(MockMvcResultHandlers.print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE + ";charset=UTF-8"))
        .andExpect(jsonPath("$._embedded.expenses[0].id", is(this.expensesList.get(0).getId().intValue())))
        .andExpect(jsonPath("$._embedded.expenses[0].name", is("test")))
        .andExpect(jsonPath("$._embedded.expenses[0].category", is("DIESEL")))
        .andExpect(jsonPath("$._embedded.expenses[1].id", is(this.expensesList.get(1).getId().intValue())))
        .andExpect(jsonPath("$._embedded.expenses[1].category", is("ESSEN")))
        .andExpect(jsonPath("$._embedded.expenses[1].ort", is("Bingen")));
  }


  @SuppressWarnings("unchecked")
  protected String json(Object o) throws IOException {
    MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
    this.mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
    return mockHttpOutputMessage.getBodyAsString();
  }

}
