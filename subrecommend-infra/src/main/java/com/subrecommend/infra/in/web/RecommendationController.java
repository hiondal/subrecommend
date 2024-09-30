package com.subrecommend.infra.in.web;

import com.subrecommend.biz.dto.RecommendedCategoryDTO;
import com.subrecommend.biz.usecase.inport.IRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommendations")
@Tag(name = "추천", description = "추천 관련 API")
public class RecommendationController {
    private final IRecommendationService recommendationService;
    private final RecommendationControllerHelper controllerHelper;

    public RecommendationController(IRecommendationService recommendationService, RecommendationControllerHelper controllerHelper) {
        this.recommendationService = recommendationService;
        this.controllerHelper = controllerHelper;
    }

    @GetMapping("/category")
    @Operation(summary = "추천 구독 카테고리 조회", description = "사용자의 지출 패턴을 기반으로 추천 구독 카테고리를 조회합니다.")
    public ResponseEntity<RecommendedCategoryDTO> getRecommendedCategory(@RequestParam String userId) {
        return ResponseEntity.ok(recommendationService.getRecommendedCategory(userId));
    }
}