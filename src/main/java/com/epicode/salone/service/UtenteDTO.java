package com.epicode.salone.service;

import java.time.Instant;

import com.epicode.salone.entity.Utente;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class UtenteDTO {

    private UtenteDTO() {
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Profilo {

        private Long id;
        private String email;
        private String nome;
        private String cognome;
        private String ruolo;
        private Instant createdAt;

        public static Profilo da(Utente utente) {
            Profilo dto = new Profilo();
            dto.setId(utente.getId());
            dto.setEmail(utente.getEmail());
            dto.setNome(utente.getNome());
            dto.setCognome(utente.getCognome());
            dto.setRuolo(utente.getRuolo().getNome().name());
            dto.setCreatedAt(utente.getCreatedAt());
            return dto;
        }
    }

    /** Modifica dei dati anagrafici dal profilo. Email, password e ruolo non si cambiano da qui. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfiloAggiorna {

        @NotBlank
        private String nome;

        @NotBlank
        private String cognome;
    }
}
