package com.subrecommend.biz.usecase.inport;

import com.subrecommend.biz.dto.RecommendedCategoryDTO;

public interface IRecommendationService {
    RecommendedCategoryDTO getRecommendedCategory(String userId);
}