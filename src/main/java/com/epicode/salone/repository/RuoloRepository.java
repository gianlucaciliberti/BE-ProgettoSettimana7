package com.epicode.salone.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.epicode.salone.entity.Ruolo;

public interface RuoloRepository extends JpaRepository<Ruolo, Long> {

    Optional<Ruolo> findByNome(Ruolo.Nome nome);
}
