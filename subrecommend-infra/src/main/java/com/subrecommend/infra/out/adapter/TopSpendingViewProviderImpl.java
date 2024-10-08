package com.subrecommend.infra.out.adapter;

import com.subrecommend.biz.dto.TopSpendingDTO;
import com.subrecommend.biz.usecase.outport.ITopSpendingViewProvider;
import com.subrecommend.infra.out.entity.TopSpendingView;
import com.subrecommend.infra.out.repo.TopSpendingViewRepository;
import org.springframework.stereotype.Component;

@Component
public class TopSpendingViewProviderImpl implements ITopSpendingViewProvider {
    private final TopSpendingViewRepository topSpendingViewRepository;

    public TopSpendingViewProviderImpl(TopSpendingViewRepository topSpendingViewRepository) {
        this.topSpendingViewRepository = topSpendingViewRepository;
    }

    @Override
    public TopSpendingDTO getTopSpendingView(String userId) {
        TopSpendingView topSpendingView = topSpendingViewRepository.findById(userId).orElse(null);

        if (topSpendingView != null) {
            TopSpendingDTO topSpendingDTO = new TopSpendingDTO();
            topSpendingDTO.setUserId(topSpendingView.getUserId());
            topSpendingDTO.setTopCategory(topSpendingView.getTopCategory());
            topSpendingDTO.setTotalSpending(topSpendingView.getTotalSpending());
            return topSpendingDTO;
        }

        return null;
    }
}