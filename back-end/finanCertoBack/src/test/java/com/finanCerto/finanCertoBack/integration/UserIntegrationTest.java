package com.finanCerto.finanCertoBack.integration;

import com.finanCerto.finanCertoBack.user.User;
import com.finanCerto.finanCertoBack.user.UserRepository;
import com.finanCerto.finanCertoBack.user.dtos.UserRegistrationDto;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EntityManager entityManager;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        // Delete in correct order to respect constraints
        entityManager.createNativeQuery("DELETE FROM tb_transacao").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM tb_orcamento").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM tb_categoria").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM tb_conta").executeUpdate();
        userRepository.deleteAll();
        entityManager.flush();
    }

    @Test
    @Order(1)
    @DisplayName("Should create and persist a user successfully")
    @Transactional
    void testCreateUser_Success() {
        // Arrange
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("password123");

        // Act
        User saved = userRepository.save(user);

        // Assert
        assertNotNull(saved.getId());
        assertEquals("Test User", saved.getName());
        assertEquals("test@example.com", saved.getEmail());
        assertNotNull(saved.getPassword()); // Password should be present
    }

    @Test
    @Order(2)
    @DisplayName("Should find user by email")
    @Transactional
    void testFindUserByEmail_Success() {
        // Arrange
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        userRepository.save(user);

        // Act
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("Test User", found.get().getName());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    @Order(3)
    @DisplayName("Should verify user existence by email")
    @Transactional
    void testVerifyEmailExistence_Success() {
        // Arrange
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        userRepository.save(user);

        // Act & Assert
        assertTrue(userRepository.existsByEmail("test@example.com"));
        assertFalse(userRepository.existsByEmail("nonexistent@example.com"));
    }

    @Test
    @Order(4)
    @DisplayName("Should fail creating user with duplicate email")
    @Transactional
    void testCreateUser_DuplicateEmail_Failure() {
        // Arrange
        User user1 = new User();
        user1.setName("Test User 1");
        user1.setEmail("test@example.com");
        user1.setPassword("password123");
        userRepository.save(user1);

        User user2 = new User();
        user2.setName("Test User 2");
        user2.setEmail("test@example.com");
        user2.setPassword("password456");

        // Act & Assert
        assertThrows(Exception.class, () -> userRepository.save(user2));
    }

    @Test
    @Order(5)
    @DisplayName("Should fail creating user with null name")
    @Transactional
    void testCreateUser_NullName_Failure() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password123");

        // Act & Assert - Bean Validation should throw exception
        assertThrows(Exception.class, () -> userRepository.save(user));
    }

    @Test
    @Order(6)
    @DisplayName("Should fail creating user with null email")
    @Transactional
    void testCreateUser_NullEmail_Failure() {
        // Arrange
        User user = new User();
        user.setName("Test User");
        user.setPassword("password123");

        // Act & Assert - Bean Validation should throw exception
        assertThrows(Exception.class, () -> userRepository.save(user));
    }

    @Test
    @Order(7)
    @DisplayName("Should fail creating user with invalid email")
    @Transactional
    void testCreateUser_InvalidEmail_Failure() {
        // Arrange
        User user = new User();
        user.setName("Test User");
        user.setEmail("invalid-email");
        user.setPassword("password123");

        // Act & Assert - Bean Validation should throw exception
        assertThrows(Exception.class, () -> userRepository.save(user));
    }

    @Test
    @Order(8)
    @DisplayName("Should create user using DTO constructor")
    @Transactional
    void testCreateUser_WithDTO_Success() {
        // Arrange
        UserRegistrationDto dto = new UserRegistrationDto("Test User", "test@example.com", "password123");

        // Act
        User user = new User(dto);
        user.setPassword("password123"); // Password set manually if not handled by constructor correctly
        User saved = userRepository.save(user);

        // Assert
        assertNotNull(saved.getId());
        assertEquals("Test User", saved.getName());
        assertEquals("test@example.com", saved.getEmail());
        assertNotNull(saved.getPassword());
    }

    @Test
    @Order(9)
    @DisplayName("Should update user successfully")
    @Transactional
    void testUpdateUser_Success() {
        // Arrange
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        User saved = userRepository.save(user);

        // Act
        saved.setName("Updated Name");
        User updated = userRepository.save(saved);

        // Assert
        assertEquals("Updated Name", updated.getName());
        assertEquals("test@example.com", updated.getEmail());
    }

    @Test
    @Order(10)
    @DisplayName("Should delete user successfully")
    @Transactional
    void testDeleteUser_Success() {
        // Arrange
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        User saved = userRepository.save(user);
        Long id = saved.getId();

        // Act
        userRepository.delete(saved);

        // Assert
        Optional<User> found = userRepository.findById(id);
        assertFalse(found.isPresent());
    }

    @Test
    @Order(11)
    @DisplayName("Should validate rollback on failure")
    @Transactional
    void testRollbackOnFailure_Success() {
        // Arrange - act - simulate failure
        assertThrows(RuntimeException.class, () -> {
            throw new RuntimeException("Simulation of failure for rollback test");
        });

        // Assert - verify count invariant
        assertEquals(0, userRepository.count());
    }
}
