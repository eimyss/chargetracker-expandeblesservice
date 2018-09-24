package de.eimantas.eimantasbackend.entities;


import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@ApiModel
public class AccountOverView {

    // TODO, soll ich das wegen caching speichern? oder benutzen cache?

    @Id
    @GeneratedValue
    private Long id;
    private Long refAccountId;
    private BigDecimal total;
    private boolean active;
    private int countExpenses;
    private Instant createDate;


}
