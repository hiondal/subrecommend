package com.subrecommend.infra.in.web;

import com.subrecommend.biz.dto.TopSpendingDTO;
import com.subrecommend.biz.usecase.inport.ISpendingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/spending")
@Tag(name = "지출", description = "지출 관련 API")
public class SpendingController {
    private final ISpendingService spendingService;
    private final SpendingControllerHelper controllerHelper;

    public SpendingController(ISpendingService spendingService, SpendingControllerHelper controllerHelper) {
        this.spendingService = spendingService;
        this.controllerHelper = controllerHelper;
    }

    @GetMapping("/top-category")
    @Operation(summary = "최고 지출 카테고리 조회", description = "사용자의 최고 지출 카테고리와 총 지출액을 조회합니다.")
    public ResponseEntity<TopSpendingDTO> getTopSpendingCategory(@RequestParam String userId) {
        return ResponseEntity.ok(spendingService.getTopSpendingCategory(userId));
    }
}