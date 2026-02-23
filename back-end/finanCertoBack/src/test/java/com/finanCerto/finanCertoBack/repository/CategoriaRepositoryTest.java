package com.finanCerto.finanCertoBack.repository;

import com.finanCerto.finanCertoBack.categoria.Categoria;
import com.finanCerto.finanCertoBack.categoria.CategoriaRepository;
import com.finanCerto.finanCertoBack.categoria.Tipo;
import com.finanCerto.finanCertoBack.usuario.Usuario;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CategoriaRepositoryTest {

    @Autowired
    private CategoriaRepository categoriaRepository;

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
                entityManager.createNativeQuery("DELETE FROM tb_usuario").executeUpdate();
                entityManager.flush();
            } catch (Exception ex) {
                
            }
        }
    }    @Test
    @DisplayName("Deve verificar existência por usuário/nome e buscar por nome")
    void deveConsultarCategoria() {
        Usuario usuario = novoUsuario("Ana", "ana@test.com");
        Categoria categoria = novaCategoria("Lazer", Tipo.DESPESA, usuario);

        entityManager.persist(usuario);
        entityManager.persist(categoria);
        entityManager.flush();

        assertTrue(categoriaRepository.existsByUsuarioIdAndNome(usuario.getId(), "Lazer"));
        assertFalse(categoriaRepository.existsByUsuarioIdAndNome(usuario.getId(), "NaoExiste"));

        Optional<Categoria> encontrada = categoriaRepository.findByNome("Lazer");
        assertTrue(encontrada.isPresent());
        assertEquals("Lazer", encontrada.get().getNome());
    }

    @Test
    @DisplayName("Deve salvar e recuperar categoria")
    void deveSalvarERecuperarCategoria() {
        Usuario usuario = novoUsuario("Paula", "paula@test.com");
        Categoria categoria = novaCategoria("Alimentação", Tipo.DESPESA, usuario);

        entityManager.persist(usuario);
        Categoria salva = categoriaRepository.save(categoria);
        entityManager.flush();

        Optional<Categoria> recuperada = categoriaRepository.findById(salva.getId());

        assertTrue(recuperada.isPresent());
        assertEquals("Alimentação", recuperada.get().getNome());
        assertEquals(Tipo.DESPESA, recuperada.get().getTipo());
    }

    @Test
    @DisplayName("Deve encontrar categoria por nome")
    void deveEncontrarCategoriaPorNome() {
        Usuario usuario = novoUsuario("Marcos", "marcos@test.com");
        Categoria categoria = novaCategoria("Transporte", Tipo.DESPESA, usuario);

        entityManager.persist(usuario);
        entityManager.persist(categoria);
        entityManager.flush();

        Optional<Categoria> encontrada = categoriaRepository.findByNome("Transporte");

        assertTrue(encontrada.isPresent());
        assertEquals("Transporte", encontrada.get().getNome());
        assertEquals(Tipo.DESPESA, encontrada.get().getTipo());
    }

    @Test
    @DisplayName("Deve retornar vazio ao buscar categoria inexistente")
    void deveRetornarVazioAoBuscarCategoriaInexistente() {
        Optional<Categoria> encontrada = categoriaRepository.findByNome("CategoriaInexistente");

        assertFalse(encontrada.isPresent());
    }

    @Test
    @DisplayName("Deve atualizar categoria existente")
    void deveAtualizarCategoriaExistente() {
        Usuario usuario = novoUsuario("Fatima", "fatima@test.com");
        Categoria categoria = novaCategoria("Saúde", Tipo.DESPESA, usuario);

        entityManager.persist(usuario);
        Categoria salva = categoriaRepository.save(categoria);
        entityManager.flush();

        salva.setNome("Saúde e Bem-estar");
        salva.setTipo(Tipo.RECEITA);
        Categoria atualizada = categoriaRepository.save(salva);
        entityManager.flush();

        Optional<Categoria> recuperada = categoriaRepository.findById(atualizada.getId());

        assertTrue(recuperada.isPresent());
        assertEquals("Saúde e Bem-estar", recuperada.get().getNome());
        assertEquals(Tipo.RECEITA, recuperada.get().getTipo());
    }

    @Test
    @DisplayName("Deve deletar categoria existente")
    void deveDeletarCategoriaExistente() {
        Usuario usuario = novoUsuario("Ester", "ester@test.com");
        Categoria categoria = novaCategoria("Educação", Tipo.DESPESA, usuario);

        entityManager.persist(usuario);
        Categoria salva = categoriaRepository.save(categoria);
        entityManager.flush();

        categoriaRepository.deleteById(salva.getId());
        entityManager.flush();

        Optional<Categoria> recuperada = categoriaRepository.findById(salva.getId());

        assertFalse(recuperada.isPresent());
    }

    @Test
    @DisplayName("Deve salvar categoria com tipo RECEITA")
    void deveSalvarCategoriaComTipoRECEITA() {
        Usuario usuario = novoUsuario("Gustavo", "gustavo@test.com");
        Categoria categoria = novaCategoria("Salário", Tipo.RECEITA, usuario);

        entityManager.persist(usuario);
        Categoria salva = categoriaRepository.save(categoria);
        entityManager.flush();

        Optional<Categoria> recuperada = categoriaRepository.findById(salva.getId());

        assertTrue(recuperada.isPresent());
        assertEquals(Tipo.RECEITA, recuperada.get().getTipo());
    }

    @Test
    @DisplayName("Deve contar total de categorias")
    void deveContarTotalCategorias() {
        Usuario usuario = novoUsuario("Helena", "helena@test.com");
        Categoria categoria1 = novaCategoria("Categoria 1", Tipo.DESPESA, usuario);
        Categoria categoria2 = novaCategoria("Categoria 2", Tipo.RECEITA, usuario);
        Categoria categoria3 = novaCategoria("Categoria 3", Tipo.DESPESA, usuario);

        entityManager.persist(usuario);
        entityManager.persist(categoria1);
        entityManager.persist(categoria2);
        entityManager.persist(categoria3);
        entityManager.flush();

        long total = categoriaRepository.count();

        assertTrue(total >= 3);
    }

    @Test
    @DisplayName("Deve recuperar todas as categorias")
    void deveRecuperarTodasCategorias() {
        Usuario usuario = novoUsuario("Igor", "igor@test.com");
        Categoria categoria1 = novaCategoria("Categoria A", Tipo.DESPESA, usuario);
        Categoria categoria2 = novaCategoria("Categoria B", Tipo.RECEITA, usuario);

        entityManager.persist(usuario);
        entityManager.persist(categoria1);
        entityManager.persist(categoria2);
        entityManager.flush();

        var todas = categoriaRepository.findAll();

        assertTrue(todas.size() >= 2);
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
}
