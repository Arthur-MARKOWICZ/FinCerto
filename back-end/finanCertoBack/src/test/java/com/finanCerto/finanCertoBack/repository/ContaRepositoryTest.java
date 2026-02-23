package com.finanCerto.finanCertoBack.repository;

import com.finanCerto.finanCertoBack.conta.Conta;
import com.finanCerto.finanCertoBack.conta.ContaRepository;
import com.finanCerto.finanCertoBack.conta.Tipos;
import com.finanCerto.finanCertoBack.usuario.Usuario;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ContaRepositoryTest {

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Deve verificar existência de conta por usuário e nome")
    void deveVerificarExistenciaContaPorUsuarioAndNome() {
        Usuario usuario = novoUsuario("Pedro", "pedro@test.com");
        Conta conta = novaConta("Conta Corrente", Tipos.CORRENTE, 1000.0, usuario);

        entityManager.persist(usuario);
        entityManager.persist(conta);
        entityManager.flush();

        assertTrue(contaRepository.existsByUsuarioIdAndNome(usuario.getId(), "Conta Corrente"));
        assertFalse(contaRepository.existsByUsuarioIdAndNome(usuario.getId(), "NaoExiste"));
        assertFalse(contaRepository.existsByUsuarioIdAndNome(9999L, "Conta Corrente"));
    }

    @Test
    @DisplayName("Deve encontrar conta por usuário e nome")
    void deveEncontrarContaPorUsuarioAndNome() {
        Usuario usuario = novoUsuario("Carlos", "carlos@test.com");
        Conta conta = novaConta("Poupança", Tipos.POUPANCA, 5000.0, usuario);

        entityManager.persist(usuario);
        entityManager.persist(conta);
        entityManager.flush();

        Optional<Conta> encontrada = contaRepository.findByNameAndUsuarioID(usuario.getId(), "Poupança");
        
        assertTrue(encontrada.isPresent());
        assertEquals("Poupança", encontrada.get().getNome());
        assertEquals(Tipos.POUPANCA, encontrada.get().getTipo());
        assertEquals(5000.0, encontrada.get().getSaldoInicial());
    }

    @Test
    @DisplayName("Deve retornar vazio ao buscar conta inexistente")
    void deveRetornarVazioAoBuscarContaInexistente() {
        Usuario usuario = novoUsuario("Marina", "marina@test.com");
        entityManager.persist(usuario);
        entityManager.flush();

        Optional<Conta> encontrada = contaRepository.findByNameAndUsuarioID(usuario.getId(), "ContaInexistente");
        
        assertFalse(encontrada.isPresent());
    }

    @Test
    @DisplayName("Deve listar todas as contas de um usuário com paginação")
    void deveListarTodosContasPorUsuarioComPaginacao() {
        Usuario usuario = novoUsuario("Felipe", "felipe@test.com");
        Conta conta1 = novaConta("Conta 1", Tipos.CORRENTE, 1000.0, usuario);
        Conta conta2 = novaConta("Conta 2", Tipos.POUPANCA, 2000.0, usuario);
        Conta conta3 = novaConta("Conta 3", Tipos.CARTAO, 3000.0, usuario);

        entityManager.persist(usuario);
        entityManager.persist(conta1);
        entityManager.persist(conta2);
        entityManager.persist(conta3);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 2);
        Page<Conta> pagina1 = contaRepository.findAllByUsuarioId(usuario.getId(), pageable);
        
        assertEquals(2, pagina1.getContent().size());
        assertEquals(3, pagina1.getTotalElements());
        assertTrue(pagina1.hasNext());

        Pageable pageable2 = PageRequest.of(1, 2);
        Page<Conta> pagina2 = contaRepository.findAllByUsuarioId(usuario.getId(), pageable2);
        
        assertEquals(1, pagina2.getContent().size());
        assertFalse(pagina2.hasNext());
    }

    @Test
    @DisplayName("Deve retornar página vazia para usuário sem contas")
    void deveRetornarPaginaVaziaParaUsuarioSemContas() {
        Usuario usuario = novoUsuario("Bruno", "bruno@test.com");
        entityManager.persist(usuario);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Conta> pagina = contaRepository.findAllByUsuarioId(usuario.getId(), pageable);
        
        assertTrue(pagina.getContent().isEmpty());
        assertEquals(0, pagina.getTotalElements());
    }

    @Test
    @DisplayName("Deve salvar e recuperar conta")
    void deveSalvarERecuperarConta() {
        Usuario usuario = novoUsuario("Lucia", "lucia@test.com");
        Conta conta = novaConta("Cartão Crédito", Tipos.CARTAO, 5000.0, usuario);

        entityManager.persist(usuario);
        Conta salva = contaRepository.save(conta);
        entityManager.flush();

        Optional<Conta> recuperada = contaRepository.findById(salva.getId());
        
        assertTrue(recuperada.isPresent());
        assertEquals("Cartão Crédito", recuperada.get().getNome());
        assertEquals(Tipos.CARTAO, recuperada.get().getTipo());
        assertEquals(5000.0, recuperada.get().getSaldoInicial());
    }

    @Test
    @DisplayName("Deve atualizar conta existente")
    void deveAtualizarContaExistente() {
        Usuario usuario = novoUsuario("Paulo", "paulo@test.com");
        Conta conta = novaConta("Conta Original", Tipos.CORRENTE, 1000.0, usuario);

        entityManager.persist(usuario);
        Conta salva = contaRepository.save(conta);
        entityManager.flush();

        salva.setNome("Conta Atualizada");
        salva.setSaldoInicial(2000.0);
        Conta atualizada = contaRepository.save(salva);
        entityManager.flush();

        Optional<Conta> recuperada = contaRepository.findById(atualizada.getId());
        
        assertTrue(recuperada.isPresent());
        assertEquals("Conta Atualizada", recuperada.get().getNome());
        assertEquals(2000.0, recuperada.get().getSaldoInicial());
    }

    @Test
    @DisplayName("Deve deletar conta existente")
    void deveDeletarContaExistente() {
        Usuario usuario = novoUsuario("Diana", "diana@test.com");
        Conta conta = novaConta("Conta Deletar", Tipos.POUPANCA, 1000.0, usuario);

        entityManager.persist(usuario);
        Conta salva = contaRepository.save(conta);
        entityManager.flush();

        contaRepository.deleteById(salva.getId());
        entityManager.flush();

        Optional<Conta> recuperada = contaRepository.findById(salva.getId());
        assertFalse(recuperada.isPresent());
    }

    @Test
    @DisplayName("Deve contar total de contas no banco de dados")
    void deveContarTotalContas() {
        Usuario usuario1 = novoUsuario("Usuario1", "usuario1@test.com");
        Usuario usuario2 = novoUsuario("Usuario2", "usuario2@test.com");
        
        Conta conta1 = novaConta("Conta 1", Tipos.CORRENTE, 1000.0, usuario1);
        Conta conta2 = novaConta("Conta 2", Tipos.POUPANCA, 2000.0, usuario1);
        Conta conta3 = novaConta("Conta 3", Tipos.CARTAO, 3000.0, usuario2);

        entityManager.persist(usuario1);
        entityManager.persist(usuario2);
        entityManager.persist(conta1);
        entityManager.persist(conta2);
        entityManager.persist(conta3);
        entityManager.flush();

        long total = contaRepository.count();
        assertEquals(3, total);
    }

    private Usuario novoUsuario(String nome, String email) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha("senha123");
        return usuario;
    }

    private Conta novaConta(String nome, Tipos tipo, double saldoInicial, Usuario usuario) {
        Conta conta = new Conta();
        conta.setNome(nome);
        conta.setTipo(tipo);
        conta.setSaldoInicial(saldoInicial);
        conta.setUsuario(usuario);
        return conta;
    }
}
