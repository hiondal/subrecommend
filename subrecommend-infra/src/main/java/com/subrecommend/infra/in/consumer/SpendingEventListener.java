package com.subrecommend.infra.in.consumer;

import com.subrecommend.biz.dto.TopSpendingDTO;
import com.subrecommend.infra.out.entity.TopSpendingView;
import com.subrecommend.infra.out.repo.TopSpendingViewRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SpendingEventListener {
    private final TopSpendingViewRepository topSpendingViewRepository;

    @Autowired
    public SpendingEventListener(TopSpendingViewRepository topSpendingViewRepository) {
        this.topSpendingViewRepository = topSpendingViewRepository;
    }

    @RabbitListener(queues = "spending-updated-queue")
    public void handleSpendingUpdatedEvent(TopSpendingDTO topSpending) {
        TopSpendingView topSpendingView = topSpendingViewRepository.findById(topSpending.getUserId())
                .orElse(new TopSpendingView());

        topSpendingView.setUserId(topSpending.getUserId());
        topSpendingView.setTopCategory(topSpending.getTopCategory());
        topSpendingView.setTotalSpending(topSpending.getTotalSpending());

        topSpendingViewRepository.save(topSpendingView);
    }
}