package com.epicode.salone.service;

import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class PreferitoDTO {

    private PreferitoDTO() {
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Crea {

        @NotNull
        private Long autoId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Risposta {

        private Long id;
        private AutoDTO.Pubblico auto;

        /** Zero, una o più soglie di prezzo fissate su questa auto (una per avviso, ognuna a sé). */
        private List<AvvisoDTO.Risposta> avvisi;

        private Instant createdAt;
    }
}
