package de.eimantas.eimantasbackend.entities.dto;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ApiModel(description = "")
public class MonthAndAmountOverview {

    private String month;
    private BigDecimal amount;
}
