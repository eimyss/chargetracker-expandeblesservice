package de.eimantas.eimantasbackend.service;

import de.eimantas.eimantasbackend.TestUtils;
import de.eimantas.eimantasbackend.client.AccountsClient;
import de.eimantas.eimantasbackend.entities.AccountOverView;
import de.eimantas.eimantasbackend.entities.Expense;
import de.eimantas.eimantasbackend.entities.dto.AllAccountsOverViewDTO;
import de.eimantas.eimantasbackend.entities.dto.MonthAndAmountOverview;
import de.eimantas.eimantasbackend.processing.OverviewProcessor;
import de.eimantas.eimantasbackend.repo.ExpenseRepository;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.mockito.InjectMocks;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AccountOverviewServiceTest {

    private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

    private MockMvc mockMvc;

    private List<Expense> expensesList = new ArrayList<>();


    @Autowired
    private ExpenseRepository expensesRepository;

    @Autowired
    private ExpensesService expensesService;

    @Autowired
    private OverviewProcessor overviewProcessor;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private List<Expense> expenses;
    private int monthsGoBack = 6;
    private int expensesForMonth = 3;


    private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());
    private KeycloakAuthenticationToken mockPrincipal;

    @Before
    public void setup() throws Exception {

        Collection<Long> list = new ArrayList<>();
        list.add(1L);


        expensesRepository.deleteAll();
        // auth stuff
        // auth stuff
        mockPrincipal = Mockito.mock(KeycloakAuthenticationToken.class);
        Mockito.when(mockPrincipal.getName()).thenReturn("test");

        KeycloakPrincipal keyPrincipal = Mockito.mock(KeycloakPrincipal.class);
        RefreshableKeycloakSecurityContext ctx = Mockito.mock(RefreshableKeycloakSecurityContext.class);

        AccessToken token = Mockito.mock(AccessToken.class);
        Mockito.when(token.getSubject()).thenReturn("Subject-111");
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
                e.setAccountId(1L);
                e.setUserId("1L");
                expenses.add(e);
            }
        }

        expensesRepository.saveAll(expenses);
    }


    @Test
    public void getTotalAmountForAcc() throws Exception {

        BigDecimal totalAmount = expensesService.getTotalAmountForAcc(1L);

        assertThat(totalAmount).isNotNull();
        assertThat(totalAmount).isEqualTo(BigDecimal.valueOf(180L));

    }


    @Test
    public void getTotalAmountForAccNoId() throws Exception {

        BigDecimal totalAmount = expensesService.getTotalAmountForAcc(0);
        assertThat(totalAmount).isNotNull();
        assertThat(totalAmount).isEqualTo(BigDecimal.ZERO);

    }


    @Test
    public void readOverivew() throws Exception {

        Optional<AccountOverView> overView = expensesService.getOverViewForAccount(1L, mockPrincipal);

        assertThat(overView.isPresent()).isEqualTo(true);
        assertThat(overView.get().getCountExpenses()).isEqualTo(18);
        assertThat(overView.get().getRefAccountId()).isEqualTo(1L);
        assertThat(overView.get().getTotal()).isEqualTo(new BigDecimal("180"));

    }

    @Test
    public void readNonExistingOverivew() throws Exception {

        Optional<AccountOverView> overView = expensesService.getOverViewForAccount(33, mockPrincipal);
        assertThat(overView.isPresent()).isEqualTo(true);

    }

    @Test(expected = SecurityException.class)
    public void readOverivewNoSecurity() throws Exception {

        Optional<AccountOverView> overView = expensesService.getOverViewForAccount(1L, null);
        assertThat(overView.isPresent()).isEqualTo(false);

    }


    @Test
    @Ignore
    public void readAllAccountsOverivew() throws Exception {

        AllAccountsOverViewDTO dto = expensesService.getAllACccountsOverViewForUser(mockPrincipal);
        assertThat(dto).isNotNull();
        logger.info(dto.toString());
        assertThat(dto.getOverview()).isNotNull();
        assertThat(dto.getOverview().size()).isEqualTo(1);
        assertThat(dto.getUnexpenced()).isNotNull();
        // one month wont be considered
        assertThat(dto.getUnexpenced().size()).isEqualTo(expensesRepository.count() - expensesForMonth);


    }

    @Ignore
    @Test(expected = SecurityException.class)
    public void readAllAccountsOverivewNoPrincipal() throws Exception {


        AllAccountsOverViewDTO dto = expensesService.getAllACccountsOverViewForUser(null);
        assertThat(dto).isNull();


    }

    @Test
    public void readAllMonthsOverview() throws Exception {
        AllAccountsOverViewDTO empty = new AllAccountsOverViewDTO();
        List<MonthAndAmountOverview> dto = overviewProcessor.getPerMonthOverView(6, expenses, empty);
        assertThat(dto).isNotNull();
        assertThat(dto.size()).isEqualTo(6);

        logger.info(dto.toString());

        assertThat(empty.getUnexpenced()).isNotNull();
        // one month wont be considered
        assertThat(empty.getUnexpenced().size()).isEqualTo(expensesRepository.count() - expensesForMonth);

    }


    @Test
    public void readAllMonthsOverviewNoDto() throws Exception {

        List<MonthAndAmountOverview> dto = overviewProcessor.getPerMonthOverView(6, expenses, null);
        assertThat(dto).isNotNull();
        assertThat(dto.size()).isEqualTo(6);

        logger.info(dto.toString());


    }


    @Test
    public void readAllMonthsOverviewNoAcc() throws Exception {

        List<MonthAndAmountOverview> dto = overviewProcessor.getPerMonthOverView(6, expenses, new AllAccountsOverViewDTO());
        assertThat(dto).isNotNull();

    }


}
