package com.spending.api;

import com.spending.dto.SpendingDTO;
import com.spending.service.SpendingService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/spending")
public class SpendingController {
    private final SpendingService spendingService;

    public SpendingController(SpendingService spendingService) {
        this.spendingService = spendingService;
    }

    @PostMapping
    public void createSpending(@RequestBody SpendingDTO spendingDTO) {
        spendingService.createSpending(spendingDTO);
    }
}