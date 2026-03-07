package com.finanCerto.finanCertoBack.repository;

import com.finanCerto.finanCertoBack.account.AccountType;
import com.finanCerto.finanCertoBack.account.Account;
import com.finanCerto.finanCertoBack.account.AccountRepository;
import com.finanCerto.finanCertoBack.user.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

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
                entityManager.createNativeQuery("DELETE FROM tb_conta").executeUpdate();
                entityManager.createNativeQuery("DELETE FROM tb_usuario").executeUpdate();
                entityManager.flush();
            } catch (Exception ex) {
                // Ignore
            }
        }
    }

    @Test
    @DisplayName("Should verify existence of account by user and name")
    void shouldVerifyAccountExistenceByUserIdAndName() {
        User user = newUser("Pedro", "pedro@test.com");
        Account account = newAccount("Checking Account", AccountType.CHECKING, 1000.0, user);

        entityManager.persist(user);
        entityManager.persist(account);
        entityManager.flush();

        assertTrue(accountRepository.existsByUserIdAndName(user.getId(), "Checking Account"));
        assertFalse(accountRepository.existsByUserIdAndName(user.getId(), "DoesNotExist"));
        assertFalse(accountRepository.existsByUserIdAndName(9999L, "Checking Account"));
    }

    @Test
    @DisplayName("Should find account by user and name")
    void shouldFindAccountByUserIdAndName() {
        User user = newUser("Carlos", "carlos@test.com");
        Account account = newAccount("Savings", AccountType.SAVINGS, 5000.0, user);

        entityManager.persist(user);
        entityManager.persist(account);
        entityManager.flush();

        Optional<Account> found = accountRepository.findByNameAndUserId(user.getId(), "Savings");
        
        assertTrue(found.isPresent());
        assertEquals("Savings", found.get().getName());
        assertEquals(AccountType.SAVINGS, found.get().getType());
        assertEquals(5000.0, found.get().getInitialBalance());
    }

    @Test
    @DisplayName("Should return empty when seeking non-existent account")
    void shouldReturnEmptyWhenSeekingNonExistentAccount() {
        User user = newUser("Marina", "marina@test.com");
        entityManager.persist(user);
        entityManager.flush();

        Optional<Account> found = accountRepository.findByNameAndUserId(user.getId(), "NonExistentAccount");
        
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should list all user accounts with pagination")
    void shouldListAllAccountsByUserIdWithPagination() {
        User user = newUser("Felipe", "felipe@test.com");
        Account account1 = newAccount("Account 1", AccountType.CHECKING, 1000.0, user);
        Account account2 = newAccount("Account 2", AccountType.SAVINGS, 2000.0, user);
        Account account3 = newAccount("Account 3", AccountType.CREDIT_CARD, 3000.0, user);

        entityManager.persist(user);
        entityManager.persist(account1);
        entityManager.persist(account2);
        entityManager.persist(account3);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 2);
        Page<Account> page1 = accountRepository.findAllByUserId(user.getId(), pageable);
        
        assertEquals(2, page1.getContent().size());
        assertEquals(3, page1.getTotalElements());
        assertTrue(page1.hasNext());

        Pageable pageable2 = PageRequest.of(1, 2);
        Page<Account> page2 = accountRepository.findAllByUserId(user.getId(), pageable2);
        
        assertEquals(1, page2.getContent().size());
        assertFalse(page2.hasNext());
    }

    @Test
    @DisplayName("Should return empty page for user without accounts")
    void shouldReturnEmptyPageForUserWithoutAccounts() {
        User user = newUser("Bruno", "bruno@test.com");
        entityManager.persist(user);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Account> page = accountRepository.findAllByUserId(user.getId(), pageable);
        
        assertTrue(page.getContent().isEmpty());
        assertEquals(0, page.getTotalElements());
    }

    @Test
    @DisplayName("Should save and retrieve account")
    void shouldSaveAndRetrieveAccount() {
        User user = newUser("Lucia", "lucia@test.com");
        Account account = newAccount("Credit Card", AccountType.CREDIT_CARD, 5000.0, user);

        entityManager.persist(user);
        Account saved = accountRepository.save(account);
        entityManager.flush();

        Optional<Account> retrieved = accountRepository.findById(saved.getId());
        
        assertTrue(retrieved.isPresent());
        assertEquals("Credit Card", retrieved.get().getName());
        assertEquals(AccountType.CREDIT_CARD, retrieved.get().getType());
        assertEquals(5000.0, retrieved.get().getInitialBalance());
    }

    @Test
    @DisplayName("Should update existing account")
    void shouldUpdateExistingAccount() {
        User user = newUser("Paulo", "paulo@test.com");
        Account account = newAccount("Original Account", AccountType.CHECKING, 1000.0, user);

        entityManager.persist(user);
        Account saved = accountRepository.save(account);
        entityManager.flush();

        saved.setName("Updated Account");
        saved.setInitialBalance(2000.0);
        Account updated = accountRepository.save(saved);
        entityManager.flush();

        Optional<Account> retrieved = accountRepository.findById(updated.getId());
        
        assertTrue(retrieved.isPresent());
        assertEquals("Updated Account", retrieved.get().getName());
        assertEquals(2000.0, retrieved.get().getInitialBalance());
    }

    @Test
    @DisplayName("Should delete existing account")
    void shouldDeleteExistingAccount() {
        User user = newUser("Diana", "diana@test.com");
        Account account = newAccount("Delete Account", AccountType.SAVINGS, 1000.0, user);

        entityManager.persist(user);
        Account saved = accountRepository.save(account);
        entityManager.flush();

        accountRepository.deleteById(saved.getId());
        entityManager.flush();

        Optional<Account> retrieved = accountRepository.findById(saved.getId());
        assertFalse(retrieved.isPresent());
    }

    @Test
    @DisplayName("Should count total accounts in database")
    void shouldCountTotalAccounts() {
        User user1 = newUser("User1", "user1@test.com");
        User user2 = newUser("User2", "user2@test.com");
        
        Account account1 = newAccount("Account 1", AccountType.CHECKING, 1000.0, user1);
        Account account2 = newAccount("Account 2", AccountType.SAVINGS, 2000.0, user1);
        Account account3 = newAccount("Account 3", AccountType.CREDIT_CARD, 3000.0, user2);

        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(account1);
        entityManager.persist(account2);
        entityManager.persist(account3);
        entityManager.flush();

        long total = accountRepository.count();
        assertEquals(3, total);
    }

    private User newUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword("password123");
        return user;
    }

    private Account newAccount(String name, AccountType type, double initialBalance, User user) {
        Account account = new Account();
        account.setName(name);
        account.setType(type);
        account.setInitialBalance(initialBalance);
        account.setUser(user);
        return account;
    }
}
