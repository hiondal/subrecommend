package com.subrecommend.biz.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SubscriptionDTO {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private String logo;
}