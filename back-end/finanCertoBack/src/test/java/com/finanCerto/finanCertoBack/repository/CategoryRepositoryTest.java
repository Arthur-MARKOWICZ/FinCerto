package com.finanCerto.finanCertoBack.repository;

import com.finanCerto.finanCertoBack.category.Category;
import com.finanCerto.finanCertoBack.category.CategoryRepository;
import com.finanCerto.finanCertoBack.category.CategoryType;
import com.finanCerto.finanCertoBack.user.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setup() {
        try {
            entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE tb_transacao").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE tb_categoria").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE tb_conta").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE tb_usuario").executeUpdate();
            entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
            entityManager.flush();
        } catch (Exception e) {
            try {
                entityManager.createNativeQuery("DELETE FROM tb_transacao").executeUpdate();
                entityManager.createNativeQuery("DELETE FROM tb_categoria").executeUpdate();
                entityManager.createNativeQuery("DELETE FROM tb_usuario").executeUpdate();
                entityManager.flush();
            } catch (Exception ex) {
                // Ignore
            }
        }
    }

    @Test
    @DisplayName("Should verify existence by user/name and find by name")
    void shouldQueryCategory() {
        User user = newUser("Ana", "ana@test.com");
        Category category = newCategory("Leisure", CategoryType.EXPENSE, user);

        entityManager.persist(user);
        entityManager.persist(category);
        entityManager.flush();

        assertTrue(categoryRepository.existsByUserIdAndName(user.getId(), "Leisure"));
        assertFalse(categoryRepository.existsByUserIdAndName(user.getId(), "DoesNotExist"));

        Optional<Category> found = categoryRepository.findByNameAndUserId("Leisure", user.getId());
        assertTrue(found.isPresent());
        assertEquals("Leisure", found.get().getName());
    }

    @Test
    @DisplayName("Should save and retrieve category")
    void shouldSaveAndRetrieveCategory() {
        User user = newUser("Paula", "paula@test.com");
        Category category = newCategory("Food", CategoryType.EXPENSE, user);

        entityManager.persist(user);
        Category saved = categoryRepository.save(category);
        entityManager.flush();

        Optional<Category> retrieved = categoryRepository.findById(saved.getId());

        assertTrue(retrieved.isPresent());
        assertEquals("Food", retrieved.get().getName());
        assertEquals(CategoryType.EXPENSE, retrieved.get().getType());
    }

    @Test
    @DisplayName("Should find category by name")
    void shouldFindCategoryByName() {
        User user = newUser("Marcos", "marcos@test.com");
        Category category = newCategory("Transport", CategoryType.EXPENSE, user);

        entityManager.persist(user);
        entityManager.persist(category);
        entityManager.flush();

        Optional<Category> found = categoryRepository.findByNameAndUserId("Transport", user.getId());

        assertTrue(found.isPresent());
        assertEquals("Transport", found.get().getName());
        assertEquals(CategoryType.EXPENSE, found.get().getType());
    }

    @Test
    @DisplayName("Should return empty when searching for non-existent category")
    void shouldReturnEmptyWhenSearchingNonExistentCategory() {
        Optional<Category> found = categoryRepository.findByNameAndUserId("NonExistentCategory", -1L);

        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should update existing category")
    void shouldUpdateExistingCategory() {
        User user = newUser("Fatima", "fatima@test.com");
        Category category = newCategory("Health", CategoryType.EXPENSE, user);

        entityManager.persist(user);
        Category saved = categoryRepository.save(category);
        entityManager.flush();

        saved.setName("Health and Wellness");
        saved.setType(CategoryType.INCOME);
        Category updated = categoryRepository.save(saved);
        entityManager.flush();

        Optional<Category> retrieved = categoryRepository.findById(updated.getId());

        assertTrue(retrieved.isPresent());
        assertEquals("Health and Wellness", retrieved.get().getName());
        assertEquals(CategoryType.INCOME, retrieved.get().getType());
    }

    @Test
    @DisplayName("Should delete existing category")
    void shouldDeleteExistingCategory() {
        User user = newUser("Ester", "ester@test.com");
        Category category = newCategory("Education", CategoryType.EXPENSE, user);

        entityManager.persist(user);
        Category saved = categoryRepository.save(category);
        entityManager.flush();

        categoryRepository.deleteById(saved.getId());
        entityManager.flush();

        Optional<Category> retrieved = categoryRepository.findById(saved.getId());

        assertFalse(retrieved.isPresent());
    }

    @Test
    @DisplayName("Should save category with type INCOME")
    void shouldSaveCategoryWithIncomeType() {
        User user = newUser("Gustavo", "gustavo@test.com");
        Category category = newCategory("Salary", CategoryType.INCOME, user);

        entityManager.persist(user);
        Category saved = categoryRepository.save(category);
        entityManager.flush();

        Optional<Category> retrieved = categoryRepository.findById(saved.getId());

        assertTrue(retrieved.isPresent());
        assertEquals(CategoryType.INCOME, retrieved.get().getType());
    }

    @Test
    @DisplayName("Should count total categories")
    void shouldCountTotalCategories() {
        User user = newUser("Helena", "helena@test.com");
        Category category1 = newCategory("Category 1", CategoryType.EXPENSE, user);
        Category category2 = newCategory("Category 2", CategoryType.INCOME, user);
        Category category3 = newCategory("Category 3", CategoryType.EXPENSE, user);

        entityManager.persist(user);
        entityManager.persist(category1);
        entityManager.persist(category2);
        entityManager.persist(category3);
        entityManager.flush();

        long total = categoryRepository.count();

        assertTrue(total >= 3);
    }

    @Test
    @DisplayName("Should retrieve all categories")
    void shouldRetrieveAllCategories() {
        User user = newUser("Igor", "igor@test.com");
        Category category1 = newCategory("Category A", CategoryType.EXPENSE, user);
        Category category2 = newCategory("Category B", CategoryType.INCOME, user);

        entityManager.persist(user);
        entityManager.persist(category1);
        entityManager.persist(category2);
        entityManager.flush();

        var all = categoryRepository.findAll();

        assertTrue(all.size() >= 2);
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
}
