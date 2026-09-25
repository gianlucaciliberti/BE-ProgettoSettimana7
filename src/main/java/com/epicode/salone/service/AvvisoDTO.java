package com.epicode.salone.service;

import java.math.BigDecimal;
import java.time.Instant;

import com.epicode.salone.entity.Avviso;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class AvvisoDTO {

    private AvvisoDTO() {
    }

    /** Fissa o aggiorna la soglia di prezzo di un avviso: stesso corpo per creazione e modifica. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Soglia {

        @NotNull
        @Positive
        private BigDecimal sogliaPrezzo;
    }

    /** Il token non compare mai qui: resta lato server, usato solo nel link della mail. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Risposta {

        private Long id;
        private BigDecimal sogliaPrezzo;
        private boolean inviato;
        private Instant createdAt;

        public static Risposta da(Avviso avviso) {
            Risposta dto = new Risposta();
            dto.setId(avviso.getId());
            dto.setSogliaPrezzo(avviso.getSogliaPrezzo());
            dto.setInviato(avviso.isInviato());
            dto.setCreatedAt(avviso.getCreatedAt());
            return dto;
        }
    }
}
