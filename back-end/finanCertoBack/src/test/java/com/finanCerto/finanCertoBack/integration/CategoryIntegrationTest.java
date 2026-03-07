package com.finanCerto.finanCertoBack.integration;

import com.finanCerto.finanCertoBack.category.*;
import com.finanCerto.finanCertoBack.user.User;
import com.finanCerto.finanCertoBack.user.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CategoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private User userTest;

    @BeforeAll
    void setUp() {
        // Clear only categories to avoid conflicts
        categoryRepository.deleteAll();
        
        // Create user with unique email for this test
        userTest = new User();
        userTest.setName("Test User Category");
        userTest.setEmail("category-test@example.com");
        userTest.setPassword("password123");
        userTest = userRepository.save(userTest);
    }

    @BeforeEach
    void cleanUp() {
        // Clear only categories between tests, keeping the user
        categoryRepository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("Should create and persist a category successfully")
    void testCreateCategory_Success() {
        Category category = new Category();
        category.setName("Food");
        category.setType(CategoryType.EXPENSE);
        category.setUser(userTest);

        Category saved = categoryRepository.save(category);

        assertNotNull(saved.getId());
        assertEquals("Food", saved.getName());
        assertEquals(CategoryType.EXPENSE, saved.getType());
        assertEquals(userTest.getId(), saved.getUser().getId());
    }

    @Test
    @Order(2)
    @DisplayName("Should find category by ID")
    void testFindCategoryById_Success() {
        Category category = new Category();
        category.setName("Salary");
        category.setType(CategoryType.INCOME);
        category.setUser(userTest);
        Category saved = categoryRepository.save(category);

        Optional<Category> found = categoryRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Salary", found.get().getName());
        assertEquals(CategoryType.INCOME, found.get().getType());
    }

    @Test
    @Order(3)
    @DisplayName("Should list categories by user")
    void testListCategoriesByUser_Success() {
        Category cat1 = new Category();
        cat1.setName("Food");
        cat1.setType(CategoryType.EXPENSE);
        cat1.setUser(userTest);
        
        Category cat2 = new Category();
        cat2.setName("Transport");
        cat2.setType(CategoryType.EXPENSE);
        cat2.setUser(userTest);
        
        Category cat3 = new Category();
        cat3.setName("Salary");
        cat3.setType(CategoryType.INCOME);
        cat3.setUser(userTest);
        
        categoryRepository.save(cat1);
        categoryRepository.save(cat2);
        categoryRepository.save(cat3);

        List<Category> categories = categoryRepository.findByUserId(userTest.getId());

        assertEquals(3, categories.size());
        assertTrue(categories.stream().anyMatch(c -> "Food".equals(c.getName())));
        assertTrue(categories.stream().anyMatch(c -> "Transport".equals(c.getName())));
        assertTrue(categories.stream().anyMatch(c -> "Salary".equals(c.getName())));
    }

    @Test
    @Order(4)
    @DisplayName("Should verify category existence by user and name")
    void testVerifyCategoryExistence_Success() {
        Category category = new Category();
        category.setName("Food");
        category.setType(CategoryType.EXPENSE);
        category.setUser(userTest);
        categoryRepository.save(category);

        assertTrue(categoryRepository.existsByUserIdAndName(userTest.getId(), "Food"));
        assertFalse(categoryRepository.existsByUserIdAndName(userTest.getId(), "Transport"));
    }

    @Test
    @Order(5)
    @DisplayName("Should fail creating category with duplicate name for same user")
    void testCreateCategory_DuplicateName_Failure() {
        Category cat1 = new Category();
        cat1.setName("Food");
        cat1.setType(CategoryType.EXPENSE);
        cat1.setUser(userTest);
        categoryRepository.save(cat1);

        Category cat2 = new Category();
        cat2.setName("Food");
        cat2.setType(CategoryType.EXPENSE);
        cat2.setUser(userTest);

        assertThrows(Exception.class, () -> categoryRepository.save(cat2));
    }

    @Test
    @Order(6)
    @DisplayName("Should allow category with same name for different users")
    void testCreateCategory_SameNameDifferentUsers_Success() {
        User user2 = new User();
        user2.setName("Another User");
        user2.setEmail("another@example.com");
        user2.setPassword("password123");
        user2 = userRepository.save(user2);

        Category cat1 = new Category();
        cat1.setName("Food");
        cat1.setType(CategoryType.EXPENSE);
        cat1.setUser(userTest);
        
        Category cat2 = new Category();
        cat2.setName("Food");
        cat2.setType(CategoryType.EXPENSE);
        cat2.setUser(user2);
        
        categoryRepository.save(cat1);

        Category saved2 = categoryRepository.save(cat2);

        assertNotNull(saved2.getId());
        assertEquals(user2.getId(), saved2.getUser().getId());
    }

    @Test
    @Order(7)
    @DisplayName("Should fail creating category with null name")
    void testCreateCategory_NullName_Failure() {
        Category category = new Category();
        category.setType(CategoryType.EXPENSE);
        category.setUser(userTest);

        assertThrows(Exception.class, () -> {
            categoryRepository.save(category);
            categoryRepository.flush();
        });
    }

    @Test
    @Order(8)
    @DisplayName("Should fail creating category with null type")
    void testCreateCategory_NullType_Failure() {
        Category category = new Category();
        category.setName("Test Category");
        category.setUser(userTest);

        assertThrows(Exception.class, () -> {
            categoryRepository.save(category);
            categoryRepository.flush();
        });
    }

    @Test
    @Order(9)
    @DisplayName("Should fail creating category without user")
    void testCreateCategory_WithoutUser_Failure() {
        Category category = new Category();
        category.setName("Test Category");
        category.setType(CategoryType.EXPENSE);

        assertThrows(Exception.class, () -> {
            categoryRepository.save(category);
            categoryRepository.flush(); 
        });
    }

    @Test
    @Order(10)
    @DisplayName("Should update category successfully")
    void testUpdateCategory_Success() {
        Category category = new Category();
        category.setName("Food");
        category.setType(CategoryType.EXPENSE);
        category.setUser(userTest);
        Category saved = categoryRepository.save(category);

        saved.setName("Restaurant");
        saved.setType(CategoryType.INCOME);
        Category updated = categoryRepository.save(saved);

        assertEquals("Restaurant", updated.getName());
        assertEquals(CategoryType.INCOME, updated.getType());
        assertEquals(userTest.getId(), updated.getUser().getId());
    }

    @Test
    @Order(11)
    @DisplayName("Should delete category successfully")
    void testDeleteCategory_Success() {
        Category category = new Category();
        category.setName("Food");
        category.setType(CategoryType.EXPENSE);
        category.setUser(userTest);
        Category saved = categoryRepository.save(category);
        Long id = saved.getId();

        categoryRepository.delete(saved);

        Optional<Category> found = categoryRepository.findById(id);
        assertFalse(found.isPresent());
    }

    @Test
    @Order(12)
    @DisplayName("Should validate relationship with user")
    void testUserRelationship_Success() {
        Category category = new Category();
        category.setName("Food");
        category.setType(CategoryType.EXPENSE);
        category.setUser(userTest);
        Category saved = categoryRepository.save(category);

        Optional<Category> found = categoryRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertNotNull(found.get().getUser());
        assertEquals(userTest.getId(), found.get().getUser().getId());
        assertEquals(userTest.getName(), found.get().getUser().getName());
        assertEquals(userTest.getEmail(), found.get().getUser().getEmail());
    }

    @Test
    @Order(13)
    @DisplayName("Should validate rollback on failure")
    void testRollbackOnFailure_Success() {
        Category cat1 = new Category();
        cat1.setName("Food");
        cat1.setType(CategoryType.EXPENSE);
        cat1.setUser(userTest);
        Category saved1 = categoryRepository.save(cat1);
        assertNotNull(saved1.getId());

        assertThrows(RuntimeException.class, () -> {
            Category cat2 = new Category();
            cat2.setName("Food");
            cat2.setType(CategoryType.EXPENSE);
            cat2.setUser(userTest);
            categoryRepository.save(cat2);
        });

        assertEquals(1, categoryRepository.count());
        assertTrue(categoryRepository.existsByUserIdAndName(userTest.getId(), "Food"));
    }
}
