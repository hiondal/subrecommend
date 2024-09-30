package com.subrecommend.infra.out.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "subscription_categories")
@Data
public class SubscriptionCategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String image;
}
