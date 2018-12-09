package de.eimantas.eimantasbackend.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.eimantas.eimantasbackend.entities.serializer.CustomLocalDateDeSerializer;
import de.eimantas.eimantasbackend.entities.serializer.CustomLocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity(name = "booking")
public class Booking {

  private long id;
  private String name;
  @JsonDeserialize(using = CustomLocalDateDeSerializer.class)
  @JsonSerialize(using = CustomLocalDateSerializer.class)
  private LocalDateTime startDate;
  @JsonDeserialize(using = CustomLocalDateDeSerializer.class)
  @JsonSerialize(using = CustomLocalDateSerializer.class)
  private LocalDateTime endDate;
  private long workLocation;
  private long projectId;
  @Id
  @GeneratedValue
  private long serverBookingId;
  private String userId;
}
