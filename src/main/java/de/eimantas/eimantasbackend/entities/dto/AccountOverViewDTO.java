package de.eimantas.eimantasbackend.entities.dto;


import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ApiModel(description = "")
public class AccountOverViewDTO {

  private Long id;
  private Long refAccountId;
  private String accountName;
  private int totalExpensesCount;
  private BigDecimal total;
  private boolean active;
  private int countExpenses;
  private Instant createDate;
  private List<CategoryAndCountOverview> categoryAndCountList;
  private List<CategoryAndAmountOverview> categoryAndAmountList;


}
