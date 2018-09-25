package de.eimantas.eimantasbackend.entities.converter;

import de.eimantas.eimantasbackend.entities.Expense;
import de.eimantas.eimantasbackend.entities.dto.CSVExpenseDTO;
import de.eimantas.eimantasbackend.entities.dto.ExpenseDTO;
import org.modelmapper.ModelMapper;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;


public class EntitiesConverter {

  @Autowired
  private ModelMapper modelMapper;

  private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());


  public ExpenseDTO getExpenseDTO(Optional<Expense> expenseOpt) {

    if (expenseOpt.isPresent()) {
      Expense expense = expenseOpt.get();
      ExpenseDTO postDto = modelMapper.<ExpenseDTO>map(expense, ExpenseDTO.class);
      return postDto;

    }
    logger.info("expense is null");
    return null;

  }


  public ExpenseDTO getExpenseDTO(Expense expense) {

    if (expense != null) {
      ExpenseDTO postDto = modelMapper.<ExpenseDTO>map(expense, ExpenseDTO.class);
      return postDto;
    }
    logger.info("expense is null");
    return null;

  }


  public Expense getExpenseFromDTO(ExpenseDTO dto) {

    if (dto != null) {
      Expense expense = modelMapper.<Expense>map(dto, Expense.class);
      return expense;
    }
    logger.info("expense is null");
    return null;
  }


  public List<ExpenseDTO> convertExpenses(Collection<Expense> unexpenced) {

    List<ExpenseDTO> converted = new ArrayList<>();

    unexpenced.forEach(exp -> converted.add(getExpenseDTO(exp)));
    return converted;
  }


  public ExpenseDTO getExpenseDTOFromCSV(CSVExpenseDTO expense) {

    if (expense != null) {
      ExpenseDTO postDto = modelMapper.<ExpenseDTO>map(expense, ExpenseDTO.class);
      return postDto;
    }
    logger.info("expense is null");
    return null;

  }


  public List<ExpenseDTO> convertCSVExpenses(List<CSVExpenseDTO> dtos) {
    List<ExpenseDTO> converted = new ArrayList<>();
    dtos.forEach(exp -> converted.add(getExpenseDTOFromCSV(exp)));
    return converted;
  }

  public List<Expense> convertExpenses(List<ExpenseDTO> dtosList) {

    List<Expense> converted = new ArrayList<>();
    dtosList.forEach(exp -> converted.add(getExpenseFromDTO(exp)));
    return converted;
  }
}
