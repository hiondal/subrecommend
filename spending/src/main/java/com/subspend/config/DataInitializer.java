package com.subspend.config;

import com.subspend.model.Spending;
import com.subspend.repository.SpendingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * 애플리케이션 시작 시 초기 데이터를 생성하고 RabbitMQ를 통해 최고 지출 카테고리 정보를 전송하는 클래스
 */
@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private final SpendingRepository spendingRepository;

    @Autowired
    public DataInitializer(SpendingRepository spendingRepository) {
        this.spendingRepository = spendingRepository;

    }

    /**
     * 애플리케이션 시작 시 실행되는 메서드
     * 기존 데이터를 삭제하고 샘플 데이터를 생성
     *
     * @param args 커맨드라인 인수
     */
    @Override
    public void run(String... args) {
        log.info("DataInitializer의 run 메서드가 실행됩니다.");
        deleteAllData();
        initSampleData();
    }

    /**
     * 기존 지출 데이터를 모두 삭제하는 메서드
     */
    private void deleteAllData() {
        log.info("기존 지출 데이터를 모두 삭제합니다.");
        spendingRepository.deleteAll();
        log.info("기존 지출 데이터가 모두 삭제되었습니다.");
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

    }

    /**
     * 지출 정보 객체를 생성하는 메서드
     *
     * @param userId   사용자 ID
     * @param category 지출 카테고리
     * @param amount   지출 금액
     * @param date     지출 일자
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

}