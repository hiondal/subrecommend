package com.subrecommend.infra.out.repo;

import com.subrecommend.infra.out.entity.SubscriptionCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ISubscriptionCategoryRepository extends JpaRepository<SubscriptionCategoryEntity, Long> {
}