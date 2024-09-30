package com.subrecommend.biz.usecase.service;

import com.subrecommend.biz.dto.SubscriptionDTO;
import com.subrecommend.biz.dto.SubscriptionDetailDTO;
import com.subrecommend.biz.usecase.inport.ISubscriptionService;
import com.subrecommend.biz.usecase.outport.ISubscriptionProvider;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SubscriptionServiceImpl implements ISubscriptionService {
    private final ISubscriptionProvider subscriptionProvider;

    public SubscriptionServiceImpl(ISubscriptionProvider subscriptionProvider) {
        this.subscriptionProvider = subscriptionProvider;
    }

    @Override
    public List<SubscriptionDTO> getSubscriptionsByCategory(String category) {
        return subscriptionProvider.getSubscriptionsByCategory(category);
    }

    @Override
    public SubscriptionDetailDTO getSubscriptionDetails(String subscriptionId) {
        return subscriptionProvider.getSubscriptionById(subscriptionId);
    }
}
