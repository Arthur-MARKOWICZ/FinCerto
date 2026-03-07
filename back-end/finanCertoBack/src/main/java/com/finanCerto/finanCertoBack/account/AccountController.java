package com.finanCerto.finanCertoBack.account;

import com.finanCerto.finanCertoBack.account.dtos.AccountRegistrationDto;
import com.finanCerto.finanCertoBack.account.dtos.AccountResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService service;

    @PostMapping
    public ResponseEntity<AccountResponseDto> register(@RequestBody AccountRegistrationDto dto) {
        log.info("Received request to create account: {}", dto.name());
        AccountResponseDto response = service.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<AccountResponseDto> getAccountByName(@PathVariable String name) {
        log.info("Request to fetch account by name: {}", name);
        AccountResponseDto response = service.getAccountByName(name);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user")
    public ResponseEntity<Page<AccountResponseDto>> getAllByUserId(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Request to list user accounts - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<AccountResponseDto> response = service.getAllByUserId(pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountResponseDto> updateAccount(@PathVariable Long id, @RequestBody AccountRegistrationDto dto) {
        log.info("Request to update account id: {}", id);
        AccountResponseDto response = service.updateAccount(id, dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/balance/{name}")
    public ResponseEntity<Double> getBalanceByName(@PathVariable String name) {
        log.info("Request for balance of account: {}", name);
        AccountResponseDto response = service.getAccountByName(name);
        return ResponseEntity.ok(response.initialBalance());
    }
}
