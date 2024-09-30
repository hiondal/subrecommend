package com.subrecommend.biz.domain;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class Subscription {
    private String id;
    private String name;
    private String category;
    private String description;
    private BigDecimal price;
    private String logo;
    private int maxSharing;
}