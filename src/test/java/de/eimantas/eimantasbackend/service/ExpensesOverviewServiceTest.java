package de.eimantas.eimantasbackend.service;

import de.eimantas.eimantasbackend.TestUtils;
import de.eimantas.eimantasbackend.entities.Expense;
import de.eimantas.eimantasbackend.entities.dto.AccountOverViewDTO;
import de.eimantas.eimantasbackend.entities.dto.CategoryAndCountOverview;
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

        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        this.expensesRepository.deleteAll();

        expenses = new ArrayList<>();

        // populate acc with expenses
        for (int i = 0; i < monthsGoBack; i++) {
            // whats ussual second name for iteration?
            for (int y = 0; y < expensesForMonth; y++) {
                Expense e = TestUtils.getExpense(i);
                e.setUserId(1L);
                e.setAccountId(1L);
                expenses.add(e);
            }
        }

        expensesRepository.saveAll(expenses);

    }


    @Test
    public void testSearchExpensesPartName() throws Exception {

        List<Expense> found = expensesService.searchExpensesForUser("upl", 1L);
        assertThat(found).isNotNull();
        assertThat(found.size()).isEqualTo(18);

    }

    @Test
    public void testSearchExpensesCompleteName() throws Exception {

        List<Expense> found = expensesService.searchExpensesByName("uploaded");
        assertThat(found).isNotNull();
        assertThat(found.size()).isEqualTo(18);

    }


    @Test
    public void testSearchExpensesInPeriod() throws Exception {
        // three months should be okay
        LocalDate before = LocalDate.now().minus(Period.ofMonths(3));

        List<Expense> found = expensesService.searchExpensesInPeriod(before.atStartOfDay().toInstant(ZoneOffset.UTC),
                LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC));
        assertThat(found).isNotNull();
        assertThat(found.size()).isEqualTo(expensesForMonth * 4);

    }

    @Test
    public void testSearchExpensesCaseSenstive() throws Exception {

        List<Expense> found = expensesService.searchExpensesForUser("Uploaded", 1L);
        assertThat(found).isNotNull();
        assertThat(found.size()).isEqualTo(0);

    }


    @Test
    public void testSearchExpensesNoText() throws Exception {

        List<Expense> found = expensesService.searchExpensesForUser("asdasd", 0L);
        assertThat(found).isNotNull();
        assertThat(found.size()).isEqualTo(0);

    }


    @Test
    public void testSearchExpensesNoString() throws Exception {

        List<Expense> found = expensesService.searchExpensesForUser(null, 0L);
        assertThat(found).isNotNull();
        assertThat(found.size()).isEqualTo(0);

    }

    @Test(expected = SecurityException.class)
    public void testSearchExpensesNoUser() throws Exception {

        List<Expense> found = expensesService.searchExpensesForUser("test", 0L);
        assertThat(found).isNotNull();
        assertThat(found.size()).isEqualTo(0);

    }


    @Test
    public void readGlobalOverivew() throws Exception {


        Optional<AccountOverViewDTO> overView = expensesService.getExpensesOverview(1L, mockPrincipal);

        assertThat(overView.isPresent()).isEqualTo(true);
        assertThat(overView.get().getCountExpenses()).isEqualTo(18);
        assertThat(overView.get().getRefAccountId()).isEqualTo(1L);
        assertThat(overView.get().getTotalExpensesCount()).isEqualTo(18);
        assertThat(overView.get().getTotal()).isEqualTo(BigDecimal.valueOf(180L));


    }

    @Test(expected = SecurityException.class)
    public void readGlobalOverivewNoAuth() throws Exception {

        Optional<AccountOverViewDTO> overView = expensesService.getExpensesOverview(1L, null);
        assertThat(overView.isPresent()).isEqualTo(false);


    }

    @Test(expected = IllegalArgumentException.class)
    public void readGlobalOverivewNoAcc() throws Exception {

        Optional<AccountOverViewDTO> overView = expensesService.getExpensesOverview(0L, mockPrincipal);
        assertThat(overView.isPresent()).isEqualTo(false);

    }


    @Test
    public void testGetExpensesCount() throws Exception {

        int count = expensesService.getExpensesCountForAcc(1L);
        assertThat(count).isEqualTo(18);

    }


    @Test
    public void testEditExpense() throws Exception {

        Optional<Expense> expOpt = expensesService.getExpenseById(expenses.get(0).getId());
        assertThat(expOpt.isPresent()).isTrue();
        String newName = "Name edited";

        Expense expense = expOpt.get();
        expense.setName(newName);

        expensesService.save(expense);

        Optional<Expense> expOptEdited = expensesService.getExpenseById(expenses.get(0).getId());
        assertThat(expOptEdited.isPresent()).isTrue();
        assertThat(expOptEdited.get().getName()).isEqualTo(newName);


    }


    @Test
    public void testGetExpensesCountNoId() throws Exception {

        int count = expensesService.getExpensesCountForAcc(0);
        assertThat(count).isEqualTo(0);

    }


    @Test
    public void testGetCategoryAndCount() throws Exception {

        List<CategoryAndCountOverview> result = expensesService.getCategoryAndCountForAcc(1L);
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);

        logger.info(result.toString());

    }


}
