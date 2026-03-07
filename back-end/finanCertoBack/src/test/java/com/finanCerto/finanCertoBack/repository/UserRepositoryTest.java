package com.finanCerto.finanCertoBack.repository;

import com.finanCerto.finanCertoBack.user.User;
import com.finanCerto.finanCertoBack.user.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

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
                userRepository.deleteAll();
                entityManager.flush();
            } catch (Exception ex) {
                // Ignore
            }
        }
    }

    @Test
    @DisplayName("Should find user by email")
    void shouldFindUserByEmail() {
        User user = newUser("João Silva", "joao.silva@test.com");

        entityManager.persist(user);
        entityManager.flush();

        Optional<User> found = userRepository.findByEmail("joao.silva@test.com");

        assertTrue(found.isPresent());
        assertEquals("João Silva", found.get().getName());
        assertEquals("joao.silva@test.com", found.get().getEmail());
    }

    @Test
    @DisplayName("Should return empty when searching for non-existent email")
    void shouldReturnEmptyWhenSearchingNonExistentEmail() {
        Optional<User> found = userRepository.findByEmail("nonexistent@test.com");

        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should verify user existence by email")
    void shouldVerifyUserExistenceByEmail() {
        User user = newUser("Maria Santos", "maria.santos@test.com");

        entityManager.persist(user);
        entityManager.flush();

        assertTrue(userRepository.existsByEmail("maria.santos@test.com"));
        assertFalse(userRepository.existsByEmail("nonexistent@test.com"));
    }

    @Test
    @DisplayName("Should save new user")
    void shouldSaveNewUser() {
        User user = newUser("Pedro Costa", "pedro.costa@test.com");
        user.setPassword("password123");

        User saved = userRepository.save(user);
        entityManager.flush();

        assertNotNull(saved.getId());
        assertEquals("Pedro Costa", saved.getName());
        assertEquals("pedro.costa@test.com", saved.getEmail());
    }

    @Test
    @DisplayName("Should retrieve user by ID")
    void shouldRetrieveUserById() {
        User user = newUser("Ana Paula", "ana.paula@test.com");
        user.setPassword("password123");

        User saved = userRepository.save(user);
        entityManager.flush();

        Optional<User> retrieved = userRepository.findById(saved.getId());

        assertTrue(retrieved.isPresent());
        assertEquals("Ana Paula", retrieved.get().getName());
        assertEquals("ana.paula@test.com", retrieved.get().getEmail());
    }

    @Test
    @DisplayName("Should update user information")
    void shouldUpdateUserInformation() {
        User user = newUser("Lucas Oliveira", "lucas.oliveira@test.com");
        user.setPassword("password123");

        User saved = userRepository.save(user);
        entityManager.flush();

        saved.setName("Lucas Oliveira Silva");
        saved.setPassword("newPassword456");
        User updated = userRepository.save(saved);
        entityManager.flush();

        Optional<User> retrieved = userRepository.findById(updated.getId());

        assertTrue(retrieved.isPresent());
        assertEquals("Lucas Oliveira Silva", retrieved.get().getName());
    }

    @Test
    @DisplayName("Should delete existing user")
    void shouldDeleteExistingUser() {
        User user = newUser("Roberto Dias", "roberto.dias@test.com");
        user.setPassword("password123");

        User saved = userRepository.save(user);
        entityManager.flush();

        userRepository.deleteById(saved.getId());
        entityManager.flush();

        Optional<User> retrieved = userRepository.findById(saved.getId());

        assertFalse(retrieved.isPresent());
    }

    @Test
    @DisplayName("Should count total users")
    void shouldCountTotalUsers() {
        User user1 = newUser("User 1", "user1@test.com");
        User user2 = newUser("User 2", "user2@test.com");
        User user3 = newUser("User 3", "user3@test.com");

        user1.setPassword("password123");
        user2.setPassword("password123");
        user3.setPassword("password123");

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);
        entityManager.flush();

        long total = userRepository.count();

        assertEquals(3, total);
    }

    @Test
    @DisplayName("Should retrieve all users")
    void shouldRetrieveAllUsers() {
        User user1 = newUser("User A", "userA@test.com");
        User user2 = newUser("User B", "userB@test.com");

        user1.setPassword("password123");
        user2.setPassword("password123");

        userRepository.save(user1);
        userRepository.save(user2);
        entityManager.flush();

        var all = userRepository.findAll();

        assertTrue(all.size() >= 2);
        assertTrue(all.stream().anyMatch(u -> u.getEmail().equals("userA@test.com")));
        assertTrue(all.stream().anyMatch(u -> u.getEmail().equals("userB@test.com")));
    }

    @Test
    @DisplayName("Should ensure email uniqueness")
    void shouldEnsureEmailUniqueness() {
        User user1 = newUser("User 1", "unique.email@test.com");
        user1.setPassword("password123");

        userRepository.save(user1);
        entityManager.flush();

        User user2 = newUser("User 2", "unique.email@test.com");
        user2.setPassword("password123");

        assertThrows(Exception.class, () -> {
            userRepository.save(user2);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Should implement UserDetails correctly")
    void shouldImplementUserDetailsCorrectly() {
        User user = newUser("Test UserDetails", "userdetails@test.com");
        user.setPassword("password123");

        User saved = userRepository.save(user);
        entityManager.flush();

        assertEquals("userdetails@test.com", saved.getUsername());
        assertEquals("password123", saved.getPassword());
        assertNotNull(saved.getAuthorities());
    }

    @Test
    @DisplayName("Should persist user password correctly")
    void shouldPersistUserPasswordCorrectly() {
        User user = newUser("Test Password", "testpassword@test.com");
        user.setPassword("StrongPassword@123");

        User saved = userRepository.save(user);
        entityManager.flush();

        Optional<User> retrieved = userRepository.findByEmail("testpassword@test.com");

        assertTrue(retrieved.isPresent());
        assertEquals("StrongPassword@123", retrieved.get().getPassword());
    }

    private User newUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword("pwd");
        return user;
    }
}
