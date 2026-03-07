package com.finanCerto.finanCertoBack.user;

import com.finanCerto.finanCertoBack.exception.InvalidPasswordException;
import com.finanCerto.finanCertoBack.exception.UserAlreadyExistsException;
import com.finanCerto.finanCertoBack.exception.UserNotFoundException;
import com.finanCerto.finanCertoBack.security.TokenService;
import com.finanCerto.finanCertoBack.user.dtos.LoginRequestDto;
import com.finanCerto.finanCertoBack.user.dtos.LoginResponseDto;
import com.finanCerto.finanCertoBack.user.dtos.UserRegistrationDto;
import com.finanCerto.finanCertoBack.user.dtos.UserResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Transactional
    public UserResponseDto register(UserRegistrationDto dto) {
        log.info("Attempting to register user with email: {}", dto.email());
        if (repository.existsByEmail(dto.email())) {
            log.warn("Registration failed: User with email {} already exists", dto.email());
            throw new UserAlreadyExistsException("User with this email already exists");
        }

        User user = new User(dto);
        user.setPassword(passwordEncoder.encode(dto.password()));

        User savedUser = repository.save(user);
        log.info("User registered successfully: id={}", savedUser.getId());
        
        return new UserResponseDto(savedUser.getId(), savedUser.getName(), savedUser.getEmail());
    }

    public LoginResponseDto login(LoginRequestDto dto) {
        log.info("Attempting login for user: {}", dto.email());
        User user = repository.findByEmail(dto.email())
                .orElseThrow(() -> {
                    log.warn("Login failed: User not found with email {}", dto.email());
                    return new UserNotFoundException("User not found");
                });

        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            log.warn("Login failed: Invalid password for user {}", dto.email());
            throw new InvalidPasswordException("Invalid password");
        }

        String token = tokenService.generateToken(user);
        log.info("Login successful for user: {}", dto.email());
        return new LoginResponseDto(token);
    }
}
