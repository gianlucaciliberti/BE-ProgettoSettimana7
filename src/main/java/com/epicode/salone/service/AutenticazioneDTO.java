package com.epicode.salone.service;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Contenitore dei DTO di registrazione/login: niente entity in giro, tutto raggruppato qui. */
public class AutenticazioneDTO {

    private AutenticazioneDTO() {
    }

    /** Registrazione di un nuovo utente. Il ruolo non c'è: lo assegna sempre il server (USER). */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Registrazione {

        @NotBlank
        @Email
        private String email;

        @NotBlank
        @Size(min = 8, max = 100)
        private String password;

        @NotBlank
        private String nome;

        @NotBlank
        private String cognome;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Login {

        @NotBlank
        private String email;

        @NotBlank
        private String password;
    }

    /** Risposta al login: solo il token e i dati minimi utili al frontend. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRisposta {

        private String token;
        private String email;
        private String ruolo;
    }
}
