package de.eimantas.eimantasbackend.service;

import de.eimantas.eimantasbackend.client.AccountsClient;
import de.eimantas.eimantasbackend.controller.exceptions.BadRequestException;
import de.eimantas.eimantasbackend.controller.exceptions.NonExistingEntityException;
import de.eimantas.eimantasbackend.entities.AccountOverView;
import de.eimantas.eimantasbackend.entities.Expense;
import de.eimantas.eimantasbackend.entities.ExpenseCategory;
import de.eimantas.eimantasbackend.entities.Specification.ExpensesSpecification;
import de.eimantas.eimantasbackend.entities.Specification.SearchCriteria;
import de.eimantas.eimantasbackend.entities.converter.EntitiesConverter;
import de.eimantas.eimantasbackend.entities.dto.*;
import de.eimantas.eimantasbackend.helpers.DateHelper;
import de.eimantas.eimantasbackend.repo.ExpenseRepository;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Instant;
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
public class ExpensesService {

    @Inject
    SecurityService securityService;
    @Inject
    ExpenseRepository expenseRepository;

    @Inject
    AccountsClient accountsClient;

    @Inject
    EntitiesConverter converter;

    private int monthsGoBack = 6;


    private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

    public List<Expense> searchExpensesForUser(String searchString, long userId) {

        if (searchString == null || searchString.equals("")) {
            logger.info("empty search string provided");
            return Collections.emptyList();
        }

        ExpensesSpecification specForUser =
                new ExpensesSpecification(new SearchCriteria("user.id", "=", userId));

        ExpensesSpecification specForSearch =
                new ExpensesSpecification(new SearchCriteria("name", ":", searchString));

        return expenseRepository.findAll(specForSearch.and(specForUser));

    }

    public Optional<AccountOverView> getOverViewForAccount(long id, KeycloakAuthenticationToken authentication) {


        if (authentication == null)
            throw new SecurityException("auth cannot be null!");


        if (!securityService.isAllowedToReadAcc(authentication, id)) {
            throw new SecurityException("this user cannot read the account info!");
        }

        Collection<Expense> expensesList = expenseRepository.findByAccountId(id);

        AccountOverView overView = new AccountOverView();
        overView.setRefAccountId(id);
        overView.setCountExpenses(expensesList.size());
        overView.setActive(true);


        if(!expensesList.isEmpty()) {
            BigDecimal sum = expensesList.stream().filter(expense -> !expense.isExpensed()).map(Expense::getBetrag).reduce(BigDecimal::add).get();
            overView.setTotal(sum);
        }
        // sum of all values that are already expensed

        return Optional.of(overView);


    }


    public List<Expense> searchExpensesByName(String searchString) {

        if (searchString == null || searchString.equals("")) {
            logger.info("empty search string provided");
            return Collections.emptyList();
        }

        ExpensesSpecification specForSearch =
                new ExpensesSpecification(new SearchCriteria("name", ":", searchString));

        return expenseRepository.findAll(specForSearch);

    }

    // we still need account id, because in dropbox you can select multiple accunts
    public Optional<AccountOverViewDTO> getExpensesOverview(long accountID, KeycloakAuthenticationToken authentication) {

        // is active
        // total expenses
        // expensed account
        // account name
        // map<category, count>
        // map <period,  map<category, amount>>

        if (authentication == null)
            throw new SecurityException("Principal cannot be null");

        if (accountID == 0)
            throw new IllegalArgumentException("account Id cannot be empty");

        AccountOverViewDTO dto = new AccountOverViewDTO();
        dto.setRefAccountId(accountID);
        dto.setTotalExpensesCount(getExpensesCountForAcc(accountID));
        dto.setCountExpenses(getExpensesCountForAcc(accountID));
        List<CategoryAndCountOverview> stats = getCategoryAndCountForAcc(accountID);
        logger.info("got stats for account id: " + accountID + " size: " + stats.size());
        dto.setCategoryAndCountList(stats);

        dto.setTotal(getTotalAmountForAcc(accountID));

        // get current week
        LocalDate date = LocalDate.now();
        TemporalField woy = WeekFields.ISO.weekOfWeekBasedYear();
        int weekNumber = date.get(woy);

        LocalDateTime begin = DateHelper.getBeginOfWeek(weekNumber);
        LocalDateTime end = DateHelper.getEndOfWeek(weekNumber);

        Stream<Expense> expensesForWeek = expenseRepository.findExpensesInPeriodForAccount(accountID, begin.toInstant(ZoneOffset.UTC), end.toInstant(ZoneOffset.UTC));

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

    public BigDecimal getTotalAmountForAcc(long id) {


        if (id == 0) {
            logger.warn("emoty id was passed for getting totals");
            return BigDecimal.ZERO;
        }


        Collection<Expense> expensesList = expenseRepository.findByAccountId(id);

        if (expensesList.size() > 0) {
            // sum of all values that are already expensed
            BigDecimal sum = expensesList.stream().filter(expense -> !expense.isExpensed()).map(Expense::getBetrag).reduce(BigDecimal::add).get();
            return sum;
        }

        logger.info("no expenses found for acc id: " + id);
        return BigDecimal.ZERO;

    }


    public int getExpensesCountForAcc(long accountID) {
        // TODO check both repo methods
        if (accountID == 0L) {
            logger.info("count was invoked for zero account id");
            return 0;
        }
        return expenseRepository.selectCountForAccount(accountID);
    }

    public List<CategoryAndCountOverview> getCategoryAndCountForAcc(long accountID) {
        return expenseRepository.findCategoryAndCount(accountID);

    }

    public List<ExpenseDTO> getExpensesForAccount(long accountId) {

        if (accountId == 0L) {
            logger.info("count was invoked for zero account id");
            return Collections.EMPTY_LIST;
        }
        Collection<Expense> expenses = expenseRepository.findByAccountId(accountId);

        return converter.convertExpenses(expenses);

    }

    public Iterable<Expense> saveAll(List<Expense> expenses) {

        return expenseRepository.saveAll(expenses);
    }

    public Optional<Expense> getExpenseById(long id) {
        return expenseRepository.findById(id);
    }

    public Expense save(Expense expense) throws NonExistingEntityException {

        logger.info("Saving expense");

        if (expense == null)
            throw new IllegalArgumentException("Expense cannot be null");


        if (expense.getId() != null) {
            logger.info("Expense is updated");
            if (!expenseRepository.existsById(expense.getId())) {
                logger.warn("Expense with id: " + expense.getId() + " doesnt exist!");
                throw new NonExistingEntityException("There is no expense for update with id: " + expense.getId());
            }
            expense.setUpdateDate(LocalDate.now());
        } else {
            logger.info("expense is new!");
            expense.setCreateDate(Instant.now());
        }

        return expenseRepository.save(expense);

    }

    public Iterable<Expense> findAll() {
        return expenseRepository.findAll();
    }

    public Collection<Expense> findByUserId(String id) {
        return expenseRepository.findByUserId(id);
    }

    public Optional<Expense> findById(long id) {
        return expenseRepository.findById(id);
    }

    public List<Expense> searchExpensesInPeriod(Instant from, Instant to) throws BadRequestException {
        if (from == null || to == null)
            throw new BadRequestException("From and to cannot be null");

        logger.info("Getting expenses from :" + from.toString() + " to " + to.toString());

        List<Expense> expenses = expenseRepository.findByCreateDateBetween(from, to);

        logger.info("found:" + expenses.size() + " expenses");

        return expenses;

    }

    public AllAccountsOverViewDTO getAllACccountsOverViewForUser(KeycloakAuthenticationToken principal) {

        if (principal == null)
            throw new SecurityException("Principal cannot be null");

        String userId = securityService.getUserIdFromPrincipal(principal);
        logger.info("obtained principal " + principal.getName() + "for user id: " + userId);

        Map<Long, List<MonthAndAmountOverview>> overview = new HashMap<Long, List<MonthAndAmountOverview>>();
        AllAccountsOverViewDTO dto = new AllAccountsOverViewDTO();

        dto.setMonthBack(monthsGoBack);
        dto.setUserId(userId);
        accountsClient.getAccountList().forEach(acc -> overview.put(acc, getPerMonthOverView(monthsGoBack, acc, dto)));
        dto.setOverview(overview);

        return dto;
    }


    /**
     * Gets overview for expenses for given months back
     *
     * @param monthsGoBack
     * @param dto
     * @return
     */
    public List<MonthAndAmountOverview> getPerMonthOverView(int monthsGoBack, long accId, AllAccountsOverViewDTO dto) {
        // TODO

        logger.info("getting overview for acc: " + accId + " for manths ago: " + monthsGoBack);
        List<MonthAndAmountOverview> list = new ArrayList<>();

        Collection<Expense> expensesForAccount = expenseRepository.findByAccountId(accId);


        if (expensesForAccount != null) {
            logger.info("account has: " + expensesForAccount.size() + " expenses");

            // ist DB faster than the stuff ?
            for (int i = monthsGoBack; i > 0; i--) {
                List<Expense> qualified;
                List<Expense> unexpenced;

                Stream<Expense> expensesStream = expensesForAccount.stream();

                int finalI = i;
                // get expenses that are fitting the date
                qualified = expensesStream.filter(new Predicate<Expense>() {
                    @Override
                    public boolean test(Expense expense) {
                        return DateHelper.isInMonth(finalI, expense.getCreateDate());
                    }
                }).collect(Collectors.toList());

                logger.info("found " + qualified.size() + " qualified expenses");

                unexpenced = qualified.stream().filter(new Predicate<Expense>() {
                    @Override
                    public boolean test(Expense expense) {
                        return !expense.isExpensed();
                    }
                }).collect(Collectors.toList());

                logger.info("found " + unexpenced.size() + " not expensed expenses");

                if (dto != null) {
                    logger.warn("passed dto is null, expenses wont be added");
                    // we check for each month, and add if not expensed
                    dto.addNotExpenced(converter.convertExpenses(unexpenced));
                }

                MonthAndAmountOverview overview = new MonthAndAmountOverview();
                overview.setMonth(DateHelper.getMonthNameForAgo(finalI));
                if (qualified.size() > 0) {
                    // this doesnt work, array is empty.
                    overview.setAmount(qualified.stream().map(Expense::getBetrag).reduce(BigDecimal::add).get());
                }

                list.add(overview);
            }

        } else {
            logger.info("acc " + accId + " has no expenses");
        }
        return list;
    }
}
