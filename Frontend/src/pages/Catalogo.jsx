import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api, ApiError } from '../api/api.js';
import { useAuth, useRichiediAccesso } from '../context/AuthContext.jsx';
import './pages.css';

const CAMPI_ORDINAMENTO = [
  { valore: 'id', etichetta: 'Più recenti' },
  { valore: 'prezzoVendita', etichetta: 'Prezzo' },
  { valore: 'marca', etichetta: 'Marca' },
  { valore: 'modello', etichetta: 'Modello' },
];

export function Catalogo() {
  const [ricercaInput, setRicercaInput] = useState('');
  const [ricerca, setRicerca] = useState('');
  const [ordinaPer, setOrdinaPer] = useState('id');
  const [direzione, setDirezione] = useState('asc');
  const [pagina, setPagina] = useState(0);

  const [risultati, setRisultati] = useState(null);
  const [caricamento, setCaricamento] = useState(true);
  const [errore, setErrore] = useState('');

  // Debounce: si cerca 300ms dopo che l'utente ha smesso di scrivere, non a ogni tasto.
  useEffect(() => {
    const timer = setTimeout(() => {
      setPagina(0);
      setRicerca(ricercaInput);
    }, 300);
    return () => clearTimeout(timer);
  }, [ricercaInput]);

  useEffect(() => {
    let annullato = false;
    setCaricamento(true);
    setErrore('');

    api
      .catalogo({ ricerca, ordinaPer, direzione, pagina, dimensione: 12 })
      .then((datiPagina) => {
        if (!annullato) setRisultati(datiPagina);
      })
      .catch((err) => {
        if (!annullato) setErrore(err instanceof ApiError ? err.message : 'Catalogo non disponibile.');
      })
      .finally(() => {
        if (!annullato) setCaricamento(false);
      });

    return () => {
      annullato = true;
    };
  }, [ricerca, ordinaPer, direzione, pagina]);

  return (
    <div>
      <h1>Catalogo</h1>

      <div className="filtri">
        <input
          type="search"
          placeholder="Cerca per marca, modello o descrizione…"
          value={ricercaInput}
          onChange={(evento) => setRicercaInput(evento.target.value)}
        />
        <select value={ordinaPer} onChange={(evento) => setOrdinaPer(evento.target.value)}>
          {CAMPI_ORDINAMENTO.map((campo) => (
            <option key={campo.valore} value={campo.valore}>
              {campo.etichetta}
            </option>
          ))}
        </select>
        <select value={direzione} onChange={(evento) => setDirezione(evento.target.value)}>
          <option value="asc">Crescente</option>
          <option value="desc">Decrescente</option>
        </select>
      </div>

      {errore && <p className="form__errore">{errore}</p>}
      {caricamento && <p>Caricamento…</p>}

      {!caricamento && risultati && risultati.content.length === 0 && <p>Nessuna auto trovata.</p>}

      <div className="griglia-auto">
        {risultati?.content.map((auto) => (
          <AutoCard key={auto.id} auto={auto} />
        ))}
      </div>

      {risultati && risultati.totalPages > 1 && (
        <div className="paginazione">
          <button type="button" disabled={pagina === 0} onClick={() => setPagina((p) => p - 1)}>
            ← Precedente
          </button>
          <span>
            Pagina {pagina + 1} di {risultati.totalPages}
          </span>
          <button
            type="button"
            disabled={pagina + 1 >= risultati.totalPages}
            onClick={() => setPagina((p) => p + 1)}
          >
            Successiva →
          </button>
        </div>
      )}
    </div>
  );
}

function AutoCard({ auto }) {
  const { autenticato } = useAuth();
  const richiediAccesso = useRichiediAccesso();
  const [stato, setStato] = useState('inattivo'); // inattivo | salvando | salvato | errore

  async function aggiungiPreferiti() {
    if (!autenticato) {
      richiediAccesso("Devi accedere all'account per aggiungere questa auto ai preferiti.");
      return;
    }
    setStato('salvando');
    try {
      await api.preferitoAggiungi(auto.id);
      setStato('salvato');
    } catch (err) {
      // Già nei preferiti: per l'utente è comunque il risultato che voleva.
      setStato(err instanceof ApiError && err.status === 409 ? 'salvato' : 'errore');
    }
  }

  return (
    <article className="auto-card">
      <h3>
        {auto.marca} {auto.modello}
      </h3>
      <p className="auto-card__descrizione">{auto.descrizione}</p>
      <p className="auto-card__prezzo">€{auto.prezzoVendita}</p>
      <div className="auto-card__azioni">
        <Link to={`/auto/${auto.id}`}>Dettagli</Link>
        <button type="button" disabled={stato === 'salvando' || stato === 'salvato'} onClick={aggiungiPreferiti}>
          {stato === 'salvato' ? 'Nei preferiti ✓' : 'Aggiungi ai preferiti'}
        </button>
      </div>
      {stato === 'errore' && <p className="form__errore">Non è stato possibile salvarla, riprova.</p>}
    </article>
  );
}

export function DettaglioAuto() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { autenticato } = useAuth();
  const richiediAccesso = useRichiediAccesso();

  const [auto, setAuto] = useState(null);
  const [caricamento, setCaricamento] = useState(true);
  const [errore, setErrore] = useState('');
  const [statoPreferito, setStatoPreferito] = useState('inattivo');

  useEffect(() => {
    let annullato = false;
    setCaricamento(true);
    setErrore('');

    api
      .autoDettaglio(id)
      .then((dati) => {
        if (!annullato) setAuto(dati);
      })
      .catch((err) => {
        if (!annullato) {
          setErrore(err instanceof ApiError && err.status === 404 ? 'Auto non trovata.' : "Impossibile caricare l'auto.");
        }
      })
      .finally(() => {
        if (!annullato) setCaricamento(false);
      });

    return () => {
      annullato = true;
    };
  }, [id]);

  async function aggiungiPreferiti() {
    if (!autenticato) {
      richiediAccesso("Devi accedere all'account per aggiungere questa auto ai preferiti.");
      return;
    }
    setStatoPreferito('salvando');
    try {
      await api.preferitoAggiungi(auto.id);
      setStatoPreferito('salvato');
    } catch (err) {
      setStatoPreferito(err instanceof ApiError && err.status === 409 ? 'salvato' : 'errore');
    }
  }

  if (caricamento) return <p>Caricamento…</p>;
  if (errore) return <p className="form__errore">{errore}</p>;
  if (!auto) return null;

  return (
    <div className="dettaglio-auto">
      <button type="button" className="link-indietro" onClick={() => navigate(-1)}>
        ← Torna indietro
      </button>

      <h1>
        {auto.marca} {auto.modello}
      </h1>
      <p className="dettaglio-auto__prezzo">€{auto.prezzoVendita}</p>
      <p>{auto.descrizione}</p>

      <button
        type="button"
        disabled={statoPreferito === 'salvando' || statoPreferito === 'salvato'}
        onClick={aggiungiPreferiti}
      >
        {statoPreferito === 'salvato' ? 'Nei preferiti ✓' : 'Aggiungi ai preferiti'}
      </button>
      {statoPreferito === 'errore' && <p className="form__errore">Non è stato possibile salvarla, riprova.</p>}
    </div>
  );
}
