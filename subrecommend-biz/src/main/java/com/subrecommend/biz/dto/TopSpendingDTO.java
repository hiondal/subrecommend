package com.subrecommend.biz.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TopSpendingDTO {
    private String topCategory;
    private BigDecimal totalSpending;
    private LocalDate baseDate;
}