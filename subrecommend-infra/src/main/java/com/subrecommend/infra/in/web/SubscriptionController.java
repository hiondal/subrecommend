package com.subrecommend.infra.in.web;

import com.subrecommend.biz.dto.SubscriptionDTO;
import com.subrecommend.biz.dto.SubscriptionDetailDTO;
import com.subrecommend.biz.usecase.inport.ISubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@Tag(name = "구독", description = "구독 서비스 관련 API")
public class SubscriptionController {
    private final ISubscriptionService subscriptionService;
    private final SubscriptionControllerHelper controllerHelper;

    public SubscriptionController(ISubscriptionService subscriptionService, SubscriptionControllerHelper controllerHelper) {
        this.subscriptionService = subscriptionService;
        this.controllerHelper = controllerHelper;
    }

    @GetMapping("/by-category")
    @Operation(summary = "카테고리별 구독 서비스 조회", description = "특정 카테고리의 구독 서비스 목록을 조회합니다.")
    public ResponseEntity<List<SubscriptionDTO>> getSubscriptionsByCategory(@RequestParam String category) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionsByCategory(category));
    }

    @GetMapping("/{subscriptionId}")
    @Operation(summary = "구독 서비스 상세 정보 조회", description = "특정 구독 서비스의 상세 정보를 조회합니다.")
    public ResponseEntity<SubscriptionDetailDTO> getSubscriptionDetails(@PathVariable String subscriptionId) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionDetails(subscriptionId));
    }
}