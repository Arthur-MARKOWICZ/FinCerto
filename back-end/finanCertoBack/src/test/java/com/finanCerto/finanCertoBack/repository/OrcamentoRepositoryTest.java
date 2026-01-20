package com.finanCerto.finanCertoBack.repository;

import com.finanCerto.finanCertoBack.categoria.Categoria;
import com.finanCerto.finanCertoBack.categoria.Tipo;
import com.finanCerto.finanCertoBack.orcamento.Orcamento;
import com.finanCerto.finanCertoBack.orcamento.OrcamentoRepository;
import com.finanCerto.finanCertoBack.usuario.Usuario;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class OrcamentoRepositoryTest {

    @Autowired
    private OrcamentoRepository orcamentoRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Deve encontrar orçamento por usuário e nome e por categoria")
    void deveConsultarOrcamento() {
        Usuario usuario = novoUsuario("Maria", "maria@test.com");
        Categoria categoria = novaCategoria("Mercado", Tipo.DESPESA, usuario);
        Orcamento orcamento = novoOrcamento("Mensal", 500.0, 100.0, usuario, categoria);

        entityManager.persist(usuario);
        entityManager.persist(categoria);
        entityManager.persist(orcamento);
        entityManager.flush();

        assertTrue(orcamentoRepository.existsByUsuarioIdAndNome(usuario.getId(), "Mensal"));

        Orcamento porNome = orcamentoRepository.findByUsuarioIdAndNome(usuario.getId(), "Mensal");
        assertNotNull(porNome);
        assertEquals("Mensal", porNome.getNome());

        Orcamento porCategoria = orcamentoRepository.findByCategoria(categoria);
        assertNotNull(porCategoria);
        assertEquals(categoria, porCategoria.getCategoria());
    }

    @Test
    @DisplayName("Deve verificar existência de orçamento por usuário e nome")
    void deveVerificarExistenciaOrcamentoPorUsuarioAndNome() {
        Usuario usuario = novoUsuario("Pedro", "pedro@test.com");
        Categoria categoria = novaCategoria("Alimentação", Tipo.DESPESA, usuario);
        Orcamento orcamento = novoOrcamento("Semana", 200.0, 50.0, usuario, categoria);

        entityManager.persist(usuario);
        entityManager.persist(categoria);
        entityManager.persist(orcamento);
        entityManager.flush();

        assertTrue(orcamentoRepository.existsByUsuarioIdAndNome(usuario.getId(), "Semana"));
        assertFalse(orcamentoRepository.existsByUsuarioIdAndNome(usuario.getId(), "Inexistente"));
        assertFalse(orcamentoRepository.existsByUsuarioIdAndNome(9999L, "Semana"));
    }

    @Test
    @DisplayName("Deve encontrar orçamento por categoria")
    void deveEncontrarOrcamentoPorCategoria() {
        Usuario usuario = novoUsuario("Ana", "ana@test.com");
        Categoria categoria = novaCategoria("Lazer", Tipo.DESPESA, usuario);
        Orcamento orcamento = novoOrcamento("Diversão", 1000.0, 200.0, usuario, categoria);

        entityManager.persist(usuario);
        entityManager.persist(categoria);
        entityManager.persist(orcamento);
        entityManager.flush();

        Orcamento encontrado = orcamentoRepository.findByCategoria(categoria);

        assertNotNull(encontrado);
        assertEquals("Diversão", encontrado.getNome());
        assertEquals(categoria, encontrado.getCategoria());
    }

    @Test
    @DisplayName("Deve salvar e recuperar orçamento")
    void deveSalvarERecuperarOrcamento() {
        Usuario usuario = novoUsuario("Carlos", "carlos@test.com");
        Categoria categoria = novaCategoria("Transporte", Tipo.DESPESA, usuario);
        Orcamento orcamento = novoOrcamento("Mensal", 500.0, 100.0, usuario, categoria);

        entityManager.persist(usuario);
        entityManager.persist(categoria);
        Orcamento salvo = orcamentoRepository.save(orcamento);
        entityManager.flush();

        Optional<Orcamento> recuperado = orcamentoRepository.findById(salvo.getId());

        assertTrue(recuperado.isPresent());
        assertEquals("Mensal", recuperado.get().getNome());
        assertEquals(500.0, recuperado.get().getValorLimite());
        assertEquals(100.0, recuperado.get().getValorAtual());
    }

    @Test
    @DisplayName("Deve atualizar orçamento existente")
    void deveAtualizarOrcamentoExistente() {
        Usuario usuario = novoUsuario("Lucia", "lucia@test.com");
        Categoria categoria = novaCategoria("Saúde", Tipo.DESPESA, usuario);
        Orcamento orcamento = novoOrcamento("Medicamentos", 300.0, 50.0, usuario, categoria);

        entityManager.persist(usuario);
        entityManager.persist(categoria);
        Orcamento salvo = orcamentoRepository.save(orcamento);
        entityManager.flush();

        salvo.setValorAtual(100.0);
        salvo.setValorLimite(400.0);
        Orcamento atualizado = orcamentoRepository.save(salvo);
        entityManager.flush();

        Optional<Orcamento> recuperado = orcamentoRepository.findById(atualizado.getId());

        assertTrue(recuperado.isPresent());
        assertEquals(100.0, recuperado.get().getValorAtual());
        assertEquals(400.0, recuperado.get().getValorLimite());
    }

    @Test
    @DisplayName("Deve deletar orçamento existente")
    void deveDeletarOrcamentoExistente() {
        Usuario usuario = novoUsuario("Felipe", "felipe@test.com");
        Categoria categoria = novaCategoria("Educação", Tipo.DESPESA, usuario);
        Orcamento orcamento = novoOrcamento("Cursos", 500.0, 100.0, usuario, categoria);

        entityManager.persist(usuario);
        entityManager.persist(categoria);
        Orcamento salvo = orcamentoRepository.save(orcamento);
        entityManager.flush();

        orcamentoRepository.deleteById(salvo.getId());
        entityManager.flush();

        Optional<Orcamento> recuperado = orcamentoRepository.findById(salvo.getId());

        assertFalse(recuperado.isPresent());
    }

    @Test
    @DisplayName("Deve contar total de orçamentos")
    void deveContarTotalOrcamentos() {
        Usuario usuario = novoUsuario("Beatriz", "beatriz@test.com");
        Categoria categoria1 = novaCategoria("Categoria 1", Tipo.DESPESA, usuario);
        Categoria categoria2 = novaCategoria("Categoria 2", Tipo.DESPESA, usuario);
        
        Orcamento orcamento1 = novoOrcamento("Orçamento 1", 500.0, 100.0, usuario, categoria1);
        Orcamento orcamento2 = novoOrcamento("Orçamento 2", 1000.0, 200.0, usuario, categoria2);

        entityManager.persist(usuario);
        entityManager.persist(categoria1);
        entityManager.persist(categoria2);
        entityManager.persist(orcamento1);
        entityManager.persist(orcamento2);
        entityManager.flush();

        long total = orcamentoRepository.count();

        assertTrue(total >= 2);
    }

    @Test
    @DisplayName("Deve recuperar todos os orçamentos")
    void deveRecuperarTodosOrcamentos() {
        Usuario usuario = novoUsuario("Gustavo", "gustavo@test.com");
        Categoria categoria1 = novaCategoria("Gastos A", Tipo.DESPESA, usuario);
        Categoria categoria2 = novaCategoria("Gastos B", Tipo.DESPESA, usuario);
        
        Orcamento orcamento1 = novoOrcamento("Orçamento A", 500.0, 100.0, usuario, categoria1);
        Orcamento orcamento2 = novoOrcamento("Orçamento B", 1000.0, 200.0, usuario, categoria2);

        entityManager.persist(usuario);
        entityManager.persist(categoria1);
        entityManager.persist(categoria2);
        entityManager.persist(orcamento1);
        entityManager.persist(orcamento2);
        entityManager.flush();

        var todos = orcamentoRepository.findAll();

        assertTrue(todos.size() >= 2);
    }

    @Test
    @DisplayName("Deve salvar orçamento com prazo")
    void deveSalvarOrcamentoComPrazo() {
        Usuario usuario = novoUsuario("Helena", "helena@test.com");
        Categoria categoria = novaCategoria("Diverso", Tipo.DESPESA, usuario);
        
        Orcamento orcamento = novoOrcamento("Com Prazo", 500.0, 0.0, usuario, categoria);
        orcamento.setPrazo(LocalDate.now().plusDays(30));

        entityManager.persist(usuario);
        entityManager.persist(categoria);
        Orcamento salvo = orcamentoRepository.save(orcamento);
        entityManager.flush();

        Optional<Orcamento> recuperado = orcamentoRepository.findById(salvo.getId());

        assertTrue(recuperado.isPresent());
        assertNotNull(recuperado.get().getPrazo());
        assertEquals(LocalDate.now().plusDays(30), recuperado.get().getPrazo());
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

    private Orcamento novoOrcamento(String nome, double limite, double atual, Usuario usuario, Categoria categoria) {
        Orcamento orcamento = new Orcamento();
        orcamento.setNome(nome);
        orcamento.setValorLimite(limite);
        orcamento.setValorAtual(atual);
        orcamento.setPrazo(LocalDate.now().plusDays(5));
        orcamento.setUsuario(usuario);
        orcamento.setCategoria(categoria);
        return orcamento;
    }
}
