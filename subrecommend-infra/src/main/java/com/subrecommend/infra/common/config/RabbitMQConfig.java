package com.subrecommend.infra.common.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class RabbitMQConfig {
    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Value("${spring.rabbitmq.port}")
    private int port;

    /**
     * RabbitMQ 연결을 위한 ConnectionFactory 빈 생성
     *
     * @return RabbitMQ 연결 팩토리
     */
    @Bean
    ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);

        log.info("RabbitMQ 연결 팩토리가 생성되었습니다. host={}, port={}", host, port);

        return connectionFactory;
    }

    /**
     * JSON 형식의 메시지 변환을 위한 MessageConverter 빈 생성
     *
     * @return JSON 메시지 변환기
     */
    @Bean
    MessageConverter messageConverter() {
        log.info("JSON 메시지 변환기가 생성되었습니다.");
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitMQ와의 통신을 위한 RabbitTemplate 빈 생성
     *
     * @param connectionFactory RabbitMQ 연결 팩토리
     * @param messageConverter JSON 메시지 변환기
     * @return RabbitTemplate
     */
    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);

        log.info("RabbitTemplate이 생성되었습니다.");

        return rabbitTemplate;
    }

    /**
     * 지출 정보 업데이트 이벤트를 수신할 Queue 빈 생성
     *
     * @return 지출 정보 업데이트 Queue
     */
    @Bean
    public Queue spendingUpdatedQueue() {
        log.info("지출 정보 업데이트 Queue가 생성되었습니다.");
        return QueueBuilder.durable("spending-updated-queue")
                .withArgument("x-dead-letter-exchange", "spending-dlx-exchange")
                .withArgument("x-dead-letter-routing-key", "spending-updated.dlx")
                .build();
    }

    /**
     * 지출 정보 업데이트 이벤트를 위한 Topic Exchange 빈 생성
     *
     * @return Topic Exchange
     */
    @Bean
    public TopicExchange spendingExchange() {
        log.info("지출 정보 업데이트 이벤트를 위한 Topic Exchange를 생성합니다.");
        return new TopicExchange("spending-exchange");
    }

    /**
     * 지출 정보 업데이트 Queue와 Exchange의 바인딩 설정
     *
     * @param spendingUpdatedQueue 지출 정보 업데이트 Queue
     * @param spendingExchange 지출 정보 업데이트 Exchange
     * @return 바인딩 객체
     */
    @Bean
    public Binding spendingUpdatedBinding(Queue spendingUpdatedQueue, TopicExchange spendingExchange) {
        log.info("지출 정보 업데이트 Queue와 Exchange가 바인딩되었습니다.");
        return BindingBuilder.bind(spendingUpdatedQueue).to(spendingExchange).with("spending.updated");
    }

    /**
     * 지출 정보 업데이트 실패 시 사용할 Dead Letter Exchange 빈 생성
     *
     * @return Dead Letter Exchange
     */
    @Bean
    public Exchange spendingDlxExchange() {
        log.info("지출 정보 업데이트 실패 시 사용할 Dead Letter Exchange가 생성되었습니다.");
        return new FanoutExchange("spending-dlx-exchange");
    }

    /**
     * 지출 정보 업데이트 실패 시 사용할 Dead Letter Queue 빈 생성
     *
     * @return Dead Letter Queue
     */
    @Bean
    public Queue spendingUpdatedDlxQueue() {
        log.info("지출 정보 업데이트 실패 시 사용할 Dead Letter Queue가 생성되었습니다.");
        return QueueBuilder.durable("spending-updated-dlx-queue").build();
    }

    /**
     * Dead Letter Queue와 Dead Letter Exchange의 바인딩 설정
     *
     * @param spendingUpdatedDlxQueue Dead Letter Queue
     * @param spendingDlxExchange Dead Letter Exchange
     * @return 바인딩 객체
     */
    @Bean
    public Binding spendingUpdatedDlxBinding(Queue spendingUpdatedDlxQueue, FanoutExchange spendingDlxExchange) {
        log.info("Dead Letter Queue와 Dead Letter Exchange가 바인딩되었습니다.");
        return BindingBuilder.bind(spendingUpdatedDlxQueue).to(spendingDlxExchange);
    }
}