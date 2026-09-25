package com.epicode.salone.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.epicode.salone.entity.Utente;

public interface UtenteRepository extends JpaRepository<Utente, Long> {

    Optional<Utente> findByEmail(String email);

    boolean existsByEmail(String email);
}
