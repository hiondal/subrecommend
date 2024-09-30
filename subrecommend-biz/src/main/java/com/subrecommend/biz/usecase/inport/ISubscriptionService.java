package com.subrecommend.biz.usecase.inport;

import com.subrecommend.biz.dto.SubscriptionDTO;
import com.subrecommend.biz.dto.SubscriptionDetailDTO;
import java.util.List;

public interface ISubscriptionService {
    List<SubscriptionDTO> getSubscriptionsByCategory(String category);
    SubscriptionDetailDTO getSubscriptionDetails(String subscriptionId);
}