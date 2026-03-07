package com.finanCerto.finanCertoBack.transaction;

import com.finanCerto.finanCertoBack.category.Category;
import com.finanCerto.finanCertoBack.category.CategoryService;
import com.finanCerto.finanCertoBack.account.Account;
import com.finanCerto.finanCertoBack.account.AccountService;
import com.finanCerto.finanCertoBack.exception.TransactionNotFoundException;
import com.finanCerto.finanCertoBack.budget.BudgetService;
import com.finanCerto.finanCertoBack.transaction.dto.TransactionRegistrationDto;
import com.finanCerto.finanCertoBack.transaction.dto.TransactionResponseDto;
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
public class TransactionService {

    private final TransactionRepository repository;
    private final CategoryService categoryService;
    private final AccountService accountService;
    private final BudgetService budgetService;

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

    private String getCategoryName(Transaction t) {
        return t.getCategory() != null ? t.getCategory().getName() : "N/A";
    }

    public Page<TransactionResponseDto> getByAccountPaged(String accountName, int page, int size) {
        log.info("Fetching transactions for account '{}'", accountName);
        Account account = accountService.findEntityByName(accountName);

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return repository.findByAccountId(account.getId(), pageable)
                .map(t -> new TransactionResponseDto(t.getId(), t.getAmount(), t.getDate(), t.getDescription(), t.getType(), t.getAccount().getName(), getCategoryName(t)));
    }

    public Page<TransactionResponseDto> getByCategoryPaged(String categoryName, int page, int size) {
        log.info("Fetching transactions for category '{}'", categoryName);
        Category category = categoryService.findEntityByName(categoryName);

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return repository.findByCategoryId(category.getId(), pageable)
                .map(t -> new TransactionResponseDto(t.getId(), t.getAmount(), t.getDate(), t.getDescription(), t.getType(), t.getAccount().getName(), getCategoryName(t)));
    }

    @Transactional
    public TransactionResponseDto register(TransactionRegistrationDto dto) {
        log.info("Registering transaction: {}", dto.description());
        User user = getAuthenticatedUser();
        Account account = accountService.findEntityByName(dto.accountName());
        Category category = categoryService.findEntityByName(dto.categoryName());

        Transaction transaction = new Transaction(dto, user, category, account);

        if (transaction.getType() == TransactionType.EXPENSE) {
            accountService.subtractBalance(transaction.getAmount(), transaction.getAccount());
            try {
                budgetService.add(transaction.getAmount(), category);
            } catch (Exception e) {
                log.warn("Budget update failed for category {}: {}", category.getName(), e.getMessage());
            }
        } else if (transaction.getType() == TransactionType.INCOME) {
            accountService.addBalance(transaction.getAmount(), transaction.getAccount());
        }

        Transaction saved = repository.save(transaction);
        log.info("Transaction saved with id={}", saved.getId());
        return new TransactionResponseDto(saved.getId(), saved.getAmount(), saved.getDate(), saved.getDescription(), saved.getType(), saved.getAccount().getName(), saved.getCategory().getName());
    }

    public TransactionResponseDto getById(Long id) {
        log.info("Fetching transaction by id={}", id);
        Transaction t = repository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));
        return new TransactionResponseDto(t.getId(), t.getAmount(), t.getDate(), t.getDescription(), t.getType(), t.getAccount().getName(), getCategoryName(t));
    }
}
