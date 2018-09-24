package de.eimantas.eimantasbackend.config;

import de.eimantas.eimantasbackend.messaging.ExpensesSender;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfiguration {



    @Bean
    public Exchange eventExchange() {
        return new TopicExchange("eventExchange");
    }

    @Bean
    public ExpensesSender expensesSender(RabbitTemplate rabbitTemplate, Exchange eventExchange) {
        return new ExpensesSender(rabbitTemplate, eventExchange);
    }

}
