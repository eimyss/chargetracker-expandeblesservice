package de.eimantas.eimantasbackend.service;

import de.eimantas.eimantasbackend.client.AccountsClient;
import de.eimantas.eimantasbackend.client.BookingsClient;
import de.eimantas.eimantasbackend.controller.exceptions.BadRequestException;
import de.eimantas.eimantasbackend.controller.exceptions.NonExistingEntityException;
import de.eimantas.eimantasbackend.entities.AccountOverView;
import de.eimantas.eimantasbackend.entities.Expense;
import de.eimantas.eimantasbackend.entities.ExpenseCategory;
import de.eimantas.eimantasbackend.entities.Specification.ExpensesSpecification;
import de.eimantas.eimantasbackend.entities.Specification.SearchCriteria;
import de.eimantas.eimantasbackend.entities.converter.EntitiesConverter;
import de.eimantas.eimantasbackend.entities.dto.AccountOverViewDTO;
import de.eimantas.eimantasbackend.entities.dto.AllAccountsOverViewDTO;
import de.eimantas.eimantasbackend.entities.dto.CategoryAndCountOverview;
import de.eimantas.eimantasbackend.entities.dto.ExpenseDTO;
import de.eimantas.eimantasbackend.messaging.ExpensesSender;
import de.eimantas.eimantasbackend.processing.OverviewProcessor;
import de.eimantas.eimantasbackend.repo.ExpenseRepository;
import org.json.JSONException;
import org.json.JSONObject;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


@Service
public class ExpensesService {

  @Inject
  SecurityService securityService;
  @Inject
  ExpenseRepository expenseRepository;
  @Inject
  BookingsClient bookingsClient;

  @Inject
  ExpensesSender expensesSender;
  @Inject
  OverviewProcessor processor;

  @Inject
  AccountsClient accountsClient;

  @Inject
  EntitiesConverter converter;

  private int monthsGoBack = 6;


  private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

  public List<Expense> searchExpensesForUser(String searchString, KeycloakAuthenticationToken authentication) {

    if (searchString == null || searchString.equals("")) {
      logger.info("empty search string provided");
      return Collections.emptyList();
    }

    ExpensesSpecification specForUser =
        new ExpensesSpecification(new SearchCriteria("user.id", "=", securityService.getUserIdFromPrincipal(authentication)));

    ExpensesSpecification specForSearch =
        new ExpensesSpecification(new SearchCriteria("name", ":", searchString));

    return expenseRepository.findAll(specForSearch.and(specForUser));

  }

  public Optional<AccountOverView> getOverViewForAccount(long id, KeycloakAuthenticationToken authentication) {

    if (authentication == null) {
      throw new SecurityException("auth cannot be null!");
    }

    if (!securityService.isAllowedToReadAcc(authentication, id)) {
      throw new SecurityException("this user cannot read the account info!");
    }

    Collection<Expense> expensesList = expenseRepository.findByAccountIdAndUserId(id, securityService.getUserIdFromPrincipal(authentication));
    return processor.getOverViewForAccount(expensesList, id);
  }

  // we still need account id, because in dropbox you can select multiple accunts
  public Optional<AccountOverViewDTO> getExpensesOverview(long accountID, KeycloakAuthenticationToken authentication) {
    return processor.getExpensesOverview(accountID, authentication);

  }

  public BigDecimal getTotalAmountForAcc(long id, KeycloakAuthenticationToken authentication) {

    if (id == 0) {
      logger.warn("empty id was passed for getting totals");
      return BigDecimal.ZERO;
    }

    Collection<Expense> expensesList = expenseRepository.findByAccountIdAndUserId(id, securityService.getUserIdFromPrincipal(authentication));
    return processor.getTotalAmountForExpenses(expensesList);

  }


  public int getExpensesCountForAcc(long accountID, KeycloakAuthenticationToken authentication) {
    if (accountID == 0L) {
      logger.info("count was invoked for zero account id");
      return 0;
    }
    return expenseRepository.selectCountForAccount(accountID, securityService.getUserIdFromPrincipal(authentication));
  }

  public List<CategoryAndCountOverview> getCategoryAndCountForAcc(long accountID, KeycloakAuthenticationToken authentication) {
    return expenseRepository.findCategoryAndCount(accountID, securityService.getUserIdFromPrincipal(authentication));

  }

  public List<ExpenseDTO> getExpensesForAccount(long accountId, KeycloakAuthenticationToken authentication) {

    if (accountId == 0L) {
      logger.info("count was invoked for zero account id");
      return Collections.EMPTY_LIST;
    }
    Collection<Expense> expenses = expenseRepository.findByAccountIdAndUserId(accountId, securityService.getUserIdFromPrincipal(authentication));
    return converter.convertExpenses(expenses);

  }

  public Iterable<Expense> saveAll(List<Expense> expenses) {
    return expenseRepository.saveAll(expenses);
  }

  public Expense getExpenseById(long id, KeycloakAuthenticationToken authentication) {
    return expenseRepository.findByIdAndUserId(id, securityService.getUserIdFromPrincipal(authentication));
  }

  public Expense save(Expense expense) throws NonExistingEntityException {

    boolean updated = false;
    logger.info("Saving expense");
    if (expense == null) {
      throw new IllegalArgumentException("Expense cannot be null");
    }


    if (expense.getId() != null) {
      logger.info("Expense is updated");
      if (!expenseRepository.existsById(expense.getId())) {
        logger.warn("Expense with id: " + expense.getId() + " doesnt exist!");
        throw new NonExistingEntityException("There is no expense for update with id: " + expense.getId());
      }

      updated = true;
      expense.setUpdateDate(LocalDate.now());
    } else {
      logger.info("expense is new!");
      expense.setCreateDate(Instant.now());
    }

    Expense expenseSaved = expenseRepository.save(expense);


    logger.info("Notifiying about created expense");
    if (!updated) {
      notifyCreatedExpense(expense.getId());
    }
    return expenseRepository.save(expense);

  }

  public void notifyCreatedExpense(Long id) {
    expensesSender.notifyCreatedExpense(id);
  }

  public Collection<Expense> findByUserId(KeycloakAuthenticationToken authentication) {
    return expenseRepository.findByUserId(securityService.getUserIdFromPrincipal(authentication));
  }

  public Collection<Expense> findByAcc(long id, KeycloakAuthenticationToken authentication) {
    return expenseRepository.findByAccountIdAndUserId(id, securityService.getUserIdFromPrincipal(authentication));
  }

  public List<Expense> searchExpensesInPeriod(Instant from, Instant to,
                                              KeycloakAuthenticationToken authentication) throws BadRequestException {

    if (from == null || to == null) {
      throw new BadRequestException("From and to cannot be null");
    }

    logger.info("Getting expenses from :" + from.toString() + " to " + to.toString());
    List<Expense> expenses = expenseRepository.findByCreateDateBetweenAndUserId(from, to,
        securityService.getUserIdFromPrincipal(authentication));
    logger.info("found:" + expenses.size() + " expenses");
    return expenses;

  }

  public AllAccountsOverViewDTO getAllACccountsOverViewForUser(KeycloakAuthenticationToken principal) {

    if (principal == null) {
      throw new SecurityException("Principal cannot be null");
    }

    String userId = securityService.getUserIdFromPrincipal(principal);
    logger.info("obtained principal " + principal.getName() + "for user id: " + userId);

    return processor.getAllACccountsOverViewForUser(principal, accountsClient.getAccountList(), monthsGoBack);

  }

  public void createExpenseFromBooking(JSONObject json) {

    try {

      int bookingId = json.getInt("booking_id");
      BigDecimal amount = new BigDecimal(json.getString("Amount"));
      int refAccountId = json.getInt("refAccountId");
      String userId = json.getString("UserId");

      logger.info("creating expense from booking: " + bookingId);
      Expense expense = new Expense();
      expense.setCreateDate(Instant.now());
      expense.setValid(true);
      expense.setUserId(userId);
      expense.setCurrency("EUR");
      expense.setForecasted(true);
      expense.setExpensable(false);
      expense.setCategory(ExpenseCategory.GENERATED_NOT_CALCULATE);
      expense.setOrt("Project Place");
      expense.setAccountId(refAccountId);
      expense.setBetrag(amount);

      logger.info("Saving expense: " + expense.toString());
      expenseRepository.save(expense);

    } catch (JSONException e) {
      logger.error("Failed to parse Json from Processed booking message: ", e.getMessage());
      e.printStackTrace();
    }

  }

  public Stream<Expense> findExpensesInPeriodForAccount(long accountID, Instant start,
                                                        Instant end, KeycloakAuthenticationToken token) {
    return expenseRepository.findExpensesInPeriodForAccount(accountID, start, end,
        securityService.getUserIdFromPrincipal(token));
  }

  public Collection<Expense> findByAccountId(Long accId, KeycloakAuthenticationToken token) {
    return expenseRepository.findByAccountIdAndUserId(accId,
        securityService.getUserIdFromPrincipal(token));
  }

  public void updateExpenseToProcessed(JSONObject json) {

    long transactionId = parseInt(json, "transactionId");
    long entityId = parseInt(json, "refEntityId");
    logger.info("Updating expense witth id : " + entityId);
    Optional<Expense> expense = expenseRepository.findById(entityId);
    if (expense.isPresent()) {
      Expense exp = expense.get();
      exp.setProcessed(true);
      exp.setTransactionId(transactionId);
      expenseRepository.save(exp);
    } else {
      logger.warn("Expense with id :" + entityId + "is not present, " +
          "but was notified for added in account. transaction id: " + transactionId);
    }

  }


  private int parseInt(JSONObject json, String key) {
    try {
      return json.getInt(key);
    } catch (JSONException e) {
      logger.info("error getting  int for key ", e);
      e.printStackTrace();
    }
    return 0;
  }

  public int getExpensesCountForUser(long accountID, KeycloakAuthenticationToken authentication) {
    if (accountID == 0L) {
      logger.info("count was invoked for zero account id");
      return 0;
    }
    return expenseRepository.selectCountByUserId(securityService.getUserIdFromPrincipal(authentication));
  }
}
