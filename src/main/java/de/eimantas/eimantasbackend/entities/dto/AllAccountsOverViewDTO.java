package de.eimantas.eimantasbackend.entities.dto;


import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ApiModel(description = "")
public class AllAccountsOverViewDTO {

   private Map<Long, List<MonthAndAmountOverview>> overview;

   int monthBack;
   private LocalDateTime createDate;
   private String userId;


    List<ExpenseDTO> unexpenced;


    public void addNotExpenced(List<ExpenseDTO> expenseDTOS) {
        if (unexpenced == null)
            unexpenced = new ArrayList<>();
        unexpenced.addAll(expenseDTOS);
    }
}
