package com.subrecommend.biz.usecase.outport;

import com.subrecommend.biz.dto.SubscriptionDTO;
import com.subrecommend.biz.dto.SubscriptionDetailDTO;
import java.util.List;

public interface ISubscriptionProvider {
    List<SubscriptionDTO> getSubscriptionsByCategory(String category);
    SubscriptionDetailDTO getSubscriptionById(String subscriptionId);
}