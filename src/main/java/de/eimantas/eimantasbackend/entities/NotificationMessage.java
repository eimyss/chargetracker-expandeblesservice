package de.eimantas.eimantasbackend.entities;


import lombok.Data;
import org.json.JSONObject;

@Data
public class NotificationMessage {

  private String userToken;
  private Object objectJson;

}
