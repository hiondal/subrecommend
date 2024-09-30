package com.subrecommend.biz.usecase.outport;

import com.subrecommend.biz.dto.SpendingDTO;
import java.util.List;

public interface ISpendingProvider {
    List<SpendingDTO> getUserSpending(String userId);
}