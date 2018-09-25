package de.eimantas.eimantasbackend.processing.Deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CustomLocalDateDeSerializer extends StdDeserializer<LocalDate> {

  private static final long serialVersionUID = 1L;
  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

  protected CustomLocalDateDeSerializer() {
    super(LocalDate.class);
  }


  @Override
  public LocalDate deserialize(JsonParser jp, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    return LocalDate.parse(jp.readValueAs(String.class), formatter);
  }

}