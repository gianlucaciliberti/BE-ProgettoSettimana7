package com.epicode.salone.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.epicode.salone.entity.Utente;
import com.epicode.salone.service.AvvisoDTO;
import com.epicode.salone.service.PreferitoDTO;
import com.epicode.salone.service.PreferitoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Preferiti dell'utente loggato e le soglie di prezzo (avvisi) su ognuno.
 * Tutto scoped sull'utente autenticato: nessun endpoint accetta un
 * identificativo di un altro utente.
 */
@RestController
@RequestMapping("/api/preferiti")
@RequiredArgsConstructor
public class PreferitoController {

    private final PreferitoService preferitoService;

    @GetMapping
    public List<PreferitoDTO.Risposta> lista(@AuthenticationPrincipal Utente utente) {
        return preferitoService.lista(utente.getId());
    }

    @PostMapping
    public ResponseEntity<PreferitoDTO.Risposta> aggiungi(@Valid @RequestBody PreferitoDTO.Crea request,
                                                            @AuthenticationPrincipal Utente utente) {
        return ResponseEntity.status(HttpStatus.CREATED).body(preferitoService.aggiungi(utente, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> rimuovi(@PathVariable Long id, @AuthenticationPrincipal Utente utente) {
        preferitoService.rimuovi(utente.getId(), id);
        return ResponseEntity.noContent().build();
    }

    /** Per la pagina di dettaglio auto: l'utente loggato ce l'ha già nei preferiti? */
    @GetMapping("/per-auto/{autoId}")
    public PreferitoDTO.Risposta perAuto(@PathVariable Long autoId, @AuthenticationPrincipal Utente utente) {
        return preferitoService.trovaPerAuto(utente.getId(), autoId);
    }

    @GetMapping("/{id}/avvisi")
    public List<AvvisoDTO.Risposta> listaAvvisi(@PathVariable Long id, @AuthenticationPrincipal Utente utente) {
        return preferitoService.listaAvvisi(utente.getId(), id);
    }

    /** Un preferito può avere più soglie: ognuna scatterà al massimo una volta. */
    @PostMapping("/{id}/avvisi")
    public ResponseEntity<AvvisoDTO.Risposta> creaAvviso(@PathVariable Long id,
                                                           @Valid @RequestBody AvvisoDTO.Soglia request,
                                                           @AuthenticationPrincipal Utente utente) {
        return ResponseEntity.status(HttpStatus.CREATED).body(preferitoService.creaAvviso(utente.getId(), id, request));
    }
}
