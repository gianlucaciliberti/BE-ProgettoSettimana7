package com.epicode.salone.entity;

/**
 * Valori ammessi per {@link Ruolo}.
 * <p>
 * USER: utente registrato, gestisce solo i propri preferiti/avvisi.
 * ADMIN: gestisce le auto che ha creato lui (bozze, prezzi, pubblicazione).
 * SUPERADMIN: account fittizio unico ("amministratore"), può intervenire
 * anche sulle auto create da un altro ADMIN.
 */
public enum RuoloNome {
    USER,
    ADMIN,
    SUPERADMIN
}
