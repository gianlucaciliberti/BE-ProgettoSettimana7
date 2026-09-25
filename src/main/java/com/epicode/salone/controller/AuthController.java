package com.epicode.salone.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.epicode.salone.entity.Utente;
import com.epicode.salone.service.AutenticazioneDTO;
import com.epicode.salone.service.UtenteDTO;
import com.epicode.salone.service.UtenteService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Registrazione, login e profilo. Riceve/restituisce solo DTO: mai l'entity Utente. */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UtenteService utenteService;

    @PostMapping("/registrazione")
    public ResponseEntity<AutenticazioneDTO.LoginRisposta> registrazione(
            @Valid @RequestBody AutenticazioneDTO.Registrazione request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(utenteService.registra(request));
    }

    @PostMapping("/login")
    public AutenticazioneDTO.LoginRisposta login(@Valid @RequestBody AutenticazioneDTO.Login request) {
        return utenteService.login(request);
    }

    @GetMapping("/profilo")
    public UtenteDTO.Profilo profilo(@AuthenticationPrincipal Utente utente) {
        return utenteService.profilo(utente.getId());
    }

    @PutMapping("/profilo")
    public UtenteDTO.Profilo aggiornaProfilo(@AuthenticationPrincipal Utente utente,
                                              @Valid @RequestBody UtenteDTO.ProfiloAggiorna request) {
        return utenteService.aggiornaProfilo(utente.getId(), request);
    }

    @DeleteMapping("/profilo")
    public ResponseEntity<Void> eliminaAccount(@AuthenticationPrincipal Utente utente) {
        utenteService.eliminaAccount(utente.getId());
        return ResponseEntity.noContent().build();
    }

    // --- Solo SUPERADMIN (vedi SecurityConfig): supervisione di tutti gli utenti. ---

    @GetMapping("/utenti")
    public List<UtenteDTO.Profilo> tuttiGliUtenti() {
        return utenteService.listaTutti();
    }

    @PatchMapping("/utenti/{id}/ruolo")
    public UtenteDTO.Profilo cambiaRuolo(@PathVariable Long id, @Valid @RequestBody UtenteDTO.CambiaRuolo request) {
        return utenteService.cambiaRuolo(id, request.getRuolo());
    }
}
