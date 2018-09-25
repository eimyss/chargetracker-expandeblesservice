package de.eimantas.eimantasbackend.entities.Specification;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchCriteria {
  private String key;
  private String operation;
  private Object value;
}