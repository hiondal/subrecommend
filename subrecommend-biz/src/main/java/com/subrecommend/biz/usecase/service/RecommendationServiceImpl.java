package com.subrecommend.biz.usecase.service;

import com.subrecommend.biz.dto.RecommendedCategoryDTO;
import com.subrecommend.biz.dto.TopSpendingDTO;
import com.subrecommend.biz.usecase.inport.IRecommendationService;
import com.subrecommend.biz.usecase.outport.ICategoryProvider;
import com.subrecommend.biz.usecase.outport.ITopSpendingViewProvider;
import org.springframework.stereotype.Service;

@Service
public class RecommendationServiceImpl implements IRecommendationService {
    private final ITopSpendingViewProvider topSpendingViewProvider;
    private final ICategoryProvider categoryProvider;

    public RecommendationServiceImpl(ITopSpendingViewProvider topSpendingViewProvider, ICategoryProvider categoryProvider) {
        this.topSpendingViewProvider = topSpendingViewProvider;
        this.categoryProvider = categoryProvider;
    }

    @Override
    public RecommendedCategoryDTO getRecommendedCategory(String userId) {
        TopSpendingDTO topSpendingDTO = topSpendingViewProvider.getTopSpendingView(userId);

        if (topSpendingDTO == null) {
            return null;
        }

        String topCategory = topSpendingDTO.getTopCategory();

        RecommendedCategoryDTO recommendedCategory = new RecommendedCategoryDTO();
        recommendedCategory.setCategoryName(topCategory);
        categoryProvider.getCategories().stream()
                .filter(c -> c.getName().equals(topCategory))
                .findFirst()
                .ifPresent(c -> recommendedCategory.setCategoryImage(c.getImage()));

        return recommendedCategory;
    }
}
