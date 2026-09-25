package com.epicode.salone.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Avvisi di prezzo dell'utente loggato (CRUD sulla soglia), più l'endpoint
 * pubblico di disattivazione da link mail (cercato per token, mai per id).
 * Stesso pattern id+proprietario di PreferitoController per gli endpoint
 * autenticati. Endpoint da aggiungere dopo DTO e Service.
 */
@RestController
@RequestMapping("/api/avvisi")
public class AvvisoController {
}
