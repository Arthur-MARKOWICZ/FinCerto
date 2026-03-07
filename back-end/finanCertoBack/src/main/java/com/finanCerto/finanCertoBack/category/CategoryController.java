package com.finanCerto.finanCertoBack.category;

import com.finanCerto.finanCertoBack.category.dto.CategoryRegistrationDto;
import com.finanCerto.finanCertoBack.category.dto.CategoryResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService service;

    @GetMapping
    public ResponseEntity<Page<CategoryResponseDto>> listCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Request to list all categories - page: {}, size: {}", page, size);
        Page<CategoryResponseDto> response = service.listAllPaged(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user")
    public ResponseEntity<Page<CategoryResponseDto>> listByUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        log.info("Request to list user categories - page: {}, size: {}", page, size);
        Page<CategoryResponseDto> response = service.getByUserPaged(page, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDto> register(@RequestBody CategoryRegistrationDto dto) {
        log.info("Request to create category: {}", dto.name());
        CategoryResponseDto response = service.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
