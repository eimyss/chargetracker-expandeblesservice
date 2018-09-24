package de.eimantas.eimantasbackend.entities.dto;

import de.eimantas.eimantasbackend.entities.ExpenseCategory;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ApiModel(description = "")
public class CategoryAndAmountOverview {

    private ExpenseCategory name;
    private BigDecimal amount;
}
