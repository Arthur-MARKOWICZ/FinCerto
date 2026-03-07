package com.finanCerto.finanCertoBack.repository;

import com.finanCerto.finanCertoBack.category.Category;
import com.finanCerto.finanCertoBack.category.CategoryType;
import com.finanCerto.finanCertoBack.account.AccountType;
import com.finanCerto.finanCertoBack.account.Account;
import com.finanCerto.finanCertoBack.transaction.TransactionType;
import com.finanCerto.finanCertoBack.transaction.Transaction;
import com.finanCerto.finanCertoBack.transaction.TransactionRepository;
import com.finanCerto.finanCertoBack.user.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

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
    @DisplayName("Should save and retrieve transaction")
    void shouldSaveAndRetrieve() {
        User user = newUser("Joao", "joao@test.com");
        Category category = newCategory("Market", CategoryType.EXPENSE, user);
        Account account = newAccount("Wallet", AccountType.CHECKING, 200.0, user);

        Transaction transaction = new Transaction();
        transaction.setDescription("Purchase");
        transaction.setAmount(50.0);
        transaction.setType(TransactionType.EXPENSE);
        transaction.setDate(LocalDateTime.now());
        transaction.setUser(user);
        transaction.setCategory(category);
        transaction.setAccount(account);

        entityManager.persist(user);
        entityManager.persist(category);
        entityManager.persist(account);
        Transaction saved = transactionRepository.save(transaction);

        Optional<Transaction> found = transactionRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Purchase", found.get().getDescription());
        assertEquals(50.0, found.get().getAmount());
    }

    @Test
    @DisplayName("Should retrieve transaction by ID")
    void shouldRetrieveTransactionById() {
        User user = newUser("Maria", "maria@test.com");
        Category category = newCategory("Leisure", CategoryType.EXPENSE, user);
        Account account = newAccount("Savings", AccountType.SAVINGS, 1000.0, user);

        Transaction transaction = new Transaction();
        transaction.setDescription("Cinema");
        transaction.setAmount(30.0);
        transaction.setType(TransactionType.EXPENSE);
        transaction.setDate(LocalDateTime.now());
        transaction.setUser(user);
        transaction.setCategory(category);
        transaction.setAccount(account);

        entityManager.persist(user);
        entityManager.persist(category);
        entityManager.persist(account);
        Transaction saved = transactionRepository.save(transaction);
        entityManager.flush();

        Optional<Transaction> retrieved = transactionRepository.findById(saved.getId());

        assertTrue(retrieved.isPresent());
        assertEquals("Cinema", retrieved.get().getDescription());
        assertEquals(30.0, retrieved.get().getAmount());
        assertEquals(TransactionType.EXPENSE, retrieved.get().getType());
    }

    @Test
    @DisplayName("Should save transaction with INCOME type")
    void shouldSaveTransactionWithIncomeType() {
        User user = newUser("Pedro", "pedro@test.com");
        Category category = newCategory("Salary", CategoryType.INCOME, user);
        Account account = newAccount("Salary Account", AccountType.CHECKING, 0.0, user);

        Transaction transaction = new Transaction();
        transaction.setDescription("Monthly salary");
        transaction.setAmount(3000.0);
        transaction.setType(TransactionType.INCOME);
        transaction.setDate(LocalDateTime.now());
        transaction.setUser(user);
        transaction.setCategory(category);
        transaction.setAccount(account);

        entityManager.persist(user);
        entityManager.persist(category);
        entityManager.persist(account);
        Transaction saved = transactionRepository.save(transaction);
        entityManager.flush();

        Optional<Transaction> retrieved = transactionRepository.findById(saved.getId());

        assertTrue(retrieved.isPresent());
        assertEquals(TransactionType.INCOME, retrieved.get().getType());
        assertEquals(3000.0, retrieved.get().getAmount());
    }

    @Test
    @DisplayName("Should update existing transaction")
    void shouldUpdateExistingTransaction() {
        User user = newUser("Carlos", "carlos@test.com");
        Category category = newCategory("Food", CategoryType.EXPENSE, user);
        Account account = newAccount("Debit", AccountType.CHECKING, 500.0, user);

        Transaction transaction = new Transaction();
        transaction.setDescription("Original purchase");
        transaction.setAmount(50.0);
        transaction.setType(TransactionType.EXPENSE);
        transaction.setDate(LocalDateTime.now());
        transaction.setUser(user);
        transaction.setCategory(category);
        transaction.setAccount(account);

        entityManager.persist(user);
        entityManager.persist(category);
        entityManager.persist(account);
        Transaction saved = transactionRepository.save(transaction);
        entityManager.flush();

        saved.setDescription("Updated purchase");
        saved.setAmount(75.0);
        Transaction updated = transactionRepository.save(saved);
        entityManager.flush();

        Optional<Transaction> retrieved = transactionRepository.findById(updated.getId());

        assertTrue(retrieved.isPresent());
        assertEquals("Updated purchase", retrieved.get().getDescription());
        assertEquals(75.0, retrieved.get().getAmount());
    }

    @Test
    @DisplayName("Should delete existing transaction")
    void shouldDeleteExistingTransaction() {
        User user = newUser("Lucia", "lucia@test.com");
        Category category = newCategory("Health", CategoryType.EXPENSE, user);
        Account account = newAccount("Credit", AccountType.CREDIT_CARD, 5000.0, user);

        Transaction transaction = new Transaction();
        transaction.setDescription("Pharmacy");
        transaction.setAmount(120.0);
        transaction.setType(TransactionType.EXPENSE);
        transaction.setDate(LocalDateTime.now());
        transaction.setUser(user);
        transaction.setCategory(category);
        transaction.setAccount(account);

        entityManager.persist(user);
        entityManager.persist(category);
        entityManager.persist(account);
        Transaction saved = transactionRepository.save(transaction);
        entityManager.flush();

        transactionRepository.deleteById(saved.getId());
        entityManager.flush();

        Optional<Transaction> retrieved = transactionRepository.findById(saved.getId());

        assertFalse(retrieved.isPresent());
    }

    @Test
    @DisplayName("Should count total transactions")
    void shouldCountTotalTransactions() {
        User user = newUser("Beatriz", "beatriz@test.com");
        Category category = newCategory("Various", CategoryType.EXPENSE, user);
        Account account = newAccount("Main", AccountType.CHECKING, 1000.0, user);

        Transaction transaction1 = createTransaction("Transaction 1", 100.0, user, category, account);
        Transaction transaction2 = createTransaction("Transaction 2", 200.0, user, category, account);
        Transaction transaction3 = createTransaction("Transaction 3", 150.0, user, category, account);

        entityManager.persist(user);
        entityManager.persist(category);
        entityManager.persist(account);
        entityManager.persist(transaction1);
        entityManager.persist(transaction2);
        entityManager.persist(transaction3);
        entityManager.flush();

        long total = transactionRepository.count();

        assertTrue(total >= 3);
    }

    @Test
    @DisplayName("Should retrieve all transactions")
    void shouldRetrieveAllTransactions() {
        User user = newUser("Felipe", "felipe@test.com");
        Category category = newCategory("Miscellaneous", CategoryType.EXPENSE, user);
        Account account = newAccount("General", AccountType.CHECKING, 2000.0, user);

        Transaction transaction1 = createTransaction("Transaction A", 50.0, user, category, account);
        Transaction transaction2 = createTransaction("Transaction B", 100.0, user, category, account);

        entityManager.persist(user);
        entityManager.persist(category);
        entityManager.persist(account);
        entityManager.persist(transaction1);
        entityManager.persist(transaction2);
        entityManager.flush();

        var all = transactionRepository.findAll();

        assertTrue(all.size() >= 2);
    }

    @Test
    @DisplayName("Should save transaction without description")
    void shouldSaveTransactionWithoutDescription() {
        User user = newUser("Gabriel", "gabriel@test.com");
        Category category = newCategory("Transport", CategoryType.EXPENSE, user);
        Account account = newAccount("Transport", AccountType.CHECKING, 500.0, user);

        Transaction transaction = new Transaction();
        transaction.setAmount(10.0);
        transaction.setType(TransactionType.EXPENSE);
        transaction.setDate(LocalDateTime.now());
        transaction.setUser(user);
        transaction.setCategory(category);
        transaction.setAccount(account);

        entityManager.persist(user);
        entityManager.persist(category);
        entityManager.persist(account);
        Transaction saved = transactionRepository.save(transaction);
        entityManager.flush();

        Optional<Transaction> retrieved = transactionRepository.findById(saved.getId());

        assertTrue(retrieved.isPresent());
        assertEquals(10.0, retrieved.get().getAmount());
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

    private Account newAccount(String name, AccountType type, double initialBalance, User user) {
        Account account = new Account();
        account.setName(name);
        account.setType(type);
        account.setInitialBalance(initialBalance);
        account.setUser(user);
        return account;
    }

    private Transaction createTransaction(String description, double amount, User user, Category category, Account account) {
        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.EXPENSE);
        transaction.setDate(LocalDateTime.now());
        transaction.setUser(user);
        transaction.setCategory(category);
        transaction.setAccount(account);
        return transaction;
    }
}
