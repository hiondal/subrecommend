package com.subspend.service;

import com.subspend.model.Spending;
import com.subspend.repository.SpendingRepository;
import com.subrecommend.biz.dto.TopSpendingDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SpendingService {
    private final SpendingRepository spendingRepository;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public SpendingService(SpendingRepository spendingRepository, RabbitTemplate rabbitTemplate) {
        this.spendingRepository = spendingRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    // 지출 정보 생성
    public Spending createSpending(Spending spending) {
        log.info("지출 정보 생성 요청 수신: {}", spending);

        Spending savedSpending = spendingRepository.save(spending);

        log.info("지출 정보가 저장되었습니다: {}", savedSpending);

        updateTopSpendingCategory(savedSpending.getUserId());

        return savedSpending;
    }

    // 사용자의 최다 지출 카테고리 업데이트
    private void updateTopSpendingCategory(String userId) {
        log.info("사용자 {}의 최다 지출 카테고리 업데이트 시작", userId);

        List<Spending> userSpendings = spendingRepository.findByUserId(userId);

        log.info("사용자 {}의 지출 내역 조회 완료: {} 건", userId, userSpendings.size());

        // 카테고리별 총 지출액 계산
        Map<String, BigDecimal> categorySpending = userSpendings.stream()
                .collect(Collectors.groupingBy(Spending::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Spending::getAmount, BigDecimal::add)));

        log.info("사용자 {}의 카테고리별 총 지출액 계산 완료: {}", userId, categorySpending);

        // 최다 지출 카테고리 찾기
        String topCategory = categorySpending.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        log.info("사용자 {}의 최다 지출 카테고리: {}", userId, topCategory);

        if (topCategory != null) {
            TopSpendingDTO topSpending = new TopSpendingDTO();
            topSpending.setTopCategory(topCategory);
            topSpending.setTotalSpending(categorySpending.get(topCategory));
            topSpending.setUserId(userId);

            log.info("최다 지출 카테고리 정보를 RabbitMQ로 전송: {}", topSpending);

            rabbitTemplate.convertAndSend("spending-exchange", "spending.updated", topSpending);
        }
    }
}