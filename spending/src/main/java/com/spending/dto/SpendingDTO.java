package com.spending.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SpendingDTO {
    private String userId;
    private String category;
    private BigDecimal amount;
    private LocalDate date;
}