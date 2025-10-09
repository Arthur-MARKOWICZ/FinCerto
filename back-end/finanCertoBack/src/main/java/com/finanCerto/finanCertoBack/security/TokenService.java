package com.finanCerto.finanCertoBack.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.finanCerto.finanCertoBack.exception.UsuarioNaoEncontrado;
import com.finanCerto.finanCertoBack.usuario.Usuario;
import com.finanCerto.finanCertoBack.usuario.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;

@Service
public class TokenService {
    @Value("${jwt.secret}")
    private String secret ;
    @Value("${jwt.expiration}")
    private long jwtExpiration;
    private final UsuarioRepository usuarioRepository;
    private  Long REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 7;
    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    public TokenService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

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
            logger.error("erro na geracao do token do usuario: {}", usuario.getEmail());
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
    public Usuario obterUsuario(String token){
        String email = getSubject(token);
        Optional<Usuario> usuario = usuarioRepository.findByEmail(email);
        if(usuario.isEmpty()){
            throw  new UsuarioNaoEncontrado("Usuario nao encontrado");
        }
        return usuario.get();
    }
}
