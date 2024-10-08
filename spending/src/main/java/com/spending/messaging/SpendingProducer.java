package com.spending.messaging;

import com.spending.dto.SpendingDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static com.spending.config.RabbitMQConfig.SPENDING_EXCHANGE;
import static com.spending.config.RabbitMQConfig.SPENDING_ROUTING_KEY;

@Component
public class SpendingProducer {
    private final RabbitTemplate rabbitTemplate;

    public SpendingProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendSpending(SpendingDTO spendingDTO) {
        rabbitTemplate.convertAndSend(SPENDING_EXCHANGE, SPENDING_ROUTING_KEY, spendingDTO);
    }
}