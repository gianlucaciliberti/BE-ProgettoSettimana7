package com.epicode.salone.controller;

import java.util.List;

import org.springframework.data.domain.Page;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.epicode.salone.entity.Utente;
import com.epicode.salone.service.AutoDTO;
import com.epicode.salone.service.AutoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Catalogo auto: sfoglia/ricerca pubblica (GET), gestione per gli admin
 * (resto). Ricerca e ordinamento sono validati nel service contro un
 * elenco chiuso di campi ammessi: qui arrivano solo come stringhe grezze.
 */
@RestController
@RequestMapping("/api/auto")
@RequiredArgsConstructor
public class AutoController {

    private final AutoService autoService;

    @GetMapping
    public Page<AutoDTO.Pubblico> catalogo(
            @RequestParam(required = false) String ricerca,
            @RequestParam(defaultValue = "id") String ordinaPer,
            @RequestParam(defaultValue = "asc") String direzione,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int dimensione) {
        return autoService.catalogoPubblico(ricerca, ordinaPer, direzione, pagina, dimensione);
    }

    @GetMapping("/{id}")
    public AutoDTO.Pubblico dettaglio(@PathVariable Long id) {
        return autoService.dettaglioPubblico(id);
    }

    /** ADMIN vede solo le proprie auto (bozze comprese); SUPERADMIN le vede tutte. */
    @GetMapping("/admin")
    public List<AutoDTO.Admin> listaAdmin(@AuthenticationPrincipal Utente utente) {
        return autoService.listaAdmin(utente);
    }

    @GetMapping("/admin/{id}")
    public AutoDTO.Admin dettaglioAdmin(@PathVariable Long id, @AuthenticationPrincipal Utente utente) {
        return autoService.dettaglioAdmin(id, utente);
    }

    @PostMapping
    public ResponseEntity<AutoDTO.Admin> crea(@Valid @RequestBody AutoDTO.Crea request,
                                               @AuthenticationPrincipal Utente utente) {
        return ResponseEntity.status(HttpStatus.CREATED).body(autoService.crea(request, utente));
    }

    @PutMapping("/{id}")
    public AutoDTO.Admin aggiorna(@PathVariable Long id, @Valid @RequestBody AutoDTO.Aggiorna request,
                                   @AuthenticationPrincipal Utente utente) {
        return autoService.aggiorna(id, request, utente);
    }

    /** Solo il prezzo: è l'azione che può scatenare la mail agli avvisi. */
    @PatchMapping("/{id}/prezzo")
    public AutoDTO.Admin cambiaPrezzo(@PathVariable Long id, @Valid @RequestBody AutoDTO.Prezzo request,
                                       @AuthenticationPrincipal Utente utente) {
        return autoService.cambiaPrezzo(id, request, utente);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> elimina(@PathVariable Long id, @AuthenticationPrincipal Utente utente) {
        autoService.elimina(id, utente);
        return ResponseEntity.noContent().build();
    }
}
