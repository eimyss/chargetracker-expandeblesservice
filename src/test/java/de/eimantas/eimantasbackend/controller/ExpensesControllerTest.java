package de.eimantas.eimantasbackend.controller;

import de.eimantas.eimantasbackend.TestUtils;
import de.eimantas.eimantasbackend.client.AccountsClient;
import de.eimantas.eimantasbackend.entities.Expense;
import de.eimantas.eimantasbackend.entities.ExpenseCategory;
import de.eimantas.eimantasbackend.entities.converter.EntitiesConverter;
import de.eimantas.eimantasbackend.entities.dto.ExpenseDTO;
import de.eimantas.eimantasbackend.repo.ExpenseRepository;
import de.eimantas.eimantasbackend.service.ExpensesService;
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
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ExpensesControllerTest {

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());
    private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

    private MockMvc mockMvc;

    @SuppressWarnings("rawtypes")
    private HttpMessageConverter mappingJackson2HttpMessageConverter;


    private List<Expense> expensesList = new ArrayList<>();

    @Autowired
    private ExpenseRepository expensesRepository;

    @Autowired
    private ExpensesService expensesService;

    @Autowired
    private EntitiesConverter entitiesConverter;


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
        Mockito.when(mockPrincipal.getName()).thenReturn("test");

        KeycloakPrincipal keyPrincipal = Mockito.mock(KeycloakPrincipal.class);
        RefreshableKeycloakSecurityContext ctx = Mockito.mock(RefreshableKeycloakSecurityContext.class);

        AccessToken token = Mockito.mock(AccessToken.class);
        Mockito.when(token.getSubject()).thenReturn("1L");
        Mockito.when(ctx.getToken()).thenReturn(token);
        Mockito.when(keyPrincipal.getKeycloakSecurityContext()).thenReturn(ctx);
        Mockito.when(mockPrincipal.getPrincipal()).thenReturn(keyPrincipal);


    }

    @Test
    public void readSingleExpense() throws Exception {
        mockMvc.perform(get("/expense/get/" + this.expensesList.get(0).getId())).andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.id", is(this.expensesList.get(0).getId().intValue())))
                .andExpect(jsonPath("$.name", is("test"))).andExpect(jsonPath("$.category", is("DIESEL")));
    }

    @Test
    public void readExpenses() throws Exception {

        // given(controller.principal).willReturn(allEmployees);
        mockMvc.perform(get("/expense/get/all").principal(mockPrincipal)).andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print()).andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(this.expensesList.get(0).getId().intValue())))
                .andExpect(jsonPath("$[0].name", is("test"))).andExpect(jsonPath("$[0].category", is("DIESEL")))
                .andExpect(jsonPath("$[1].id", is(this.expensesList.get(1).getId().intValue())))
                .andExpect(jsonPath("$[1].category", is("ESSEN"))).andExpect(jsonPath("$[1].ort", is("Bingen")));
    }

    @Test
    @Ignore
    public void populateExpenses() throws Exception {

        mockMvc.perform(get("/expense/populate").principal(mockPrincipal)).andExpect(status().isCreated())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @Ignore
    public void searchExpenses() throws Exception {

        mockMvc.perform(get("/expense/search?name=tes").principal(mockPrincipal)).andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print()).andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(this.expensesList.get(0).getId().intValue())))
                .andExpect(jsonPath("$[0].name", is("test"))).andExpect(jsonPath("$[0].category", is("DIESEL")))
                .andExpect(jsonPath("$[1].id", is(this.expensesList.get(1).getId().intValue())))
                .andExpect(jsonPath("$[1].category", is("ESSEN"))).andExpect(jsonPath("$[1].ort", is("Bingen")));
    }


    @Test
    @Transactional
    public void createExpense() throws Exception {

        ExpenseDTO exp = TestUtils.getExpenseDTO();
        String bookmarkJson = json(exp);

        this.mockMvc.perform(post("/expense/add").principal(mockPrincipal).contentType(contentType).content(bookmarkJson))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print()).andExpect(content().contentType(contentType));
    }


    @Test
    @Transactional
    public void updateExpense() throws Exception {

        String name = "updated";
        ExpenseDTO exp = entitiesConverter.getExpenseDTO(expensesList.get(0));
        exp.setName(name);
        String bookmarkJson = json(exp);

        this.mockMvc.perform(put("/expense/add").principal(mockPrincipal).contentType(contentType).content(bookmarkJson))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print()).andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.id", is(this.expensesList.get(0).getId().intValue())))
                .andExpect(jsonPath("$.name", is(name))).andExpect(jsonPath("$.category", is("DIESEL")));
    }


    @Test
    @Transactional
    public void updateExpenseWrongId() throws Exception {

        String name = "updated";
        ExpenseDTO exp = entitiesConverter.getExpenseDTO(expensesList.get(0));
        exp.setName(name);
        exp.setId(1231L);
        String bookmarkJson = json(exp);

        this.mockMvc.perform(put("/expense/add").principal(mockPrincipal).contentType(contentType).content(bookmarkJson))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void updateExpenseNoId() throws Exception {

        String name = "updated";
        ExpenseDTO exp = entitiesConverter.getExpenseDTO(expensesList.get(0));
        exp.setName(name);
        exp.setId(null);
        String bookmarkJson = json(exp);

        this.mockMvc.perform(put("/expense/add").principal(mockPrincipal).contentType(contentType).content(bookmarkJson))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
    }


    @Test
    @Transactional
    public void createExpenseWithID() throws Exception {

        ExpenseDTO exp = TestUtils.getExpenseDTO();
        exp.setId(12L);

        String bookmarkJson = json(exp);

        this.mockMvc.perform(post("/expense/add").contentType(contentType).content(bookmarkJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void readOverview() throws Exception {
        // given(controller.principal).willReturn(allEmployees);
        mockMvc.perform(get("/expense/overview/" + 1).principal(mockPrincipal)).andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print()).andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.refAccountId", is(1)))
                .andExpect(jsonPath("$.total", is(20)))
                .andExpect(jsonPath("$.countExpenses", is(18)));

    }


    @Test
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

        // given(controller.principal).willReturn(allEmployees);
        mockMvc.perform(get("/expense/global-overview").principal(mockPrincipal)).andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print()).andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.monthBack", is(6)))
                .andExpect(jsonPath("$.userId", is("1L")))
                .andExpect(jsonPath("$.overview." + 1, hasSize(6)))
                .andExpect(jsonPath("$.unexpenced", hasSize((int) expensesRepository.count())));

    }

    @Test
    public void testGetExpensesOverview() throws Exception {

        // given(controller.principal).willReturn(allEmployees);
        mockMvc.perform(get("/expense/overview/expenses/" + 1).principal(mockPrincipal)).andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print()).andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.refAccountId", is(1)))
                .andExpect(jsonPath("$.totalExpensesCount", is(18)))
                .andExpect(jsonPath("$.total", is(180)))
                .andExpect(jsonPath("$.countExpenses", is(18)))
                .andExpect(jsonPath("$.categoryAndCountList[0].category", is("STEUER")))
                .andExpect(jsonPath("$.categoryAndCountList[0].count", is(18)));
        //  .andExpect(jsonPath("$.categoryAndAmountList[0].name", is("STEUER")))
        //  .andExpect(jsonPath("$.categoryAndAmountList[0].amount", is(30)));


    }


    @Test
    public void testGetAccountListNoAuth() throws Exception {

        mockMvc.perform(get("/expense/list")).andExpect(status().isForbidden())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @Ignore
    public void testGetGlobalOverviewNoAuth() throws Exception {

        mockMvc.perform(get("/expense/global-overview")).andExpect(status().isForbidden())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testGetExpensesOverviewNoID() throws Exception {

        // given(controller.principal).willReturn(allEmployees);
        mockMvc.perform(get("/expense/overview/expenses/0").principal(mockPrincipal)).andExpect(status().isBadRequest())
                .andDo(MockMvcResultHandlers.print());

    }

    @Test
    public void testGetExpensesOverviewEmptyID() throws Exception {


        // given(controller.principal).willReturn(allEmployees);
        mockMvc.perform(get("/expense/overview/expenses").principal(mockPrincipal)).andExpect(status().isBadRequest());

    }


    @Test
    public void testGetExpensesOverviewNoAuth() throws Exception {

        mockMvc.perform(get("/expense/overview/expenses/" + 1)).andExpect(status().isForbidden());

    }


}
