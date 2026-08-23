package br.com.gado.security;

import br.com.gado.enums.EnPerfilUsuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

/** Emite e valida os JWT usados para autenticar requisições à API (ver Authorization: Bearer). */
@Service
public class JwtService {

    private static final String CLAIM_PERFIL = "perfil";

    private final SecretKey chave;
    private final long expiracaoMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                       @Value("${app.jwt.expiration-ms}") long expiracaoMs) {
        this.chave = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret.getBytes(StandardCharsets.UTF_8)));
        this.expiracaoMs = expiracaoMs;
    }

    public String gerarToken(String email, EnPerfilUsuario perfil) {
        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + expiracaoMs);

        return Jwts.builder()
                .subject(email)
                .claim(CLAIM_PERFIL, perfil.name())
                .issuedAt(agora)
                .expiration(expiracao)
                .signWith(chave)
                .compact();
    }

    public Claims validarEExtrairClaims(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extrairEmail(Claims claims) {
        return claims.getSubject();
    }

    public EnPerfilUsuario extrairPerfil(Claims claims) {
        return EnPerfilUsuario.valueOf(claims.get(CLAIM_PERFIL, String.class));
    }
}
