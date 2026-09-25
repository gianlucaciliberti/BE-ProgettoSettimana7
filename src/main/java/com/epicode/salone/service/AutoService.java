package com.epicode.salone.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.epicode.salone.entity.Auto;
import com.epicode.salone.entity.Ruolo;
import com.epicode.salone.entity.Utente;
import com.epicode.salone.repository.AutoRepository;
import com.epicode.salone.repository.AvvisoRepository;
import com.epicode.salone.repository.PreferitoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AutoService {

    /**
     * Elenco chiuso dei campi su cui si può ordinare il catalogo: quello che
     * arriva dal client si confronta con questo set, non si concatena mai
     * direttamente in una query o in un Sort.
     */
    private static final Set<String> CAMPI_ORDINAMENTO_AMMESSI =
            Set.of("id", "marca", "modello", "prezzoVendita", "createdAt");

    private final AutoRepository autoRepository;
    private final PreferitoRepository preferitoRepository;
    private final AvvisoRepository avvisoRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<AutoDTO.Pubblico> catalogoPubblico(String ricerca, String ordinaPer, String direzione,
                                                     int pagina, int dimensione) {
        String campo = CAMPI_ORDINAMENTO_AMMESSI.contains(ordinaPer) ? ordinaPer : "id";
        Sort.Direction dir = "desc".equalsIgnoreCase(direzione) ? Sort.Direction.DESC : Sort.Direction.ASC;
        PageRequest richiesta = PageRequest.of(pagina, dimensione, Sort.by(dir, campo));

        Specification<Auto> spec = (root, query, cb) -> cb.equal(root.get("stato"), Auto.Stato.PUBBLICATA);
        if (ricerca != null && !ricerca.isBlank()) {
            String pattern = "%" + ricerca.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("marca")), pattern),
                    cb.like(cb.lower(root.get("modello")), pattern),
                    cb.like(cb.lower(root.get("descrizione")), pattern)));
        }

        return autoRepository.findAll(spec, richiesta).map(AutoDTO.Pubblico::da);
    }

    @Transactional(readOnly = true)
    public AutoDTO.Pubblico dettaglioPubblico(Long id) {
        Auto auto = autoRepository.findById(id)
                .filter(a -> a.getStato() == Auto.Stato.PUBBLICATA)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return AutoDTO.Pubblico.da(auto);
    }

    /** ADMIN vede solo le proprie auto (bozze comprese); SUPERADMIN le vede tutte. */
    @Transactional(readOnly = true)
    public List<AutoDTO.Admin> listaAdmin(Utente richiedente) {
        List<Auto> auto = isSuperAdmin(richiedente)
                ? autoRepository.findAll()
                : autoRepository.findByCreatoDaId(richiedente.getId());
        return auto.stream().map(AutoDTO.Admin::da).toList();
    }

    @Transactional(readOnly = true)
    public AutoDTO.Admin dettaglioAdmin(Long id, Utente richiedente) {
        return AutoDTO.Admin.da(trovaConAccesso(id, richiedente));
    }

    @Transactional
    public AutoDTO.Admin crea(AutoDTO.Crea request, Utente admin) {
        Auto auto = new Auto();
        auto.setMarca(request.getMarca());
        auto.setModello(request.getModello());
        auto.setDescrizione(request.getDescrizione());
        auto.setPrezzoVendita(request.getPrezzoVendita());
        auto.setPrezzoAcquisto(request.getPrezzoAcquisto());
        auto.setStato(Auto.Stato.BOZZA);
        auto.setCreatoDa(admin);
        if (request.getFoto() != null) {
            auto.getFoto().addAll(request.getFoto());
        }
        autoRepository.save(auto);
        return AutoDTO.Admin.da(auto);
    }

    @Transactional
    public AutoDTO.Admin aggiorna(Long id, AutoDTO.Aggiorna request, Utente richiedente) {
        Auto auto = trovaConAccesso(id, richiedente);
        BigDecimal prezzoPrecedente = auto.getPrezzoVendita();

        auto.setMarca(request.getMarca());
        auto.setModello(request.getModello());
        auto.setDescrizione(request.getDescrizione());
        auto.setPrezzoVendita(request.getPrezzoVendita());
        auto.setPrezzoAcquisto(request.getPrezzoAcquisto());
        auto.setStato(request.getStato());
        if (request.getFoto() != null) {
            auto.getFoto().clear();
            auto.getFoto().addAll(request.getFoto());
        }
        autoRepository.save(auto);

        pubblicaEventoSePrezzoCambiato(auto, prezzoPrecedente, request.getPrezzoVendita());
        return AutoDTO.Admin.da(auto);
    }

    /** Solo il prezzo: è l'azione pensata per generare l'evento controllato dagli Avvisi. */
    @Transactional
    public AutoDTO.Admin cambiaPrezzo(Long id, AutoDTO.Prezzo request, Utente richiedente) {
        Auto auto = trovaConAccesso(id, richiedente);
        BigDecimal prezzoPrecedente = auto.getPrezzoVendita();
        auto.setPrezzoVendita(request.getPrezzoVendita());
        autoRepository.save(auto);

        pubblicaEventoSePrezzoCambiato(auto, prezzoPrecedente, request.getPrezzoVendita());
        return AutoDTO.Admin.da(auto);
    }

    @Transactional
    public void elimina(Long id, Utente richiedente) {
        Auto auto = trovaConAccesso(id, richiedente);
        avvisoRepository.deleteByPreferitoAutoId(auto.getId());
        preferitoRepository.deleteByAutoId(auto.getId());
        autoRepository.delete(auto);
    }

    private void pubblicaEventoSePrezzoCambiato(Auto auto, BigDecimal prezzoPrecedente, BigDecimal prezzoNuovo) {
        if (prezzoPrecedente.compareTo(prezzoNuovo) != 0) {
            eventPublisher.publishEvent(new PrezzoAutoCambiatoEvent(auto.getId(), prezzoPrecedente, prezzoNuovo));
        }
    }

    /**
     * Un ADMIN può leggere/modificare solo le auto che ha creato lui; il
     * SUPERADMIN può intervenire su tutte. Chi non ha diritto riceve 403,
     * non un 404 che nasconderebbe l'esistenza della risorsa (qui, a
     * differenza di preferiti/avvisi, l'id auto non è un segreto: fa parte
     * del catalogo).
     */
    private Auto trovaConAccesso(Long id, Utente richiedente) {
        Auto auto = autoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!isSuperAdmin(richiedente) && !auto.getCreatoDa().getId().equals(richiedente.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return auto;
    }

    private boolean isSuperAdmin(Utente utente) {
        return utente.getRuolo().getNome() == Ruolo.Nome.SUPERADMIN;
    }

    /** Pubblicato quando il prezzo di vendita di un'auto cambia davvero (non su ogni salvataggio). */
    public record PrezzoAutoCambiatoEvent(Long autoId, BigDecimal prezzoPrecedente, BigDecimal prezzoNuovo) {
    }
}
