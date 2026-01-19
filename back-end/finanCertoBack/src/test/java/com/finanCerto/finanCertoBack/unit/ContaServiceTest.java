package com.finanCerto.finanCertoBack.unit;

import com.finanCerto.finanCertoBack.conta.Conta;
import com.finanCerto.finanCertoBack.conta.ContaRepository;
import com.finanCerto.finanCertoBack.conta.ContaService;
import com.finanCerto.finanCertoBack.conta.dtos.ContaCadastroDto;
import com.finanCerto.finanCertoBack.exception.ContaComOMesmoNome;
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
    @BeforeEach
    void setup(){

        UsuarioCadastroDto usuarioCadastroDto = new UsuarioCadastroDto("setup","setup@test.com",
                "test");
        Usuario usuario = new Usuario(usuarioCadastroDto);
        ContaCadastroDto contaCadastroDto = new ContaCadastroDto("setup",CORRENTE,100.00, "ftestToken");
        Conta conta = new Conta(contaCadastroDto,usuario);
        when(tokenService.obterUsuario("ftestToken")).thenReturn(usuario);
        when(contaRepository.save(any(Conta.class))).thenReturn(conta);
        Conta contaCadastrada = contaService.cadastro(contaCadastroDto);
    }
    @Test
    @DisplayName("Deve criar um conta")
    void DeveCriarConta(){

        UsuarioCadastroDto usuarioCadastroDto = new UsuarioCadastroDto("test","test@test.com",
                "test");
        Usuario usuario = new Usuario(usuarioCadastroDto);
        ContaCadastroDto contaCadastroDto = new ContaCadastroDto("test",CORRENTE,100.00, "ftestToken");
        Conta conta = new Conta(contaCadastroDto,usuario);
        when(tokenService.obterUsuario("ftestToken")).thenReturn(usuario);
        when(contaRepository.save(any(Conta.class))).thenReturn(conta);
        Conta contaCadastrada = contaService.cadastro(contaCadastroDto);
        assertEquals("test" , contaCadastrada.getNome());

    }
    @Test
    @DisplayName("Deve impedir de criar conta com o mesmo nome")
    void ImpedirContaComMesmoNome(){
        UsuarioCadastroDto usuarioCadastroDto = new UsuarioCadastroDto("test","test@test.com",
                "test");
        Usuario usuario = new Usuario(usuarioCadastroDto);
        ContaCadastroDto contaCadastroDto = new ContaCadastroDto("test",CORRENTE,100.00, "ftestToken");
        Conta conta = new Conta(contaCadastroDto,usuario);
        when(tokenService.obterUsuario("ftestToken")).thenReturn(usuario);
        when(contaRepository.save(any(Conta.class))).thenReturn(conta);
        Conta contaCadastrada = contaService.cadastro(contaCadastroDto);
        ContaCadastroDto contaCadastroDto2 = new ContaCadastroDto("test",CORRENTE,10034.00 ,"ftestToken");
        when(contaRepository.existsByUsuarioIdAndNome(usuario.getId(), "test")).thenReturn(true);
        assertThrows(ContaComOMesmoNome.class, () -> {
            contaService.cadastro(contaCadastroDto2);
        });
    }
    @Test
    @DisplayName("Deve obter conta por nome")
    void obterContaPeloNome(){
        Conta conta = contaService.obterContaPorNome("setup","ftestToken");
    }
}
