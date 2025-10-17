package com.finanCerto.finanCertoBack.unit;

import com.finanCerto.finanCertoBack.exception.UsuarioJaExiste;
import com.finanCerto.finanCertoBack.exception.UsuarioNaoEncontrado;
import com.finanCerto.finanCertoBack.security.TokenService;
import com.finanCerto.finanCertoBack.usuario.Usuario;
import com.finanCerto.finanCertoBack.usuario.UsuarioRepository;
import com.finanCerto.finanCertoBack.usuario.UsuarioService;
import com.finanCerto.finanCertoBack.usuario.dtos.LoginRequestDto;
import com.finanCerto.finanCertoBack.usuario.dtos.LoginResponseDto;
import com.finanCerto.finanCertoBack.usuario.dtos.UsuarioCadastroDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {
    @InjectMocks
    private UsuarioService service;
    @Mock
    private UsuarioRepository repository;
    @Mock
    private BCryptPasswordEncoder encoder;
    @Mock
    private TokenService tokenService;
    @Test
    @DisplayName("deve cadastrar usuario com todos os dados coretos")
    void DeveCadastrarUsuario(){
        UsuarioCadastroDto usuarioCadastroDto = new UsuarioCadastroDto("test","test@test.com",
                "test");
        Usuario usuario = new Usuario(usuarioCadastroDto);
        when(repository.save(any(Usuario.class))).thenReturn(usuario);
        Usuario usuarioCadastrado = service.cadastro(usuarioCadastroDto);
        assertEquals("test", usuarioCadastrado.getNome());
    }
    @Test
    @DisplayName("Deve fazer login com sucesso")
    void deveFazerLoginComSucesso() {
        // Arrange
        String email = "test@test.com";
        String senha = "test";
        String senhaCriptografada = "encodedTest";

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("test");
        usuario.setEmail(email);
        usuario.setSenha(senhaCriptografada);

        when(repository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(encoder.matches(senha, senhaCriptografada)).thenReturn(true);
        when(tokenService.generateToken(usuario)).thenReturn("fake-jwt-token");

        LoginRequestDto requestDto = new LoginRequestDto(email, senha);

        // Act
        LoginResponseDto responseDto = service.login(requestDto);

        // Assert
        assertNotNull(responseDto);
        assertEquals("fake-jwt-token", responseDto.token());
        verify(repository).findByEmail(email);
    }
    @Test
    @DisplayName("Deve impedir cadastro quando o email já estiver em uso")
    void deveImpedirCadastroComEmailExistente() {
      
        UsuarioCadastroDto usuarioCadastroDto = new UsuarioCadastroDto("test", "test@test.com", "test");
        Usuario usuarioExistente = new Usuario(usuarioCadastroDto);
        
        when(repository.existsByEmail(usuarioCadastroDto.email()))
                .thenReturn(true);

        // Act & Assert
        assertThrows(UsuarioJaExiste.class,
                () -> service.cadastro(usuarioCadastroDto));

        verify(repository, never()).save(any(Usuario.class));
    }
    @Test
    @DisplayName("Deve fazer login com a senha errada")
    void deveFazerLoginComSenhaErrada() {
        // Arrange
        String email = "test@test.com";
        String senha = "test";
        String senhaErrada = "test2";
        String senhaCriptografada = "encodedTest";

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("test");
        usuario.setEmail(email);
        usuario.setSenha(senhaCriptografada);

        when(repository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(encoder.matches(senhaErrada  , senhaCriptografada)).thenReturn(false);

        LoginRequestDto requestDto = new LoginRequestDto(email, "test2");

        // Act
        assertThrows(UsuarioNaoEncontrado.class,
                () -> service.login(requestDto));

        // Assert
        verify(tokenService, never()).generateToken(any(Usuario.class));
        verify(repository).findByEmail(email);
    }


}
