package com.finanCerto.finanCertoBack.category;

import com.finanCerto.finanCertoBack.category.dto.CategoryRegistrationDto;
import com.finanCerto.finanCertoBack.category.dto.CategoryResponseDto;
import com.finanCerto.finanCertoBack.exception.CategoryAlreadyExistsException;
import com.finanCerto.finanCertoBack.exception.CategoryNotFoundException;
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
public class CategoryService {

    private final CategoryRepository repository;

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

    public Page<CategoryResponseDto> listAllPaged(int page, int size) {
        log.info("Listing all categories - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        return repository.findAll(pageable)
                .map(cat -> new CategoryResponseDto(cat.getId(), cat.getName(), cat.getType()));
    }

    public Page<CategoryResponseDto> getByUserPaged(int page, int size) {
        User user = getAuthenticatedUser();
        log.info("Listing categories for user '{}' - page: {}, size: {}", user.getEmail(), page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return repository.findByUserId(user.getId(), pageable)
                .map(cat -> new CategoryResponseDto(cat.getId(), cat.getName(), cat.getType()));
    }

    @Transactional
    public CategoryResponseDto register(CategoryRegistrationDto dto) {
        User user = getAuthenticatedUser();
        log.info("Creating category '{}' for user '{}'", dto.name(), user.getEmail());

        if (repository.existsByUserIdAndName(user.getId(), dto.name())) {
            log.warn("Category creation failed: name '{}' already exists for this user", dto.name());
            throw new CategoryAlreadyExistsException("Category with this name already exists for this user");
        }

        Category category = new Category(dto);
        category.setUser(user);
        Category saved = repository.save(category);
        
        log.info("Category created successfully with id={}", saved.getId());
        return new CategoryResponseDto(saved.getId(), saved.getName(), saved.getType());
    }

    public CategoryResponseDto getCategoryByName(String name) {
        Category cat = findEntityByName(name);
        return new CategoryResponseDto(cat.getId(), cat.getName(), cat.getType());
    }

    public Category findEntityByName(String name) {
        User user = getAuthenticatedUser();
        log.info("Fetching category entity '{}' for user '{}'", name, user.getEmail());
        return repository.findByNameAndUserId(name, user.getId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
    }
}
