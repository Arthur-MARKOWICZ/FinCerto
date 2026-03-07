package com.finanCerto.finanCertoBack.transaction;

import com.finanCerto.finanCertoBack.transaction.dto.TransactionRegistrationDto;
import com.finanCerto.finanCertoBack.transaction.dto.TransactionResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService service;

    @GetMapping("/account/{accountName}/paged")
    public ResponseEntity<Page<TransactionResponseDto>> getByAccountPaged(
            @PathVariable String accountName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Request for transactions of account '{}' - page: {}, size: {}", accountName, page, size);
        Page<TransactionResponseDto> response = service.getByAccountPaged(accountName, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{categoryName}/paged")
    public ResponseEntity<Page<TransactionResponseDto>> getByCategoryPaged(
            @PathVariable String categoryName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Request for transactions of category '{}' - page: {}, size: {}", categoryName, page, size);
        Page<TransactionResponseDto> response = service.getByCategoryPaged(categoryName, page, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<TransactionResponseDto> register(@RequestBody TransactionRegistrationDto dto) {
        log.info("Request to create transaction: {}", dto.description());
        TransactionResponseDto response = service.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponseDto> getById(@PathVariable Long id) {
        log.info("Request for transaction id={}", id);
        TransactionResponseDto response = service.getById(id);
        return ResponseEntity.ok(response);
    }
}
