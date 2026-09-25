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

    Optional<Avviso> findByPreferitoId(Long preferitoId);

    /** Link di disattivazione nella mail: si cerca per token, mai per id. */
    Optional<Avviso> findByToken(String token);

    /** Tutti gli avvisi (di qualunque utente) legati a una data auto, per capire chi avvisare quando cambia il prezzo. */
    List<Avviso> findByPreferitoAutoId(Long autoId);

    /**
     * Marca l'avviso come inviato solo se non lo era già, in un'unica
     * operazione atomica. Se il risultato è 0, un'altra richiesta ha già
     * segnato l'invio: non si manda una seconda mail.
     */
    @Modifying
    @Query("UPDATE Avviso a SET a.inviato = true WHERE a.id = :id AND a.inviato = false")
    int marcaInviatoSeNonGiaFatto(@Param("id") Long id);
}
