package com.epicode.salone.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.epicode.salone.entity.Auto;

/**
 * {@link JpaSpecificationExecutor} è qui per costruire in modo sicuro la
 * ricerca/ordinamento del catalogo (slide "Gli attacchi: i dati in
 * ingresso"): il campo su cui ordinare va validato contro un elenco chiuso
 * di valori ammessi PRIMA di costruire la Specification, non concatenato
 * come stringa.
 */
public interface AutoRepository extends JpaRepository<Auto, Long>, JpaSpecificationExecutor<Auto> {

    List<Auto> findByCreatoDaId(Long adminId);
}
