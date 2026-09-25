package com.epicode.salone.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.epicode.salone.entity.Ruolo;
import com.epicode.salone.entity.Utente;
import com.epicode.salone.repository.AvvisoRepository;
import com.epicode.salone.repository.PreferitoRepository;
import com.epicode.salone.repository.RuoloRepository;
import com.epicode.salone.repository.UtenteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UtenteService {

    private final UtenteRepository utenteRepository;
    private final RuoloRepository ruoloRepository;
    private final PreferitoRepository preferitoRepository;
    private final AvvisoRepository avvisoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AutenticazioneDTO.LoginRisposta registra(AutenticazioneDTO.Registrazione request) {
        if (utenteRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email già registrata");
        }
        // Il ruolo è sempre USER alla registrazione: non è mai il client a deciderlo.
        Ruolo ruoloUser = ruoloRepository.findByNome(Ruolo.Nome.USER)
                .orElseThrow(() -> new IllegalStateException("Ruolo USER non seminato"));

        Utente utente = new Utente();
        utente.setEmail(request.getEmail());
        utente.setPassword(passwordEncoder.encode(request.getPassword()));
        utente.setNome(request.getNome());
        utente.setCognome(request.getCognome());
        utente.setRuolo(ruoloUser);
        utenteRepository.save(utente);

        return generaRisposta(utente);
    }

    @Transactional(readOnly = true)
    public AutenticazioneDTO.LoginRisposta login(AutenticazioneDTO.Login request) {
        Utente utente = utenteRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenziali non valide"));
        if (!passwordEncoder.matches(request.getPassword(), utente.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenziali non valide");
        }
        return generaRisposta(utente);
    }

    @Transactional(readOnly = true)
    public UtenteDTO.Profilo profilo(Long utenteId) {
        return UtenteDTO.Profilo.da(trova(utenteId));
    }

    @Transactional
    public UtenteDTO.Profilo aggiornaProfilo(Long utenteId, UtenteDTO.ProfiloAggiorna request) {
        Utente utente = trova(utenteId);
        utente.setNome(request.getNome());
        utente.setCognome(request.getCognome());
        return UtenteDTO.Profilo.da(utente);
    }

    /** "Elimina il mio account": via anche avvisi e preferiti, da lì non parte più nessuna mail. */
    @Transactional
    public void eliminaAccount(Long utenteId) {
        avvisoRepository.deleteByPreferitoUtenteId(utenteId);
        preferitoRepository.deleteByUtenteId(utenteId);
        utenteRepository.deleteById(utenteId);
    }

    private Utente trova(Long id) {
        return utenteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private AutenticazioneDTO.LoginRisposta generaRisposta(Utente utente) {
        AutenticazioneDTO.LoginRisposta risposta = new AutenticazioneDTO.LoginRisposta();
        risposta.setToken(jwtService.generaToken(utente));
        risposta.setEmail(utente.getEmail());
        risposta.setRuolo(utente.getRuolo().getNome().name());
        return risposta;
    }
}
