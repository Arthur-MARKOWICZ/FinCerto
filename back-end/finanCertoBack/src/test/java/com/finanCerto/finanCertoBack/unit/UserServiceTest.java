package com.finanCerto.finanCertoBack.unit;

import com.finanCerto.finanCertoBack.exception.InvalidPasswordException;
import com.finanCerto.finanCertoBack.exception.UserAlreadyExistsException;
import com.finanCerto.finanCertoBack.exception.UserNotFoundException;
import com.finanCerto.finanCertoBack.security.TokenService;
import com.finanCerto.finanCertoBack.user.User;
import com.finanCerto.finanCertoBack.user.UserRepository;
import com.finanCerto.finanCertoBack.user.UserService;
import com.finanCerto.finanCertoBack.user.dtos.LoginRequestDto;
import com.finanCerto.finanCertoBack.user.dtos.LoginResponseDto;
import com.finanCerto.finanCertoBack.user.dtos.UserRegistrationDto;
import com.finanCerto.finanCertoBack.user.dtos.UserResponseDto;
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
public class UserServiceTest {
    @InjectMocks
    private UserService service;
    @Mock
    private UserRepository repository;
    @Mock
    private BCryptPasswordEncoder encoder;
    @Mock
    private TokenService tokenService;

    @Test
    @DisplayName("Should register user with correct data")
    void shouldRegisterUser() {
        UserRegistrationDto userRegistrationDto = new UserRegistrationDto("test", "test@test.com", "test");
        User user = new User(userRegistrationDto);
        user.setId(1L);
        
        when(repository.save(any(User.class))).thenReturn(user);
        
        UserResponseDto response = service.register(userRegistrationDto);
        assertEquals("test", response.name());
        assertEquals("test@test.com", response.email());
    }

    @Test
    @DisplayName("Should login successfully")
    void shouldLoginSuccessfully() {
        // Arrange
        String email = "test@test.com";
        String password = "test";
        String encryptedPassword = "encodedTest";

        User user = new User();
        user.setId(1L);
        user.setName("test");
        user.setEmail(email);
        user.setPassword(encryptedPassword);

        when(repository.findByEmail(email)).thenReturn(Optional.of(user));
        when(encoder.matches(password, encryptedPassword)).thenReturn(true);
        when(tokenService.generateToken(user)).thenReturn("fake-jwt-token");

        LoginRequestDto requestDto = new LoginRequestDto(email, password);

        // Act
        LoginResponseDto responseDto = service.login(requestDto);

        // Assert
        assertNotNull(responseDto);
        assertEquals("fake-jwt-token", responseDto.token());
        verify(repository).findByEmail(email);
    }

    @Test
    @DisplayName("Should prevent registration when email is already in use")
    void shouldPreventRegistrationWithExistingEmail() {
        UserRegistrationDto userRegistrationDto = new UserRegistrationDto("test", "test@test.com", "test");
        
        when(repository.existsByEmail(userRegistrationDto.email()))
                .thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class,
                () -> service.register(userRegistrationDto));

        verify(repository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should prevent login with wrong password")
    void shouldPreventLoginWithWrongPassword() {
        // Arrange
        String email = "test@test.com";
        String password = "test";
        String wrongPassword = "test2";
        String encryptedPassword = "encodedTest";

        User user = new User();
        user.setId(1L);
        user.setName("test");
        user.setEmail(email);
        user.setPassword(encryptedPassword);

        when(repository.findByEmail(email)).thenReturn(Optional.of(user));
        when(encoder.matches(wrongPassword, encryptedPassword)).thenReturn(false);

        LoginRequestDto requestDto = new LoginRequestDto(email, "test2");

        // Act & Assert
        assertThrows(InvalidPasswordException.class,
                () -> service.login(requestDto));

        verify(tokenService, never()).generateToken(any(User.class));
        verify(repository).findByEmail(email);
    }
}
