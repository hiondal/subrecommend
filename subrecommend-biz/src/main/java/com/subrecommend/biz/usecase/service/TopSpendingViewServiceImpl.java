package com.subrecommend.biz.usecase.service;

import com.subrecommend.biz.dto.TopSpendingDTO;
import com.subrecommend.biz.usecase.inport.ITopSpendingViewService;
import com.subrecommend.biz.usecase.outport.ITopSpendingViewProvider;
import org.springframework.stereotype.Service;

@Service
public class TopSpendingViewServiceImpl implements ITopSpendingViewService {
    private final ITopSpendingViewProvider topSpendingViewProvider;

    public TopSpendingViewServiceImpl(ITopSpendingViewProvider topSpendingViewProvider) {
        this.topSpendingViewProvider = topSpendingViewProvider;
    }

    @Override
    public TopSpendingDTO getTopSpendingView(String userId) {
        return topSpendingViewProvider.getTopSpendingView(userId);
    }
}