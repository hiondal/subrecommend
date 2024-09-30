package com.subrecommend.biz.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SubscriptionDetailDTO extends SubscriptionDTO {
    private String category;
    private int maxSharing;
}