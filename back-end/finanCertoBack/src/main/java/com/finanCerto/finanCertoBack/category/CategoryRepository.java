package com.finanCerto.finanCertoBack.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Page<Category> findByUserId(Long userId, Pageable pageable);
    List<Category> findByUserId(Long userId);
    boolean existsByUserIdAndName(Long userId, String name);
    Optional<Category> findByNameAndUserId(String name, Long userId);
}
