package com.spending.service;

import com.spending.domain.Spending;
import com.spending.domain.SpendingRepository;
import com.spending.dto.SpendingDTO;
import com.spending.messaging.SpendingProducer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SpendingService {
    private final SpendingRepository spendingRepository;
    private final SpendingProducer spendingProducer;

    public SpendingService(SpendingRepository spendingRepository, SpendingProducer spendingProducer) {
        this.spendingRepository = spendingRepository;
        this.spendingProducer = spendingProducer;
    }

    public void createSpending(SpendingDTO spendingDTO) {
        Spending spending = convertToEntity(spendingDTO);
        spendingRepository.save(spending);
        spendingProducer.sendSpending(spendingDTO);
    }

    private Spending convertToEntity(SpendingDTO spendingDTO) {
        Spending spending = new Spending();
        spending.setUserId(spendingDTO.getUserId());
        spending.setCategory(spendingDTO.getCategory());
        spending.setAmount(spendingDTO.getAmount());
        spending.setDate(spendingDTO.getDate());
        return spending;
    }
}