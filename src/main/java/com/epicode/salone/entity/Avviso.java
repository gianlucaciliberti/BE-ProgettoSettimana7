package com.epicode.salone.entity;

import java.math.BigDecimal;
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
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Soglia di prezzo fissata su un {@link Preferito}. Un preferito può avere
 * più avvisi contemporaneamente (soglie diverse), ognuno indipendente e
 * "one-time": una volta che {@code inviato} diventa true resta così per
 * sempre, anche se il prezzo risale e riscende sotto quella stessa soglia.
 * Per un'altra mail serve un nuovo avviso con una nuova soglia, non il
 * reset di uno esistente. {@code token} è il valore casuale e monouso usato
 * nel link della mail per disattivare l'avviso: mai l'id.
 */
@Entity
@Table(name = "avvisi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Avviso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferito_id", nullable = false)
    private Preferito preferito;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal sogliaPrezzo;

    @Column(nullable = false)
    private boolean inviato = false;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
