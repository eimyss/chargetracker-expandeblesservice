package de.eimantas.eimantasbackend.messaging;

import de.eimantas.eimantasbackend.service.BookingService;
import de.eimantas.eimantasbackend.service.ExpensesService;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;


public class BookingReceiver {

  @Inject
  BookingService bookingService;

  @Inject
  ExpensesService expensesService;

  private static final Logger logger = LoggerFactory.getLogger(BookingReceiver.class);

  public BookingReceiver() {

  }

  //  @RabbitListener(queues = "orderServiceQueue")
  public void receive(String message) {
    logger.info("Received message '{}'", message);
  }

  public void handleMessage(Object message) throws IOException {
    logger.info("Received message about processed booking ID: '{}'", message);

    try {
      JSONObject json = new JSONObject((String) message);
      expensesService.createExpenseFromBooking(json);

      logger.info("got json: " + json.toString());
      logger.info("booking id: " + json.getInt("booking_id"));
    } catch (JSONException e) {
      e.printStackTrace();
    }

  }


}
