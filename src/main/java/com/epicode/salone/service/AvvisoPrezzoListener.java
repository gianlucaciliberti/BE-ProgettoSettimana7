package com.epicode.salone.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.util.HtmlUtils;

import com.epicode.salone.entity.Auto;
import com.epicode.salone.entity.Avviso;
import com.epicode.salone.entity.Utente;
import com.epicode.salone.repository.AvvisoRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Ascolta il cambio di prezzo dopo il commit della transazione che lo ha
 * salvato (AFTER_COMMIT): se il salvataggio fallisce non parte nessuna mail,
 * e chi ha salvato non aspetta Gmail (@Async). Costruisce e manda anche la
 * mail stessa: non c'è nessun altro punto dell'app che invia posta, non
 * serviva un service a parte.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AvvisoPrezzoListener {

    private final AvvisoRepository avvisoRepository;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mittente;

    @Value("${app.base-url}")
    private String baseUrl;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPrezzoCambiato(AutoService.PrezzoAutoCambiatoEvent evento) {
        List<Avviso> candidati = avvisoRepository.findNonInviatiByAutoIdConDettagli(evento.autoId());

        for (Avviso avviso : candidati) {
            boolean sogliaAttraversata = evento.prezzoPrecedente().compareTo(avviso.getSogliaPrezzo()) > 0
                    && evento.prezzoNuovo().compareTo(avviso.getSogliaPrezzo()) <= 0;
            if (!sogliaAttraversata) {
                continue;
            }

            // Update atomica: se un'altra esecuzione ha già segnato l'invio,
            // qui il risultato è 0 e non si manda una seconda mail.
            int aggiornate = avvisoRepository.marcaInviatoSeNonGiaFatto(avviso.getId());
            if (aggiornate != 1) {
                continue;
            }

            try {
                inviaMail(avviso, evento.prezzoNuovo());
            } catch (Exception e) {
                log.error("Invio mail per l'avviso {} fallito", avviso.getId(), e);
            }
        }
    }

    /**
     * Nome utente, marca/modello/descrizione arrivano dal DB ma in ultima
     * analisi da input utente (registrazione, creazione auto): nel corpo
     * mail vanno sempre passati già "escaped", mai concatenati come HTML grezzo.
     */
    private void inviaMail(Avviso avviso, BigDecimal prezzoNuovo) {
        Utente utente = avviso.getPreferito().getUtente();
        Auto auto = avviso.getPreferito().getAuto();

        String nome = HtmlUtils.htmlEscape(utente.getNome());
        String marca = HtmlUtils.htmlEscape(auto.getMarca());
        String modello = HtmlUtils.htmlEscape(auto.getModello());
        String descrizione = HtmlUtils.htmlEscape(auto.getDescrizione());
        String linkDisattiva = baseUrl + "/api/avvisi/disattiva?token=" + avviso.getToken();

        String html = """
                <p>Ciao %s,</p>
                <p>il prezzo di <strong>%s %s</strong> &egrave; sceso a &euro;%s, sotto la soglia che avevi impostato.</p>
                <p>%s</p>
                <p><a href="%s">Disattiva questo avviso</a></p>
                """.formatted(nome, marca, modello, prezzoNuovo, descrizione, linkDisattiva);

        try {
            MimeMessage messaggio = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(messaggio, "UTF-8");
            helper.setTo(utente.getEmail());
            helper.setFrom(mittente);
            helper.setSubject("Sceso il prezzo di " + marca + " " + modello);
            helper.setText(html, true);
            mailSender.send(messaggio);
        } catch (MessagingException e) {
            throw new IllegalStateException("Invio mail fallito", e);
        }
    }
}
