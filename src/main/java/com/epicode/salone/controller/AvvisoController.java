package com.epicode.salone.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.epicode.salone.entity.Utente;
import com.epicode.salone.service.AvvisoDTO;
import com.epicode.salone.service.PreferitoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Modifica/rimozione di un avviso già creato (scoped sul proprietario), più
 * l'endpoint pubblico di disattivazione da link mail (cercato per token,
 * mai per id). La logica vive in PreferitoService: un avviso non esiste
 * senza il preferito del suo proprietario.
 */
@RestController
@RequestMapping("/api/avvisi")
@RequiredArgsConstructor
public class AvvisoController {

    private final PreferitoService preferitoService;

    /** Solo se non è già scattato: dopo l'invio la soglia è definitiva. */
    @PutMapping("/{id}")
    public AvvisoDTO.Risposta aggiorna(@PathVariable Long id, @Valid @RequestBody AvvisoDTO.Soglia request,
                                        @AuthenticationPrincipal Utente utente) {
        return preferitoService.aggiornaAvviso(utente.getId(), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> rimuovi(@PathVariable Long id, @AuthenticationPrincipal Utente utente) {
        preferitoService.rimuoviAvviso(utente.getId(), id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Link cliccato dalla mail: un browser su un link fa sempre GET, quindi
     * niente REST puro qui per scelta. Nessuna autenticazione: il token
     * casuale e monouso nel link è la prova che serve.
     */
    @GetMapping(value = "/disattiva", produces = MediaType.TEXT_HTML_VALUE)
    public String disattiva(@RequestParam String token) {
        preferitoService.disattivaAvvisoViaToken(token);
        return "<p>Avviso disattivato. Puoi chiudere questa pagina.</p>";
    }
}
