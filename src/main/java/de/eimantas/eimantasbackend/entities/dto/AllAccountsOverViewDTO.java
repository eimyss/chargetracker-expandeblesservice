package de.eimantas.eimantasbackend.entities.dto;


import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ApiModel(description = "")
public class AllAccountsOverViewDTO {

  private List<MontlyAmountOverview> overview;
  private List<ExpenseDTO> unexpenced;

  int monthBack;
  private LocalDateTime createDate;
  private String userId;


  public void addNotExpenced(List<ExpenseDTO> expenseDTOS) {
    if (unexpenced == null) {
      unexpenced = new ArrayList<>();
    }
    unexpenced.addAll(expenseDTOS);
  }
}
