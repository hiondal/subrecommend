package com.subspend.config;

import com.subspend.model.Spending;
import com.subspend.repository.SpendingRepository;
import com.subrecommend.biz.dto.TopSpendingDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DataInitializer implements CommandLineRunner {
    private final SpendingRepository spendingRepository;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public DataInitializer(SpendingRepository spendingRepository, RabbitTemplate rabbitTemplate) {
        this.spendingRepository = spendingRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void run(String... args) {
        initSampleData();
    }

    private void initSampleData() {
        List<Spending> spendings = Arrays.asList(
                createSpending("user1", "식비", new BigDecimal("580000"), LocalDate.now()),
                createSpending("user1", "엔터테인먼트", new BigDecimal("150000"), LocalDate.now().minusDays(1)),
                createSpending("user1", "쇼핑", new BigDecimal("250000"), LocalDate.now().minusDays(2)),
                createSpending("user1", "뷰티", new BigDecimal("100000"), LocalDate.now().minusDays(3))
        );
        spendingRepository.saveAll(spendings);

        sendTopSpendingUpdatedEvent("user1");
    }

    private Spending createSpending(String userId, String category, BigDecimal amount, LocalDate date) {
        Spending spending = new Spending();
        spending.setUserId(userId);
        spending.setCategory(category);
        spending.setAmount(amount);
        spending.setDate(date);
        return spending;
    }

    private void sendTopSpendingUpdatedEvent(String userId) {
        List<Spending> userSpendings = spendingRepository.findByUserId(userId);
        Map<String, BigDecimal> categorySpending = userSpendings.stream()
                .collect(Collectors.groupingBy(Spending::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Spending::getAmount, BigDecimal::add)));

        String topCategory = categorySpending.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        if (topCategory != null) {
            TopSpendingDTO topSpending = new TopSpendingDTO();
            topSpending.setTopCategory(topCategory);
            topSpending.setTotalSpending(categorySpending.get(topCategory));
            topSpending.setUserId(userId);

            rabbitTemplate.convertAndSend("spending-exchange", "spending.updated", topSpending);
        }
    }
}