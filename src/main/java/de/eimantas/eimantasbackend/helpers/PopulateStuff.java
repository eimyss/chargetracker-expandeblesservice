package de.eimantas.eimantasbackend.helpers;

import de.eimantas.eimantasbackend.entities.Expense;
import de.eimantas.eimantasbackend.entities.ExpenseCategory;
import de.eimantas.eimantasbackend.entities.dto.ExpenseDTO;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneOffset;

public class PopulateStuff {


  public static final String TEST_USER_ID ="9a204126-12b9-4efe-9d9b-3808aba51ba3";

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
    exp.setUserId("asdasd-12321");

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
    exp.setValid(false);
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
