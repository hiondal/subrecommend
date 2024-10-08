package com.subspend.web;

import com.subspend.model.Spending;
import com.subspend.service.SpendingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/spending")
public class SpendingController {
    private final SpendingService spendingService;

    @Autowired
    public SpendingController(SpendingService spendingService) {
        this.spendingService = spendingService;
    }

    @PostMapping
    public Spending createSpending(@RequestBody Spending spending) {
        return spendingService.createSpending(spending);
    }
}