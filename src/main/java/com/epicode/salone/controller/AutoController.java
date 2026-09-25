package com.epicode.salone.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Catalogo auto: sfoglia/ricerca pubblica, creazione/modifica/prezzo per gli
 * admin. Ricerca e ordinamento vanno validati contro un elenco chiuso di
 * campi ammessi (mai concatenati). Endpoint da aggiungere dopo DTO e Service.
 */
@RestController
@RequestMapping("/api/auto")
public class AutoController {
}
