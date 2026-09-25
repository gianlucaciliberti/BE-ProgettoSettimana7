package com.epicode.salone.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.epicode.salone.entity.Preferito;

public interface PreferitoRepository extends JpaRepository<Preferito, Long> {

    List<Preferito> findByUtenteId(Long utenteId);

    /**
     * Id + proprietario insieme: chi prova a leggere/modificare il preferito
     * di un altro utente non lo trova (404), non riceve un 403 che
     * confermerebbe l'esistenza della risorsa altrui.
     */
    Optional<Preferito> findByIdAndUtenteId(Long id, Long utenteId);

    Optional<Preferito> findByUtenteIdAndAutoId(Long utenteId, Long autoId);

    boolean existsByUtenteIdAndAutoId(Long utenteId, Long autoId);
}
