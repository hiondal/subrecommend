package com.subrecommend.biz.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TopSpendingDTO {
    private String userId;
    private String topCategory;
    private BigDecimal totalSpending;
}