package com.epicode.salone.entity;

import java.math.BigDecimal;
import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Auto in catalogo. Il prezzo d'acquisto è visibile solo agli admin
 * (mai esposto nel DTO pubblico). Ogni auto è legata all'admin che l'ha
 * creata ({@link #creatoDa}): un ADMIN può modificare/eliminare solo le
 * proprie, il SUPERADMIN può intervenire su tutte.
 */
@Entity
@Table(name = "auto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Auto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String marca;

    @Column(nullable = false)
    private String modello;

    @Lob
    @Column(nullable = false)
    private String descrizione;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prezzoVendita;

    /** Prezzo d'acquisto: solo gli admin lo vedono/impostano. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prezzoAcquisto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Stato stato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creato_da", nullable = false)
    private Utente creatoDa;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    /**
     * BOZZA: visibile solo agli admin (ADMIN proprietario e SUPERADMIN).
     * PUBBLICATA: visibile a chiunque, anche senza login.
     */
    public enum Stato {
        BOZZA,
        PUBBLICATA
    }
}
