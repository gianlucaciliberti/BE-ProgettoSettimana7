package com.epicode.salone.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Auto messa tra i preferiti di un utente. Un utente non può avere due volte
 * la stessa auto nei preferiti (vincolo di unicità sulla coppia utente/auto).
 * L'eventuale soglia di prezzo vive nell'entity {@link Avviso}, collegata
 * uno a uno a questo preferito.
 */
@Entity
@Table(name = "preferiti", uniqueConstraints = @UniqueConstraint(columnNames = {"utente_id", "auto_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Preferito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utente_id", nullable = false)
    private Utente utente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auto_id", nullable = false)
    private Auto auto;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
