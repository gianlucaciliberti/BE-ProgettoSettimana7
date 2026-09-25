package com.epicode.salone.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
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

    // Niente @Lob: su Postgres mapperebbe su oid (large object) invece che
    // su testo normale, inutilmente complesso per una descrizione.
    @Column(nullable = false, columnDefinition = "TEXT")
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

    /** URL delle foto, in ordine: la prima è la copertina mostrata nelle card. */
    @ElementCollection
    @CollectionTable(name = "auto_foto", joinColumns = @JoinColumn(name = "auto_id"))
    @OrderColumn(name = "posizione")
    @Column(name = "url", nullable = false, length = 1000)
    private List<String> foto = new ArrayList<>();

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
