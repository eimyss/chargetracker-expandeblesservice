package de.eimantas.eimantasbackend.processing;

import de.eimantas.eimantasbackend.entities.Expense;
import de.eimantas.eimantasbackend.entities.converter.EntitiesConverter;
import de.eimantas.eimantasbackend.entities.dto.AllAccountsOverViewDTO;
import de.eimantas.eimantasbackend.entities.dto.MonthAndAmountOverview;
import de.eimantas.eimantasbackend.helpers.DateHelper;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class OverviewProcessor {


  private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

  @Inject
  private EntitiesConverter entitiesConverter;


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

    List<Expense> unexpenced = qualified.stream().filter(new Predicate<Expense>() {
      @Override
      public boolean test(Expense expense) {
        return !expense.isExpensed();
      }
    }).collect(Collectors.toList());

    return unexpenced;

  }

  private List<Expense> getQualifiedExpenses(int month, Stream<Expense> expensesStream) {

    // get expenses that are fitting the date
    List<Expense> qualified = expensesStream.filter(new Predicate<Expense>() {
      @Override
      public boolean test(Expense expense) {
        return DateHelper.isInMonth(month, expense.getCreateDate());
      }
    }).collect(Collectors.toList());

    return qualified;
  }


  public BigDecimal getTotalAmountForExpenses(Collection<Expense> expenses) {

    if (expenses.size() > 0) {
      // sum of all values that are already expensed
      BigDecimal sum = expenses.stream().filter(expense -> !expense.isExpensed()).map(Expense::getBetrag).reduce(BigDecimal::add).get();
      return sum;
    }

    return BigDecimal.ZERO;

  }

}
