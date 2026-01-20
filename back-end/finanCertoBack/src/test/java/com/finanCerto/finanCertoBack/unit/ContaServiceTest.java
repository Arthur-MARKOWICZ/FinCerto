package com.finanCerto.finanCertoBack.unit;

import com.finanCerto.finanCertoBack.conta.Conta;
import com.finanCerto.finanCertoBack.conta.ContaRepository;
import com.finanCerto.finanCertoBack.conta.ContaService;
import com.finanCerto.finanCertoBack.conta.dtos.ContaCadastroDto;
import com.finanCerto.finanCertoBack.exception.ContaComOMesmoNome;
import com.finanCerto.finanCertoBack.exception.ContaNaoEncontrada;
import com.finanCerto.finanCertoBack.security.TokenService;
import com.finanCerto.finanCertoBack.usuario.Usuario;
import com.finanCerto.finanCertoBack.usuario.UsuarioService;
import com.finanCerto.finanCertoBack.usuario.dtos.UsuarioCadastroDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static com.finanCerto.finanCertoBack.conta.Tipos.CORRENTE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ContaServiceTest {
    @InjectMocks
    private ContaService contaService;
    @Mock
    private UsuarioService usuarioService;
    @Mock
    private ContaRepository contaRepository;
    @Mock
    private TokenService tokenService;
    private Usuario usuarioSetup;
    private Conta contaSetup;

    @BeforeEach
    void setup() {

        UsuarioCadastroDto usuarioCadastroDto =
                new UsuarioCadastroDto("setup","setup@test.com","test");

        usuarioSetup = new Usuario(usuarioCadastroDto);
        usuarioSetup.setId(1L);

        ContaCadastroDto contaCadastroDto =
                new ContaCadastroDto("setup", CORRENTE, 100.00, "ftestToken");

        contaSetup = new Conta(contaCadastroDto, usuarioSetup);




    }
    @Test
    @DisplayName("Deve criar um conta")
    void DeveCriarConta(){
        when(tokenService.obterUsuario("ftestToken"))
                .thenReturn(usuarioSetup);
        when(contaRepository.save(any(Conta.class)))
                .thenReturn(contaSetup);
        ContaCadastroDto contaCadastroDto =
                new ContaCadastroDto("setup", CORRENTE, 100.00, "ftestToken");
        Conta contaCadastrada = contaService.cadastro(contaCadastroDto);
        assertEquals("setup" , contaCadastrada.getNome());

    }
    @Test
    @DisplayName("Deve impedir de criar conta com o mesmo nome")
    void ImpedirContaComMesmoNome(){
        when(tokenService.obterUsuario("ftestToken"))
                .thenReturn(usuarioSetup);
        when(contaRepository.save(any(Conta.class)))
                .thenReturn(contaSetup);
        UsuarioCadastroDto usuarioCadastroDto = new UsuarioCadastroDto("test","test@test.com",
                "test");
        Usuario usuario = new Usuario(usuarioCadastroDto);
        ContaCadastroDto contaCadastroDto = new ContaCadastroDto("test",CORRENTE,100.00, "ftestToken");
        Conta contaCadastrada = contaService.cadastro(contaCadastroDto);
        ContaCadastroDto contaCadastroDto2 = new ContaCadastroDto("test",CORRENTE,10034.00 ,"ftestToken");
        when(contaRepository.existsByUsuarioIdAndNome(1l, "test")).thenReturn(true);
        assertThrows(ContaComOMesmoNome.class, () -> {
            contaService.cadastro(contaCadastroDto2);
        });
    }
    @Test
    @DisplayName("Deve obter conta por nome")
    void obterContaPeloNome(){
        when(tokenService.obterUsuario("ftestToken"))
                .thenReturn(usuarioSetup);
        when(contaRepository.findByNameAndUsuarioID(
                usuarioSetup.getId(), "setup"))
                .thenReturn(Optional.of(contaSetup));

        Conta conta = contaService.obterContaPorNome("setup", "ftestToken");

        assertEquals("setup", conta.getNome());
    }
    @Test
    @DisplayName("Deve pegar todas as contas do usuário pelo token")
    void devePegarTodasContasPorUsuario() {

        Pageable pageable = PageRequest.of(0, 10);
        Page<Conta> page = new PageImpl<>(List.of(contaSetup));

        when(tokenService.obterUsuario("ftestToken"))
                .thenReturn(usuarioSetup);

        when(contaRepository.findAllByUsuarioId(usuarioSetup.getId(), pageable))
                .thenReturn(page);

        Page<Conta> resultado =
                contaService.pegarTodosPorUsuarioId("ftestToken", pageable);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("setup", resultado.getContent().get(0).getNome());
    }
    @Test
    @DisplayName("Deve atualizar os dados da conta")
    void deveAtualizarConta() {
        ContaCadastroDto dto =
                new ContaCadastroDto("novaConta", CORRENTE, 500.00, "ftestToken");

        when(contaRepository.findById(1L))
                .thenReturn(Optional.of(contaSetup));

        Conta contaAtualizada = contaService.atualizarConta(1L, dto);

        assertEquals("novaConta", contaAtualizada.getNome());
        assertEquals(500.00, contaAtualizada.getSaldoInicial());
    }
    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar conta inexistente")
    void deveLancarExcecaoAoAtualizarContaInexistente() {
        ContaCadastroDto dto =
                new ContaCadastroDto("novaConta", CORRENTE, 500.00, "ftestToken");

        when(contaRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(ContaNaoEncontrada.class, () ->
                contaService.atualizarConta(99L, dto)
        );
    }
    @Test
    @DisplayName("Deve retornar o saldo da conta")
    void deveRetornarSaldoDaConta() {
        when(contaRepository.findById(1L))
                .thenReturn(Optional.of(contaSetup));

        double saldo = contaService.pegarSaldoConta(1L);

        assertEquals(100.00, saldo);
    }
    @Test
    @DisplayName("Deve lançar exceção ao tentar pegar saldo de conta inexistente")
    void deveLancarExcecaoAoPegarSaldoContaInexistente() {
        when(contaRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(ContaNaoEncontrada.class, () ->
                contaService.pegarSaldoConta(99L)
        );
    }
    @Test
    @DisplayName("Deve adicionar saldo na conta")
    void deveAdicionarSaldo() {
        contaService.AdicionarSaldo(50.00, contaSetup);

        assertEquals(150.00, contaSetup.getSaldoInicial());
    }
    @Test
    @DisplayName("Deve diminuir saldo da conta")
    void deveDiminuirSaldo() {
        contaService.DiminuirSaldo(30.00, contaSetup);

        assertEquals(70.00, contaSetup.getSaldoInicial());
    }


}
