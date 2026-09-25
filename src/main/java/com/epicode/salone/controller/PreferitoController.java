package com.epicode.salone.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Preferiti dell'utente loggato. Ogni risorsa va cercata per id + proprietario
 * insieme: chi non è il proprietario riceve 404, non 403. Endpoint da
 * aggiungere dopo DTO e Service.
 */
@RestController
@RequestMapping("/api/preferiti")
public class PreferitoController {
}
