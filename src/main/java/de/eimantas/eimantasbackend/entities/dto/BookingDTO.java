package de.eimantas.eimantasbackend.entities.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.eimantas.eimantasbackend.entities.serializer.CustomLocalDateDeSerializer;
import de.eimantas.eimantasbackend.entities.serializer.CustomLocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class BookingDTO {
  private int id;
  private String name;
  @JsonDeserialize(using = CustomLocalDateDeSerializer.class)
  @JsonSerialize(using = CustomLocalDateSerializer.class)
  private LocalDateTime startDate;
  @JsonDeserialize(using = CustomLocalDateDeSerializer.class)
  @JsonSerialize(using = CustomLocalDateSerializer.class)
  private LocalDateTime endDate;
  private int projectId;
  private int serverBookingId;
  private String userId;
}
