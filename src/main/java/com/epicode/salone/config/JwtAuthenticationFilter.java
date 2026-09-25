package com.epicode.salone.config;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.epicode.salone.entity.Utente;
import com.epicode.salone.repository.UtenteRepository;
import com.epicode.salone.service.JwtService;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Legge "Authorization: Bearer &lt;token&gt;", valida il JWT e, se valido,
 * carica l'utente (per email, il subject del token) e lo mette nel
 * SecurityContext come principal: i controller lo ricevono già pronto con
 * {@code @AuthenticationPrincipal Utente utente}, senza un'altra query.
 * Token assente o non valido: nessuna Authentication, si prosegue anonimi
 * (le regole di autorizzazione decidono poi se la richiesta può passare).
 * <p>
 * Niente {@code @Component}: Spring Boot registrerebbe automaticamente
 * ogni bean Filter anche come filtro servlet globale (url-pattern "/*"),
 * in aggiunta a quello nella catena di Spring Security aggiunto con
 * addFilterBefore in {@link SecurityConfig}. È SecurityConfig a costruirlo
 * esplicitamente, così esiste una sola istanza, nel posto giusto.
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UtenteRepository utenteRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                String email = jwtService.valida(token).getPayload().getSubject();
                utenteRepository.findByEmail(email).ifPresent(this::autentica);
            } catch (JwtException e) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private void autentica(Utente utente) {
        var autorita = new SimpleGrantedAuthority("ROLE_" + utente.getRuolo().getNome().name());
        var authentication = new UsernamePasswordAuthenticationToken(utente, null, List.of(autorita));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
