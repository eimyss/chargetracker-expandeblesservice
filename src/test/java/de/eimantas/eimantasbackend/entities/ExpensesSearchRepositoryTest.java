package de.eimantas.eimantasbackend.entities;

import de.eimantas.eimantasbackend.entities.Specification.ExpensesSpecification;
import de.eimantas.eimantasbackend.entities.Specification.SearchCriteria;
import de.eimantas.eimantasbackend.repo.ExpenseRepository;
import org.junit.Assert;
import org.junit.Before;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class ExpensesSearchRepositoryTest {

    private MockMvc mockMvc;
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());


    private List<Expense> expensesList = new ArrayList<>();

    @Autowired
    private ExpenseRepository expensesRepository;


    @Autowired
    private WebApplicationContext webApplicationContext;


    @Before
    public void setup() throws Exception {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();

        this.expensesRepository.deleteAll();

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
        // this.expensesList.add(expensesRepository.save(exp2));

    }


    @Test
    public void contexLoads() throws Exception {
        assertThat(expensesRepository).isNotNull();
    }

    @Test
    public void searchBasic() throws Exception {
        ExpensesSpecification spec =
                new ExpensesSpecification(new SearchCriteria("ort", ":", "Bingen"));
        List<Expense> results = expensesRepository.findAll(spec);
        Assert.assertThat(results.size(), greaterThan(0));
    }

    @Test
    public void searchMultipleBasic() throws Exception {
        ExpensesSpecification spec =
                new ExpensesSpecification(new SearchCriteria("ort", ":", "Bingen"));
        ExpensesSpecification spec1 =
                new ExpensesSpecification(new SearchCriteria("name", ":", "test-2"));
        List<Expense> results = expensesRepository.findAll(spec1.and(spec));
        Assert.assertThat(results.size(), greaterThan(0));
    }


    @Test
    public void searchMultipleLike() throws Exception {
        ExpensesSpecification spec =
                new ExpensesSpecification(new SearchCriteria("ort", ":", "Bin"));
        List<Expense> results = expensesRepository.findAll(spec);
        Assert.assertThat(results.size(), greaterThan(0));
    }


    @Test
    public void searchNonExistant() throws Exception {
        ExpensesSpecification spec =
                new ExpensesSpecification(new SearchCriteria("ort", ":", "FFM"));
        List<Expense> results = expensesRepository.findAll(spec);
        Assert.assertThat(results.size(), equalTo(0));
    }


    @Test
    public void searchBasicWithUser() throws Exception {
        ExpensesSpecification spec =
                new ExpensesSpecification(new SearchCriteria("user.id", "=", 1L));
        List<Expense> results = expensesRepository.findAll(spec);
        Assert.assertThat(results.size(), greaterThan(0));
    }


    @Test
    public void searchBasicWitouthUser() throws Exception {
        ExpensesSpecification spec =
                new ExpensesSpecification(new SearchCriteria("ort", ":", (1L + 2)));
        List<Expense> results = expensesRepository.findAll(spec);
        Assert.assertThat(results.size(), equalTo(0));
    }


}
