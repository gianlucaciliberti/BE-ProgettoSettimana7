package com.epicode.salone.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.epicode.salone.entity.Auto;
import com.epicode.salone.entity.Avviso;
import com.epicode.salone.entity.Preferito;
import com.epicode.salone.entity.Utente;
import com.epicode.salone.repository.AutoRepository;
import com.epicode.salone.repository.AvvisoRepository;
import com.epicode.salone.repository.PreferitoRepository;

import lombok.RequiredArgsConstructor;

/**
 * Preferiti e i loro Avvisi (soglie di prezzo) insieme: un avviso non esiste
 * senza un preferito del suo stesso proprietario, non ha senso separarli in
 * due service.
 */
@Service
@RequiredArgsConstructor
public class PreferitoService {

    private final PreferitoRepository preferitoRepository;
    private final AvvisoRepository avvisoRepository;
    private final AutoRepository autoRepository;

    // ---- Preferiti ----------------------------------------------------

    @Transactional(readOnly = true)
    public List<PreferitoDTO.Risposta> lista(Long utenteId) {
        return preferitoRepository.findByUtenteId(utenteId).stream()
                .map(this::mappa)
                .toList();
    }

    @Transactional
    public PreferitoDTO.Risposta aggiungi(Utente utente, PreferitoDTO.Crea request) {
        // Un'auto ancora in bozza non è nel catalogo: non si può metterla nei preferiti.
        Auto auto = autoRepository.findById(request.getAutoId())
                .filter(a -> a.getStato() == Auto.Stato.PUBBLICATA)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (preferitoRepository.existsByUtenteIdAndAutoId(utente.getId(), auto.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Auto già nei preferiti");
        }

        Preferito preferito = new Preferito();
        preferito.setUtente(utente);
        preferito.setAuto(auto);
        preferitoRepository.save(preferito);
        return mappa(preferito);
    }

    @Transactional
    public void rimuovi(Long utenteId, Long preferitoId) {
        Preferito preferito = preferitoRepository.findByIdAndUtenteId(preferitoId, utenteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        avvisoRepository.deleteByPreferitoId(preferito.getId());
        preferitoRepository.delete(preferito);
    }

    private PreferitoDTO.Risposta mappa(Preferito preferito) {
        PreferitoDTO.Risposta dto = new PreferitoDTO.Risposta();
        dto.setId(preferito.getId());
        dto.setAuto(AutoDTO.Pubblico.da(preferito.getAuto()));
        dto.setAvvisi(avvisoRepository.findByPreferitoId(preferito.getId()).stream()
                .map(AvvisoDTO.Risposta::da)
                .toList());
        dto.setCreatedAt(preferito.getCreatedAt());
        return dto;
    }

    // ---- Avvisi (soglie di prezzo) -------------------------------------
    // Un preferito può avere più avvisi contemporaneamente. Ogni avviso è
    // "one-time": una volta inviato resta inviato per sempre, anche se il
    // prezzo risale e riscende sotto la stessa soglia. Per un'altra mail
    // l'utente deve creare un nuovo avviso, non riattivarne uno vecchio.

    @Transactional
    public AvvisoDTO.Risposta creaAvviso(Long utenteId, Long preferitoId, AvvisoDTO.Soglia request) {
        Preferito preferito = preferitoRepository.findByIdAndUtenteId(preferitoId, utenteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Avviso avviso = new Avviso();
        avviso.setPreferito(preferito);
        avviso.setSogliaPrezzo(request.getSogliaPrezzo());
        // Casuale e monouso: nel link di disattivazione della mail non ci va mai l'id.
        avviso.setToken(UUID.randomUUID().toString());
        avvisoRepository.save(avviso);
        return AvvisoDTO.Risposta.da(avviso);
    }

    @Transactional(readOnly = true)
    public List<AvvisoDTO.Risposta> listaAvvisi(Long utenteId, Long preferitoId) {
        preferitoRepository.findByIdAndUtenteId(preferitoId, utenteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return avvisoRepository.findByPreferitoId(preferitoId).stream()
                .map(AvvisoDTO.Risposta::da)
                .toList();
    }

    /** Modifica la soglia solo finché l'avviso non è ancora scattato: dopo è definitivo. */
    @Transactional
    public AvvisoDTO.Risposta aggiornaAvviso(Long utenteId, Long avvisoId, AvvisoDTO.Soglia request) {
        Avviso avviso = trovaAvviso(utenteId, avvisoId);
        if (avviso.isInviato()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Avviso già inviato: crea un nuovo avviso per un'altra soglia");
        }
        avviso.setSogliaPrezzo(request.getSogliaPrezzo());
        return AvvisoDTO.Risposta.da(avviso);
    }

    @Transactional
    public void rimuoviAvviso(Long utenteId, Long avvisoId) {
        avvisoRepository.delete(trovaAvviso(utenteId, avvisoId));
    }

    /** Endpoint pubblico da link mail: si cerca per token, mai per id. */
    @Transactional
    public void disattivaAvvisoViaToken(String token) {
        Avviso avviso = avvisoRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        avvisoRepository.delete(avviso);
    }

    private Avviso trovaAvviso(Long utenteId, Long avvisoId) {
        return avvisoRepository.findByIdAndPreferitoUtenteId(avvisoId, utenteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
