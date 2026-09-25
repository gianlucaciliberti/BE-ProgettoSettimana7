package com.epicode.salone.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.epicode.salone.entity.Ruolo;
import com.epicode.salone.entity.Utente;
import com.epicode.salone.repository.RuoloRepository;
import com.epicode.salone.repository.UtenteRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * All'avvio: semina le 3 righe fisse di Ruolo se mancano, poi crea l'account
 * SUPERADMIN fittizio "amministratore" se non esiste già. Email e password
 * arrivano da ADMIN_EMAIL/ADMIN_PASSWORD: senza ADMIN_PASSWORD impostata
 * l'account non viene creato (niente password di ripiego in chiaro nel codice).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements ApplicationRunner {

    private final RuoloRepository ruoloRepository;
    private final UtenteRepository utenteRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (Ruolo.Nome nome : Ruolo.Nome.values()) {
            if (ruoloRepository.findByNome(nome).isEmpty()) {
                Ruolo ruolo = new Ruolo();
                ruolo.setNome(nome);
                ruoloRepository.save(ruolo);
            }
        }

        seminaSuperAdmin();
    }

    private void seminaSuperAdmin() {
        if (utenteRepository.existsByEmail(adminEmail)) {
            return;
        }
        if (adminPassword == null || adminPassword.isBlank()) {
            log.warn("ADMIN_PASSWORD non impostata: account SUPERADMIN non creato all'avvio");
            return;
        }

        Ruolo ruoloSuperAdmin = ruoloRepository.findByNome(Ruolo.Nome.SUPERADMIN)
                .orElseThrow(() -> new IllegalStateException("Ruolo SUPERADMIN non seminato"));

        Utente amministratore = new Utente();
        amministratore.setEmail(adminEmail);
        amministratore.setPassword(passwordEncoder.encode(adminPassword));
        amministratore.setNome("Amministratore");
        amministratore.setCognome("Sistema");
        amministratore.setRuolo(ruoloSuperAdmin);
        utenteRepository.save(amministratore);
        log.info("Account SUPERADMIN creato: {}", adminEmail);
    }
}
