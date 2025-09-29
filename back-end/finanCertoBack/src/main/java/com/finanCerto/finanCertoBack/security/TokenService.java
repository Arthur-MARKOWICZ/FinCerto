package com.finanCerto.finanCertoBack.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.finanCerto.finanCertoBack.usuario.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

@Service
public class TokenService {
    @Value("${jwt.secret}")
    private String secret ;
    @Value("${jwt.expiration}")
    private long jwtExpiration;
    private  Long REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 7;
    private Algorithm algorithm() {
        return Algorithm.HMAC256(secret);
    }
    public String generateToken(Usuario usuario){
        try{
            var algorithm = algorithm();
            String token = JWT.create()
                    .withIssuer("FinanCerto-api")
                    .withSubject(usuario.getEmail())
                    .withExpiresAt(generateExpirationDate())
                    .sign(algorithm);
            return token;
        }catch (JWTCreationException e){
            throw new RuntimeException("Erro na geração do token", e);

        }
    }
    public String generateRefreshToken(Usuario usuario) {
        return JWT.create()
                .withSubject(usuario.getEmail())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .sign(algorithm());
    }
    public boolean validateToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(algorithm()).build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }
    public String getSubject(String token) {
        JWTVerifier verifier = JWT.require(algorithm()).build();
        DecodedJWT jwt = verifier.verify(token);
        return jwt.getSubject();
    }
    private Instant generateExpirationDate(){
        return LocalDateTime.now().plusHours(24).toInstant(ZoneOffset.of("-03:00"));
    }
}
