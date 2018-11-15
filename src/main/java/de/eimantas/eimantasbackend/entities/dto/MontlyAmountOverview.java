package de.eimantas.eimantasbackend.entities.dto;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ApiModel(description = "")
public class MontlyAmountOverview {
  private Long accountId;
  private List<MonthAndAmountOverview> overviews;
}
