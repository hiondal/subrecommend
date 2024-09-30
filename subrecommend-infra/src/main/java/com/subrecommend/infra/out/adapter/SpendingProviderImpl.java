package com.subrecommend.infra.out.adapter;

import com.subrecommend.biz.dto.SpendingDTO;
import com.subrecommend.biz.usecase.outport.ISpendingProvider;
import com.subrecommend.infra.out.entity.SpendingEntity;
import com.subrecommend.infra.out.repo.ISpendingRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SpendingProviderImpl implements ISpendingProvider {
    private final ISpendingRepository spendingRepository;

    public SpendingProviderImpl(ISpendingRepository spendingRepository) {
        this.spendingRepository = spendingRepository;
    }

    @Override
    public List<SpendingDTO> getUserSpending(String userId) {
        return spendingRepository.findByUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private SpendingDTO mapToDTO(SpendingEntity entity) {
        SpendingDTO dto = new SpendingDTO();
        dto.setCategory(entity.getCategory());
        dto.setAmount(entity.getAmount());
        dto.setDate(entity.getDate());
        return dto;
    }
}