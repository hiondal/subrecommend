package com.subrecommend.infra.out.repo;

import com.subrecommend.infra.out.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ICategoryRepository extends JpaRepository<CategoryEntity, Long> {
}