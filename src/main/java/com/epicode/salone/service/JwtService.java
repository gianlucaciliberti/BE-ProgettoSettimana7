package com.epicode.salone.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.epicode.salone.entity.Utente;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Genera e valida i JWT. Nel token solo i dati che servono per autorizzare
 * le richieste (id, email, ruolo): niente password, niente altro.
 */
@Service
public class JwtService {

    private final SecretKey chiave;
    private final long scadenzaMinuti;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                       @Value("${app.jwt.expiration-minutes}") long scadenzaMinuti) {
        if (secret == null || secret.isBlank()) {
            // Fail fast: senza un secret vero il token sarebbe firmato con una
            // chiave prevedibile. Va impostato con la variabile d'ambiente JWT_SECRET.
            throw new IllegalStateException("JWT_SECRET non impostato");
        }
        this.chiave = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.scadenzaMinuti = scadenzaMinuti;
    }

    public String generaToken(Utente utente) {
        Instant ora = Instant.now();
        return Jwts.builder()
                .subject(utente.getEmail())
                .claim("id", utente.getId())
                .claim("ruolo", utente.getRuolo().getNome().name())
                .issuedAt(Date.from(ora))
                .expiration(Date.from(ora.plus(scadenzaMinuti, ChronoUnit.MINUTES)))
                .signWith(chiave)
                .compact();
    }

    /** Lancia un'eccezione (non catturata qui) se il token è scaduto, malformato o con firma non valida. */
    public Jws<Claims> valida(String token) {
        return Jwts.parser()
                .verifyWith(chiave)
                .build()
                .parseSignedClaims(token);
    }
}
