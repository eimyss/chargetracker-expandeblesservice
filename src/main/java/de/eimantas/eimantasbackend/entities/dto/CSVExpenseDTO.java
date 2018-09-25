package de.eimantas.eimantasbackend.entities.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.eimantas.eimantasbackend.processing.Deserializer.CustomInstantDeserializer;
import de.eimantas.eimantasbackend.processing.Deserializer.CustomLBigDecimalDeserializer;
import de.eimantas.eimantasbackend.processing.Deserializer.CustomLocalDateDeSerializer;
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
public class CSVExpenseDTO {

  private
  String name;
  private String ort;
  private String purpose;
  @JsonDeserialize(using = CustomInstantDeserializer.class)
  private Instant createDate;
  @JsonDeserialize(using = CustomLocalDateDeSerializer.class)
  private LocalDate bookingDate;
  @JsonDeserialize(using = CustomLBigDecimalDeserializer.class)
  private BigDecimal betrag;
  private String currency;
}

