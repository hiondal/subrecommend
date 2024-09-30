package com.subrecommend.infra.out.repo;

import com.subrecommend.infra.out.entity.SpendingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ISpendingRepository extends JpaRepository<SpendingEntity, Long> {
    List<SpendingEntity> findByUserId(String userId);
}