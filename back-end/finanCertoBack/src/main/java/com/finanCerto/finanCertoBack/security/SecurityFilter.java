package com.finanCerto.finanCertoBack.security;

import com.finanCerto.finanCertoBack.usuario.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {
    @Autowired
    private TokenService tokenService;
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // LOG PARA DEBUG
        System.out.println("SecurityFilter: Processing request: " + request.getMethod() + " " + request.getRequestURI());
        System.out.println("SecurityFilter: Origin: " + request.getHeader("Origin"));
        System.out.println("SecurityFilter: Authorization: " + request.getHeader("Authorization"));
        
        String path = request.getRequestURI();

     

        var token = this.recoverToken(request);
        if (token != null) {
            try {

                if (tokenService.validateToken(token)) {
                    var email = tokenService.getSubject(token);
                    var usuarioOptional = usuarioRepository.findByEmail(email);
                    if (usuarioOptional.isPresent()) {
                        var usuario = usuarioOptional.get();
                        var authentication = new UsernamePasswordAuthenticationToken(
                                usuarioOptional, null, usuario.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    } else {
                        System.out.println("Usuario not found for email: " + email);
                    }
                } else {
                    System.out.println("Invalid token: " + token);
                }
            } catch (Exception e) {
                System.out.println("Error validating token: " + e.getMessage());

            }
        }
        filterChain.doFilter(request, response);
    }
    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null) {
            return null;
        }
        return authHeader.replace("Bearer ", "");
    }


}
