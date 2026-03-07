package com.finanCerto.finanCertoBack.transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByUserId(Long userId, Pageable pageable);
    Page<Transaction> findByAccountId(Long accountId, Pageable pageable);
    Page<Transaction> findByCategoryId(Long categoryId, Pageable pageable);
}
