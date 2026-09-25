import { motion } from 'motion/react';
import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api, ApiError } from '../api/api.js';
import { useAuth, useRichiediAccesso } from '../context/AuthContext.jsx';
import './pages.css';

const PLACEHOLDER = 'https://picsum.photos/seed/salone-auto-placeholder/900/600';

export function Catalogo() {
  const [risultati, setRisultati] = useState(null);
  const [caricamento, setCaricamento] = useState(true);
  const [errore, setErrore] = useState('');

  // Poche auto: niente ricerca/paginazione, il catalogo intero compare da solo.
  useEffect(() => {
    let annullato = false;
    api
      .catalogo({ dimensione: 100 })
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
  }, []);

  return (
    <div>
      <motion.section
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ duration: 0.6 }}
        className="hero-salone"
      >
        <div className="hero-salone__testo">
          <h1>Salone Auto</h1>
          <p>
            Un piccolo salone, auto scelte una per una. Sfoglia il catalogo, salva quelle che ti piacciono e
            fissa una soglia di prezzo: ti avvisiamo noi quando scende.
          </p>
        </div>
      </motion.section>

      {errore && <p className="form__errore">{errore}</p>}
      {caricamento && <p className="testo-muto">Caricamento…</p>}
      {!caricamento && risultati?.content.length === 0 && (
        <p className="testo-muto">Nessuna auto pubblicata al momento.</p>
      )}

      <div className="griglia-auto">
        {risultati?.content.map((auto, indice) => (
          <AutoCard key={auto.id} auto={auto} indice={indice} />
        ))}
      </div>
    </div>
  );
}

function AutoCard({ auto, indice }) {
  const { autenticato } = useAuth();
  const richiediAccesso = useRichiediAccesso();
  const [stato, setStato] = useState('inattivo'); // inattivo | salvando | salvato | errore

  async function aggiungiPreferiti(evento) {
    evento.preventDefault();
    if (!autenticato) {
      richiediAccesso("Devi accedere all'account per aggiungere questa auto ai preferiti.");
      return;
    }
    setStato('salvando');
    try {
      await api.preferitoAggiungi(auto.id);
      setStato('salvato');
    } catch (err) {
      setStato(err instanceof ApiError && err.status === 409 ? 'salvato' : 'errore');
    }
  }

  return (
    <motion.article
      className="auto-card"
      initial={{ opacity: 0, y: 16 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.35, delay: Math.min(indice * 0.05, 0.3) }}
      whileHover={{ y: -4 }}
    >
      <Link to={`/auto/${auto.id}`} className="auto-card__link">
        <div className="auto-card__foto">
          <img src={auto.foto?.[0] || PLACEHOLDER} alt={`${auto.marca} ${auto.modello}`} loading="lazy" />
          <span className="auto-card__prezzo-tag">€{Number(auto.prezzoVendita).toLocaleString('it-IT')}</span>
        </div>
        <div className="auto-card__corpo">
          <h3>
            {auto.marca} {auto.modello}
          </h3>
          <p className="auto-card__descrizione">{auto.descrizione}</p>
        </div>
      </Link>
      <div className="auto-card__azioni">
        <motion.button
          type="button"
          whileTap={{ scale: 0.96 }}
          disabled={stato === 'salvando' || stato === 'salvato'}
          onClick={aggiungiPreferiti}
          className={stato === 'salvato' ? 'bottone-preferito bottone-preferito--attivo' : 'bottone-preferito'}
        >
          {stato === 'salvato' ? '♥ Nei preferiti' : '♡ Aggiungi ai preferiti'}
        </motion.button>
      </div>
    </motion.article>
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
  const [fotoAttiva, setFotoAttiva] = useState(0);

  const [preferito, setPreferito] = useState(null); // null = non caricato/non presente
  const [statoPreferito, setStatoPreferito] = useState('inattivo');
  const [nuovaSoglia, setNuovaSoglia] = useState('');
  const [statoSoglia, setStatoSoglia] = useState('inattivo');
  const [erroreSoglia, setErroreSoglia] = useState('');

  useEffect(() => {
    let annullato = false;
    setCaricamento(true);
    setErrore('');
    setFotoAttiva(0);

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

  useEffect(() => {
    if (!autenticato) return;
    let annullato = false;
    api
      .preferitoPerAuto(id)
      .then((dati) => {
        if (!annullato) setPreferito(dati);
      })
      .catch(() => {
        // 404: non è (ancora) nei preferiti, resta null.
      });
    return () => {
      annullato = true;
    };
  }, [id, autenticato]);

  async function aggiungiPreferiti() {
    if (!autenticato) {
      richiediAccesso("Devi accedere all'account per aggiungere questa auto ai preferiti.");
      return;
    }
    setStatoPreferito('salvando');
    try {
      const nuovo = await api.preferitoAggiungi(auto.id);
      setPreferito(nuovo);
    } catch (err) {
      if (err instanceof ApiError && err.status === 409) {
        // Già nei preferiti ma non l'avevamo ancora ricaricato: recupera lo stato vero.
        try {
          setPreferito(await api.preferitoPerAuto(auto.id));
        } catch {
          /* ignorato */
        }
      } else {
        setStatoPreferito('errore');
      }
    } finally {
      setStatoPreferito('inattivo');
    }
  }

  async function creaSoglia(evento) {
    evento.preventDefault();
    setErroreSoglia('');
    const valore = Number(nuovaSoglia);
    if (!valore || valore <= 0) {
      setErroreSoglia('Inserisci un prezzo valido.');
      return;
    }
    setStatoSoglia('salvando');
    try {
      const avviso = await api.avvisoCrea(preferito.id, valore);
      setPreferito((precedente) => ({ ...precedente, avvisi: [...precedente.avvisi, avviso] }));
      setNuovaSoglia('');
    } catch (err) {
      setErroreSoglia(err instanceof ApiError ? err.message : 'Non è stato possibile salvare la soglia.');
    } finally {
      setStatoSoglia('inattivo');
    }
  }

  if (caricamento) return <p className="testo-muto">Caricamento…</p>;
  if (errore) return <p className="form__errore">{errore}</p>;
  if (!auto) return null;

  const foto = auto.foto?.length ? auto.foto : [PLACEHOLDER];

  return (
    <motion.div
      className="dettaglio-auto"
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.35 }}
    >
      <button type="button" className="link-indietro" onClick={() => navigate(-1)}>
        ← Torna indietro
      </button>

      <div className="dettaglio-auto__layout">
        <div className="dettaglio-auto__galleria">
          <motion.img
            key={foto[fotoAttiva]}
            src={foto[fotoAttiva]}
            alt={`${auto.marca} ${auto.modello}`}
            className="dettaglio-auto__foto-principale"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 0.25 }}
          />
          {foto.length > 1 && (
            <div className="dettaglio-auto__miniature">
              {foto.map((url, indice) => (
                <button
                  type="button"
                  key={url}
                  className={indice === fotoAttiva ? 'miniatura miniatura--attiva' : 'miniatura'}
                  onClick={() => setFotoAttiva(indice)}
                >
                  <img src={url} alt="" />
                </button>
              ))}
            </div>
          )}
        </div>

        <div className="dettaglio-auto__info">
          <h1>
            {auto.marca} {auto.modello}
          </h1>
          <p className="dettaglio-auto__prezzo">€{Number(auto.prezzoVendita).toLocaleString('it-IT')}</p>
          <p>{auto.descrizione}</p>

          {!preferito ? (
            <motion.button
              type="button"
              whileTap={{ scale: 0.97 }}
              disabled={statoPreferito === 'salvando'}
              onClick={aggiungiPreferiti}
              className="bottone-primario"
            >
              {statoPreferito === 'salvando' ? 'Aggiungo…' : '♡ Aggiungi ai preferiti'}
            </motion.button>
          ) : (
            <div className="riquadro-soglie">
              <p className="riquadro-soglie__titolo">♥ Nei tuoi preferiti</p>

              {preferito.avvisi.length > 0 && (
                <ul className="lista-soglie">
                  {preferito.avvisi.map((avviso) => (
                    <li key={avviso.id} className={avviso.inviato ? 'soglia soglia--scattata' : 'soglia'}>
                      Avvisami sotto €{Number(avviso.sogliaPrezzo).toLocaleString('it-IT')}
                      {avviso.inviato && <span className="soglia__etichetta">già inviato</span>}
                    </li>
                  ))}
                </ul>
              )}

              <form className="form-soglia" onSubmit={creaSoglia}>
                <label>
                  Nuova soglia di prezzo
                  <input
                    type="number"
                    min="1"
                    step="0.01"
                    placeholder="es. 20000"
                    value={nuovaSoglia}
                    onChange={(evento) => setNuovaSoglia(evento.target.value)}
                  />
                </label>
                <button type="submit" disabled={statoSoglia === 'salvando'}>
                  {statoSoglia === 'salvando' ? 'Salvo…' : 'Aggiungi soglia'}
                </button>
              </form>
              {erroreSoglia && <p className="form__errore">{erroreSoglia}</p>}
              <p className="testo-muto testo-piccolo">
                Ogni soglia manda al massimo una mail: quando il prezzo la attraversa scatta e basta, per un'altra
                notifica serve una nuova soglia.
              </p>
            </div>
          )}
          {statoPreferito === 'errore' && <p className="form__errore">Non è stato possibile salvare, riprova.</p>}
        </div>
      </div>
    </motion.div>
  );
}
