package de.eimantas.eimantasbackend.messaging;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class BookingsSender {
  private final RabbitTemplate rabbitTemplate;

  private ObjectMapper mapper = new ObjectMapper();

  private final Exchange exchange;

  private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());


  public BookingsSender(RabbitTemplate rabbitTemplate, Exchange exchange) {
    this.rabbitTemplate = rabbitTemplate;
    this.exchange = exchange;

    JavaTimeModule module = new JavaTimeModule();
    mapper.registerModule(module);
  }


  public void notifyCreateExpense(long id) {
    String routingKey = "booking.created";
    logger.info("Sending to exchange: " + exchange.getName() + " with message: " + id);
    rabbitTemplate.convertAndSend(exchange.getName(), routingKey, id);
  }
}