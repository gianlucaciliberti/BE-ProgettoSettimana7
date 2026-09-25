package com.epicode.salone.service;

import java.math.BigDecimal;
import java.time.Instant;

import com.epicode.salone.entity.Auto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class AutoDTO {

    private AutoDTO() {
    }

    /** Vista pubblica del catalogo: niente prezzo d'acquisto, niente stato/bozza. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pubblico {

        private Long id;
        private String marca;
        private String modello;
        private String descrizione;
        private BigDecimal prezzoVendita;

        public static Pubblico da(Auto auto) {
            Pubblico dto = new Pubblico();
            dto.setId(auto.getId());
            dto.setMarca(auto.getMarca());
            dto.setModello(auto.getModello());
            dto.setDescrizione(auto.getDescrizione());
            dto.setPrezzoVendita(auto.getPrezzoVendita());
            return dto;
        }
    }

    /** Vista completa per gli admin: include prezzo d'acquisto, stato e proprietario. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Admin {

        private Long id;
        private String marca;
        private String modello;
        private String descrizione;
        private BigDecimal prezzoVendita;
        private BigDecimal prezzoAcquisto;
        private String stato;
        private Long creatoDaId;
        private Instant createdAt;
        private Instant updatedAt;

        public static Admin da(Auto auto) {
            Admin dto = new Admin();
            dto.setId(auto.getId());
            dto.setMarca(auto.getMarca());
            dto.setModello(auto.getModello());
            dto.setDescrizione(auto.getDescrizione());
            dto.setPrezzoVendita(auto.getPrezzoVendita());
            dto.setPrezzoAcquisto(auto.getPrezzoAcquisto());
            dto.setStato(auto.getStato().name());
            dto.setCreatoDaId(auto.getCreatoDa().getId());
            dto.setCreatedAt(auto.getCreatedAt());
            dto.setUpdatedAt(auto.getUpdatedAt());
            return dto;
        }
    }

    /** Creazione di una nuova auto: parte sempre come BOZZA, lo stato si cambia con Aggiorna. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Crea {

        @NotBlank
        private String marca;

        @NotBlank
        private String modello;

        @NotBlank
        private String descrizione;

        @NotNull
        @Positive
        private BigDecimal prezzoVendita;

        @NotNull
        @Positive
        private BigDecimal prezzoAcquisto;
    }

    /**
     * Modifica completa di un'auto esistente, incluso il passaggio
     * BOZZA/PUBBLICATA. Lo stato è tipizzato sull'enum dell'entity: un
     * valore fuori dai due ammessi viene rifiutato da Jackson prima ancora
     * di arrivare al service (elenco chiuso, non stringa libera).
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Aggiorna {

        @NotBlank
        private String marca;

        @NotBlank
        private String modello;

        @NotBlank
        private String descrizione;

        @NotNull
        @Positive
        private BigDecimal prezzoVendita;

        @NotNull
        @Positive
        private BigDecimal prezzoAcquisto;

        @NotNull
        private Auto.Stato stato;
    }

    /** Solo il cambio di prezzo di vendita: l'azione che genera l'evento verificato dagli Avvisi. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Prezzo {

        @NotNull
        @Positive
        private BigDecimal prezzoVendita;
    }
}
