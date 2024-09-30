package com.subrecommend.infra.out.repo;

import com.subrecommend.infra.out.entity.SubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ISubscriptionRepository extends JpaRepository<SubscriptionEntity, String> {
    List<SubscriptionEntity> findByCategory(String category);
}