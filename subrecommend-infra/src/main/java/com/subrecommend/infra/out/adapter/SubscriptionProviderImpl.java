package com.subrecommend.infra.out.adapter;

import com.subrecommend.biz.dto.SubscriptionDTO;
import com.subrecommend.biz.dto.SubscriptionDetailDTO;
import com.subrecommend.biz.usecase.outport.ISubscriptionProvider;
import com.subrecommend.infra.out.entity.SubscriptionEntity;
import com.subrecommend.infra.out.repo.ISubscriptionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SubscriptionProviderImpl implements ISubscriptionProvider {
    private final ISubscriptionRepository subscriptionRepository;

    public SubscriptionProviderImpl(ISubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public List<SubscriptionDTO> getSubscriptionsByCategory(String category) {
        return subscriptionRepository.findByCategory(category).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SubscriptionDetailDTO getSubscriptionById(String subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .map(this::mapToDetailDTO)
                .orElse(null);
    }

    private SubscriptionDTO mapToDTO(SubscriptionEntity entity) {
        SubscriptionDTO dto = new SubscriptionDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setPrice(entity.getPrice());
        dto.setLogo(entity.getLogo());
        return dto;
    }

    private SubscriptionDetailDTO mapToDetailDTO(SubscriptionEntity entity) {
        SubscriptionDetailDTO dto = new SubscriptionDetailDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCategory(entity.getCategory());
        dto.setDescription(entity.getDescription());
        dto.setPrice(entity.getPrice());
        dto.setLogo(entity.getLogo());
        dto.setMaxSharing(entity.getMaxSharing());
        return dto;
    }
}