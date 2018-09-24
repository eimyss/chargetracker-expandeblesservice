package de.eimantas.eimantasbackend.controller;

import de.eimantas.eimantasbackend.TestUtils;
import de.eimantas.eimantasbackend.client.AccountsClient;
import de.eimantas.eimantasbackend.entities.Expense;
import de.eimantas.eimantasbackend.entities.dto.ExpenseDTO;
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
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class MultipleExpensesControllerTest {

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());
    private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

    private MockMvc mockMvc;

    @SuppressWarnings("rawtypes")
    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    private int monthsGoBack = 2;
    private int expensesForMonth = 2;

    private List<Expense> expensesList = new ArrayList<>();

    private List<Expense> expensesList2 = new ArrayList<>();

    @Autowired
    private AccountsClient client;

    @Autowired
    private ExpenseRepository expensesRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;
    private KeycloakAuthenticationToken mockPrincipal;

    @Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {

        this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream()
                .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny().orElse(null);

        assertNotNull("the JSON message converter must not be null", this.mappingJackson2HttpMessageConverter);
    }

    @Before
    @Transactional
    public void setup() throws Exception {

        expensesRepository.deleteAll();

        // auth stuff
        mockPrincipal = Mockito.mock(KeycloakAuthenticationToken.class);
        Mockito.when(mockPrincipal.getName()).thenReturn("test");

        KeycloakPrincipal keyPrincipal = Mockito.mock(KeycloakPrincipal.class);
        RefreshableKeycloakSecurityContext ctx = Mockito.mock(RefreshableKeycloakSecurityContext.class);

        AccessToken token = Mockito.mock(AccessToken.class);
        Mockito.when(token.getSubject()).thenReturn("1L");
        Mockito.when(ctx.getToken()).thenReturn(token);
        Mockito.when(keyPrincipal.getKeycloakSecurityContext()).thenReturn(ctx);
        Mockito.when(mockPrincipal.getPrincipal()).thenReturn(keyPrincipal);

        this.mockMvc = webAppContextSetup(webApplicationContext).build();


        expensesList = new ArrayList<>();

        // populate acc with expenses
        for (int i = 0; i < monthsGoBack; i++) {
            // whats ussual second name for iteration?
            for (int y = 0; y < expensesForMonth; y++) {
                Expense e = TestUtils.getExpense(i);
                e.setAccountId(1L);
                e.setUserId("1L");
                expensesList.add(e);

            }
        }

        expensesList2 = new ArrayList<>();

        // populate acc with expenses
        for (int i = 0; i < monthsGoBack; i++) {
            // whats ussual second name for iteration?
            for (int y = 0; y < expensesForMonth; y++) {
                Expense e = TestUtils.getExpense(i);
                e.setAccountId(2L);
                e.setUserId("2L");
                expensesList2.add(e);
            }
        }

        //logger.info("acc repo count: " + accountRepository.count());
        //logger.info("acc count: " + usr.getAccounts().size());

        expensesRepository.saveAll(expensesList);
        expensesRepository.saveAll(expensesList2);

    }


    @Test
    @Transactional
    public void readSingleExpense() throws Exception {
        mockMvc.perform(get("/expense/get/" + this.expensesList.get(0).getId())).andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print()).andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.id", is(this.expensesList.get(0).getId().intValue())))
                .andExpect(jsonPath("$.name", is("uploaded"))).andExpect(jsonPath("$.category", is("STEUER")));


        mockMvc.perform(get("/expense/get/" + this.expensesList2.get(0).getId())).andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print()).andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.id", is(this.expensesList2.get(0).getId().intValue())))
                .andExpect(jsonPath("$.name", is("uploaded"))).andExpect(jsonPath("$.category", is("STEUER")));

    }

    @Test
    @Transactional
    public void readTypes() throws Exception {
        mockMvc.perform(get("/expense/types")).andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print()).andExpect(content().contentType(contentType))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)));


    }


    @Test
    @Transactional
    public void getExpensesInPeriod() throws Exception {

        Date from = Date.from(LocalDate.now().minus(Period.ofMonths(3)).atStartOfDay().toInstant(ZoneOffset.UTC));
        Date to = new Date();

        SimpleDateFormat formatted = new SimpleDateFormat("yyyy-MM-dd");

        logger.info(formatted.format(from));
        logger.info(formatted.format(to));

        mockMvc.perform(get("/expense/get/period?from=" + formatted.format(from) + "&to=" + formatted.format(to)).principal(mockPrincipal)).andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print()).andExpect(content().contentType(contentType))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(2))));


    }


    @Test
    @Transactional
    public void readExpenses() throws Exception {

        // given(controller.principal).willReturn(allEmployees);
        mockMvc.perform(get("/expense/get/all").principal(mockPrincipal)).andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print()).andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$", hasSize(8)));

    }

    @Test
    @Transactional
    @Ignore
    public void readCSVExpenses() throws Exception {

        long sizeBefore = expensesRepository.countByAccountId(1L);

        mockMvc.perform(get("/expense/csv/read/" + 1L).principal(mockPrincipal)).andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print()).andExpect(content().contentType(contentType));
        logger.info("size before: " + sizeBefore + "size after: " + expensesRepository.countByAccountId(1L));
    }


    @Test
    @Transactional
    public void searchExpenses() throws Exception {

        mockMvc.perform(get("/expense/search?name=upload").principal(mockPrincipal)).andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print()).andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$", hasSize(greaterThan(2))))
                .andExpect(jsonPath("$[0].id", is(this.expensesList.get(0).getId().intValue())))
                .andExpect(jsonPath("$[0].name", is("uploaded"))).andExpect(jsonPath("$[0].category", is("STEUER")))
                .andExpect(jsonPath("$[1].id", is(this.expensesList.get(1).getId().intValue())))
                .andExpect(jsonPath("$[1].category", is("STEUER"))).andExpect(jsonPath("$[1].ort", is("Mainz")));
    }


    @Test
    @Transactional
    public void createExpenseNoId() throws Exception {

        ExpenseDTO exp = TestUtils.getExpenseDTO();
        exp.setAccountId(1L);
        String bookmarkJson = json(exp);

        this.mockMvc.perform(post("/expense/add").principal(mockPrincipal).contentType(contentType).content(bookmarkJson))
                .andDo(MockMvcResultHandlers.print()).andExpect(content().contentType(contentType))
                .andExpect(status().isOk());
    }


    @Test
    @Ignore
    @Transactional
    public void createExpenseWrongId() throws Exception {

        ExpenseDTO exp = TestUtils.getExpenseDTO(12312);
        String bookmarkJson = json(exp);

        this.mockMvc.perform(post("/expense/add").principal(mockPrincipal).contentType(contentType).content(bookmarkJson))
                .andExpect(status().isBadRequest());
    }


    @Test
    @Transactional
    public void getExpensesByAccountId() throws Exception {

        this.mockMvc.perform(get("/expense/account/" + 1L).contentType(contentType))
                .andDo(MockMvcResultHandlers.print()).andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$", hasSize(4)));


        this.mockMvc.perform(get("/expense/account/" + 2L).contentType(contentType))
                .andDo(MockMvcResultHandlers.print()).andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    @Transactional
    public void readOverview() throws Exception {
        // given(controller.principal).willReturn(allEmployees);
        mockMvc.perform(get("/expense/overview/" + 1).principal(mockPrincipal)).andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print()).andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.refAccountId", is(1)))
                .andExpect(jsonPath("$.total", is(40)))
                .andExpect(jsonPath("$.countExpenses", is(4)));


        mockMvc.perform(get("/expense/overview/" + 2).principal(mockPrincipal)).andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print()).andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.refAccountId", is(2)))
                .andExpect(jsonPath("$.total", is(180)))
                .andExpect(jsonPath("$.countExpenses", is(18)));

    }


    @Test
    @Ignore
    public void readNonExistingOverview() throws Exception {

        // given(controller.principal).willReturn(allEmployees);
        mockMvc.perform(get("/expense/overview/" + 98).principal(mockPrincipal)).andExpect(status().isNotFound());
    }


    @SuppressWarnings("unchecked")
    protected String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        this.mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }


    @Test
   @Ignore
    public void testGetGlobalOverview() throws Exception {

        ArrayList<Long> list = new ArrayList<>();
        list.add(1L);
        Mockito.when(client.getAccountList()).thenReturn(list);

        // given(controller.principal).willReturn(allEmployees);
        mockMvc.perform(get("/expense/global-overview").principal(mockPrincipal)).andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print()).andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.monthBack", is(6)))
                .andExpect(jsonPath("$.userId", is("1L")))
                .andExpect(jsonPath("$.overview." + 1, hasSize(6)))
                .andExpect(jsonPath("$.unexpenced", hasSize((int) expensesRepository.count() - expensesForMonth * 2)));

    }

    @Test
    @Transactional
    public void testGetExpensesOverview() throws Exception {

        // given(controller.principal).willReturn(allEmployees);
        mockMvc.perform(get("/expense/overview/expenses/" + 1).principal(mockPrincipal)).andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print()).andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.refAccountId", is(1)))
                .andExpect(jsonPath("$.totalExpensesCount", is(4)))
                .andExpect(jsonPath("$.total", is(40)))
                .andExpect(jsonPath("$.countExpenses", is(2)))
                .andExpect(jsonPath("$.categoryAndCountList[0].category", is("STEUER")))
                .andExpect(jsonPath("$.categoryAndCountList[0].count", is(2)));
        //  .andExpect(jsonPath("$.categoryAndAmountList[0].name", is("STEUER")))
        //  .andExpect(jsonPath("$.categoryAndAmountList[0].amount", is(30)));


    }


}
