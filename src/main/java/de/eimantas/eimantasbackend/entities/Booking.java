package de.eimantas.eimantasbackend.entities;

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

  private int id;
  private String name;
  private LocalDateTime startdate;
  private LocalDateTime endDate;
  private int projectId;
  @Id
  @GeneratedValue
  private long serverBookingId;
  private String userId;
}
