package de.eimantas.eimantasbackend.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;


@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity(name = "expenses")
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "id")
public class Expense {
  @Id
  @GeneratedValue
  private Long id;
  private @NonNull
  @Column(length = 1000)
  String name;
  private String ort;
  private String userId;
  private Instant createDate;
  private boolean processed;
  private long transactionId;
  @Column(length = 1000)
  private String purpose;
  private String currency;
  private LocalDate bookingDate;
  private boolean expensed;
  private boolean valid;
  private LocalDate expensedDate;
  private boolean forecasted;
  private boolean expensable;
  private boolean periodic;
  long accountId;
  private BigDecimal betrag;
  @Enumerated(EnumType.STRING)
  private ExpenseCategory category;
  private LocalDate updateDate;
}