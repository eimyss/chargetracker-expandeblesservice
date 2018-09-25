package de.eimantas.eimantasbackend.entities.dto;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;


@AllArgsConstructor
@NoArgsConstructor
@Data
@ApiModel(description = "")
public class ExpenseDTO {

  private Long id;
  private
  String name;
  private String ort;
  private String purpose;
  String userId;
  private Instant createDate;
  private LocalDate updateDate;
  private LocalDate bookingDate;
  private boolean expensed;
  private boolean expensable;
  private boolean valid;
  private boolean periodic;
  Long accountId;
  private BigDecimal betrag;
  private String currency;
  private String category;
  private boolean processed;
  private LocalDate processedDate;
}

