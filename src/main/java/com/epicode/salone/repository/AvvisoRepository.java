package com.epicode.salone.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.epicode.salone.entity.Avviso;

public interface AvvisoRepository extends JpaRepository<Avviso, Long> {

    /** Id + proprietario insieme: stesso motivo di PreferitoRepository. */
    Optional<Avviso> findByIdAndPreferitoUtenteId(Long id, Long utenteId);

    /** Un preferito può avere più avvisi (soglie diverse) contemporaneamente. */
    List<Avviso> findByPreferitoId(Long preferitoId);

    /** Link di disattivazione nella mail: si cerca per token, mai per id. */
    Optional<Avviso> findByToken(String token);

    /**
     * Avvisi non ancora inviati per una data auto, con utente e auto già
     * caricati: il listener che li usa gira in un thread @Async dopo il
     * commit, quando il contesto di persistenza originale non c'è più, quindi
     * niente lazy-loading su preferito/utente/auto.
     */
    @Query("SELECT a FROM Avviso a JOIN FETCH a.preferito p JOIN FETCH p.utente JOIN FETCH p.auto "
            + "WHERE p.auto.id = :autoId AND a.inviato = false")
    List<Avviso> findNonInviatiByAutoIdConDettagli(@Param("autoId") Long autoId);

    /**
     * Marca l'avviso come inviato solo se non lo era già, in un'unica
     * operazione atomica. Se il risultato è 0, un'altra richiesta ha già
     * segnato l'invio: non si manda una seconda mail.
     */
    @Modifying
    @Query("UPDATE Avviso a SET a.inviato = true WHERE a.id = :id AND a.inviato = false")
    int marcaInviatoSeNonGiaFatto(@Param("id") Long id);

    /** Cascata manuale per "elimina il mio account". */
    void deleteByPreferitoUtenteId(Long utenteId);

    /** Cascata manuale per l'eliminazione di un'auto da parte di un admin. */
    void deleteByPreferitoAutoId(Long autoId);

    /** Cascata manuale per la rimozione di un preferito (tutti i suoi avvisi, quanti siano). */
    void deleteByPreferitoId(Long preferitoId);
}
