package de.eimantas.eimantasbackend.entities.dto;

import de.eimantas.eimantasbackend.entities.ExpenseCategory;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ApiModel(description = "")
public class CategoryAndCountOverview {

  private ExpenseCategory category;
  private long count;

}
