package com.wavepass.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.wavepass.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    public String gerarToken(Usuario usuario) {
        try {

            var algoritmo = Algorithm.HMAC256(secret);

            return JWT.create()
                    .withIssuer("API Wavepass")
                    .withSubject(usuario.getEmail())
                    .withExpiresAt(dataExpiracao())
                    .sign(algoritmo);
        } catch(JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token JWT.");
        }
    }

    public String validarToken(String token) {
        var decodedToken = JWT.require(Algorithm.HMAC256(secret))
                .withIssuer("API Wavepass")
                .withClaimPresence("exp")
                .build()
                .verify(token);

        String email = decodedToken.getSubject();
        if (email == null || email.isBlank()) {
            throw new JWTVerificationException("Token sem usuario.");
        }
        return email;
    }

    private Instant dataExpiracao() {
        return LocalDateTime
                .now()
                .plusDays(7)
                .toInstant(ZoneOffset.of("-03:00"));
    }
}
