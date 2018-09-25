package de.eimantas.eimantasbackend;

import de.eimantas.eimantasbackend.entities.Expense;
import de.eimantas.eimantasbackend.entities.ExpenseCategory;
import de.eimantas.eimantasbackend.entities.dto.ExpenseDTO;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneOffset;

public class TestUtils {


  public static Expense getExpense() {

    Expense exp = new Expense();
    exp.setName("uploaded");
    exp.setCategory(ExpenseCategory.STEUER);
    exp.setBetrag(BigDecimal.TEN);
    exp.setOrt("Mainz");
    exp.setCreateDate(Instant.now());
    return exp;

  }


  public static Expense getFullExpense() {
    Expense exp = new Expense();
    exp.setName("uploaded");
    exp.setCategory(ExpenseCategory.STEUER);
    exp.setBetrag(BigDecimal.TEN);
    exp.setExpensable(true);
    exp.setCreateDate(Instant.now());
    exp.setOrt("Mainz");
    exp.setAccountId(1L);
    exp.setUserId("1L");

    return exp;
  }


  public static ExpenseDTO getExpenseDTO(long accountId) {

    ExpenseDTO exp = getExpenseDTO();
    exp.setAccountId(accountId);
    return exp;
  }


  public static ExpenseDTO getExpenseDTO() {

    ExpenseDTO exp = new ExpenseDTO();
    exp.setName("uploaded");
    exp.setCategory(ExpenseCategory.STEUER.toString());
    exp.setBetrag(BigDecimal.TEN);
    exp.setExpensable(true);
    exp.setExpensed(false);
    exp.setPeriodic(false);
    exp.setCreateDate(Instant.now());
    exp.setOrt("Mainz");

    return exp;

  }


  public static ExpenseDTO getExpenseDTO(Long accountID) {

    ExpenseDTO exp = getExpenseDTO();
    exp.setAccountId(accountID);

    return exp;

  }


  public static Expense getExpense(int monthAgo) {
    Expense e = getExpense();
    LocalDate date = LocalDate.now().minus(Period.ofMonths(monthAgo));
    e.setCreateDate(date.atStartOfDay().toInstant(ZoneOffset.UTC));
    return e;
  }


}
