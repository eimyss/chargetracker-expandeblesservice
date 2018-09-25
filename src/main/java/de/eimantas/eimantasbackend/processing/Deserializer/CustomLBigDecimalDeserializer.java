package de.eimantas.eimantasbackend.processing.Deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

public class CustomLBigDecimalDeserializer extends StdDeserializer<BigDecimal> {

  private static final long serialVersionUID = 1L;
  Locale de = new Locale("de", "DE");
  private DecimalFormat nf;


  protected CustomLBigDecimalDeserializer() {
    super(BigDecimal.class);
    nf = (DecimalFormat) NumberFormat.getInstance(de);
    nf.setParseBigDecimal(true);
  }


  @Override
  public BigDecimal deserialize(JsonParser jp, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    return (BigDecimal) nf.parse(jp.readValueAs(String.class), new ParsePosition(0));
  }

}