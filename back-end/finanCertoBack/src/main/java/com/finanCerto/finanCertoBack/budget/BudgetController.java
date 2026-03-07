package com.finanCerto.finanCertoBack.budget;

import com.finanCerto.finanCertoBack.budget.dto.BudgetRegistrationDto;
import com.finanCerto.finanCertoBack.budget.dto.BudgetResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService service;

    @PostMapping
    public ResponseEntity<BudgetResponseDto> register(@RequestBody BudgetRegistrationDto dto) {
        log.info("Request to create budget: {}", dto.name());
        BudgetResponseDto response = service.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/user/paged")
    public ResponseEntity<Page<BudgetResponseDto>> getByUserPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        log.info("Request for user budgets - page: {}, size: {}", page, size);
        Page<BudgetResponseDto> response = service.getByUserPaged(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{categoryName}")
    public ResponseEntity<Page<BudgetResponseDto>> getByCategory(
            @PathVariable String categoryName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        log.info("Request for budgets of category '{}' - page: {}, size: {}", categoryName, page, size);
        Page<BudgetResponseDto> response = service.getByCategory(categoryName, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<BudgetResponseDto> getByName(@PathVariable String name) {
        log.info("Request for budget by name: {}", name);
        BudgetResponseDto response = service.getByName(name);
        return ResponseEntity.ok(response);
    }
}
