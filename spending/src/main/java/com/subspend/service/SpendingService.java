package com.subspend.service;

import com.subspend.model.Spending;
import com.subspend.repository.SpendingRepository;
import com.subrecommend.biz.dto.TopSpendingDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SpendingService {
    private final SpendingRepository spendingRepository;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public SpendingService(SpendingRepository spendingRepository, RabbitTemplate rabbitTemplate) {
        this.spendingRepository = spendingRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public Spending createSpending(Spending spending) {
        Spending savedSpending = spendingRepository.save(spending);
        updateTopSpendingCategory(savedSpending.getUserId());
        return savedSpending;
    }

    private void updateTopSpendingCategory(String userId) {
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

            rabbitTemplate.convertAndSend("sub-event-exchange", "spending.updated", topSpending);
        }
    }
}