package com.finanCerto.finanCertoBack.budget;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    boolean existsByUserIdAndName(Long userId, String name);
    Budget findByUserIdAndName(Long userId, String name);
    Page<Budget> findByUserId(Long userId, Pageable pageable);
    Page<Budget> findByCategoryId(Long categoryId, Pageable pageable);
    Budget findByCategory(com.finanCerto.finanCertoBack.category.Category category);
}
