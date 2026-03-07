package com.finanCerto.finanCertoBack.unit;

import com.finanCerto.finanCertoBack.category.Category;
import com.finanCerto.finanCertoBack.category.CategoryRepository;
import com.finanCerto.finanCertoBack.category.CategoryService;
import com.finanCerto.finanCertoBack.category.CategoryType;
import com.finanCerto.finanCertoBack.category.dto.CategoryRegistrationDto;
import com.finanCerto.finanCertoBack.category.dto.CategoryResponseDto;
import com.finanCerto.finanCertoBack.exception.CategoryAlreadyExistsException;
import com.finanCerto.finanCertoBack.exception.CategoryNotFoundException;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @InjectMocks
    private CategoryService categoryService;
    @Mock
    private CategoryRepository repository;

    private User user;
    private CategoryRegistrationDto dto;
    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setName("Tester");
        user.setEmail("tester@mail.com");
        user.setPassword("secret");

        dto = new CategoryRegistrationDto("Leisure", CategoryType.EXPENSE, "token-123");

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
    @DisplayName("Should register category when no other with the same name exists")
    void shouldRegisterCategory() {
        when(repository.existsByUserIdAndName(user.getId(), dto.name())).thenReturn(false);
        when(repository.save(any(Category.class))).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            c.setId(10L);
            return c;
        });

        CategoryResponseDto response = categoryService.register(dto);

        assertEquals("Leisure", response.name());
        verify(repository).save(any(Category.class));
    }

    @Test
    @DisplayName("Should throw exception when trying to register duplicate category")
    void shouldPreventDuplicateCategory() {
        when(repository.existsByUserIdAndName(user.getId(), dto.name())).thenReturn(true);

        assertThrows(CategoryAlreadyExistsException.class, () -> categoryService.register(dto));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should get category by name or throw CategoryNotFoundException")
    void shouldGetCategoryOrThrow() {
        Category category = new Category();
        category.setId(10L);
        category.setName("Leisure");
        category.setType(CategoryType.EXPENSE);
        
        when(repository.findByNameAndUserId("Leisure", user.getId())).thenReturn(Optional.of(category));

        CategoryResponseDto response = categoryService.getCategoryByName("Leisure");
        assertEquals("Leisure", response.name());

        when(repository.findByNameAndUserId("Leisure", user.getId())).thenReturn(Optional.empty());
        assertThrows(CategoryNotFoundException.class, () -> categoryService.getCategoryByName("Leisure"));
    }
}
