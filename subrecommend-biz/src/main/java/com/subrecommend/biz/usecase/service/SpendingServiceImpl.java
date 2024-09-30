package com.subrecommend.biz.usecase.service;

import com.subrecommend.biz.dto.SpendingDTO;
import com.subrecommend.biz.dto.TopSpendingDTO;
import com.subrecommend.biz.usecase.inport.ISpendingService;
import com.subrecommend.biz.usecase.outport.ISpendingProvider;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SpendingServiceImpl implements ISpendingService {
    private final ISpendingProvider spendingProvider;

    public SpendingServiceImpl(ISpendingProvider spendingProvider) {
        this.spendingProvider = spendingProvider;
    }

    @Override
    public TopSpendingDTO getTopSpendingCategory(String userId) {
        List<SpendingDTO> userSpendings = spendingProvider.getUserSpending(userId);
        Map<String, BigDecimal> categorySpending = userSpendings.stream()
                .collect(Collectors.groupingBy(SpendingDTO::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, SpendingDTO::getAmount, BigDecimal::add)));

        Map.Entry<String, BigDecimal> topCategory = categorySpending.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);

        if (topCategory == null) {
            return null;
        }

        TopSpendingDTO topSpending = new TopSpendingDTO();
        topSpending.setTopCategory(topCategory.getKey());
        topSpending.setTotalSpending(topCategory.getValue());
        topSpending.setBaseDate(LocalDate.now());

        return topSpending;
    }
}