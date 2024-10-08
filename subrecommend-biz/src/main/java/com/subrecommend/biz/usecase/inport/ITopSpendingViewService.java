package com.subrecommend.biz.usecase.inport;

import com.subrecommend.biz.dto.TopSpendingDTO;

public interface ITopSpendingViewService {
    TopSpendingDTO getTopSpendingView(String userId);
}