package com.subrecommend.infra.in.consumer;

import com.subrecommend.biz.dto.TopSpendingDTO;
import com.subrecommend.infra.out.entity.TopSpendingView;
import com.subrecommend.infra.out.repo.TopSpendingViewRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * RabbitMQ를 통해 수신한 지출 정보 업데이트 이벤트를 처리하는 리스너 클래스
 */
@Component
@Slf4j
public class SpendingEventListener {
    private final TopSpendingViewRepository topSpendingViewRepository;

    /**
     * TopSpendingViewRepository를 주입받는 생성자
     *
     * @param topSpendingViewRepository TopSpendingView 엔티티를 다루는 리포지토리
     */
    @Autowired
    public SpendingEventListener(TopSpendingViewRepository topSpendingViewRepository) {
        this.topSpendingViewRepository = topSpendingViewRepository;
        log.info("SpendingEventListener가 초기화되었습니다. TopSpendingViewRepository: {}", topSpendingViewRepository);
    }

    /**
     * 지출 정보 업데이트 이벤트를 수신하여 처리하는 메서드
     *
     * @param topSpending 수신한 TopSpendingDTO 객체
     */
    @RabbitListener(queues = "spending-updated-queue")
    public void handleSpendingUpdatedEvent(TopSpendingDTO topSpending) {
        log.info("지출 정보 업데이트 이벤트를 수신하였습니다. userId: {}, topCategory: {}, totalSpending: {}",
                topSpending.getUserId(), topSpending.getTopCategory(), topSpending.getTotalSpending());

        // 수신한 TopSpendingDTO를 기반으로 TopSpendingView 엔티티를 조회하거나 생성
        TopSpendingView topSpendingView = topSpendingViewRepository.findById(topSpending.getUserId())
                .orElse(new TopSpendingView());

        // 수신한 정보로 TopSpendingView 엔티티 업데이트
        topSpendingView.setUserId(topSpending.getUserId());
        topSpendingView.setTopCategory(topSpending.getTopCategory());
        topSpendingView.setTotalSpending(topSpending.getTotalSpending());

        // 업데이트된 TopSpendingView 엔티티를 리포지토리에 저장
        TopSpendingView savedTopSpendingView = topSpendingViewRepository.save(topSpendingView);
        log.info("TopSpendingView 엔티티가 업데이트되었습니다. id: {}, userId: {}, topCategory: {}, totalSpending: {}",
                savedTopSpendingView.getUserId(), savedTopSpendingView.getUserId(), savedTopSpendingView.getTopCategory(),
                savedTopSpendingView.getTotalSpending());
    }
}