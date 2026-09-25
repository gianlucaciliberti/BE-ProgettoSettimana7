import { motion } from 'motion/react';
import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api, ApiError } from '../api/api.js';
import { useAuth } from '../context/AuthContext.jsx';
import './pages.css';

const PLACEHOLDER = 'https://picsum.photos/seed/salone-auto-placeholder/900/600';

export function Profilo() {
  const { utente, logout, aggiornaUtente } = useAuth();
  const navigate = useNavigate();

  const [nome, setNome] = useState(utente?.nome ?? '');
  const [cognome, setCognome] = useState(utente?.cognome ?? '');
  const [messaggio, setMessaggio] = useState('');
  const [errore, setErrore] = useState('');
  const [salvando, setSalvando] = useState(false);
  const [eliminando, setEliminando] = useState(false);
  const [confermaEliminazione, setConfermaEliminazione] = useState(false);

  async function salva(evento) {
    evento.preventDefault();
    setErrore('');
    setMessaggio('');
    setSalvando(true);
    try {
      const aggiornato = await api.aggiornaProfilo({ nome, cognome });
      aggiornaUtente(aggiornato);
      setMessaggio('Profilo aggiornato.');
    } catch (err) {
      setErrore(err instanceof ApiError ? err.message : 'Salvataggio non riuscito.');
    } finally {
      setSalvando(false);
    }
  }

  async function eliminaAccount() {
    setEliminando(true);
    try {
      await api.eliminaAccount();
      logout();
      navigate('/');
    } catch (err) {
      setErrore(err instanceof ApiError ? err.message : "Eliminazione non riuscita.");
      setEliminando(false);
    }
  }

  if (!utente) return null;

  return (
    <motion.div
      className="pagina-form"
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.35 }}
    >
      <h1>Il tuo profilo</h1>
      <p className="testo-muto testo-piccolo">
        {utente.email} · ruolo {utente.ruolo} · iscritto dal{' '}
        {new Date(utente.createdAt).toLocaleDateString('it-IT')}
      </p>

      <form className="form" onSubmit={salva}>
        <label className="form__campo">
          Nome
          <input value={nome} onChange={(e) => setNome(e.target.value)} required />
        </label>
        <label className="form__campo">
          Cognome
          <input value={cognome} onChange={(e) => setCognome(e.target.value)} required />
        </label>

        {errore && <p className="form__errore">{errore}</p>}
        {messaggio && <p className="testo-muto">{messaggio}</p>}

        <button type="submit" disabled={salvando}>
          {salvando ? 'Salvo…' : 'Salva modifiche'}
        </button>
      </form>

      <hr style={{ margin: '28px 0', border: 'none', borderTop: '1px solid var(--border)' }} />

      {!confermaEliminazione ? (
        <button type="button" className="link-pericolo" onClick={() => setConfermaEliminazione(true)}>
          Elimina il mio account
        </button>
      ) : (
        <div className="banner" style={{ borderColor: 'var(--danger)', background: 'var(--danger-bg)' }}>
          <p>
            Sicuro? Vengono cancellati anche i tuoi preferiti e i tuoi avvisi di prezzo: da quel momento non
            arriverà più nessuna mail. L'operazione non si può annullare.
          </p>
          <button type="button" disabled={eliminando} onClick={eliminaAccount}>
            {eliminando ? 'Elimino…' : 'Sì, elimina definitivamente'}
          </button>{' '}
          <button type="button" onClick={() => setConfermaEliminazione(false)}>
            Annulla
          </button>
        </div>
      )}
    </motion.div>
  );
}

export function Preferiti() {
  const [preferiti, setPreferiti] = useState(null);
  const [errore, setErrore] = useState('');

  function ricarica() {
    api
      .preferitiLista()
      .then(setPreferiti)
      .catch((err) => setErrore(err instanceof ApiError ? err.message : 'Impossibile caricare i preferiti.'));
  }

  useEffect(ricarica, []);

  async function rimuovi(id) {
    try {
      await api.preferitoRimuovi(id);
      ricarica();
    } catch (err) {
      setErrore(err instanceof ApiError ? err.message : 'Rimozione non riuscita.');
    }
  }

  return (
    <motion.div initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.35 }}>
      <div className="pagina-intestazione">
        <h1>I tuoi preferiti</h1>
        <p className="pagina-sottotitolo">Le auto che hai salvato, con le soglie di prezzo impostate.</p>
      </div>

      {errore && <p className="form__errore">{errore}</p>}
      {preferiti && preferiti.length === 0 && (
        <p className="testo-muto">
          Non hai ancora nessun preferito. <Link to="/">Sfoglia il catalogo</Link>.
        </p>
      )}

      <div className="griglia-auto">
        {preferiti?.map((p) => (
          <article key={p.id} className="auto-card">
            <Link to={`/auto/${p.auto.id}`} className="auto-card__link">
              <div className="auto-card__foto">
                <img src={p.auto.foto?.[0] || PLACEHOLDER} alt={`${p.auto.marca} ${p.auto.modello}`} />
                <span className="auto-card__prezzo-tag">
                  €{Number(p.auto.prezzoVendita).toLocaleString('it-IT')}
                </span>
              </div>
              <div className="auto-card__corpo">
                <h3>
                  {p.auto.marca} {p.auto.modello}
                </h3>
                {p.avvisi.length > 0 ? (
                  <p className="auto-card__descrizione">
                    {p.avvisi.length} soglia{p.avvisi.length > 1 ? 'e' : ''} impostata
                    {p.avvisi.length > 1 ? 'e' : ''}
                  </p>
                ) : (
                  <p className="auto-card__descrizione testo-muto">Nessuna soglia di prezzo impostata</p>
                )}
              </div>
            </Link>
            <div className="auto-card__azioni">
              <button type="button" className="bottone-preferito" onClick={() => rimuovi(p.id)}>
                Rimuovi dai preferiti
              </button>
            </div>
          </article>
        ))}
      </div>
    </motion.div>
  );
}
