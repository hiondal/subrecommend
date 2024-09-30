package com.subrecommend.biz.usecase.inport;

import com.subrecommend.biz.dto.TopSpendingDTO;

public interface ISpendingService {
    TopSpendingDTO getTopSpendingCategory(String userId);
}