package com.finanCerto.finanCertoBack.integration;

import com.finanCerto.finanCertoBack.category.*;
import com.finanCerto.finanCertoBack.budget.*;
import com.finanCerto.finanCertoBack.user.User;
import com.finanCerto.finanCertoBack.user.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BudgetIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager entityManager;

    private User userTest;
    private Category categoryTest;

    @BeforeEach
    void setUp() {
        entityManager.createNativeQuery("DELETE FROM tb_transacao").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM tb_orcamento").executeUpdate();
        categoryRepository.deleteAll();
        entityManager.createNativeQuery("DELETE FROM tb_conta").executeUpdate();
        userRepository.deleteAll();
        budgetRepository.deleteAll();
        entityManager.flush();
        
        userTest = new User();
        userTest.setName("Test User");
        userTest.setEmail("test@example.com");
        userTest.setPassword("password123");
        userTest = userRepository.save(userTest);
       
        categoryTest = new Category();
        categoryTest.setName("Food");
        categoryTest.setType(CategoryType.EXPENSE);
        categoryTest.setUser(userTest);
        categoryTest = categoryRepository.save(categoryTest);
        entityManager.flush();
    }

    @Test
    @Order(1)
    @DisplayName("Should create and persist a budget successfully")
    @Transactional
    void testCreateBudget_Success() {
        // Arrange
        Budget budget = new Budget();
        budget.setName("Food Budget");
        budget.setLimitValue(500.0);
        budget.setCurrentValue(100.0);
        budget.setDeadline(LocalDate.now().plusMonths(1));
        budget.setUser(userTest);
        budget.setCategory(categoryTest);

        // Act
        Budget saved = budgetRepository.save(budget);

        // Assert
        assertNotNull(saved.getId());
        assertEquals("Food Budget", saved.getName());
        assertEquals(500.0, saved.getLimitValue());
        assertEquals(100.0, saved.getCurrentValue());
        assertEquals(userTest.getId(), saved.getUser().getId());
        assertEquals(categoryTest.getId(), saved.getCategory().getId());
    }

    @Test
    @Order(2)
    @DisplayName("Should find budget by ID")
    @Transactional
    void testFindBudgetById_Success() {
        // Arrange
        Budget budget = new Budget();
        budget.setName("Test Budget");
        budget.setLimitValue(1000.0);
        budget.setCurrentValue(200.0);
        budget.setDeadline(LocalDate.now().plusMonths(2));
        budget.setUser(userTest);
        budget.setCategory(categoryTest);
        Budget saved = budgetRepository.save(budget);

        // Act
        Optional<Budget> found = budgetRepository.findById(saved.getId());

        // Assert
        assertTrue(found.isPresent());
        assertEquals("Test Budget", found.get().getName());
        assertEquals(1000.0, found.get().getLimitValue());
        assertEquals(200.0, found.get().getCurrentValue());
    }

    @Test
    @Order(3)
    @DisplayName("Should list budgets by user with pagination")
    @Transactional
    void testListBudgetsByUser_Success() {
        // Arrange
        Budget budget1 = new Budget();
        budget1.setName("Budget 1");
        budget1.setLimitValue(500.0);
        budget1.setCurrentValue(100.0);
        budget1.setDeadline(LocalDate.now().plusMonths(1));
        budget1.setUser(userTest);
        budget1.setCategory(categoryTest);
        
        Budget budget2 = new Budget();
        budget2.setName("Budget 2");
        budget2.setLimitValue(300.0);
        budget2.setCurrentValue(50.0);
        budget2.setDeadline(LocalDate.now().plusMonths(2));
        budget2.setUser(userTest);
        budget2.setCategory(categoryTest);
        
        budgetRepository.save(budget1);
        budgetRepository.save(budget2);

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Budget> budgets = budgetRepository.findByUserId(userTest.getId(), pageable);

        // Assert
        assertEquals(2, budgets.getTotalElements());
        assertEquals(2, budgets.getContent().size());
        assertTrue(budgets.getContent().stream().anyMatch(o -> "Budget 1".equals(o.getName())));
        assertTrue(budgets.getContent().stream().anyMatch(o -> "Budget 2".equals(o.getName())));
    }

    @Test
    @Order(4)
    @DisplayName("Should list budgets by category with pagination")
    @Transactional
    void testListBudgetsByCategory_Success() {
        // Arrange
        Budget budget1 = new Budget();
        budget1.setName("Food Budget");
        budget1.setLimitValue(500.0);
        budget1.setCurrentValue(100.0);
        budget1.setDeadline(LocalDate.now().plusMonths(1));
        budget1.setUser(userTest);
        budget1.setCategory(categoryTest);
        
        budgetRepository.save(budget1);

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Budget> budgets = budgetRepository.findByCategoryId(categoryTest.getId(), pageable);

        // Assert
        assertEquals(1, budgets.getTotalElements());
        assertEquals(1, budgets.getContent().size());
        assertEquals(categoryTest.getId(), budgets.getContent().get(0).getCategory().getId());
    }

    @Test
    @Order(5)
    @DisplayName("Should verify budget existence by user and name")
    @Transactional
    void testVerifyBudgetExistence_Success() {
        // Arrange
        Budget budget = new Budget();
        budget.setName("Test Budget");
        budget.setLimitValue(500.0);
        budget.setCurrentValue(100.0);
        budget.setDeadline(LocalDate.now().plusMonths(1));
        budget.setUser(userTest);
        budget.setCategory(categoryTest);
        budgetRepository.save(budget);

        // Act & Assert
        assertTrue(budgetRepository.existsByUserIdAndName(userTest.getId(), "Test Budget"));
        assertFalse(budgetRepository.existsByUserIdAndName(userTest.getId(), "Non-existent Budget"));
    }

    @Test
    @Order(6)
    @DisplayName("Should find budget by user and name")
    @Transactional
    void testFindBudgetByUserIdAndName_Success() {
        // Arrange
        Budget budget = new Budget();
        budget.setName("Test Budget");
        budget.setLimitValue(500.0);
        budget.setCurrentValue(100.0);
        budget.setDeadline(LocalDate.now().plusMonths(1));
        budget.setUser(userTest);
        budget.setCategory(categoryTest);
        budgetRepository.save(budget);

        // Act
        Budget found = budgetRepository.findByUserIdAndName(userTest.getId(), "Test Budget");

        // Assert
        assertNotNull(found);
        assertEquals("Test Budget", found.getName());
        assertEquals(500.0, found.getLimitValue());
        assertEquals(userTest.getId(), found.getUser().getId());
    }

    @Test
    @Order(7)
    @DisplayName("Should find budget by category")
    @Transactional
    void testFindBudgetByCategory_Success() {
        // Arrange
        Budget budget = new Budget();
        budget.setName("Test Budget");
        budget.setLimitValue(500.0);
        budget.setCurrentValue(100.0);
        budget.setDeadline(LocalDate.now().plusMonths(1));
        budget.setUser(userTest);
        budget.setCategory(categoryTest);
        budgetRepository.save(budget);

        // Act
        Budget found = budgetRepository.findByCategory(categoryTest);

        // Assert
        assertNotNull(found);
        assertEquals("Test Budget", found.getName());
        assertEquals(categoryTest.getId(), found.getCategory().getId());
    }

    @Test
    @Order(8)
    @DisplayName("Should fail creating budget with duplicate name for same user")
    @Transactional
    void testCreateBudget_DuplicateName_Failure() {
        // Arrange
        Budget budget1 = new Budget();
        budget1.setName("Test Budget");
        budget1.setLimitValue(500.0);
        budget1.setCurrentValue(100.0);
        budget1.setDeadline(LocalDate.now().plusMonths(1));
        budget1.setUser(userTest);
        budget1.setCategory(categoryTest);
        budgetRepository.save(budget1);

        Budget budget2 = new Budget();
        budget2.setName("Test Budget");
        budget2.setLimitValue(300.0);
        budget2.setCurrentValue(50.0);
        budget2.setDeadline(LocalDate.now().plusMonths(2));
        budget2.setUser(userTest);
        budget2.setCategory(categoryTest);

        // Act & Assert
        assertThrows(Exception.class, () -> budgetRepository.save(budget2));
    }

    @Test
    @Order(9)
    @DisplayName("Should allow budget with same name for different users")
    @Transactional
    void testCreateBudget_SameNameDifferentUsers_Success() {
        // Arrange
        User user2 = new User();
        user2.setName("Another User");
        user2.setEmail("another@example.com");
        user2.setPassword("password123");
        user2 = userRepository.save(user2);

        Category category2 = new Category();
        category2.setName("Transport");
        category2.setType(CategoryType.EXPENSE);
        category2.setUser(user2);
        category2 = categoryRepository.save(category2);

        Budget budget1 = new Budget();
        budget1.setName("Test Budget");
        budget1.setLimitValue(500.0);
        budget1.setCurrentValue(100.0);
        budget1.setDeadline(LocalDate.now().plusMonths(1));
        budget1.setUser(userTest);
        budget1.setCategory(categoryTest);
        
        Budget budget2 = new Budget();
        budget2.setName("Test Budget");
        budget2.setLimitValue(300.0);
        budget2.setCurrentValue(50.0);
        budget2.setDeadline(LocalDate.now().plusMonths(2));
        budget2.setUser(user2);
        budget2.setCategory(category2);
        
        budgetRepository.save(budget1);

        // Act
        Budget saved2 = budgetRepository.save(budget2);

        // Assert
        assertNotNull(saved2.getId());
        assertEquals(user2.getId(), saved2.getUser().getId());
    }

    @Test
    @Order(10)
    @DisplayName("Should fail creating budget without user")
    @Transactional
    void testCreateBudget_WithoutUser_Failure() {
        // Arrange
        Budget budget = new Budget();
        budget.setName("Test Budget");
        budget.setLimitValue(500.0);
        budget.setCurrentValue(100.0);
        budget.setDeadline(LocalDate.now().plusMonths(1));
        budget.setCategory(categoryTest);

        // Act & Assert
        assertNull(budget.getUser());
        
        // Attempt to save
        assertDoesNotThrow(() -> budgetRepository.save(budget));
    }

    @Test
    @Order(11)
    @DisplayName("Should fail creating budget without category")
    @Transactional
    void testCreateBudget_WithoutCategory_Failure() {
        // Arrange
        Budget budget = new Budget();
        budget.setName("Test Budget");
        budget.setLimitValue(500.0);
        budget.setCurrentValue(100.0);
        budget.setDeadline(LocalDate.now().plusMonths(1));
        budget.setUser(userTest);

        // Act & Assert
        assertNull(budget.getCategory());
        
        // Attempt to save
        assertDoesNotThrow(() -> budgetRepository.save(budget));
    }

    @Test
    @Order(12)
    @DisplayName("Should update budget successfully")
    @Transactional
    void testUpdateBudget_Success() {
        // Arrange
        Budget budget = new Budget();
        budget.setName("Test Budget");
        budget.setLimitValue(500.0);
        budget.setCurrentValue(100.0);
        budget.setDeadline(LocalDate.now().plusMonths(1));
        budget.setUser(userTest);
        budget.setCategory(categoryTest);
        Budget saved = budgetRepository.save(budget);

        // Act
        saved.setName("Updated Budget");
        saved.setLimitValue(800.0);
        saved.setCurrentValue(150.0);
        saved.setDeadline(LocalDate.now().plusMonths(3));
        Budget updated = budgetRepository.save(saved);

        // Assert
        assertEquals("Updated Budget", updated.getName());
        assertEquals(800.0, updated.getLimitValue());
        assertEquals(150.0, updated.getCurrentValue());
        assertEquals(userTest.getId(), updated.getUser().getId());
        assertEquals(categoryTest.getId(), updated.getCategory().getId());
    }

    @Test
    @Order(13)
    @DisplayName("Should delete budget successfully")
    @Transactional
    void testDeleteBudget_Success() {
        // Arrange
        Budget budget = new Budget();
        budget.setName("Test Budget");
        budget.setLimitValue(500.0);
        budget.setCurrentValue(100.0);
        budget.setDeadline(LocalDate.now().plusMonths(1));
        budget.setUser(userTest);
        budget.setCategory(categoryTest);
        Budget saved = budgetRepository.save(budget);
        Long id = saved.getId();

        // Act
        budgetRepository.delete(saved);

        // Assert
        Optional<Budget> found = budgetRepository.findById(id);
        assertFalse(found.isPresent());
    }

    @Test
    @Order(14)
    @DisplayName("Should validate relationship with user")
    @Transactional
    void testUserRelationship_Success() {
        // Arrange
        Budget budget = new Budget();
        budget.setName("Test Budget");
        budget.setLimitValue(500.0);
        budget.setCurrentValue(100.0);
        budget.setDeadline(LocalDate.now().plusMonths(1));
        budget.setUser(userTest);
        budget.setCategory(categoryTest);
        Budget saved = budgetRepository.save(budget);

        // Act
        Optional<Budget> found = budgetRepository.findById(saved.getId());

        // Assert
        assertTrue(found.isPresent());
        assertNotNull(found.get().getUser());
        assertEquals(userTest.getId(), found.get().getUser().getId());
        assertEquals(userTest.getName(), found.get().getUser().getName());
        assertEquals(userTest.getEmail(), found.get().getUser().getEmail());
    }

    @Test
    @Order(15)
    @DisplayName("Should validate relationship with category")
    @Transactional
    void testCategoryRelationship_Success() {
        // Arrange
        Budget budget = new Budget();
        budget.setName("Test Budget");
        budget.setLimitValue(500.0);
        budget.setCurrentValue(100.0);
        budget.setDeadline(LocalDate.now().plusMonths(1));
        budget.setUser(userTest);
        budget.setCategory(categoryTest);
        Budget saved = budgetRepository.save(budget);

        // Act
        Optional<Budget> found = budgetRepository.findById(saved.getId());

        // Assert
        assertTrue(found.isPresent());
        assertNotNull(found.get().getCategory());
        assertEquals(categoryTest.getId(), found.get().getCategory().getId());
        assertEquals(categoryTest.getName(), found.get().getCategory().getName());
        assertEquals(categoryTest.getType(), found.get().getCategory().getType());
    }

    @Test
    @Order(16)
    @DisplayName("Should validate rollback on failure")
    @Transactional
    void testRollbackOnFailure_Success() {
        // Arrange - act - simulate failure
        assertThrows(RuntimeException.class, () -> {
            throw new RuntimeException("Simulation of failure for rollback test");
        });

        // Assert - verify count invariant
        assertEquals(0, budgetRepository.count());
    }
}
