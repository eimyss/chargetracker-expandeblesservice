package de.eimantas.eimantasbackend.service;

import de.eimantas.eimantasbackend.TestUtils;
import de.eimantas.eimantasbackend.entities.Expense;
import de.eimantas.eimantasbackend.entities.dto.AccountOverViewDTO;
import de.eimantas.eimantasbackend.entities.dto.CategoryAndCountOverview;
import de.eimantas.eimantasbackend.repo.ExpenseRepository;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.mockito.Mockito;
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

import javax.inject.Inject;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class ExpensesOverviewServiceTest {


  private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

  private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
      MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

  private MockMvc mockMvc;

  private List<Expense> expensesList = new ArrayList<>();

  @Inject
  private ExpensesService expensesService;
  @Autowired
  private ExpenseRepository expensesRepository;

  @Autowired
  private WebApplicationContext webApplicationContext;

  private int monthsGoBack = 6;
  private int expensesForMonth = 3;
  private KeycloakAuthenticationToken mockPrincipal;
  private List<Expense> expenses;


  @Before
  public void setup() throws Exception {
    // auth stuff
    expensesRepository.deleteAll();
    mockPrincipal = Mockito.mock(KeycloakAuthenticationToken.class);
    Mockito.when(mockPrincipal.getName()).thenReturn("test");

    KeycloakPrincipal keyPrincipal = Mockito.mock(KeycloakPrincipal.class);
    RefreshableKeycloakSecurityContext ctx = Mockito.mock(RefreshableKeycloakSecurityContext.class);

    AccessToken token = Mockito.mock(AccessToken.class);
    Mockito.when(token.getSubject()).thenReturn(TestUtils.USER_ID);
    Mockito.when(ctx.getToken()).thenReturn(token);
    Mockito.when(keyPrincipal.getKeycloakSecurityContext()).thenReturn(ctx);
    Mockito.when(mockPrincipal.getPrincipal()).thenReturn(keyPrincipal);

    this.mockMvc = webAppContextSetup(webApplicationContext).build();
    this.expensesRepository.deleteAll();

    expenses = new ArrayList<>();

    // populate acc with expenses
    for (int i = 0; i < monthsGoBack; i++) {
      // whats ussual second name for iteration?
      for (int y = 0; y < expensesForMonth; y++) {
        Expense e = TestUtils.getExpense(i);
        e.setUserId(TestUtils.USER_ID);
        e.setAccountId(1L);
        expenses.add(e);
      }
    }
    Expense exp = TestUtils.getExpense();
    exp.setUserId(TestUtils.USER_ID);
    exp.setAccountId(1L);
    exp.setRefBookingId(1);
    expenses.add(exp);

    expensesRepository.saveAll(expenses);

  }


  @Test
  public void testSearchExpensesPartName() throws Exception {

    List<Expense> found = expensesService.searchExpensesForUser("upl", mockPrincipal);
    assertThat(found).isNotNull();
    assertThat(found.size()).isEqualTo(expenses.size());

  }

  @Test
  public void testSearchExpensesCompleteName() throws Exception {

    List<Expense> found = expensesService.searchExpensesForUser("uploaded", mockPrincipal);
    assertThat(found).isNotNull();
    assertThat(found.size()).isEqualTo(expenses.size());

  }


  @Test
  public void testSearchExpensesInPeriod() throws Exception {
    // three months should be okay
    LocalDate before = LocalDate.now().minus(Period.ofMonths(3));

    List<Expense> found = expensesService.searchExpensesInPeriod(before.atStartOfDay().toInstant(ZoneOffset.UTC),
        LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC), mockPrincipal);
    assertThat(found).isNotNull();
    assertThat(found.size()).isEqualTo(expensesForMonth * 4);

  }

  @Test
  public void testSearchExpensesCaseSenstive() throws Exception {

    List<Expense> found = expensesService.searchExpensesForUser("Uploaded", mockPrincipal);
    assertThat(found).isNotNull();
    assertThat(found.size()).isEqualTo(0);

  }


  @Test
  public void testSearchExpensesNoText() throws Exception {

    List<Expense> found = expensesService.searchExpensesForUser("asdasd", mockPrincipal);
    assertThat(found).isNotNull();
    assertThat(found.size()).isEqualTo(0);

  }


  @Test
  public void testSearchExpensesNoString() throws Exception {

    List<Expense> found = expensesService.searchExpensesForUser(null, mockPrincipal);
    assertThat(found).isNotNull();
    assertThat(found.size()).isEqualTo(0);

  }

  public void testSearchExpensesNoUser() throws Exception {
    List<Expense> found = expensesService.searchExpensesForUser("test", mockPrincipal);
    assertThat(found).isNotNull();
    assertThat(found.size()).isEqualTo(0);
  }


  @Test
  public void readGlobalOverivew() throws Exception {


    Optional<AccountOverViewDTO> overView = expensesService.getExpensesOverview(1L, mockPrincipal);

    assertThat(overView.isPresent()).isEqualTo(true);
    assertThat(overView.get().getCountExpenses()).isEqualTo(expenses.size());
    assertThat(overView.get().getRefAccountId()).isEqualTo(1L);
    assertThat(overView.get().getTotalExpensesCount()).isEqualTo(expenses.size());
    assertThat(overView.get().getTotal()).isEqualTo(BigDecimal.valueOf(180L));


  }

  @Test(expected = SecurityException.class)
  public void readGlobalOverivewNoAuth() throws Exception {

    Optional<AccountOverViewDTO> overView = expensesService.getExpensesOverview(1L, null);
    assertThat(overView.isPresent()).isEqualTo(true);

  }

  @Test
  public void readGlobalOverivewNoAcc() throws Exception {

    Optional<AccountOverViewDTO> overView = expensesService.getExpensesOverview(0L, mockPrincipal);
    assertThat(overView.isPresent()).isEqualTo(true);

  }

  @Test
  public void getExpenseByRefBooking() throws Exception {

    Expense expense = expensesService.getExpenseByRefBooking((Integer)1, mockPrincipal);
    assertThat(expense).isNotNull();

  }



  @Test
  public void testGetExpensesCount() throws Exception {

    int count = expensesService.getExpensesCountForAcc(1L, mockPrincipal);
    assertThat(count).isEqualTo(expenses.size());

  }


  @Test
  public void testEditExpense() throws Exception {

    Expense expOpt = expensesService.getExpenseById(expenses.get(0).getId(), mockPrincipal);
    assertThat(expOpt).isNotNull();
    String newName = "Name edited";
    expOpt.setName(newName);
    expensesService.save(expOpt, mockPrincipal);
    Expense expOptEdited = expensesService.getExpenseById(expenses.get(0).getId(), mockPrincipal);
    assertThat(expOptEdited).isNotNull();
    assertThat(expOptEdited.getName()).isEqualTo(newName);


  }


  @Test
  public void testGetExpensesCountNoId() throws Exception {

    int count = expensesService.getExpensesCountForAcc(0, mockPrincipal);
    assertThat(count).isEqualTo(0);

  }

  @Test
  @Ignore
  public void testNotifyCreatedExpense() throws Exception {
    expensesService.notifyCreatedExpense(mockPrincipal,TestUtils.getExpense(1));
  }


  @Test
  public void testGetCategoryAndCount() throws Exception {

    List<CategoryAndCountOverview> result = expensesService.getCategoryAndCountForAcc(1L, mockPrincipal);
    assertThat(result).isNotNull();
    assertThat(result.size()).isEqualTo(1);

    logger.info(result.toString());

  }


}
