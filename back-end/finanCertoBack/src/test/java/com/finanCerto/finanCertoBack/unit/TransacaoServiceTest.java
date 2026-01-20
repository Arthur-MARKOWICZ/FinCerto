package com.finanCerto.finanCertoBack.unit;

import com.finanCerto.finanCertoBack.categoria.Categoria;
import com.finanCerto.finanCertoBack.categoria.CategoriaService;
import com.finanCerto.finanCertoBack.conta.Conta;
import com.finanCerto.finanCertoBack.conta.ContaService;
import com.finanCerto.finanCertoBack.conta.dtos.ContaCadastroDto;
import com.finanCerto.finanCertoBack.exception.TransacaoNaoEncontrada;
import com.finanCerto.finanCertoBack.orcamento.OrcamentoService;
import com.finanCerto.finanCertoBack.security.TokenService;
import com.finanCerto.finanCertoBack.transacao.Tipos;
import com.finanCerto.finanCertoBack.transacao.Transacao;
import com.finanCerto.finanCertoBack.transacao.TransacaoRepository;
import com.finanCerto.finanCertoBack.transacao.TransacaoService;
import com.finanCerto.finanCertoBack.transacao.dto.TransacaoCadastroDto;
import com.finanCerto.finanCertoBack.usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.finanCerto.finanCertoBack.conta.Tipos.CORRENTE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransacaoServiceTest {

    @InjectMocks
    private TransacaoService transacaoService;
    @Mock
    private TransacaoRepository repository;
    @Mock
    private CategoriaService categoriaService;
    @Mock
    private TokenService tokenService;
    @Mock
    private ContaService contaService;
    @Mock
    private OrcamentoService orcamentoService;

    private Usuario usuario;
    private Categoria categoria;
    private Conta conta;

    @BeforeEach
    void setup() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Tester");
        usuario.setEmail("tester@mail.com");
        usuario.setSenha("secret");

        categoria = new Categoria();
        categoria.setNome("Alimentacao");

        conta = new Conta(new ContaCadastroDto("Carteira", CORRENTE, 300.0, "token"), usuario);
    }

    @Test
    @DisplayName("Deve registrar despesa e diminuir saldo, atualizar orçamento e salvar")
    void deveCadastrarDespesa() {
        TransacaoCadastroDto dto = new TransacaoCadastroDto(
                50.0, LocalDateTime.now(), "Mercado", Tipos.DESPESA, conta.getNome(), categoria.getNome(), "token"
        );

        when(tokenService.obterUsuario(dto.token())).thenReturn(usuario);
        when(contaService.obterContaPorNome(dto.nomeConta(), dto.token())).thenReturn(conta);
        when(categoriaService.obterCategoriaPorNome(dto.nomeCategoria())).thenReturn(categoria);
        when(repository.save(any(Transacao.class))).thenAnswer(inv -> inv.getArgument(0));

        Transacao transacao = transacaoService.cadastro(dto);

        assertEquals(Tipos.DESPESA, transacao.getTipo());
        verify(contaService).DiminuirSaldo(dto.valor(), conta);
        verify(orcamentoService).adicionar(dto.valor(), categoria);
        verify(repository).save(any(Transacao.class));
    }

    @Test
    @DisplayName("Deve registrar receita e aumentar saldo, atualizar orçamento e salvar")
    void deveCadastrarReceita() {
        TransacaoCadastroDto dto = new TransacaoCadastroDto(
                200.0, LocalDateTime.now(), "Salário", Tipos.RECEITA, conta.getNome(), categoria.getNome(), "token"
        );

        when(tokenService.obterUsuario(dto.token())).thenReturn(usuario);
        when(contaService.obterContaPorNome(dto.nomeConta(), dto.token())).thenReturn(conta);
        when(categoriaService.obterCategoriaPorNome(dto.nomeCategoria())).thenReturn(categoria);
        when(repository.save(any(Transacao.class))).thenAnswer(inv -> inv.getArgument(0));

        Transacao transacao = transacaoService.cadastro(dto);

        assertEquals(Tipos.RECEITA, transacao.getTipo());
        verify(contaService).AdicionarSaldo(dto.valor(), conta);
        verify(orcamentoService).adicionar(dto.valor(), categoria);
        verify(repository).save(any(Transacao.class));
    }

    @Test
    @DisplayName("Deve obter transação por id ou lançar exceção quando não existir")
    void deveObterPorIdOuLancar() {
        Transacao transacao = new Transacao();
        transacao.setId(10L);

        when(repository.findById(10L)).thenReturn(Optional.of(transacao));
        when(repository.findById(99L)).thenReturn(Optional.empty());

        Transacao resultado = transacaoService.obterPorId(10L);
        assertEquals(10L, resultado.getId());

        assertThrows(TransacaoNaoEncontrada.class, () -> transacaoService.obterPorId(99L));
    }
}
