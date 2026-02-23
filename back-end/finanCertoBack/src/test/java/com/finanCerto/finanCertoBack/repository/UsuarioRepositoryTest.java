package com.finanCerto.finanCertoBack.repository;

import com.finanCerto.finanCertoBack.usuario.Usuario;
import com.finanCerto.finanCertoBack.usuario.UsuarioRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setup() {
        usuarioRepository.deleteAll();
        entityManager.flush();
    }    @Test
    @DisplayName("Deve encontrar usuário por email")
    void deveEncontrarUsuarioPorEmail() {
        Usuario usuario = novoUsuario("João Silva", "joao.silva@test.com");

        entityManager.persist(usuario);
        entityManager.flush();

        Optional<Usuario> encontrado = usuarioRepository.findByEmail("joao.silva@test.com");

        assertTrue(encontrado.isPresent());
        assertEquals("João Silva", encontrado.get().getNome());
        assertEquals("joao.silva@test.com", encontrado.get().getEmail());
    }

    @Test
    @DisplayName("Deve retornar vazio ao buscar email inexistente")
    void deveRetornarVazioAoBuscarEmailInexistente() {
        Optional<Usuario> encontrado = usuarioRepository.findByEmail("inexistente@test.com");

        assertFalse(encontrado.isPresent());
    }

    @Test
    @DisplayName("Deve verificar existência de usuário por email")
    void deveVerificarExistenciaUsuarioPorEmail() {
        Usuario usuario = novoUsuario("Maria Santos", "maria.santos@test.com");

        entityManager.persist(usuario);
        entityManager.flush();

        assertTrue(usuarioRepository.existsByEmail("maria.santos@test.com"));
        assertFalse(usuarioRepository.existsByEmail("naoexiste@test.com"));
    }

    @Test
    @DisplayName("Deve salvar novo usuário")
    void deveSalvarNovoUsuario() {
        Usuario usuario = novoUsuario("Pedro Costa", "pedro.costa@test.com");
        usuario.setSenha("senha123");

        Usuario salvo = usuarioRepository.save(usuario);
        entityManager.flush();

        assertNotNull(salvo.getId());
        assertEquals("Pedro Costa", salvo.getNome());
        assertEquals("pedro.costa@test.com", salvo.getEmail());
    }

    @Test
    @DisplayName("Deve recuperar usuário por ID")
    void deveRecuperarUsuarioPorId() {
        Usuario usuario = novoUsuario("Ana Paula", "ana.paula@test.com");
        usuario.setSenha("senha123");

        Usuario salvo = usuarioRepository.save(usuario);
        entityManager.flush();

        Optional<Usuario> recuperado = usuarioRepository.findById(salvo.getId());

        assertTrue(recuperado.isPresent());
        assertEquals("Ana Paula", recuperado.get().getNome());
        assertEquals("ana.paula@test.com", recuperado.get().getEmail());
    }

    @Test
    @DisplayName("Deve atualizar informações do usuário")
    void deveAtualizarInformacoesUsuario() {
        Usuario usuario = novoUsuario("Lucas Oliveira", "lucas.oliveira@test.com");
        usuario.setSenha("senha123");

        Usuario salvo = usuarioRepository.save(usuario);
        entityManager.flush();

        salvo.setNome("Lucas Oliveira Silva");
        salvo.setSenha("novaSenha456");
        Usuario atualizado = usuarioRepository.save(salvo);
        entityManager.flush();

        Optional<Usuario> recuperado = usuarioRepository.findById(atualizado.getId());

        assertTrue(recuperado.isPresent());
        assertEquals("Lucas Oliveira Silva", recuperado.get().getNome());
    }

    @Test
    @DisplayName("Deve deletar usuário existente")
    void deveDeletarUsuarioExistente() {
        Usuario usuario = novoUsuario("Roberto Dias", "roberto.dias@test.com");
        usuario.setSenha("senha123");

        Usuario salvo = usuarioRepository.save(usuario);
        entityManager.flush();

        usuarioRepository.deleteById(salvo.getId());
        entityManager.flush();

        Optional<Usuario> recuperado = usuarioRepository.findById(salvo.getId());

        assertFalse(recuperado.isPresent());
    }

    @Test
    @DisplayName("Deve contar total de usuários")
    void deveContarTotalUsuarios() {
        Usuario usuario1 = novoUsuario("Usuário 1", "usuario1@test.com");
        Usuario usuario2 = novoUsuario("Usuário 2", "usuario2@test.com");
        Usuario usuario3 = novoUsuario("Usuário 3", "usuario3@test.com");

        usuario1.setSenha("senha123");
        usuario2.setSenha("senha123");
        usuario3.setSenha("senha123");

        usuarioRepository.save(usuario1);
        usuarioRepository.save(usuario2);
        usuarioRepository.save(usuario3);
        entityManager.flush();

        long total = usuarioRepository.count();

        assertEquals(3, total);
    }

    @Test
    @DisplayName("Deve recuperar todos os usuários")
    void deveRecuperarTodosUsuarios() {
        Usuario usuario1 = novoUsuario("Usuário A", "usuarioA@test.com");
        Usuario usuario2 = novoUsuario("Usuário B", "usuarioB@test.com");

        usuario1.setSenha("senha123");
        usuario2.setSenha("senha123");

        usuarioRepository.save(usuario1);
        usuarioRepository.save(usuario2);
        entityManager.flush();

        var todos = usuarioRepository.findAll();

        assertTrue(todos.size() >= 2);
        assertTrue(todos.stream().anyMatch(u -> u.getEmail().equals("usuarioA@test.com")));
        assertTrue(todos.stream().anyMatch(u -> u.getEmail().equals("usuarioB@test.com")));
    }

    @Test
    @DisplayName("Deve garantir unicidade de email")
    void deveGarantirUnicidadeEmail() {
        Usuario usuario1 = novoUsuario("Usuário 1", "email.unico@test.com");
        usuario1.setSenha("senha123");

        usuarioRepository.save(usuario1);
        entityManager.flush();

        Usuario usuario2 = novoUsuario("Usuário 2", "email.unico@test.com");
        usuario2.setSenha("senha123");

        assertThrows(Exception.class, () -> {
            usuarioRepository.save(usuario2);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Deve implementar UserDetails corretamente")
    void deveImplementarUserDetailsCorretamente() {
        Usuario usuario = novoUsuario("Teste UserDetails", "userdetails@test.com");
        usuario.setSenha("senha123");

        Usuario salvo = usuarioRepository.save(usuario);
        entityManager.flush();

        assertEquals("userdetails@test.com", salvo.getUsername());
        assertEquals("senha123", salvo.getPassword());
        assertNotNull(salvo.getAuthorities());
    }

    @Test
    @DisplayName("Deve persistir senha do usuário corretamente")
    void devePersistirSenhaUsuarioCorretamente() {
        Usuario usuario = novoUsuario("Teste Senha", "testsenha@test.com");
        usuario.setSenha("senhaForte@123");

        Usuario salvo = usuarioRepository.save(usuario);
        entityManager.flush();

        Optional<Usuario> recuperado = usuarioRepository.findByEmail("testsenha@test.com");

        assertTrue(recuperado.isPresent());
        assertEquals("senhaForte@123", recuperado.get().getSenha());
    }

    private Usuario novoUsuario(String nome, String email) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha("pwd");
        return usuario;
    }
}
