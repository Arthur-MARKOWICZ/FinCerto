package com.finanCerto.finanCertoBack.user;

import com.finanCerto.finanCertoBack.user.dtos.LoginRequestDto;
import com.finanCerto.finanCertoBack.user.dtos.LoginResponseDto;
import com.finanCerto.finanCertoBack.user.dtos.UserRegistrationDto;
import com.finanCerto.finanCertoBack.user.dtos.UserResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @PostMapping
    public ResponseEntity<UserResponseDto> register(@RequestBody @Valid UserRegistrationDto dto) {
        log.info("Received request to register user: {}", dto.email());
        UserResponseDto response = service.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginRequestDto dto) {
        log.info("Received login request for user: {}", dto.email());
        LoginResponseDto response = service.login(dto);
        return ResponseEntity.ok(response);
    }
}
