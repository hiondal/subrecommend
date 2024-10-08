package com.subspend.repository;

import com.subspend.model.Spending;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SpendingRepository extends JpaRepository<Spending, Long> {
    List<Spending> findByUserId(String userId);
}