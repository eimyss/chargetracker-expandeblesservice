package de.eimantas.eimantasbackend.processing;

import de.eimantas.eimantasbackend.entities.AccountOverView;
import de.eimantas.eimantasbackend.entities.Expense;
import de.eimantas.eimantasbackend.entities.ExpenseCategory;
import de.eimantas.eimantasbackend.entities.converter.EntitiesConverter;
import de.eimantas.eimantasbackend.entities.dto.*;
import de.eimantas.eimantasbackend.helpers.DateHelper;
import de.eimantas.eimantasbackend.service.ExpensesService;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;


@Service
public class OverviewProcessor {


  private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

  @Inject
  private EntitiesConverter entitiesConverter;

  @Inject
  private ExpensesService expensesService;


  /**
   * Gets overview for expenses for given months back
   *
   * @param monthsGoBack
   * @param dto
   * @return
   */
  public List<MonthAndAmountOverview> getPerMonthOverView(int monthsGoBack, Collection<Expense> expensesForAccount, AllAccountsOverViewDTO dto) {
    // TODO

    logger.info("getting overview for acc: for manths ago: " + monthsGoBack);
    List<MonthAndAmountOverview> list = new ArrayList<>();

    if (expensesForAccount != null) {
      logger.info("account has: " + expensesForAccount.size() + " expenses");

      // ist DB faster than the stuff ?
      for (int i = monthsGoBack; i > 0; i--) {
        List<Expense> qualified;
        List<Expense> unexpenced;

        Stream<Expense> expensesStream = expensesForAccount.stream();
        qualified = getQualifiedExpenses(i, expensesStream);

        logger.info("found " + qualified.size() + " qualified expenses");

        unexpenced = getUnexpenced(qualified);

        logger.info("found " + unexpenced.size() + " not expensed expenses");

        if (dto != null) {
          // we check for each month, and add if not expensed
          dto.addNotExpenced(entitiesConverter.convertExpenses(unexpenced));
        }

        MonthAndAmountOverview overview = new MonthAndAmountOverview();
        overview.setMonth(DateHelper.getMonthNameForAgo(i));
        if (qualified.size() > 0) {
          // this doesnt work, array is empty.
          overview.setAmount(qualified.stream().map(Expense::getBetrag).reduce(BigDecimal::add).get());
        }
        list.add(overview);
      }

    } else {
      logger.info("there is no expenses");
    }
    return list;
  }

  private List<Expense> getUnexpenced(List<Expense> qualified) {

    return qualified.stream().filter(new Predicate<Expense>() {
      @Override
      public boolean test(Expense expense) {
        return !expense.isExpensed();
      }
    }).collect(Collectors.toList());

  }

  private List<Expense> getQualifiedExpenses(int month, Stream<Expense> expensesStream) {

    // get expenses that are fitting the date
    return expensesStream.filter(new Predicate<Expense>() {
      @Override
      public boolean test(Expense expense) {
        return DateHelper.isInMonth(month, expense.getCreateDate());
      }
    }).collect(Collectors.toList());

  }


  public BigDecimal getTotalAmountForExpenses(Collection<Expense> expenses) {

    if (expenses.size() > 0) {
      // sum of all values that are already expensed
      BigDecimal sum = expenses.stream().filter(expense -> !expense.isExpensed()).map(Expense::getBetrag).reduce(BigDecimal::add).get();
      return sum;
    }

    return BigDecimal.ZERO;

  }

  // we still need account id, because in dropbox you can select multiple accunts
  public Optional<AccountOverViewDTO> getExpensesOverview(long accountID, KeycloakAuthenticationToken authentication) {

    AccountOverViewDTO dto = new AccountOverViewDTO();
    dto.setRefAccountId(accountID);
    dto.setTotalExpensesCount(expensesService.getExpensesCountForUser(accountID, authentication));
    dto.setCountExpenses(expensesService.getExpensesCountForAcc(accountID, authentication));
    List<CategoryAndCountOverview> stats = expensesService.getCategoryAndCountForAcc(accountID, authentication);
    logger.info("got stats for account id: " + accountID + " size: " + stats.size());
    dto.setCategoryAndCountList(stats);

    dto.setTotal(expensesService.getTotalAmountForAcc(accountID, authentication));

    // get current week
    LocalDate date = LocalDate.now();
    TemporalField woy = WeekFields.ISO.weekOfWeekBasedYear();
    int weekNumber = date.get(woy);

    LocalDateTime begin = DateHelper.getBeginOfWeek(weekNumber);
    LocalDateTime end = DateHelper.getEndOfWeek(weekNumber);

    Stream<Expense> expensesForWeek = expensesService.findExpensesInPeriodForAccount(accountID,
        begin.toInstant(ZoneOffset.UTC), end.toInstant(ZoneOffset.UTC), authentication);

    Map<ExpenseCategory, List<Expense>> expensesPerType = expensesForWeek
        .collect(groupingBy(Expense::getCategory));

    logger.info("got expenses for type for acc id: " + accountID + " size: " + expensesPerType.size());

    List<CategoryAndAmountOverview> amountsForWeek = new ArrayList<>();

    // OMG IF THIS WORKS!!!!
    expensesPerType.forEach(
        ((key, value) -> {
          amountsForWeek.add(new CategoryAndAmountOverview(key, value.stream().map((x) -> x.getBetrag()).reduce((x, y) -> x.add(y)).get()));
        }));

    logger.info("got expenses amounts for week: for acc id: " + accountID + " size: " + expensesPerType.size());

    dto.setCategoryAndAmountList(amountsForWeek);

    return Optional.of(dto);


  }

  public AllAccountsOverViewDTO getAllACccountsOverViewForUser(KeycloakAuthenticationToken userId, Collection<Long> accountIds, int monthsGoBack) {

    logger.info("getAllACccountsOverViewForUser for user id: " + userId.toString());

    Map<Long, List<MonthAndAmountOverview>> overview = new HashMap<Long, List<MonthAndAmountOverview>>();
    AllAccountsOverViewDTO dto = new AllAccountsOverViewDTO();

    dto.setMonthBack(monthsGoBack);
    //dto.setUserId(userId);

    logger.info("got accounts List: " + accountIds.size());
    for (Long accId : accountIds) {
      Collection<Expense> expenses = expensesService.findByAccountId(accId, userId);
      logger.info("found " + expenses.size() + " for account id: " + accId);
      overview.put(accId, getPerMonthOverView(monthsGoBack, expenses, dto));
    }

    dto.setOverview(overview);

    return dto;
  }


  public Optional<AccountOverView> getOverViewForAccount(Collection<Expense> expensesList, long accId) {

    AccountOverView overView = new AccountOverView();
    overView.setRefAccountId(accId);
    overView.setCountExpenses(expensesList.size());
    overView.setActive(true);

    if (!expensesList.isEmpty()) {
      BigDecimal sum = expensesList.stream().filter(expense -> !expense.isExpensed()).map(Expense::getBetrag).reduce(BigDecimal::add).get();
      overView.setTotal(sum);
    }
    // sum of all values that are already expensed

    return Optional.of(overView);

  }


}
