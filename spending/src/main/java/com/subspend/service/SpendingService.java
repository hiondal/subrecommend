package com.subspend.service;

import com.subspend.model.Spending;
import com.subspend.repository.SpendingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SpendingService {
    private final SpendingRepository spendingRepository;

    @Autowired
    public SpendingService(SpendingRepository spendingRepository) {
        this.spendingRepository = spendingRepository;
    }

    // 지출 정보 생성
    public Spending createSpending(Spending spending) {
        log.info("지출 정보 생성 요청 수신: {}", spending);

        Spending savedSpending = spendingRepository.save(spending);

        log.info("지출 정보가 저장되었습니다: {}", savedSpending);

        return savedSpending;
    }
}