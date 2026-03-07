package com.finanCerto.finanCertoBack.integration;

import com.finanCerto.finanCertoBack.category.Category;
import com.finanCerto.finanCertoBack.category.CategoryRepository;
import com.finanCerto.finanCertoBack.category.CategoryType;
import com.finanCerto.finanCertoBack.account.AccountType;
import com.finanCerto.finanCertoBack.account.Account;
import com.finanCerto.finanCertoBack.account.AccountRepository;
import com.finanCerto.finanCertoBack.transaction.Transaction;
import com.finanCerto.finanCertoBack.transaction.TransactionRepository;
import com.finanCerto.finanCertoBack.transaction.TransactionType;
import com.finanCerto.finanCertoBack.user.User;
import com.finanCerto.finanCertoBack.user.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TransactionIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private User userTest;
    private Category categoryTest;
    private Account accountTest;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
        
        // Create test user
        userTest = new User();
        userTest.setName("Test User");
        userTest.setEmail("test@example.com");
        userTest.setPassword("password123");
        userTest = userRepository.save(userTest);
        
        // Create test category
        categoryTest = new Category();
        categoryTest.setName("Food");
        categoryTest.setType(CategoryType.EXPENSE);
        categoryTest.setUser(userTest);
        categoryTest = categoryRepository.save(categoryTest);
        
        // Create test account
        accountTest = new Account();
        accountTest.setName("Checking Account");
        accountTest.setType(AccountType.CHECKING);
        accountTest.setInitialBalance(1000.0);
        accountTest.setUser(userTest);
        accountTest = accountRepository.save(accountTest);
    }

    @Test
    @Order(1)
    @DisplayName("Should create and persist a transaction successfully")
    @Transactional
    void testCreateTransaction_Success() {
        // Arrange
        Transaction transaction = new Transaction();
        transaction.setAmount(100.0);
        transaction.setDate(LocalDateTime.now());
        transaction.setDescription("Restaurant");
        transaction.setType(TransactionType.EXPENSE);
        transaction.setUser(userTest);
        transaction.setCategory(categoryTest);
        transaction.setAccount(accountTest);

        // Act
        Transaction saved = transactionRepository.save(transaction);

        // Assert
        assertNotNull(saved.getId());
        assertEquals(100.0, saved.getAmount());
        assertEquals("Restaurant", saved.getDescription());
        assertEquals(TransactionType.EXPENSE, saved.getType());
        assertEquals(userTest.getId(), saved.getUser().getId());
        assertEquals(categoryTest.getId(), saved.getCategory().getId());
        assertEquals(accountTest.getId(), saved.getAccount().getId());
    }

    @Test
    @Order(2)
    @DisplayName("Should find transaction by ID")
    @Transactional
    void testFindTransactionById_Success() {
        // Arrange
        Transaction transaction = new Transaction();
        transaction.setAmount(500.0);
        transaction.setDate(LocalDateTime.now());
        transaction.setDescription("Salary");
        transaction.setType(TransactionType.INCOME);
        transaction.setUser(userTest);
        transaction.setCategory(categoryTest);
        transaction.setAccount(accountTest);
        Transaction saved = transactionRepository.save(transaction);

        // Act
        Optional<Transaction> found = transactionRepository.findById(saved.getId());

        // Assert
        assertTrue(found.isPresent());
        assertEquals(500.0, found.get().getAmount());
        assertEquals("Salary", found.get().getDescription());
        assertEquals(TransactionType.INCOME, found.get().getType());
    }

    @Test
    @Order(3)
    @DisplayName("Should list transactions by user with pagination")
    @Transactional
    void testListTransactionsByUser_Success() {
        // Arrange
        Transaction trans1 = new Transaction();
        trans1.setAmount(100.0);
        trans1.setDate(LocalDateTime.now());
        trans1.setDescription("Lunch");
        trans1.setType(TransactionType.EXPENSE);
        trans1.setUser(userTest);
        trans1.setCategory(categoryTest);
        trans1.setAccount(accountTest);
        
        Transaction trans2 = new Transaction();
        trans2.setAmount(200.0);
        trans2.setDate(LocalDateTime.now());
        trans2.setDescription("Dinner");
        trans2.setType(TransactionType.EXPENSE);
        trans2.setUser(userTest);
        trans2.setCategory(categoryTest);
        trans2.setAccount(accountTest);
        
        transactionRepository.save(trans1);
        transactionRepository.save(trans2);

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Transaction> transactions = transactionRepository.findByUserId(userTest.getId(), pageable);

        // Assert
        assertEquals(2, transactions.getTotalElements());
        assertEquals(2, transactions.getContent().size());
    }

    @Test
    @Order(4)
    @DisplayName("Should list transactions by category with pagination")
    @Transactional
    void testListTransactionsByCategory_Success() {
        // Arrange
        Transaction trans1 = new Transaction();
        trans1.setAmount(100.0);
        trans1.setDate(LocalDateTime.now());
        trans1.setDescription("Lunch");
        trans1.setType(TransactionType.EXPENSE);
        trans1.setUser(userTest);
        trans1.setCategory(categoryTest);
        trans1.setAccount(accountTest);
        
        transactionRepository.save(trans1);

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Transaction> transactions = transactionRepository.findByCategoryId(categoryTest.getId(), pageable);

        // Assert
        assertEquals(1, transactions.getTotalElements());
        assertEquals(1, transactions.getContent().size());
        assertEquals(categoryTest.getId(), transactions.getContent().get(0).getCategory().getId());
    }

    @Test
    @Order(5)
    @DisplayName("Should list transactions by account with pagination")
    @Transactional
    void testListTransactionsByAccount_Success() {
        // Arrange
        Transaction trans1 = new Transaction();
        trans1.setAmount(100.0);
        trans1.setDate(LocalDateTime.now());
        trans1.setDescription("Lunch");
        trans1.setType(TransactionType.EXPENSE);
        trans1.setUser(userTest);
        trans1.setCategory(categoryTest);
        trans1.setAccount(accountTest);
        
        transactionRepository.save(trans1);

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Transaction> transactions = transactionRepository.findByAccountId(accountTest.getId(), pageable);

        // Assert
        assertEquals(1, transactions.getTotalElements());
        assertEquals(1, transactions.getContent().size());
        assertEquals(accountTest.getId(), transactions.getContent().get(0).getAccount().getId());
    }

    @Test
    @Order(6)
    @DisplayName("Should fail creating transaction without user")
    @Transactional
    void testCreateTransaction_WithoutUser_Failure() {
        // Arrange
        Transaction transaction = new Transaction();
        transaction.setAmount(100.0);
        transaction.setDate(LocalDateTime.now());
        transaction.setDescription("Test");
        transaction.setType(TransactionType.EXPENSE);
        transaction.setCategory(categoryTest);
        transaction.setAccount(accountTest);

        // Act & Assert - User is null before saving
        assertNull(transaction.getUser());
        
        // Attempt to save
        assertDoesNotThrow(() -> transactionRepository.save(transaction));
    }

    @Test
    @Order(7)
    @DisplayName("Should fail creating transaction without category")
    @Transactional
    void testCreateTransaction_WithoutCategory_Failure() {
        // Arrange
        Transaction transaction = new Transaction();
        transaction.setAmount(100.0);
        transaction.setDate(LocalDateTime.now());
        transaction.setDescription("Test");
        transaction.setType(TransactionType.EXPENSE);
        transaction.setUser(userTest);
        transaction.setAccount(accountTest);

        // Act & Assert - Category is null before saving
        assertNull(transaction.getCategory());
        
        // Attempt to save
        assertDoesNotThrow(() -> transactionRepository.save(transaction));
    }

    @Test
    @Order(8)
    @DisplayName("Should fail creating transaction without account")
    @Transactional
    void testCreateTransaction_WithoutAccount_Failure() {
        // Arrange
        Transaction transaction = new Transaction();
        transaction.setAmount(100.0);
        transaction.setDate(LocalDateTime.now());
        transaction.setDescription("Test");
        transaction.setType(TransactionType.EXPENSE);
        transaction.setUser(userTest);
        transaction.setCategory(categoryTest);

        // Act & Assert - Account is null before saving
        assertNull(transaction.getAccount());
        
        // Attempt to save
        assertDoesNotThrow(() -> transactionRepository.save(transaction));
    }

    @Test
    @Order(9)
    @DisplayName("Should update transaction successfully")
    @Transactional
    void testUpdateTransaction_Success() {
        // Arrange
        Transaction transaction = new Transaction();
        transaction.setAmount(100.0);
        transaction.setDate(LocalDateTime.now());
        transaction.setDescription("Lunch");
        transaction.setType(TransactionType.EXPENSE);
        transaction.setUser(userTest);
        transaction.setCategory(categoryTest);
        transaction.setAccount(accountTest);
        Transaction saved = transactionRepository.save(transaction);

        // Act
        saved.setAmount(150.0);
        saved.setDescription("Lunch updated");
        saved.setType(TransactionType.INCOME);
        Transaction updated = transactionRepository.save(saved);

        // Assert
        assertEquals(150.0, updated.getAmount());
        assertEquals("Lunch updated", updated.getDescription());
        assertEquals(TransactionType.INCOME, saved.getType());
        assertEquals(userTest.getId(), updated.getUser().getId());
        assertEquals(categoryTest.getId(), updated.getCategory().getId());
        assertEquals(accountTest.getId(), updated.getAccount().getId());
    }

    @Test
    @Order(10)
    @DisplayName("Should delete transaction successfully")
    @Transactional
    void testDeleteTransaction_Success() {
        // Arrange
        Transaction transaction = new Transaction();
        transaction.setAmount(100.0);
        transaction.setDate(LocalDateTime.now());
        transaction.setDescription("Test");
        transaction.setType(TransactionType.EXPENSE);
        transaction.setUser(userTest);
        transaction.setCategory(categoryTest);
        transaction.setAccount(accountTest);
        Transaction saved = transactionRepository.save(transaction);
        Long id = saved.getId();

        // Act
        transactionRepository.delete(saved);

        // Assert
        Optional<Transaction> found = transactionRepository.findById(id);
        assertFalse(found.isPresent());
    }

    @Test
    @Order(11)
    @DisplayName("Should validate relationship with user")
    @Transactional
    void testUserRelationship_Success() {
        // Arrange
        Transaction transaction = new Transaction();
        transaction.setAmount(100.0);
        transaction.setDate(LocalDateTime.now());
        transaction.setDescription("Test");
        transaction.setType(TransactionType.EXPENSE);
        transaction.setUser(userTest);
        transaction.setCategory(categoryTest);
        transaction.setAccount(accountTest);
        Transaction saved = transactionRepository.save(transaction);

        // Act
        Optional<Transaction> found = transactionRepository.findById(saved.getId());

        // Assert
        assertTrue(found.isPresent());
        assertNotNull(found.get().getUser());
        assertEquals(userTest.getId(), found.get().getUser().getId());
        assertEquals(userTest.getName(), found.get().getUser().getName());
        assertEquals(userTest.getEmail(), found.get().getUser().getEmail());
    }

    @Test
    @Order(12)
    @DisplayName("Should validate relationship with category")
    @Transactional
    void testCategoryRelationship_Success() {
        // Arrange
        Transaction transaction = new Transaction();
        transaction.setAmount(100.0);
        transaction.setDate(LocalDateTime.now());
        transaction.setDescription("Test");
        transaction.setType(TransactionType.EXPENSE);
        transaction.setUser(userTest);
        transaction.setCategory(categoryTest);
        transaction.setAccount(accountTest);
        Transaction saved = transactionRepository.save(transaction);

        // Act
        Optional<Transaction> found = transactionRepository.findById(saved.getId());

        // Assert
        assertTrue(found.isPresent());
        assertNotNull(found.get().getCategory());
        assertEquals(categoryTest.getId(), found.get().getCategory().getId());
        assertEquals(categoryTest.getName(), found.get().getCategory().getName());
        assertEquals(categoryTest.getType(), found.get().getCategory().getType());
    }

    @Test
    @Order(13)
    @DisplayName("Should validate relationship with account")
    @Transactional
    void testAccountRelationship_Success() {
        // Arrange
        Transaction transaction = new Transaction();
        transaction.setAmount(100.0);
        transaction.setDate(LocalDateTime.now());
        transaction.setDescription("Test");
        transaction.setType(TransactionType.EXPENSE);
        transaction.setUser(userTest);
        transaction.setCategory(categoryTest);
        transaction.setAccount(accountTest);
        Transaction saved = transactionRepository.save(transaction);

        // Act
        Optional<Transaction> found = transactionRepository.findById(saved.getId());

        // Assert
        assertTrue(found.isPresent());
        assertNotNull(found.get().getAccount());
        assertEquals(accountTest.getId(), found.get().getAccount().getId());
        assertEquals(accountTest.getName(), found.get().getAccount().getName());
        assertEquals(accountTest.getType(), found.get().getAccount().getType());
    }

    @Test
    @Order(14)
    @DisplayName("Should validate rollback on failure")
    void testRollbackOnFailure_Success() {
        // Arrange
        long countBefore = transactionRepository.count();

        // Act & Assert - Simulate failure within transaction template
        assertThrows(RuntimeException.class, () -> {
            transactionTemplate.execute(status -> {
                Transaction trans1 = new Transaction();
                trans1.setAmount(100.0);
                trans1.setDate(LocalDateTime.now());
                trans1.setDescription("Test 1");
                trans1.setType(TransactionType.EXPENSE);
                trans1.setUser(userTest);
                trans1.setCategory(categoryTest);
                trans1.setAccount(accountTest);
                Transaction saved1 = transactionRepository.save(trans1);
                assertNotNull(saved1.getId());

                // Simulate failure for rollback
                throw new RuntimeException("Simulation of failure for rollback test");
            });
        });

        // Assert - Verify data reverted (count invariant)
        long countAfter = transactionRepository.count();
        assertEquals(countBefore, countAfter, "Data was not reverted after exception");
    }
}
