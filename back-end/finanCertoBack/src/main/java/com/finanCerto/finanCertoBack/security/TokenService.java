package com.finanCerto.finanCertoBack.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.finanCerto.finanCertoBack.exception.UserNotFoundException;
import com.finanCerto.finanCertoBack.user.User;
import com.finanCerto.finanCertoBack.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private final UserRepository userRepository;

    private static final String ISSUER = "FinanCerto-api";
    private static final long REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 7;

    private Algorithm getAlgorithm() {
        return Algorithm.HMAC256(secret);
    }

    public String generateToken(User user) {
        try {
            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(user.getEmail())
                    .withExpiresAt(generateExpirationDate())
                    .sign(getAlgorithm());
        } catch (JWTCreationException e) {
            log.error("Error generating token for user {}: {}", user.getEmail(), e.getMessage());
            throw new RuntimeException("Error generating token", e);
        }
    }

    public String generateRefreshToken(User user) {
        return JWT.create()
                .withSubject(user.getEmail())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .sign(getAlgorithm());
    }

    public boolean validateToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(getAlgorithm()).build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            log.warn("Invalid token detected: {}", e.getMessage());
            return false;
        }
    }

    public String getSubject(String token) {
        JWTVerifier verifier = JWT.require(getAlgorithm()).build();
        DecodedJWT jwt = verifier.verify(token);
        return jwt.getSubject();
    }

    private Instant generateExpirationDate() {
        return LocalDateTime.now().plusHours(24).toInstant(ZoneOffset.of("-03:00"));
    }

    public User getUserFromToken(String token) {
        String email = getSubject(token);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
