package com.finanCerto.finanCertoBack.repository;

import com.finanCerto.finanCertoBack.category.Category;
import com.finanCerto.finanCertoBack.category.CategoryType;
import com.finanCerto.finanCertoBack.budget.Budget;
import com.finanCerto.finanCertoBack.budget.BudgetRepository;
import com.finanCerto.finanCertoBack.user.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BudgetRepositoryTest {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Should find budget by user and name and by category")
    void shouldQueryBudget() {
        User user = newUser("Maria", "maria@test.com");
        Category category = newCategory("Market", CategoryType.EXPENSE, user);
        Budget budget = newBudget("Monthly", 500.0, 100.0, user, category);

        entityManager.persist(user);
        entityManager.persist(category);
        entityManager.persist(budget);
        entityManager.flush();

        assertTrue(budgetRepository.existsByUserIdAndName(user.getId(), "Monthly"));

        Budget byName = budgetRepository.findByUserIdAndName(user.getId(), "Monthly");
        assertNotNull(byName);
        assertEquals("Monthly", byName.getName());

        Budget byCategory = budgetRepository.findByCategory(category);
        assertNotNull(byCategory);
        assertEquals(category, byCategory.getCategory());
    }

    @Test
    @DisplayName("Should verify existence of budget by user and name")
    void shouldVerifyBudgetExistenceByUserIdAndName() {
        User user = newUser("Pedro", "pedro@test.com");
        Category category = newCategory("Food", CategoryType.EXPENSE, user);
        Budget budget = newBudget("Week", 200.0, 50.0, user, category);

        entityManager.persist(user);
        entityManager.persist(category);
        entityManager.persist(budget);
        entityManager.flush();

        assertTrue(budgetRepository.existsByUserIdAndName(user.getId(), "Week"));
        assertFalse(budgetRepository.existsByUserIdAndName(user.getId(), "NonExistent"));
        assertFalse(budgetRepository.existsByUserIdAndName(9999L, "Week"));
    }

    @Test
    @DisplayName("Should find budget by category")
    void shouldFindBudgetByCategory() {
        User user = newUser("Ana", "ana@test.com");
        Category category = newCategory("Leisure", CategoryType.EXPENSE, user);
        Budget budget = newBudget("Entertainment", 1000.0, 200.0, user, category);

        entityManager.persist(user);
        entityManager.persist(category);
        entityManager.persist(budget);
        entityManager.flush();

        Budget found = budgetRepository.findByCategory(category);

        assertNotNull(found);
        assertEquals("Entertainment", found.getName());
        assertEquals(category, found.getCategory());
    }

    @Test
    @DisplayName("Should save and retrieve budget")
    void shouldSaveAndRetrieveBudget() {
        User user = newUser("Carlos", "carlos@test.com");
        Category category = newCategory("Transport", CategoryType.EXPENSE, user);
        Budget budget = newBudget("Monthly", 500.0, 100.0, user, category);

        entityManager.persist(user);
        entityManager.persist(category);
        Budget saved = budgetRepository.save(budget);
        entityManager.flush();

        Optional<Budget> retrieved = budgetRepository.findById(saved.getId());

        assertTrue(retrieved.isPresent());
        assertEquals("Monthly", retrieved.get().getName());
        assertEquals(500.0, retrieved.get().getLimitValue());
        assertEquals(100.0, retrieved.get().getCurrentValue());
    }

    @Test
    @DisplayName("Should update existing budget")
    void shouldUpdateExistingBudget() {
        User user = newUser("Lucia", "lucia@test.com");
        Category category = newCategory("Health", CategoryType.EXPENSE, user);
        Budget budget = newBudget("Medicine", 300.0, 50.0, user, category);

        entityManager.persist(user);
        entityManager.persist(category);
        Budget saved = budgetRepository.save(budget);
        entityManager.flush();

        saved.setCurrentValue(100.0);
        saved.setLimitValue(400.0);
        Budget updated = budgetRepository.save(saved);
        entityManager.flush();

        Optional<Budget> retrieved = budgetRepository.findById(updated.getId());

        assertTrue(retrieved.isPresent());
        assertEquals(100.0, retrieved.get().getCurrentValue());
        assertEquals(400.0, retrieved.get().getLimitValue());
    }

    @Test
    @DisplayName("Should delete existing budget")
    void shouldDeleteExistingBudget() {
        User user = newUser("Felipe", "felipe@test.com");
        Category category = newCategory("Education", CategoryType.EXPENSE, user);
        Budget budget = newBudget("Courses", 500.0, 100.0, user, category);

        entityManager.persist(user);
        entityManager.persist(category);
        Budget saved = budgetRepository.save(budget);
        entityManager.flush();

        budgetRepository.deleteById(saved.getId());
        entityManager.flush();

        Optional<Budget> retrieved = budgetRepository.findById(saved.getId());

        assertFalse(retrieved.isPresent());
    }

    @Test
    @DisplayName("Should count total budgets")
    void shouldCountTotalBudgets() {
        User user = newUser("Beatriz", "beatriz@test.com");
        Category category1 = newCategory("Category 1", CategoryType.EXPENSE, user);
        Category category2 = newCategory("Category 2", CategoryType.EXPENSE, user);
        
        Budget budget1 = newBudget("Budget 1", 500.0, 100.0, user, category1);
        Budget budget2 = newBudget("Budget 2", 1000.0, 200.0, user, category2);

        entityManager.persist(user);
        entityManager.persist(category1);
        entityManager.persist(category2);
        entityManager.persist(budget1);
        entityManager.persist(budget2);
        entityManager.flush();

        long total = budgetRepository.count();

        assertTrue(total >= 2);
    }

    @Test
    @DisplayName("Should retrieve all budgets")
    void shouldRetrieveAllBudgets() {
        User user = newUser("Gustavo", "gustavo@test.com");
        Category category1 = newCategory("Expenses A", CategoryType.EXPENSE, user);
        Category category2 = newCategory("Expenses B", CategoryType.EXPENSE, user);
        
        Budget budget1 = newBudget("Budget A", 500.0, 100.0, user, category1);
        Budget budget2 = newBudget("Budget B", 1000.0, 200.0, user, category2);

        entityManager.persist(user);
        entityManager.persist(category1);
        entityManager.persist(category2);
        entityManager.persist(budget1);
        entityManager.persist(budget2);
        entityManager.flush();

        var all = budgetRepository.findAll();

        assertTrue(all.size() >= 2);
    }

    @Test
    @DisplayName("Should save budget with deadline")
    void shouldSaveBudgetWithDeadline() {
        User user = newUser("Helena", "helena@test.com");
        Category category = newCategory("Various", CategoryType.EXPENSE, user);
        
        Budget budget = newBudget("With Deadline", 500.0, 0.0, user, category);
        budget.setDeadline(LocalDate.now().plusDays(30));

        entityManager.persist(user);
        entityManager.persist(category);
        Budget saved = budgetRepository.save(budget);
        entityManager.flush();

        Optional<Budget> retrieved = budgetRepository.findById(saved.getId());

        assertTrue(retrieved.isPresent());
        assertNotNull(retrieved.get().getDeadline());
        assertEquals(LocalDate.now().plusDays(30), retrieved.get().getDeadline());
    }

    private User newUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword("pwd");
        return user;
    }

    private Category newCategory(String name, CategoryType type, User user) {
        Category category = new Category();
        category.setName(name);
        category.setType(type);
        category.setUser(user);
        return category;
    }

    private Budget newBudget(String name, double limit, double current, User user, Category category) {
        Budget budget = new Budget();
        budget.setName(name);
        budget.setLimitValue(limit);
        budget.setCurrentValue(current);
        budget.setDeadline(LocalDate.now().plusDays(5));
        budget.setUser(user);
        budget.setCategory(category);
        return budget;
    }
}
