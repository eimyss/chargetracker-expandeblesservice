package de.eimantas.eimantasbackend.messaging;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.eimantas.eimantasbackend.entities.Booking;
import de.eimantas.eimantasbackend.entities.NotificationMessage;
import de.eimantas.eimantasbackend.service.SecurityService;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class BookingsSender {
  private final RabbitTemplate rabbitTemplate;

  @Inject
  SecurityService securityService;

  private ObjectMapper mapper = new ObjectMapper();


  private final Exchange exchange;

  private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());


  public BookingsSender(RabbitTemplate rabbitTemplate, Exchange exchange) {
    this.rabbitTemplate = rabbitTemplate;
    this.exchange = exchange;
    JavaTimeModule module = new JavaTimeModule();
    mapper.registerModule(module);
  }

  public void notifyCreateExpense(KeycloakAuthenticationToken principal, Booking response) {
    String routingKey = "booking.created";
    try {
      ObjectMapper mapper = new ObjectMapper();
      NotificationMessage message = new NotificationMessage();
      message.setObjectJson(response);
      message.setUserToken(securityService.getToken(principal));
      logger.info("Sending to exchange: " + exchange.getName() + " with message: " + mapper.writeValueAsString(message));
      rabbitTemplate.convertAndSend(exchange.getName(), routingKey, mapper.writeValueAsString(message));
    } catch (JsonProcessingException e) {
      logger.warn("cannot create notification message for booking", e);
    }

  }
}