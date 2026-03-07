package com.finanCerto.finanCertoBack.unit;

import com.finanCerto.finanCertoBack.category.Category;
import com.finanCerto.finanCertoBack.category.CategoryService;
import com.finanCerto.finanCertoBack.exception.BudgetAlreadyExistsException;
import com.finanCerto.finanCertoBack.exception.BudgetNotFoundException;
import com.finanCerto.finanCertoBack.budget.Budget;
import com.finanCerto.finanCertoBack.budget.BudgetRepository;
import com.finanCerto.finanCertoBack.budget.BudgetService;
import com.finanCerto.finanCertoBack.budget.dto.BudgetRegistrationDto;
import com.finanCerto.finanCertoBack.budget.dto.BudgetResponseDto;
import com.finanCerto.finanCertoBack.security.TokenService;
import com.finanCerto.finanCertoBack.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BudgetServiceTest {

    @InjectMocks
    private BudgetService budgetService;
    @Mock
    private BudgetRepository repository;
    @Mock
    private CategoryService categoryService;
    @Mock
    private TokenService tokenService;

    private User user;
    private Category category;
    private BudgetRegistrationDto dto;
    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setName("Tester");
        user.setEmail("tester@mail.com");
        user.setPassword("secret");

        category = new Category();
        category.setName("Food");
        category.setId(1L);

        dto = new BudgetRegistrationDto(
                500.0,
                100.0,
                "Monthly",
                LocalDate.now().plusDays(10),
                category.getName(),
                "token-123"
        );

        // Mock SecurityContextHolder
        mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityContextHolder.close();
    }

    @Test
    @DisplayName("Should register budget when no other with same name exists")
    void shouldRegisterBudget() {
        when(categoryService.findEntityByName(dto.categoryName())).thenReturn(category);
        when(repository.existsByUserIdAndName(user.getId(), dto.name())).thenReturn(false);
        when(repository.save(any(Budget.class))).thenAnswer(inv -> {
            Budget b = inv.getArgument(0);
            b.setId(1L);
            return b;
        });

        BudgetResponseDto response = budgetService.register(dto);

        assertEquals("Monthly", response.name());
        assertEquals(100.0, response.currentValue());
        verify(repository).save(any(Budget.class));
    }

    @Test
    @DisplayName("Should throw error when registering duplicate budget for same user")
    void shouldPreventDuplicate() {
        when(categoryService.findEntityByName(dto.categoryName())).thenReturn(category);
        when(repository.existsByUserIdAndName(user.getId(), dto.name())).thenReturn(true);

        assertThrows(BudgetAlreadyExistsException.class, () -> budgetService.register(dto));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should get budget by name using token and throw error if not found")
    void shouldGetByNameOrThrow() {
        Budget budget = new Budget(dto, category, user);
        budget.setId(1L);
        when(repository.findByUserIdAndName(user.getId(), "Monthly")).thenReturn(budget);

        BudgetResponseDto outcome = budgetService.getByName("Monthly");
        assertEquals("Monthly", outcome.name());

        when(repository.findByUserIdAndName(user.getId(), "Monthly")).thenReturn(null);
        assertThrows(BudgetNotFoundException.class, () -> budgetService.getByName("Monthly"));
    }

    @Test
    @DisplayName("Should get budget by category or throw exception")
    void shouldGetByCategoryOrThrow() {
        Budget budget = new Budget(dto, category, user);
        budget.setId(1L);
        when(categoryService.findEntityByName("Food")).thenReturn(category);
        when(repository.findByCategoryId(category.getId(), PageRequest.of(0, 10, Sort.by("id").descending())))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(java.util.List.of(budget)));

        var outcome = budgetService.getByCategory("Food", 0, 10);
        assertEquals(1, outcome.getContent().size());
        assertEquals("Monthly", outcome.getContent().get(0).name());
    }

    @Test
    @DisplayName("Should add value to budget for given category")
    void shouldAddValue() {
        Budget budget = new Budget(dto, category, user);
        budget.setCurrentValue(200.0);
        when(repository.findByCategoryId(category.getId(), PageRequest.of(0, 1)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(java.util.List.of(budget)));

        budgetService.add(50.0, category);

        assertEquals(250.0, budget.getCurrentValue());
    }
}
