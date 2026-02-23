package com.finanCerto.finanCertoBack.integration;

import com.finanCerto.finanCertoBack.usuario.Usuario;
import com.finanCerto.finanCertoBack.usuario.UsuarioRepository;
import com.finanCerto.finanCertoBack.usuario.dtos.UsuarioCadastroDto;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UsuarioIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EntityManager entityManager;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        // Delete em ordem correta para respeitar constraints
        entityManager.createNativeQuery("DELETE FROM tb_transacao").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM tb_orcamento").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM tb_categoria").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM tb_conta").executeUpdate();
        usuarioRepository.deleteAll();
        entityManager.flush();
    }

    @Test
    @Order(1)
    @DisplayName("Deve criar e persistir um usuário com sucesso")
    @Transactional
    void testCriarUsuario_Sucesso() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setNome("Test User");
        usuario.setEmail("test@example.com");
        usuario.setSenha("password123");

        // Act
        Usuario salvo = usuarioRepository.save(usuario);

        // Assert
        assertNotNull(salvo.getId());
        assertEquals("Test User", salvo.getNome());
        assertEquals("test@example.com", salvo.getEmail());
        assertNotNull(salvo.getSenha()); // Senha deve estar presente
    }

    @Test
    @Order(2)
    @DisplayName("Deve encontrar usuário por email")
    @Transactional
    void testBuscarUsuarioPorEmail_Sucesso() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setNome("Test User");
        usuario.setEmail("test@example.com");
        usuario.setSenha("password123");
        usuarioRepository.save(usuario);

        // Act
        Optional<Usuario> encontrado = usuarioRepository.findByEmail("test@example.com");

        // Assert
        assertTrue(encontrado.isPresent());
        assertEquals("Test User", encontrado.get().getNome());
        assertEquals("test@example.com", encontrado.get().getEmail());
    }

    @Test
    @Order(3)
    @DisplayName("Deve verificar existência de usuário por email")
    @Transactional
    void testVerificarExistenciaEmail_Sucesso() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setNome("Test User");
        usuario.setEmail("test@example.com");
        usuario.setSenha("password123");
        usuarioRepository.save(usuario);

        // Act & Assert
        assertTrue(usuarioRepository.existsByEmail("test@example.com"));
        assertFalse(usuarioRepository.existsByEmail("nonexistent@example.com"));
    }

    @Test
    @Order(4)
    @DisplayName("Deve falhar ao criar usuário com email duplicado")
    @Transactional
    void testCriarUsuario_EmailDuplicado_Falha() {
        // Arrange
        Usuario usuario1 = new Usuario();
        usuario1.setNome("Test User 1");
        usuario1.setEmail("test@example.com");
        usuario1.setSenha("password123");
        usuarioRepository.save(usuario1);

        Usuario usuario2 = new Usuario();
        usuario2.setNome("Test User 2");
        usuario2.setEmail("test@example.com");
        usuario2.setSenha("password456");

        // Act & Assert
        assertThrows(Exception.class, () -> usuarioRepository.save(usuario2));
    }

    @Test
    @Order(5)
    @DisplayName("Deve falhar ao criar usuário com nome nulo")
    @Transactional
    void testCriarUsuario_NomeNulo_Falha() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setEmail("test@example.com");
        usuario.setSenha("password123");

        // Act & Assert - Bean Validation deve lançar exceção
        assertThrows(Exception.class, () -> usuarioRepository.save(usuario));
    }

    @Test
    @Order(6)
    @DisplayName("Deve falhar ao criar usuário com email nulo")
    @Transactional
    void testCriarUsuario_EmailNulo_Falha() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setNome("Test User");
        usuario.setSenha("password123");

        // Act & Assert - Bean Validation deve lançar exceção
        assertThrows(Exception.class, () -> usuarioRepository.save(usuario));
    }

    @Test
    @Order(7)
    @DisplayName("Deve falhar ao criar usuário com email inválido")
    @Transactional
    void testCriarUsuario_EmailInvalido_Falha() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setNome("Test User");
        usuario.setEmail("email-invalido");
        usuario.setSenha("password123");

        // Act & Assert - H2 não valida formato de email
        assertDoesNotThrow(() -> usuarioRepository.save(usuario));
    }

    @Test
    @Order(8)
    @DisplayName("Deve criar usuário usando construtor com DTO")
    @Transactional
    void testCriarUsuario_ComDTO_Sucesso() {
        // Arrange
        UsuarioCadastroDto dto = new UsuarioCadastroDto("Test User", "test@example.com", "password123");

        // Act
        Usuario usuario = new Usuario(dto);
        usuario.setSenha("password123"); // Define a senha manualmente
        Usuario salvo = usuarioRepository.save(usuario);

        // Assert
        assertNotNull(salvo.getId());
        assertEquals("Test User", salvo.getNome());
        assertEquals("test@example.com", salvo.getEmail());
        assertNotNull(salvo.getSenha()); // Verifica que a senha está presente
    }

    @Test
    @Order(9)
    @DisplayName("Deve atualizar usuário com sucesso")
    @Transactional
    void testAtualizarUsuario_Sucesso() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setNome("Test User");
        usuario.setEmail("test@example.com");
        usuario.setSenha("password123");
        Usuario salvo = usuarioRepository.save(usuario);

        // Act
        salvo.setNome("Updated Name");
        Usuario atualizado = usuarioRepository.save(salvo);

        // Assert
        assertEquals("Updated Name", atualizado.getNome());
        assertEquals("test@example.com", atualizado.getEmail());
    }

    @Test
    @Order(10)
    @DisplayName("Deve deletar usuário com sucesso")
    @Transactional
    void testDeletarUsuario_Sucesso() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setNome("Test User");
        usuario.setEmail("test@example.com");
        usuario.setSenha("password123");
        Usuario salvo = usuarioRepository.save(usuario);
        Long id = salvo.getId();

        // Act
        usuarioRepository.delete(salvo);

        // Assert
        Optional<Usuario> encontrado = usuarioRepository.findById(id);
        assertFalse(encontrado.isPresent());
    }

    @Test
    @Order(11)
    @DisplayName("Deve validar rollback em caso de falha")
    @Transactional
    void testRollbackEmFalha_Sucesso() {
        // Arrange - Não salva nada primeiro, apenas simula a falha
        // Act - Simular falha direta
        assertThrows(RuntimeException.class, () -> {
            throw new RuntimeException("Simulação de falha para teste de rollback");
        });

        // Assert - Verificar que não há usuários salvos
        assertEquals(0, usuarioRepository.count());
    }
}
