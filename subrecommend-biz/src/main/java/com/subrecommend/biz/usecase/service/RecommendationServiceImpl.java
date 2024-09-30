package com.subrecommend.biz.usecase.service;

import com.subrecommend.biz.dto.RecommendedCategoryDTO;
import com.subrecommend.biz.dto.SpendingDTO;
import com.subrecommend.biz.usecase.inport.IRecommendationService;
import com.subrecommend.biz.usecase.outport.ISpendingProvider;
import com.subrecommend.biz.usecase.outport.ICategoryProvider;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RecommendationServiceImpl implements IRecommendationService {
    private final ISpendingProvider spendingProvider;
    private final ICategoryProvider categoryProvider;

    public RecommendationServiceImpl(ISpendingProvider spendingProvider, ICategoryProvider categoryProvider) {
        this.spendingProvider = spendingProvider;
        this.categoryProvider = categoryProvider;
    }

    @Override
    public RecommendedCategoryDTO getRecommendedCategory(String userId) {
        List<SpendingDTO> userSpendings = spendingProvider.getUserSpending(userId);
        Map<String, Double> categorySpending = userSpendings.stream()
                .collect(Collectors.groupingBy(SpendingDTO::getCategory,
                        Collectors.summingDouble(s -> s.getAmount().doubleValue())));

        String topCategory = categorySpending.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        if (topCategory == null) {
            return null;
        }

        RecommendedCategoryDTO recommendedCategory = new RecommendedCategoryDTO();
        recommendedCategory.setCategoryName(topCategory);
        categoryProvider.getCategories().stream()
                .filter(c -> c.getName().equals(topCategory))
                .findFirst()
                .ifPresent(c -> recommendedCategory.setCategoryImage(c.getImage()));

        return recommendedCategory;
    }
}