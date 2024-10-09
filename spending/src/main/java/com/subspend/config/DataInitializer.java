package com.subspend.config;

import com.subspend.model.Spending;
import com.subspend.repository.SpendingRepository;
import com.subrecommend.biz.dto.TopSpendingDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 애플리케이션 시작 시 초기 데이터를 생성하고 RabbitMQ를 통해 최고 지출 카테고리 정보를 전송하는 클래스
 */
@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private final SpendingRepository spendingRepository;
    private final RabbitTemplate rabbitTemplate;

    /**
     * SpendingRepository와 RabbitTemplate을 주입받는 생성자
     *
     * @param spendingRepository 지출 정보를 저장하는 리포지토리
     * @param rabbitTemplate RabbitMQ 메시지 전송을 위한 템플릿
     */
    @Autowired
    public DataInitializer(SpendingRepository spendingRepository, RabbitTemplate rabbitTemplate) {
        this.spendingRepository = spendingRepository;
        this.rabbitTemplate = rabbitTemplate;
        log.info("DataInitializer가 초기화되었습니다. SpendingRepository: {}, RabbitTemplate: {}", spendingRepository, rabbitTemplate);
    }

    /**
     * 애플리케이션 시작 시 실행되는 메서드
     *
     * @param args 커맨드라인 인수
     */
    @Override
    public void run(String... args) {
        log.info("DataInitializer의 run 메서드가 실행됩니다.");
        initSampleData();
    }

    /**
     * 샘플 지출 데이터를 생성하고 최고 지출 카테고리 정보를 전송하는 메서드
     */
    private void initSampleData() {
        log.info("샘플 지출 데이터를 생성합니다.");

        List<Spending> spendings = Arrays.asList(
                createSpending("user1", "식비", new BigDecimal("580000"), LocalDate.now()),
                createSpending("user1", "엔터테인먼트", new BigDecimal("150000"), LocalDate.now().minusDays(1)),
                createSpending("user1", "쇼핑", new BigDecimal("250000"), LocalDate.now().minusDays(2)),
                createSpending("user1", "뷰티", new BigDecimal("100000"), LocalDate.now().minusDays(3))
        );

        List<Spending> savedSpendings = spendingRepository.saveAll(spendings);
        log.info("샘플 지출 데이터가 저장되었습니다. 저장된 지출 데이터 수: {}", savedSpendings.size());

        sendTopSpendingUpdatedEvent("user1");
    }

    /**
     * 지출 정보 객체를 생성하는 메서드
     *
     * @param userId 사용자 ID
     * @param category 지출 카테고리
     * @param amount 지출 금액
     * @param date 지출 일자
     * @return 생성된 Spending 객체
     */
    private Spending createSpending(String userId, String category, BigDecimal amount, LocalDate date) {
        log.debug("지출 정보를 생성합니다. userId: {}, category: {}, amount: {}, date: {}", userId, category, amount, date);

        Spending spending = new Spending();
        spending.setUserId(userId);
        spending.setCategory(category);
        spending.setAmount(amount);
        spending.setDate(date);

        log.debug("지출 정보가 생성되었습니다. {}", spending);
        return spending;
    }

    /**
     * 최고 지출 카테고리 정보를 RabbitMQ를 통해 전송하는 메서드
     *
     * @param userId 사용자 ID
     */
    private void sendTopSpendingUpdatedEvent(String userId) {
        log.info("최고 지출 카테고리 정보를 전송합니다. userId: {}", userId);

        List<Spending> userSpendings = spendingRepository.findByUserId(userId);
        log.debug("사용자의 지출 정보를 조회했습니다. userId: {}, 조회된 지출 정보 수: {}", userId, userSpendings.size());

        Map<String, BigDecimal> categorySpending = userSpendings.stream()
                .collect(Collectors.groupingBy(Spending::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Spending::getAmount, BigDecimal::add)));
        log.debug("카테고리별 지출 금액을 계산했습니다. {}", categorySpending);

        String topCategory = categorySpending.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
        log.debug("최고 지출 카테고리를 파악했습니다. topCategory: {}", topCategory);

        if (topCategory != null) {
            TopSpendingDTO topSpending = new TopSpendingDTO();
            topSpending.setTopCategory(topCategory);
            topSpending.setTotalSpending(categorySpending.get(topCategory));
            topSpending.setUserId(userId);
            log.debug("TopSpendingDTO 객체를 생성했습니다. {}", topSpending);

            rabbitTemplate.convertAndSend("spending-exchange", "spending.updated", topSpending);
            log.info("최고 지출 카테고리 정보를 RabbitMQ로 전송했습니다. userId: {}, topCategory: {}", userId, topCategory);
        } else {
            log.warn("최고 지출 카테고리가 없습니다. userId: {}", userId);
        }
    }
}