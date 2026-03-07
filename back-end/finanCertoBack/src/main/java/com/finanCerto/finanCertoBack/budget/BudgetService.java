package com.finanCerto.finanCertoBack.budget;

import com.finanCerto.finanCertoBack.category.Category;
import com.finanCerto.finanCertoBack.category.CategoryService;
import com.finanCerto.finanCertoBack.exception.BudgetAlreadyExistsException;
import com.finanCerto.finanCertoBack.exception.BudgetNotFoundException;
import com.finanCerto.finanCertoBack.budget.dto.BudgetRegistrationDto;
import com.finanCerto.finanCertoBack.budget.dto.BudgetResponseDto;
import com.finanCerto.finanCertoBack.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository repository;
    private final CategoryService categoryService;

    private User getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Optional<?> opt && opt.isPresent() && opt.get() instanceof User user) {
            return user;
        }
        if (principal instanceof User user) {
            return user;
        }
        throw new RuntimeException("User not authenticated");
    }

    private BudgetResponseDto toDto(Budget b) {
        return new BudgetResponseDto(
                b.getId(),
                b.getLimitValue(),
                b.getCurrentValue(),
                b.getName(),
                b.getDeadline(),
                b.getCategory() != null ? b.getCategory().getName() : "N/A"
        );
    }

    @Transactional
    public BudgetResponseDto register(BudgetRegistrationDto dto) {
        log.info("Creating budget '{}' for user", dto.name());
        Category category = categoryService.findEntityByName(dto.categoryName());
        User user = getAuthenticatedUser();

        if (repository.existsByUserIdAndName(user.getId(), dto.name())) {
            log.warn("Budget creation failed: name '{}' already exists for this user", dto.name());
            throw new BudgetAlreadyExistsException("Budget with this name already exists for this user");
        }

        Budget budget = new Budget(dto, category, user);
        Budget saved = repository.save(budget);
        log.info("Budget created with id={}", saved.getId());
        return toDto(saved);
    }

    public BudgetResponseDto getByName(String name) {
        User user = getAuthenticatedUser();
        log.info("Fetching budget '{}' for user '{}'", name, user.getEmail());
        Budget budget = repository.findByUserIdAndName(user.getId(), name);
        if (budget == null) {
            throw new BudgetNotFoundException("Budget not found");
        }
        return toDto(budget);
    }

    public Page<BudgetResponseDto> getByUserPaged(int page, int size) {
        User user = getAuthenticatedUser();
        log.info("Listing budgets for user '{}' - page: {}, size: {}", user.getEmail(), page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return repository.findByUserId(user.getId(), pageable).map(this::toDto);
    }

    public Page<BudgetResponseDto> getByCategory(String categoryName, int page, int size) {
        log.info("Fetching budgets for category '{}'", categoryName);
        Category category = categoryService.findEntityByName(categoryName);
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return repository.findByCategoryId(category.getId(), pageable).map(this::toDto);
    }

    @Transactional
    public void add(double value, Category category) {
        log.info("Adding value {} to budget of category '{}'", value, category.getName());
        Pageable pageable = PageRequest.of(0, 1);
        Page<Budget> budgetsPage = repository.findByCategoryId(category.getId(), pageable);
        if (!budgetsPage.isEmpty()) {
            Budget budget = budgetsPage.getContent().get(0);
            budget.setCurrentValue(budget.getCurrentValue() + value);
            log.info("Value added to budget: {}", budget.getName());
        } else {
            log.warn("No budget found for category: {}", category.getName());
        }
    }
}
