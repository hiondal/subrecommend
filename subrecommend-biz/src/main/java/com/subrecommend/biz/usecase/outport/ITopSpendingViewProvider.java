package com.subrecommend.biz.usecase.outport;

import com.subrecommend.biz.dto.TopSpendingDTO;

public interface ITopSpendingViewProvider {
    TopSpendingDTO getTopSpendingView(String userId);
}