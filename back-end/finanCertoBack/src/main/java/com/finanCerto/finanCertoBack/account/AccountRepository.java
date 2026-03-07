package com.finanCerto.finanCertoBack.account;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByUserIdAndName(Long userId, String name);

    @Query("SELECT a FROM Account a WHERE a.user.id = :userId AND a.name = :name")
    Optional<Account> findByNameAndUserId(Long userId, String name);

    @Query("SELECT a From Account a WHERE a.user.id = :userId")
    Page<Account> findAllByUserId(Long userId, Pageable pageable);
}
