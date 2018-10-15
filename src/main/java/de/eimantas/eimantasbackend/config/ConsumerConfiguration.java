package de.eimantas.eimantasbackend.config;

import de.eimantas.eimantasbackend.messaging.BookingReceiver;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConsumerConfiguration {


  @Bean
  public TopicExchange eventExchange() {
    return new TopicExchange("eventExchange");
  }

  @Bean
  public Queue bookingQueue() {
    return new Queue("bookingServiceQueue");
  }


  @Bean
  Binding bookingsBinding() {
    return BindingBuilder.bind(bookingQueue()).to(eventExchange()).with("booking.processed");
  }

  @Bean
  public BookingReceiver bookingReceiver() {
    return new BookingReceiver();
  }


  @Bean
  public MessageListenerAdapter bookingListenerAdapter(BookingReceiver receiver) {
    return new MessageListenerAdapter(receiver);
  }


  @Bean
  SimpleMessageListenerContainer bookingContainer(ConnectionFactory connectionFactory,
                                                  @Qualifier("bookingListenerAdapter") MessageListenerAdapter listenerAdapter) {
    SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);
    container.setQueueNames("bookingServiceQueue");
    container.setMessageListener(listenerAdapter);
    return container;
  }


}
