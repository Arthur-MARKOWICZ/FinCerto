package com.finanCerto.finanCertoBack.account;

import com.finanCerto.finanCertoBack.account.dtos.AccountRegistrationDto;
import com.finanCerto.finanCertoBack.account.dtos.AccountResponseDto;
import com.finanCerto.finanCertoBack.exception.AccountAlreadyExistsException;
import com.finanCerto.finanCertoBack.exception.AccountNotFoundException;
import com.finanCerto.finanCertoBack.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository repository;

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

    @Transactional
    public AccountResponseDto register(AccountRegistrationDto dto) {
        User user = getAuthenticatedUser();
        log.info("Creating account '{}' for user '{}'", dto.name(), user.getEmail());

        if (repository.existsByUserIdAndName(user.getId(), dto.name())) {
            log.warn("Account creation failed: name already exists for this user");
            throw new AccountAlreadyExistsException("Account with this name already exists");
        }

        Account account = new Account(dto, user);
        Account savedAccount = repository.save(account);
        log.info("Account created successfully with id={}", savedAccount.getId());

        return new AccountResponseDto(savedAccount.getId(), savedAccount.getName(), savedAccount.getType(), savedAccount.getInitialBalance());
    }

    public AccountResponseDto getAccountByName(String name) {
        Account account = findEntityByName(name);
        return new AccountResponseDto(account.getId(), account.getName(), account.getType(), account.getInitialBalance());
    }

    public Account findEntityByName(String name) {
        User user = getAuthenticatedUser();
        log.info("Fetching account entity '{}' for user '{}'", name, user.getEmail());
        return repository.findByNameAndUserId(user.getId(), name)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
    }

    public Page<AccountResponseDto> getAllByUserId(Pageable pageable) {
        User user = getAuthenticatedUser();
        log.info("Listing all accounts for user '{}'", user.getEmail());

        return repository.findAllByUserId(user.getId(), pageable)
                .map(c -> new AccountResponseDto(c.getId(), c.getName(), c.getType(), c.getInitialBalance()));
    }

    @Transactional
    public AccountResponseDto updateAccount(Long id, AccountRegistrationDto dto) {
        User user = getAuthenticatedUser();
        log.info("Updating account id={} for user '{}'", id, user.getEmail());

        Account account = repository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        if (account.getUser().getId() != user.getId()) {
            throw new RuntimeException("Unauthorized to modify this account");
        }

        account.setName(dto.name());
        account.setType(dto.type());
        account.setInitialBalance(dto.initialBalance());

        log.info("Account updated successfully");
        return new AccountResponseDto(account.getId(), account.getName(), account.getType(), account.getInitialBalance());
    }

    @Transactional
    public void addBalance(double value, Account account) {
        log.info("Adding {} to account '{}'", value, account.getName());
        account.setInitialBalance(account.getInitialBalance() + value);
    }

    @Transactional
    public void subtractBalance(double value, Account account) {
        log.info("Subtracting {} from account '{}'", value, account.getName());
        account.setInitialBalance(account.getInitialBalance() - value);
    }
}
