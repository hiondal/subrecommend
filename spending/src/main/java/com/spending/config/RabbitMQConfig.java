package com.spending.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String SPENDING_QUEUE = "spending.queue";
    public static final String SPENDING_EXCHANGE = "spending.exchange";
    public static final String SPENDING_ROUTING_KEY = "spending.routingkey";

    @Bean
    public Queue spendingQueue() {
        return new Queue(SPENDING_QUEUE, true);
    }

    @Bean
    public DirectExchange spendingExchange() {
        return new DirectExchange(SPENDING_EXCHANGE);
    }

    @Bean
    public Binding spendingBinding(Queue spendingQueue, DirectExchange spendingExchange) {
        return BindingBuilder
                .bind(spendingQueue)
                .to(spendingExchange)
                .with(SPENDING_ROUTING_KEY);
    }
}