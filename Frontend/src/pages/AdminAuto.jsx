import { motion } from 'motion/react';
import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api, ApiError } from '../api/api.js';
import './pages.css';

export function AdminAutoLista() {
  const [auto, setAuto] = useState(null);
  const [errore, setErrore] = useState('');

  function ricarica() {
    api
      .autoAdminLista()
      .then(setAuto)
      .catch((err) => setErrore(err instanceof ApiError ? err.message : 'Impossibile caricare le auto.'));
  }

  useEffect(ricarica, []);

  async function elimina(id) {
    try {
      await api.autoElimina(id);
      ricarica();
    } catch (err) {
      setErrore(err instanceof ApiError ? err.message : 'Eliminazione non riuscita.');
    }
  }

  return (
    <motion.div initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.35 }}>
      <div className="pagina-intestazione pagina-intestazione--riga">
        <div>
          <h1>Gestione auto</h1>
          <p className="pagina-sottotitolo">Le tue auto, bozze comprese.</p>
        </div>
        <Link to="/admin/auto/nuova" className="bottone-primario bottone-primario--link">
          + Nuova auto
        </Link>
      </div>

      {errore && <p className="form__errore">{errore}</p>}
      {auto?.length === 0 && <p className="testo-muto">Non hai ancora pubblicato nessuna auto.</p>}

      <div className="griglia-auto">
        {auto?.map((a) => (
          <article key={a.id} className="auto-card">
            <div className="auto-card__foto">
              <img src={a.foto?.[0] || 'https://picsum.photos/seed/salone-auto-placeholder/900/600'} alt="" />
              <span className={a.stato === 'PUBBLICATA' ? 'etichetta-stato etichetta-stato--pubblicata' : 'etichetta-stato'}>
                {a.stato}
              </span>
            </div>
            <div className="auto-card__corpo">
              <h3>
                {a.marca} {a.modello}
              </h3>
              <p className="auto-card__descrizione">
                Vendita €{Number(a.prezzoVendita).toLocaleString('it-IT')} · Acquisto €
                {Number(a.prezzoAcquisto).toLocaleString('it-IT')}
              </p>
            </div>
            <div className="auto-card__azioni auto-card__azioni--doppie">
              <Link to={`/admin/auto/${a.id}/modifica`} className="bottone-preferito">
                Modifica
              </Link>
              <button type="button" className="bottone-preferito" onClick={() => elimina(a.id)}>
                Elimina
              </button>
            </div>
          </article>
        ))}
      </div>
    </motion.div>
  );
}

const AUTO_VUOTA = {
  marca: '',
  modello: '',
  descrizione: '',
  prezzoVendita: '',
  prezzoAcquisto: '',
  stato: 'BOZZA',
  fotoTesto: '',
};

export function AdminAutoForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const modifica = Boolean(id);

  const [dati, setDati] = useState(AUTO_VUOTA);
  const [caricamento, setCaricamento] = useState(modifica);
  const [salvando, setSalvando] = useState(false);
  const [errore, setErrore] = useState('');

  useEffect(() => {
    if (!modifica) return;
    api
      .autoAdminDettaglio(id)
      .then((a) =>
        setDati({
          marca: a.marca,
          modello: a.modello,
          descrizione: a.descrizione,
          prezzoVendita: a.prezzoVendita,
          prezzoAcquisto: a.prezzoAcquisto,
          stato: a.stato,
          fotoTesto: (a.foto || []).join('\n'),
        }),
      )
      .catch((err) => setErrore(err instanceof ApiError ? err.message : "Impossibile caricare l'auto."))
      .finally(() => setCaricamento(false));
  }, [id, modifica]);

  function campo(chiave) {
    return (evento) => setDati((precedente) => ({ ...precedente, [chiave]: evento.target.value }));
  }

  async function salva(evento) {
    evento.preventDefault();
    setErrore('');
    setSalvando(true);

    const foto = dati.fotoTesto
      .split('\n')
      .map((riga) => riga.trim())
      .filter(Boolean);

    const corpo = {
      marca: dati.marca,
      modello: dati.modello,
      descrizione: dati.descrizione,
      prezzoVendita: Number(dati.prezzoVendita),
      prezzoAcquisto: Number(dati.prezzoAcquisto),
      foto,
    };

    try {
      if (modifica) {
        await api.autoAggiorna(id, { ...corpo, stato: dati.stato });
      } else {
        const creata = await api.autoCrea(corpo);
        // Appena creata è sempre BOZZA: se l'admin ha scelto PUBBLICATA nel form, la aggiorniamo subito.
        if (dati.stato === 'PUBBLICATA') {
          await api.autoAggiorna(creata.id, { ...corpo, stato: 'PUBBLICATA' });
        }
      }
      navigate('/admin/auto');
    } catch (err) {
      setErrore(err instanceof ApiError ? err.message : 'Salvataggio non riuscito.');
    } finally {
      setSalvando(false);
    }
  }

  if (caricamento) return <p className="testo-muto">Caricamento…</p>;

  return (
    <motion.div
      className="pagina-form"
      style={{ maxWidth: 560 }}
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.35 }}
    >
      <h1>{modifica ? "Modifica auto" : "Nuova auto"}</h1>

      <form className="form" onSubmit={salva}>
        <label className="form__campo">
          Marca
          <input value={dati.marca} onChange={campo('marca')} required />
        </label>
        <label className="form__campo">
          Modello
          <input value={dati.modello} onChange={campo('modello')} required />
        </label>
        <label className="form__campo">
          Descrizione
          <textarea rows={4} value={dati.descrizione} onChange={campo('descrizione')} required />
        </label>
        <label className="form__campo">
          Prezzo di vendita (€)
          <input type="number" min="1" step="0.01" value={dati.prezzoVendita} onChange={campo('prezzoVendita')} required />
        </label>
        <label className="form__campo">
          Prezzo di acquisto (€) — visibile solo agli admin
          <input type="number" min="1" step="0.01" value={dati.prezzoAcquisto} onChange={campo('prezzoAcquisto')} required />
        </label>
        <label className="form__campo">
          Foto (un URL per riga, la prima è la copertina)
          <textarea rows={3} value={dati.fotoTesto} onChange={campo('fotoTesto')} placeholder="https://…" />
        </label>
        <label className="form__campo">
          Stato
          <select value={dati.stato} onChange={campo('stato')}>
            <option value="BOZZA">Bozza (non visibile nel catalogo)</option>
            <option value="PUBBLICATA">Pubblicata</option>
          </select>
        </label>

        {errore && <p className="form__errore">{errore}</p>}

        <button type="submit" disabled={salvando}>
          {salvando ? 'Salvo…' : 'Salva'}
        </button>
      </form>
    </motion.div>
  );
}
