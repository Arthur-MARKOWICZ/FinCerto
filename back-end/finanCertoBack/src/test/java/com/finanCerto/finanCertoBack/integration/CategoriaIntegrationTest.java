package com.finanCerto.finanCertoBack.integration;

import com.finanCerto.finanCertoBack.categoria.*;
import com.finanCerto.finanCertoBack.usuario.Usuario;
import com.finanCerto.finanCertoBack.usuario.UsuarioRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CategoriaIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario usuarioTest;

    @BeforeEach
    void setUp() {
        categoriaRepository.deleteAll();
        usuarioRepository.deleteAll();
        
        usuarioTest = new Usuario();
        usuarioTest.setNome("Test User");
        usuarioTest.setEmail("test@example.com");
        usuarioTest.setSenha("password123");
        usuarioTest = usuarioRepository.save(usuarioTest);
    }

    @Test
    @Order(1)
    @DisplayName("Deve criar e persistir uma categoria com sucesso")
    @Transactional
    void testCriarCategoria_Sucesso() {
        Categoria categoria = new Categoria();
        categoria.setNome("Alimentação");
        categoria.setTipo(Tipo.DESPESA);
        categoria.setUsuario(usuarioTest);

        Categoria salva = categoriaRepository.save(categoria);

        assertNotNull(salva.getId());
        assertEquals("Alimentação", salva.getNome());
        assertEquals(Tipo.DESPESA, salva.getTipo());
        assertEquals(usuarioTest.getId(), salva.getUsuario().getId());
    }

    @Test
    @Order(2)
    @DisplayName("Deve encontrar categoria por ID")
    @Transactional
    void testBuscarCategoriaPorId_Sucesso() {
        Categoria categoria = new Categoria();
        categoria.setNome("Salário");
        categoria.setTipo(Tipo.RECEITA);
        categoria.setUsuario(usuarioTest);
        Categoria salva = categoriaRepository.save(categoria);

        Optional<Categoria> encontrada = categoriaRepository.findById(salva.getId());

        assertTrue(encontrada.isPresent());
        assertEquals("Salário", encontrada.get().getNome());
        assertEquals(Tipo.RECEITA, encontrada.get().getTipo());
    }

    @Test
    @Order(3)
    @DisplayName("Deve listar categorias por usuário")
    @Transactional
    void testListarCategoriasPorUsuario_Sucesso() {
        Categoria cat1 = new Categoria();
        cat1.setNome("Alimentação");
        cat1.setTipo(Tipo.DESPESA);
        cat1.setUsuario(usuarioTest);
        
        Categoria cat2 = new Categoria();
        cat2.setNome("Transporte");
        cat2.setTipo(Tipo.DESPESA);
        cat2.setUsuario(usuarioTest);
        
        Categoria cat3 = new Categoria();
        cat3.setNome("Salário");
        cat3.setTipo(Tipo.RECEITA);
        cat3.setUsuario(usuarioTest);
        
        categoriaRepository.save(cat1);
        categoriaRepository.save(cat2);
        categoriaRepository.save(cat3);

        List<Categoria> categorias = categoriaRepository.findByUsuarioId(usuarioTest.getId());

        assertEquals(3, categorias.size());
        assertTrue(categorias.stream().anyMatch(c -> "Alimentação".equals(c.getNome())));
        assertTrue(categorias.stream().anyMatch(c -> "Transporte".equals(c.getNome())));
        assertTrue(categorias.stream().anyMatch(c -> "Salário".equals(c.getNome())));
    }

    @Test
    @Order(4)
    @DisplayName("Deve verificar existência de categoria por usuário e nome")
    @Transactional
    void testVerificarExistenciaCategoria_Sucesso() {
        Categoria categoria = new Categoria();
        categoria.setNome("Alimentação");
        categoria.setTipo(Tipo.DESPESA);
        categoria.setUsuario(usuarioTest);
        categoriaRepository.save(categoria);

        assertTrue(categoriaRepository.existsByUsuarioIdAndNome(usuarioTest.getId(), "Alimentação"));
        assertFalse(categoriaRepository.existsByUsuarioIdAndNome(usuarioTest.getId(), "Transporte"));
    }

    @Test
    @Order(5)
    @DisplayName("Deve falhar ao criar categoria com nome duplicado para mesmo usuário")
    @Transactional
    void testCriarCategoria_NomeDuplicado_Falha() {
        Categoria cat1 = new Categoria();
        cat1.setNome("Alimentação");
        cat1.setTipo(Tipo.DESPESA);
        cat1.setUsuario(usuarioTest);
        categoriaRepository.save(cat1);

        Categoria cat2 = new Categoria();
        cat2.setNome("Alimentação");
        cat2.setTipo(Tipo.DESPESA);
        cat2.setUsuario(usuarioTest);

        assertThrows(Exception.class, () -> categoriaRepository.save(cat2));
    }

    @Test
    @Order(6)
    @DisplayName("Deve permitir categoria com mesmo nome para usuários diferentes")
    @Transactional
    void testCriarCategoria_MesmoNomeUsuariosDiferentes_Sucesso() {
        Usuario usuario2 = new Usuario();
        usuario2.setNome("Another User");
        usuario2.setEmail("another@example.com");
        usuario2.setSenha("password123");
        usuario2 = usuarioRepository.save(usuario2);

        Categoria cat1 = new Categoria();
        cat1.setNome("Alimentação");
        cat1.setTipo(Tipo.DESPESA);
        cat1.setUsuario(usuarioTest);
        
        Categoria cat2 = new Categoria();
        cat2.setNome("Alimentação");
        cat2.setTipo(Tipo.DESPESA);
        cat2.setUsuario(usuario2);
        
        categoriaRepository.save(cat1);

        Categoria salva2 = categoriaRepository.save(cat2);

        assertNotNull(salva2.getId());
        assertEquals(usuario2.getId(), salva2.getUsuario().getId());
    }

    @Test
    @Order(7)
    @DisplayName("Deve falhar ao criar categoria com nome nulo")
    @Transactional
    void testCriarCategoria_NomeNulo_Falha() {
        Categoria categoria = new Categoria();
        categoria.setTipo(Tipo.DESPESA);
        categoria.setUsuario(usuarioTest);

        assertThrows(Exception.class, () -> categoriaRepository.save(categoria));
    }

    @Test
    @Order(8)
    @DisplayName("Deve falhar ao criar categoria com tipo nulo")
    @Transactional
    void testCriarCategoria_TipoNulo_Falha() {
        Categoria categoria = new Categoria();
        categoria.setNome("Test Categoria");
        categoria.setUsuario(usuarioTest);

        assertThrows(Exception.class, () -> categoriaRepository.save(categoria));
    }

    @Test
    @Order(9)
    @DisplayName("Deve falhar ao criar categoria sem usuário")
    @Transactional
    void testCriarCategoria_SemUsuario_Falha() {
        Categoria categoria = new Categoria();
        categoria.setNome("Test Categoria");
        categoria.setTipo(Tipo.DESPESA);

        assertThrows(Exception.class, () -> categoriaRepository.save(categoria));
    }

    @Test
    @Order(10)
    @DisplayName("Deve atualizar categoria com sucesso")
    @Transactional
    void testAtualizarCategoria_Sucesso() {
        Categoria categoria = new Categoria();
        categoria.setNome("Alimentação");
        categoria.setTipo(Tipo.DESPESA);
        categoria.setUsuario(usuarioTest);
        Categoria salva = categoriaRepository.save(categoria);

        salva.setNome("Restaurante");
        salva.setTipo(Tipo.RECEITA);
        Categoria atualizada = categoriaRepository.save(salva);

        assertEquals("Restaurante", atualizada.getNome());
        assertEquals(Tipo.RECEITA, atualizada.getTipo());
        assertEquals(usuarioTest.getId(), atualizada.getUsuario().getId());
    }

    @Test
    @Order(11)
    @DisplayName("Deve deletar categoria com sucesso")
    @Transactional
    void testDeletarCategoria_Sucesso() {
        Categoria categoria = new Categoria();
        categoria.setNome("Alimentação");
        categoria.setTipo(Tipo.DESPESA);
        categoria.setUsuario(usuarioTest);
        Categoria salva = categoriaRepository.save(categoria);
        Long id = salva.getId();

        categoriaRepository.delete(salva);

        Optional<Categoria> encontrada = categoriaRepository.findById(id);
        assertFalse(encontrada.isPresent());
    }

    @Test
    @Order(12)
    @DisplayName("Deve validar relacionamento com usuário")
    @Transactional
    void testRelacionamentoUsuario_Sucesso() {
        Categoria categoria = new Categoria();
        categoria.setNome("Alimentação");
        categoria.setTipo(Tipo.DESPESA);
        categoria.setUsuario(usuarioTest);
        Categoria salva = categoriaRepository.save(categoria);

        Optional<Categoria> encontrada = categoriaRepository.findById(salva.getId());

        assertTrue(encontrada.isPresent());
        assertNotNull(encontrada.get().getUsuario());
        assertEquals(usuarioTest.getId(), encontrada.get().getUsuario().getId());
        assertEquals(usuarioTest.getNome(), encontrada.get().getUsuario().getNome());
        assertEquals(usuarioTest.getEmail(), encontrada.get().getUsuario().getEmail());
    }

    @Test
    @Order(13)
    @DisplayName("Deve validar rollback em caso de falha")
    @Transactional
    void testRollbackEmFalha_Sucesso() {
        Categoria cat1 = new Categoria();
        cat1.setNome("Alimentação");
        cat1.setTipo(Tipo.DESPESA);
        cat1.setUsuario(usuarioTest);
        Categoria salva1 = categoriaRepository.save(cat1);
        assertNotNull(salva1.getId());

        assertThrows(RuntimeException.class, () -> {
            Categoria cat2 = new Categoria();
            cat2.setNome("Alimentação");
            cat2.setTipo(Tipo.DESPESA);
            cat2.setUsuario(usuarioTest);
            categoriaRepository.save(cat2);
        });

        assertEquals(1, categoriaRepository.count());
        assertTrue(categoriaRepository.existsByUsuarioIdAndNome(usuarioTest.getId(), "Alimentação"));
    }
}
