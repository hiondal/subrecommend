package com.subrecommend.infra.out.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "top_spending_view")
@Data
public class TopSpendingView {
    @Id
    private String userId;

    @Column(nullable = false)
    private String topCategory;

    @Column(nullable = false)
    private BigDecimal totalSpending;
}