package com.subrecommend.biz.usecase.outport;

import com.subrecommend.biz.dto.SpendingDTO;
import java.util.List;

public interface ISpendingViewProvider {
    List<SpendingDTO> getUserSpending(String userId);
}