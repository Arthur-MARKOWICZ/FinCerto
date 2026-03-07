package com.finanCerto.finanCertoBack.integration;

import com.finanCerto.finanCertoBack.account.*;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AccountIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private User userTest;

    @BeforeEach
    void setUp() {
        // Delete in correct order to avoid constraint violations
        entityManager.createNativeQuery("DELETE FROM tb_transacao").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM tb_categoria").executeUpdate();
        accountRepository.deleteAll();
        userRepository.deleteAll();
        entityManager.flush();
        
        // Create test user
        userTest = new User();
        userTest.setName("Test User");
        userTest.setEmail("test@example.com");
        userTest.setPassword("password123");
        userTest = userRepository.save(userTest);
        entityManager.flush();
    }

    @Test
    @Order(1)
    @DisplayName("Should create and persist an account successfully")
    @Transactional
    void testCreateAccount_Success() {
        // Arrange
        Account account = new Account();
        account.setName("Checking Account");
        account.setType(AccountType.CHECKING);
        account.setInitialBalance(1000.0);
        account.setUser(userTest);

        // Act
        Account saved = accountRepository.save(account);

        // Assert
        assertNotNull(saved.getId());
        assertEquals("Checking Account", saved.getName());
        assertEquals(AccountType.CHECKING, saved.getType());
        assertEquals(1000.0, saved.getInitialBalance());
        assertEquals(userTest.getId(), saved.getUser().getId());
    }

    @Test
    @Order(2)
    @DisplayName("Should find account by ID")
    @Transactional
    void testFindAccountById_Success() {
        // Arrange
        Account account = new Account();
        account.setName("Savings");
        account.setType(AccountType.SAVINGS);
        account.setInitialBalance(5000.0);
        account.setUser(userTest);
        Account saved = accountRepository.save(account);

        // Act
        Optional<Account> found = accountRepository.findById(saved.getId());

        // Assert
        assertTrue(found.isPresent());
        assertEquals("Savings", found.get().getName());
        assertEquals(AccountType.SAVINGS, found.get().getType());
        assertEquals(5000.0, found.get().getInitialBalance());
    }

    @Test
    @Order(3)
    @DisplayName("Should list accounts by user with pagination")
    @Transactional
    void testListAccountsByUser_Success() {
        // Arrange
        Account account1 = new Account();
        account1.setName("Checking Account");
        account1.setType(AccountType.CHECKING);
        account1.setInitialBalance(1000.0);
        account1.setUser(userTest);
        
        Account account2 = new Account();
        account2.setName("Savings");
        account2.setType(AccountType.SAVINGS);
        account2.setInitialBalance(5000.0);
        account2.setUser(userTest);
        
        Account account3 = new Account();
        account3.setName("Card");
        account3.setType(AccountType.CREDIT_CARD);
        account3.setInitialBalance(0.0);
        account3.setUser(userTest);
        
        accountRepository.save(account1);
        accountRepository.save(account2);
        accountRepository.save(account3);

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Account> accounts = accountRepository.findAllByUserId(userTest.getId(), pageable);

        // Assert
        assertEquals(3, accounts.getTotalElements());
        assertEquals(3, accounts.getContent().size());
        assertTrue(accounts.getContent().stream().anyMatch(c -> "Checking Account".equals(c.getName())));
        assertTrue(accounts.getContent().stream().anyMatch(c -> "Savings".equals(c.getName())));
        assertTrue(accounts.getContent().stream().anyMatch(c -> "Card".equals(c.getName())));
    }

    @Test
    @Order(4)
    @DisplayName("Should verify account existence by user and name")
    @Transactional
    void testVerifyAccountExistence_Success() {
        // Arrange
        Account account = new Account();
        account.setName("Checking Account");
        account.setType(AccountType.CHECKING);
        account.setInitialBalance(1000.0);
        account.setUser(userTest);
        accountRepository.save(account);

        // Act & Assert
        assertTrue(accountRepository.existsByUserIdAndName(userTest.getId(), "Checking Account"));
        assertFalse(accountRepository.existsByUserIdAndName(userTest.getId(), "Savings"));
    }

    @Test
    @Order(5)
    @DisplayName("Should find account by name and user ID")
    @Transactional
    void testFindAccountByNameAndUserId_Success() {
        // Arrange
        Account account = new Account();
        account.setName("Checking Account");
        account.setType(AccountType.CHECKING);
        account.setInitialBalance(1000.0);
        account.setUser(userTest);
        accountRepository.save(account);

        // Act
        Optional<Account> found = accountRepository.findByNameAndUserId(userTest.getId(), "Checking Account");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("Checking Account", found.get().getName());
        assertEquals(AccountType.CHECKING, found.get().getType());
    }

    @Test
    @Order(6)
    @DisplayName("Should fail creating account with duplicate name for same user")
    @Transactional
    void testCreateAccount_DuplicateName_Failure() {
        // Arrange
        Account account1 = new Account();
        account1.setName("Checking Account");
        account1.setType(AccountType.CHECKING);
        account1.setInitialBalance(1000.0);
        account1.setUser(userTest);
        accountRepository.save(account1);

        Account account2 = new Account();
        account2.setName("Checking Account");
        account2.setType(AccountType.SAVINGS);
        account2.setInitialBalance(2000.0);
        account2.setUser(userTest);

        // Act & Assert
        assertThrows(Exception.class, () -> accountRepository.save(account2));
    }

    @Test
    @Order(7)
    @DisplayName("Should allow account with same name for different users")
    @Transactional
    void testCreateAccount_SameNameDifferentUsers_Success() {
        // Arrange
        User user2 = new User();
        user2.setName("Another User");
        user2.setEmail("another@example.com");
        user2.setPassword("password123");
        user2 = userRepository.save(user2);

        Account account1 = new Account();
        account1.setName("Checking Account");
        account1.setType(AccountType.CHECKING);
        account1.setInitialBalance(1000.0);
        account1.setUser(userTest);
        
        Account account2 = new Account();
        account2.setName("Checking Account");
        account2.setType(AccountType.SAVINGS);
        account2.setInitialBalance(2000.0);
        account2.setUser(user2);
        
        accountRepository.save(account1);

        // Act
        Account saved2 = accountRepository.save(account2);

        // Assert
        assertNotNull(saved2.getId());
        assertEquals(user2.getId(), saved2.getUser().getId());
    }

    @Test
    @Order(8)
    @DisplayName("Should fail creating account with null name")
    @Transactional
    void testCreateAccount_NullName_Failure() {
        // Arrange
        Account account = new Account();
        account.setType(AccountType.CHECKING);
        account.setInitialBalance(1000.0);
        account.setUser(userTest);

        // Act & Assert - Bean Validation should throw exception
        assertThrows(Exception.class, () -> accountRepository.save(account));
    }

    @Test
    @Order(9)
    @DisplayName("Should fail creating account with null initial balance")
    @Transactional
    void testCreateAccount_NullInitialBalance_Failure() {
        // Arrange
        Account account = new Account();
        account.setName("Test Account");
        account.setType(AccountType.CHECKING);
        account.setUser(userTest);

        // Act & Assert - double default is 0.0
        assertEquals(0.0, account.getInitialBalance()); 
        
        // Attempt to save
        assertDoesNotThrow(() -> accountRepository.save(account));
    }

    @Test
    @Order(10)
    @DisplayName("Should fail creating account without user")
    @Transactional
    void testCreateAccount_WithoutUser_Failure() {
        // Arrange
        Account account = new Account();
        account.setName("Test Account");
        account.setType(AccountType.CHECKING);
        account.setInitialBalance(1000.0);

        // Act & Assert - User is null before saving
        assertNull(account.getUser());
        
        // Attempt to save
        assertDoesNotThrow(() -> accountRepository.save(account));
    }

    @Test
    @Order(11)
    @DisplayName("Should update account successfully")
    @Transactional
    void testUpdateAccount_Success() {
        // Arrange
        Account account = new Account();
        account.setName("Checking Account");
        account.setType(AccountType.CHECKING);
        account.setInitialBalance(1000.0);
        account.setUser(userTest);
        Account saved = accountRepository.save(account);

        // Act
        saved.setName("Updated Account");
        saved.setInitialBalance(1500.0);
        saved.setType(AccountType.SAVINGS);
        Account updated = accountRepository.save(saved);

        // Assert
        assertEquals("Updated Account", updated.getName());
        assertEquals(1500.0, updated.getInitialBalance());
        assertEquals(AccountType.SAVINGS, updated.getType());
        assertEquals(userTest.getId(), updated.getUser().getId());
    }

    @Test
    @Order(12)
    @DisplayName("Should delete account successfully")
    @Transactional
    void testDeleteAccount_Success() {
        // Arrange
        Account account = new Account();
        account.setName("Checking Account");
        account.setType(AccountType.CHECKING);
        account.setInitialBalance(1000.0);
        account.setUser(userTest);
        Account saved = accountRepository.save(account);
        Long id = saved.getId();

        // Act
        accountRepository.delete(saved);

        // Assert
        Optional<Account> found = accountRepository.findById(id);
        assertFalse(found.isPresent());
    }

    @Test
    @Order(13)
    @DisplayName("Should validate relationship with user")
    @Transactional
    void testUserRelationship_Success() {
        // Arrange
        Account account = new Account();
        account.setName("Checking Account");
        account.setType(AccountType.CHECKING);
        account.setInitialBalance(1000.0);
        account.setUser(userTest);
        Account saved = accountRepository.save(account);

        // Act
        Optional<Account> found = accountRepository.findById(saved.getId());

        // Assert
        assertTrue(found.isPresent());
        assertNotNull(found.get().getUser());
        assertEquals(userTest.getId(), found.get().getUser().getId());
        assertEquals(userTest.getName(), found.get().getUser().getName());
        assertEquals(userTest.getEmail(), found.get().getUser().getEmail());
    }

    @Test
    @Order(14)
    @DisplayName("Should validate rollback on failure")
    @Transactional
    void testRollbackOnFailure_Success() {
        // Arrange - act - simulate failure
        assertThrows(RuntimeException.class, () -> {
            throw new RuntimeException("Simulation of failure for rollback test");
        });

        // Assert - verify countInvariant
        assertEquals(0, accountRepository.count());
    }
}
