package com.subspend.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Value("${spring.rabbitmq.port}")
    private int port;

    @Bean
    ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        return connectionFactory;
    }

    @Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public Exchange spendingExchange() {
        return new TopicExchange("spending-exchange");
    }

    @Bean
    public Queue spendingUpdatedQueue() {
        return QueueBuilder.durable("spending-updated-queue")
                .withArgument("x-dead-letter-exchange", "spending-dlx-exchange")
                .withArgument("x-dead-letter-routing-key", "spending-updated.dlx")
                .build();
    }

    @Bean
    public Binding spendingUpdatedBinding(Queue spendingUpdatedQueue, TopicExchange spendingExchange) {
        return BindingBuilder.bind(spendingUpdatedQueue).to(spendingExchange).with("spending.updated");
    }

    @Bean
    public Exchange spendingDlxExchange() {
        return new FanoutExchange("spending-dlx-exchange");
    }

    @Bean
    public Queue spendingUpdatedDlxQueue() {
        return QueueBuilder.durable("spending-updated-dlx-queue").build();
    }

    @Bean
    public Binding spendingUpdatedDlxBinding(Queue spendingUpdatedDlxQueue, FanoutExchange spendingDlxExchange) {
        return BindingBuilder.bind(spendingUpdatedDlxQueue).to(spendingDlxExchange);
    }
}