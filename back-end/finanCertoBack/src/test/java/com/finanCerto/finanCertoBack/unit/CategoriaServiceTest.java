package com.finanCerto.finanCertoBack.unit;

import com.finanCerto.finanCertoBack.categoria.Categoria;
import com.finanCerto.finanCertoBack.categoria.CategoriaRepository;
import com.finanCerto.finanCertoBack.categoria.CategoriaService;
import com.finanCerto.finanCertoBack.categoria.Tipo;
import com.finanCerto.finanCertoBack.categoria.dto.CategoriaCadastroDto;
import com.finanCerto.finanCertoBack.exception.CategoriaComMesmoNome;
import com.finanCerto.finanCertoBack.exception.CategoriaNaoEncontrada;
import com.finanCerto.finanCertoBack.security.TokenService;
import com.finanCerto.finanCertoBack.usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @InjectMocks
    private CategoriaService categoriaService;
    @Mock
    private CategoriaRepository repository;
    @Mock
    private TokenService tokenService;

    private Usuario usuario;
    private CategoriaCadastroDto dto;

    @BeforeEach
    void setup() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Tester");
        usuario.setEmail("tester@mail.com");
        usuario.setSenha("secret");

        dto = new CategoriaCadastroDto("Lazer", Tipo.DESPESA, "token-123");
    }

    @Test
    @DisplayName("Deve cadastrar categoria quando não existir outra com o mesmo nome")
    void deveCadastrarCategoria() {
        when(tokenService.obterUsuario(dto.token())).thenReturn(usuario);
        when(repository.existsByUsuarioIdAndNome(usuario.getId(), dto.nome())).thenReturn(false);
        when(repository.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));

        Categoria categoria = categoriaService.cadastro(dto);

        assertEquals("Lazer", categoria.getNome());
        assertEquals(usuario, categoria.getUsuario());
        verify(repository).save(any(Categoria.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar categoria duplicada")
    void deveImpedirCategoriaDuplicada() {
        when(tokenService.obterUsuario(dto.token())).thenReturn(usuario);
        when(repository.existsByUsuarioIdAndNome(usuario.getId(), dto.nome())).thenReturn(true);

        assertThrows(CategoriaComMesmoNome.class, () -> categoriaService.cadastro(dto));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve obter categoria por nome ou lançar CategoriaNaoEncontrada")
    void deveObterCategoriaOuLancar() {
        Categoria categoria = new Categoria(dto);
        when(repository.findByNome("Lazer")).thenReturn(Optional.of(categoria));

        Categoria resultado = categoriaService.obterCategoriaPorNome("Lazer");
        assertEquals("Lazer", resultado.getNome());

        when(repository.findByNome("Lazer")).thenReturn(Optional.empty());
        assertThrows(CategoriaNaoEncontrada.class, () -> categoriaService.obterCategoriaPorNome("Lazer"));
    }
}
