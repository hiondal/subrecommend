package com.subrecommend.biz.domain;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class Spending {
    private Long id;
    private String userId;
    private String category;
    private BigDecimal amount;
    private LocalDate date;
}