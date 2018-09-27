package de.eimantas.eimantasbackend.controller;

import de.eimantas.eimantasbackend.controller.exceptions.BadRequestException;
import de.eimantas.eimantasbackend.controller.exceptions.ErrorDesc;
import de.eimantas.eimantasbackend.controller.exceptions.NonExistingEntityException;
import de.eimantas.eimantasbackend.entities.AccountOverView;
import de.eimantas.eimantasbackend.entities.Expense;
import de.eimantas.eimantasbackend.entities.ExpenseCategory;
import de.eimantas.eimantasbackend.entities.converter.EntitiesConverter;
import de.eimantas.eimantasbackend.entities.dto.AccountOverViewDTO;
import de.eimantas.eimantasbackend.entities.dto.AllAccountsOverViewDTO;
import de.eimantas.eimantasbackend.entities.dto.CSVExpenseDTO;
import de.eimantas.eimantasbackend.entities.dto.ExpenseDTO;
import de.eimantas.eimantasbackend.helpers.PopulateStuff;
import de.eimantas.eimantasbackend.messaging.ExpensesSender;
import de.eimantas.eimantasbackend.processing.FileProcessor;
import de.eimantas.eimantasbackend.service.ExpensesService;
import de.eimantas.eimantasbackend.service.SecurityService;
import io.swagger.annotations.ApiResponse;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/expense")
public class ExpenseController {
  private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());


  @Inject
  SecurityService securityService;

  @Inject
  ExpensesSender expensesSender;

  @Inject
  ExpensesService expensesService;

  @Inject
  private FileProcessor dataProcessor;

  @Inject
  EntitiesConverter entitiesConverter;


  public ExpenseController() {
  }

  @GetMapping(value = "/get/all", produces = MediaType.APPLICATION_JSON_VALUE)
  @CrossOrigin(origins = "*")
  public Collection<ExpenseDTO> getExpenses(Principal principal) {

    logger.info(String.format("returning count: %d",
        StreamSupport.stream(expensesService.findAll().spliterator(), false)
            .collect(Collectors.toList()).size()));
    return StreamSupport.stream(expensesService.findAll().spliterator(), false)
        .map(e -> entitiesConverter.getExpenseDTO(e))
        .collect(Collectors.toList());
  }

  @GetMapping(value = "/user-expenses", produces = MediaType.APPLICATION_JSON_VALUE)
  @CrossOrigin(origins = "*")
  @Transactional
  public Collection<ExpenseDTO> getStuffFromUser(Principal principal) {

    KeycloakAuthenticationToken userAuth = (KeycloakAuthenticationToken) principal;

    String userId = securityService.getUserIdFromPrincipal(userAuth);

    logger.info("requesting expenses for user: " + userId);
    logger.info(String.format("returning count: %d",
        StreamSupport.stream(expensesService.findByUserId(userId).spliterator(), false)
            .collect(Collectors.toList()).size()));

    return StreamSupport.stream(expensesService.findByUserId(userId).spliterator(), false)
        .map(e -> entitiesConverter.getExpenseDTO(e))
        .collect(Collectors.toList());

  }


  @GetMapping("/get/{id}")
  @CrossOrigin(origins = "*")
  public ExpenseDTO getExpenseById(@PathVariable long id) throws NonExistingEntityException {
    logger.info("get expense for id: " + id);

    Optional<Expense> expenseOptional = expensesService.findById(id);

    if (!expenseOptional.isPresent()) {
      logger.warn("expense with id : " + id + "is not present");
      throw new NonExistingEntityException("Expense by id: '" + id + "' is null");
    }
    logger.info("expense found!");
    return entitiesConverter.getExpenseDTO(expenseOptional.get());
  }


  @GetMapping("/account/{accountId}")
  @CrossOrigin(origins = "*")
  public List<ExpenseDTO> getExpenseByAccount(@PathVariable long accountId) {
    logger.info("get expense for account id: " + accountId);

    List<ExpenseDTO> expenses = expensesService.getExpensesForAccount(accountId);
    logger.info("returning " + expenses.size() + " expsenses");
    return expenses;
  }


  @GetMapping(value = "/populate/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @CrossOrigin(origins = "*")
  @Transactional
  public List<ExpenseDTO> populateExpenses(Principal principal, @PathVariable long accountId) {
    KeycloakAuthenticationToken userAuth = (KeycloakAuthenticationToken) principal;

    String userId = securityService.getUserIdFromPrincipal(userAuth);

    List<Expense> expenses = new ArrayList<>();

    // populate acc with expenses
    int monthsGoBack = 8;
    int expensesForMonth = 8;
    for (int i = 0; i < monthsGoBack; i++) {
      // whats ussual second name for iteration?
      for (int y = 0; y < expensesForMonth; y++) {
        Expense e = PopulateStuff.getExpense(i);
        e.setUserId(userId);
        e.setAccountId(accountId);
        expenses.add(e);
      }
    }

    Iterable<Expense> expensesSaved = expensesService.saveAll(expenses);
    logger.info("succesfully populated stuff");
    List<Expense> savedList = new ArrayList<Expense>();
    expensesSaved.forEach(savedList::add);
    logger.info("populated :" + savedList.size() + " expenses");

    return entitiesConverter.convertExpenses(savedList);
  }

  @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
  @CrossOrigin(origins = "*")
  public Collection<ExpenseDTO> searchExpenses(Principal principal, @RequestParam("name") String name) {

    KeycloakAuthenticationToken userAuth = (KeycloakAuthenticationToken) principal;
    String userId = securityService.getUserIdFromPrincipal(userAuth);
    logger.info("Getting search for string: " + name);
    List<Expense> foundexpenses = expensesService.searchExpensesByName(name);
    logger.info("found " + foundexpenses.size() + " for string" + name);
    return StreamSupport.stream(foundexpenses.spliterator(), false)
        .map(e -> entitiesConverter.getExpenseDTO(e))
        .collect(Collectors.toList());

  }


  @GetMapping(value = "/get/period", produces = MediaType.APPLICATION_JSON_VALUE)
  @CrossOrigin(origins = "*")
  public Collection<ExpenseDTO> getExpensesInPeriod(Principal principal, @RequestParam("from")
  @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
                                                    @RequestParam("to") @DateTimeFormat(pattern = "yyyy-MM-dd") Date toDate)
      throws BadRequestException {

    if (fromDate == null || toDate == null) {
      throw new BadRequestException("From and To Dates cannot be null!");
    }

    logger.info("getting expenses in period from " + fromDate.toString()
        + " to " + toDate.toString());
    // prep for future
    KeycloakAuthenticationToken userAuth = (KeycloakAuthenticationToken) principal;
    String userId = securityService.getUserIdFromPrincipal(userAuth);
    logger.info("Requested for user: " + userId);
    List<Expense> expenses = expensesService.searchExpensesInPeriod(fromDate.toInstant(), toDate.toInstant());

    logger.info("found: '" + expenses.size() + "' expenses");

    return entitiesConverter.convertExpenses(expenses);


  }


  @GetMapping(value = "/types", produces = MediaType.APPLICATION_JSON_VALUE)
  @CrossOrigin(origins = "*")
  public Collection<ExpenseCategory> getTypes() {

    logger.info("getting types for expenses");
    return Arrays.asList(ExpenseCategory.values());

  }


  @GetMapping(value = "/csv/read/{accountId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @CrossOrigin(origins = "*")
  @Transactional
  public Collection<ExpenseDTO> getImportedExpenses(Principal principal, @PathVariable long accountId) throws BadRequestException {


    KeycloakAuthenticationToken userAuth = (KeycloakAuthenticationToken) principal;
    String userId = securityService.getUserIdFromPrincipal(userAuth);
    logger.info("got user: " + userId);


    logger.info("reading csv list");
    List<CSVExpenseDTO> dtos = dataProcessor.loadObjectList(CSVExpenseDTO.class, "data.csv");
    logger.info("size read: " + dtos.size());
    List<ExpenseDTO> dtosconverted = entitiesConverter.convertCSVExpenses(dtos);
    logger.info(" dtosconverted size: " + dtosconverted.size());

    dtosconverted.forEach(dto -> {
      dto.setAccountId(accountId);
      dto.setUserId(userId);
      dto.setExpensable(true);
      dto.setValid(true);
      dto.setName(dto.getPurpose());
      dto.setCategory("IMPORTED");
    });

    logger.info("Account is: " + accountId);

    logger.info("adding account");
    List<Expense> converted = entitiesConverter.convertExpenses(dtosconverted);
    converted.forEach(expense -> {
      expense.setAccountId(accountId);
    });

    logger.info("saving expenses");
    Iterable<Expense> saved = expensesService.saveAll(converted);

    logger.info("imported files saved: count: " + converted.size());
    // not really efficient, but at least we know what actually is saved
    return StreamSupport.stream(saved.spliterator(), true).map(expense -> entitiesConverter.getExpenseDTO(expense)).collect(Collectors.toList());

  }

  @PostMapping("/add")
  @CrossOrigin(origins = "*")
  @Transactional
  public ExpenseDTO persistExpense(Principal principal, @RequestBody ExpenseDTO expense) throws BadRequestException, NonExistingEntityException {

    if (expense == null) {
      logger.warn("expense is null in post");
      throw new BadRequestException("expense is null");
    }

    if (expense.getId() != null && expense.getId() > 0) {
      logger.warn("trying to post expense with ID");
      throw new BadRequestException("cannot save a new expense with id!");

    }

    logger.info("creating expense: " + expense.toString());

    KeycloakAuthenticationToken userAuth = (KeycloakAuthenticationToken) principal;
    String userId = securityService.getUserIdFromPrincipal(userAuth);

    logger.info("setting epxense for userid: " + userId);


    logger.info("Publishing expenses to messaging");
    //     boolean sent = expensesSender.send(expense);
    //logger.info("expense is sent: " +sent);


    //expense.setUserId(user.getId());
    Expense exp = entitiesConverter.getExpenseFromDTO(expense);
    exp.setUserId(userId);
    Expense response = expensesService.save(exp);
    logger.info("Expense is saved: " + response.toString());
    ExpenseDTO saved = entitiesConverter.getExpenseDTO(response);


    return saved;

  }

  @PutMapping("/add")
  @CrossOrigin(origins = "*")
  @Transactional
  public ExpenseDTO updateExpense(Principal principal, @RequestBody ExpenseDTO expense) throws BadRequestException, NonExistingEntityException {

    if (expense == null) {
      logger.warn("expense is null in post");
      throw new BadRequestException("expense is null");
    }

    logger.info("update expense: " + expense.toString());

    KeycloakAuthenticationToken userAuth = (KeycloakAuthenticationToken) principal;
    String userId = securityService.getUserIdFromPrincipal(userAuth);

    logger.info("setting epxense for userid: " + userId);

    logger.info("checking account");

    if (expense.getId() == null) {
      logger.warn("trying to update new expense");
      throw new BadRequestException("cannot update an expense without ID");

    }

    if (expense.getAccountId() == null || expense.getAccountId() == 0) {
      logger.warn("no account id is set for dto");
      throw new BadRequestException("cannot update an expense without account");

    }

    Expense exp = entitiesConverter.getExpenseFromDTO(expense);
    Expense response = expensesService.save(exp);

    logger.info("Expense is updated: " + response.toString());

    return entitiesConverter.getExpenseDTO(response);

  }

  @GetMapping(value = "/overview/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @CrossOrigin(origins = "*")
  public AccountOverView getAccOverview(Principal principal, @PathVariable long id) throws NonExistingEntityException {


    KeycloakAuthenticationToken userAuth = (KeycloakAuthenticationToken) principal;

    Optional<AccountOverView> overview = expensesService.getOverViewForAccount(id, userAuth);

    if (!overview.isPresent()) {
      throw new NonExistingEntityException("Entity is not found with id: " + id);
    }

    return overview.get();

  }


  @GetMapping(value = "/global-overview", produces = MediaType.APPLICATION_JSON_VALUE)
  @CrossOrigin(origins = "*")
  @Transactional
  @ApiResponse(response = AllAccountsOverViewDTO.class, message = "Returns overiew for all accounts", code = 200)
  public AllAccountsOverViewDTO getGlobalOverview(Principal principal) {

    KeycloakAuthenticationToken user = (KeycloakAuthenticationToken) principal;
    AllAccountsOverViewDTO dto = expensesService.getAllACccountsOverViewForUser(user);
    return dto;

  }


  @GetMapping(value = "/overview/expenses/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @CrossOrigin(origins = "*")
  public AccountOverViewDTO getExpensesOverview(Principal principal, @PathVariable long id) {


    KeycloakAuthenticationToken user = (KeycloakAuthenticationToken) principal;
    Optional<AccountOverViewDTO> overview = expensesService.getExpensesOverview(id, user);
    return overview.get();
  }

  @ExceptionHandler(BadRequestException.class)
  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  public @ResponseBody
  ErrorDesc handleException(BadRequestException e) {
    return new ErrorDesc(e.getMessage());
  }

  @ExceptionHandler(NonExistingEntityException.class)
  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  public @ResponseBody
  ErrorDesc handleException(NonExistingEntityException e) {
    return new ErrorDesc(e.getMessage());
  }


}