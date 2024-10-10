package com.subrecommend.infra.out.repo;

import com.subrecommend.infra.out.entity.TopSpendingView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ITopSpendingViewRepository extends JpaRepository<TopSpendingView, String> {
}