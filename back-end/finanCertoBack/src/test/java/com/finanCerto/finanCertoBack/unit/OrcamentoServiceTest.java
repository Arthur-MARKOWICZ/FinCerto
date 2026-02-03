package com.finanCerto.finanCertoBack.unit;

import com.finanCerto.finanCertoBack.categoria.Categoria;
import com.finanCerto.finanCertoBack.categoria.CategoriaService;
import com.finanCerto.finanCertoBack.exception.OrcamentoComMesmoNome;
import com.finanCerto.finanCertoBack.exception.OrcamentoNaoEncontrado;
import com.finanCerto.finanCertoBack.orcamento.Orcamento;
import com.finanCerto.finanCertoBack.orcamento.OrcamentoRepository;
import com.finanCerto.finanCertoBack.orcamento.OrcamentoService;
import com.finanCerto.finanCertoBack.orcamento.dto.OrcamentoCadastroDto;
import com.finanCerto.finanCertoBack.security.TokenService;
import com.finanCerto.finanCertoBack.usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrcamentoServiceTest {

    @InjectMocks
    private OrcamentoService orcamentoService;
    @Mock
    private OrcamentoRepository repository;
    @Mock
    private CategoriaService categoriaService;
    @Mock
    private TokenService tokenService;

    private Usuario usuario;
    private Categoria categoria;
    private OrcamentoCadastroDto dto;

    @BeforeEach
    void setup() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Tester");
        usuario.setEmail("tester@mail.com");
        usuario.setSenha("secret");

        categoria = new Categoria();
        categoria.setNome("Alimentacao");

        dto = new OrcamentoCadastroDto(
                500.0,
                100.0,
                "Mensal",
                LocalDate.now().plusDays(10),
                categoria.getNome(),
                "token-123"
        );
    }

    @Test
    @DisplayName("Deve cadastrar orçamento quando não existir outro com mesmo nome")
    void deveCadastrarOrcamento() {
        when(categoriaService.obterCategoriaPorNome(dto.categoriaNome())).thenReturn(categoria);
        when(tokenService.obterUsuario(dto.token())).thenReturn(usuario);
        when(repository.existsByUsuarioIdAndNome(usuario.getId(), dto.nome())).thenReturn(false);
        when(repository.save(any(Orcamento.class))).thenAnswer(inv -> inv.getArgument(0));

        Orcamento criado = orcamentoService.cadastro(dto);

        assertEquals("Mensal", criado.getNome());
        assertEquals(100.0, criado.getValorAtual());
        verify(repository).save(any(Orcamento.class));
    }

    @Test
    @DisplayName("Deve lançar erro ao cadastrar orçamento duplicado para o mesmo usuário")
    void deveImpidirDuplicado() {
        when(categoriaService.obterCategoriaPorNome(dto.categoriaNome())).thenReturn(categoria);
        when(tokenService.obterUsuario(dto.token())).thenReturn(usuario);
        when(repository.existsByUsuarioIdAndNome(usuario.getId(), dto.nome())).thenReturn(true);

        assertThrows(OrcamentoComMesmoNome.class, () -> orcamentoService.cadastro(dto));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve obter orçamento por nome usando token e lançar erro se não encontrado")
    void deveObterPorNomeOuLancar() {
        Orcamento orcamento = new Orcamento(dto, categoria, usuario);
        when(tokenService.obterUsuario("token-123")).thenReturn(usuario);
        when(repository.findByUsuarioIdAndNome(usuario.getId(), "Mensal")).thenReturn(orcamento);

        Orcamento resultado = orcamentoService.obterPorNome("Mensal", "token-123");
        assertEquals("Mensal", resultado.getNome());

        when(repository.findByUsuarioIdAndNome(usuario.getId(), "Mensal")).thenReturn(null);
        assertThrows(OrcamentoNaoEncontrado.class, () -> orcamentoService.obterPorNome("Mensal", "token-123"));
    }

    @Test
    @DisplayName("Deve obter orçamento por categoria ou lançar exceção")
    void deveObterPorCategoriaOuLancar() {
        Orcamento orcamento = new Orcamento(dto, categoria, usuario);
        when(tokenService.obterUsuario("token-123")).thenReturn(usuario);
        when(categoriaService.obterCategoriaPorNome("Alimentacao")).thenReturn(categoria);
        when(repository.findByCategoriaId(categoria.getId(), PageRequest.of(0, 10, Sort.by("id").descending())))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(java.util.List.of(orcamento)));

        var resultado = orcamentoService.obterPorCategoria("Alimentacao", "token-123", 0, 10);
        assertEquals(1, resultado.getContent().size());
        assertEquals(orcamento, resultado.getContent().get(0));
    }

    @Test
    @DisplayName("Deve adicionar valor ao orçamento da categoria")
    void deveAdicionarValor() {
        Orcamento orcamento = new Orcamento(dto, categoria, usuario);
        orcamento.setValorAtual(200.0);
        when(repository.findByCategoriaId(categoria.getId(), PageRequest.of(0, 1)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(java.util.List.of(orcamento)));

        orcamentoService.adicionar(50.0, categoria);

        assertEquals(250.0, orcamento.getValorAtual());
    }
}
