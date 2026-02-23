package com.finanCerto.finanCertoBack.repository;

import com.finanCerto.finanCertoBack.categoria.Categoria;
import com.finanCerto.finanCertoBack.categoria.Tipo;
import com.finanCerto.finanCertoBack.conta.Conta;
import com.finanCerto.finanCertoBack.conta.Tipos;
import com.finanCerto.finanCertoBack.transacao.Transacao;
import com.finanCerto.finanCertoBack.transacao.TransacaoRepository;
import com.finanCerto.finanCertoBack.usuario.Usuario;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TransacaoRepositoryTest {

    @Autowired
    private TransacaoRepository transacaoRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Deve salvar e recuperar transação")
    void deveSalvarERecuperar() {
        Usuario usuario = novoUsuario("Joao", "joao@test.com");
        Categoria categoria = novaCategoria("Mercado", Tipo.DESPESA, usuario);
        Conta conta = novaConta("Carteira", Tipos.CORRENTE, 200.0, usuario);

        Transacao transacao = new Transacao();
        transacao.setDescricao("Compra");
        transacao.setValor(50.0);
        transacao.setTipo(com.finanCerto.finanCertoBack.transacao.Tipos.DESPESA);
        transacao.setDate(LocalDateTime.now());
        transacao.setUsuario(usuario);
        transacao.setCategoria(categoria);
        transacao.setConta(conta);

        entityManager.persist(usuario);
        entityManager.persist(categoria);
        entityManager.persist(conta);
        Transacao salva = transacaoRepository.save(transacao);

        Optional<Transacao> encontrada = transacaoRepository.findById(salva.getId());
        assertTrue(encontrada.isPresent());
        assertEquals("Compra", encontrada.get().getDescricao());
        assertEquals(50.0, encontrada.get().getValor());
    }

    @Test
    @DisplayName("Deve recuperar transação por ID")
    void deveRecuperarTransacaoPorId() {
        Usuario usuario = novoUsuario("Maria", "maria@test.com");
        Categoria categoria = novaCategoria("Lazer", Tipo.DESPESA, usuario);
        Conta conta = novaConta("Poupança", Tipos.POUPANCA, 1000.0, usuario);

        Transacao transacao = new Transacao();
        transacao.setDescricao("Cinema");
        transacao.setValor(30.0);
        transacao.setTipo(com.finanCerto.finanCertoBack.transacao.Tipos.DESPESA);
        transacao.setDate(LocalDateTime.now());
        transacao.setUsuario(usuario);
        transacao.setCategoria(categoria);
        transacao.setConta(conta);

        entityManager.persist(usuario);
        entityManager.persist(categoria);
        entityManager.persist(conta);
        Transacao salva = transacaoRepository.save(transacao);
        entityManager.flush();

        Optional<Transacao> recuperada = transacaoRepository.findById(salva.getId());

        assertTrue(recuperada.isPresent());
        assertEquals("Cinema", recuperada.get().getDescricao());
        assertEquals(30.0, recuperada.get().getValor());
        assertEquals(com.finanCerto.finanCertoBack.transacao.Tipos.DESPESA, recuperada.get().getTipo());
    }

    @Test
    @DisplayName("Deve salvar transação com tipo RECEITA")
    void deveSalvarTransacaoComTipoRECEITA() {
        Usuario usuario = novoUsuario("Pedro", "pedro@test.com");
        Categoria categoria = novaCategoria("Salário", Tipo.RECEITA, usuario);
        Conta conta = novaConta("Conta Salário", Tipos.CORRENTE, 0.0, usuario);

        Transacao transacao = new Transacao();
        transacao.setDescricao("Salário mensal");
        transacao.setValor(3000.0);
        transacao.setTipo(com.finanCerto.finanCertoBack.transacao.Tipos.RECEITA);
        transacao.setDate(LocalDateTime.now());
        transacao.setUsuario(usuario);
        transacao.setCategoria(categoria);
        transacao.setConta(conta);

        entityManager.persist(usuario);
        entityManager.persist(categoria);
        entityManager.persist(conta);
        Transacao salva = transacaoRepository.save(transacao);
        entityManager.flush();

        Optional<Transacao> recuperada = transacaoRepository.findById(salva.getId());

        assertTrue(recuperada.isPresent());
        assertEquals(com.finanCerto.finanCertoBack.transacao.Tipos.RECEITA, recuperada.get().getTipo());
        assertEquals(3000.0, recuperada.get().getValor());
    }

    @Test
    @DisplayName("Deve atualizar transação existente")
    void deveAtualizarTransacaoExistente() {
        Usuario usuario = novoUsuario("Carlos", "carlos@test.com");
        Categoria categoria = novaCategoria("Alimentação", Tipo.DESPESA, usuario);
        Conta conta = novaConta("Débito", Tipos.CORRENTE, 500.0, usuario);

        Transacao transacao = new Transacao();
        transacao.setDescricao("Compra original");
        transacao.setValor(50.0);
        transacao.setTipo(com.finanCerto.finanCertoBack.transacao.Tipos.DESPESA);
        transacao.setDate(LocalDateTime.now());
        transacao.setUsuario(usuario);
        transacao.setCategoria(categoria);
        transacao.setConta(conta);

        entityManager.persist(usuario);
        entityManager.persist(categoria);
        entityManager.persist(conta);
        Transacao salva = transacaoRepository.save(transacao);
        entityManager.flush();

        salva.setDescricao("Compra atualizada");
        salva.setValor(75.0);
        Transacao atualizada = transacaoRepository.save(salva);
        entityManager.flush();

        Optional<Transacao> recuperada = transacaoRepository.findById(atualizada.getId());

        assertTrue(recuperada.isPresent());
        assertEquals("Compra atualizada", recuperada.get().getDescricao());
        assertEquals(75.0, recuperada.get().getValor());
    }

    @Test
    @DisplayName("Deve deletar transação existente")
    void deveDeletarTransacaoExistente() {
        Usuario usuario = novoUsuario("Lucia", "lucia@test.com");
        Categoria categoria = novaCategoria("Saúde", Tipo.DESPESA, usuario);
        Conta conta = novaConta("Crédito", Tipos.CARTAO, 5000.0, usuario);

        Transacao transacao = new Transacao();
        transacao.setDescricao("Farmácia");
        transacao.setValor(120.0);
        transacao.setTipo(com.finanCerto.finanCertoBack.transacao.Tipos.DESPESA);
        transacao.setDate(LocalDateTime.now());
        transacao.setUsuario(usuario);
        transacao.setCategoria(categoria);
        transacao.setConta(conta);

        entityManager.persist(usuario);
        entityManager.persist(categoria);
        entityManager.persist(conta);
        Transacao salva = transacaoRepository.save(transacao);
        entityManager.flush();

        transacaoRepository.deleteById(salva.getId());
        entityManager.flush();

        Optional<Transacao> recuperada = transacaoRepository.findById(salva.getId());

        assertFalse(recuperada.isPresent());
    }

    @Test
    @DisplayName("Deve contar total de transações")
    void deveContarTotalTransacoes() {
        Usuario usuario = novoUsuario("Beatriz", "beatriz@test.com");
        Categoria categoria = novaCategoria("Diverso", Tipo.DESPESA, usuario);
        Conta conta = novaConta("Principal", Tipos.CORRENTE, 1000.0, usuario);

        Transacao transacao1 = criarTransacao("Transação 1", 100.0, usuario, categoria, conta);
        Transacao transacao2 = criarTransacao("Transação 2", 200.0, usuario, categoria, conta);
        Transacao transacao3 = criarTransacao("Transação 3", 150.0, usuario, categoria, conta);

        entityManager.persist(usuario);
        entityManager.persist(categoria);
        entityManager.persist(conta);
        entityManager.persist(transacao1);
        entityManager.persist(transacao2);
        entityManager.persist(transacao3);
        entityManager.flush();

        long total = transacaoRepository.count();

        assertTrue(total >= 3);
    }

    @Test
    @DisplayName("Deve recuperar todas as transações")
    void deveRecuperarTodasTransacoes() {
        Usuario usuario = novoUsuario("Felipe", "felipe@test.com");
        Categoria categoria = novaCategoria("Diversos", Tipo.DESPESA, usuario);
        Conta conta = novaConta("Geral", Tipos.CORRENTE, 2000.0, usuario);

        Transacao transacao1 = criarTransacao("Transação A", 50.0, usuario, categoria, conta);
        Transacao transacao2 = criarTransacao("Transação B", 100.0, usuario, categoria, conta);

        entityManager.persist(usuario);
        entityManager.persist(categoria);
        entityManager.persist(conta);
        entityManager.persist(transacao1);
        entityManager.persist(transacao2);
        entityManager.flush();

        var todas = transacaoRepository.findAll();

        assertTrue(todas.size() >= 2);
    }

    @Test
    @DisplayName("Deve salvar transação sem descrição")
    void deveSalvarTransacaoSemDescricao() {
        Usuario usuario = novoUsuario("Gabriel", "gabriel@test.com");
        Categoria categoria = novaCategoria("Transporte", Tipo.DESPESA, usuario);
        Conta conta = novaConta("Transporte", Tipos.CORRENTE, 500.0, usuario);

        Transacao transacao = new Transacao();
        transacao.setValor(10.0);
        transacao.setTipo(com.finanCerto.finanCertoBack.transacao.Tipos.DESPESA);
        transacao.setDate(LocalDateTime.now());
        transacao.setUsuario(usuario);
        transacao.setCategoria(categoria);
        transacao.setConta(conta);

        entityManager.persist(usuario);
        entityManager.persist(categoria);
        entityManager.persist(conta);
        Transacao salva = transacaoRepository.save(transacao);
        entityManager.flush();

        Optional<Transacao> recuperada = transacaoRepository.findById(salva.getId());

        assertTrue(recuperada.isPresent());
        assertEquals(10.0, recuperada.get().getValor());
    }

    private Usuario novoUsuario(String nome, String email) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha("pwd");
        return usuario;
    }

    private Categoria novaCategoria(String nome, Tipo tipo, Usuario usuario) {
        Categoria categoria = new Categoria();
        categoria.setNome(nome);
        categoria.setTipo(tipo);
        categoria.setUsuario(usuario);
        return categoria;
    }

    private Conta novaConta(String nome, Tipos tipo, double saldoInicial, Usuario usuario) {
        Conta conta = new Conta();
        conta.setNome(nome);
        conta.setTipo(tipo);
        conta.setSaldoInicial(saldoInicial);
        conta.setUsuario(usuario);
        return conta;
    }

    private Transacao criarTransacao(String descricao, double valor, Usuario usuario, Categoria categoria, Conta conta) {
        Transacao transacao = new Transacao();
        transacao.setDescricao(descricao);
        transacao.setValor(valor);
        transacao.setTipo(com.finanCerto.finanCertoBack.transacao.Tipos.DESPESA);
        transacao.setDate(LocalDateTime.now());
        transacao.setUsuario(usuario);
        transacao.setCategoria(categoria);
        transacao.setConta(conta);
        return transacao;
    }
}
