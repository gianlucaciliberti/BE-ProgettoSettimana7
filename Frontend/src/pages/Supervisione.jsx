import { motion } from 'motion/react';
import { useEffect, useState } from 'react';
import { api, ApiError } from '../api/api.js';
import './pages.css';
import './Supervisione.css';

export default function Supervisione() {
  const [utenti, setUtenti] = useState(null);
  const [auto, setAuto] = useState(null);
  const [errore, setErrore] = useState('');
  const [inCorso, setInCorso] = useState(null); // id dell'utente il cui ruolo si sta cambiando

  function ricarica() {
    Promise.all([api.utentiLista(), api.autoAdminLista()])
      .then(([listaUtenti, listaAuto]) => {
        setUtenti(listaUtenti);
        setAuto(listaAuto);
      })
      .catch((err) => setErrore(err instanceof ApiError ? err.message : 'Impossibile caricare i dati.'));
  }

  useEffect(ricarica, []);

  async function cambiaRuolo(utente, nuovoRuolo) {
    setInCorso(utente.id);
    try {
      await api.utenteCambiaRuolo(utente.id, nuovoRuolo);
      ricarica();
    } catch (err) {
      setErrore(err instanceof ApiError ? err.message : 'Cambio ruolo non riuscito.');
    } finally {
      setInCorso(null);
    }
  }

  if (errore) return <p className="form__errore">{errore}</p>;
  if (!utenti || !auto) return <p className="testo-muto">Caricamento…</p>;

  const autoPerAdmin = new Map();
  for (const a of auto) {
    if (!autoPerAdmin.has(a.creatoDaId)) autoPerAdmin.set(a.creatoDaId, []);
    autoPerAdmin.get(a.creatoDaId).push(a);
  }

  return (
    <motion.div initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.35 }}>
      <div className="pagina-intestazione">
        <h1>Supervisione</h1>
        <p className="pagina-sottotitolo">Utenti, ruoli e stato delle auto pubblicate da ogni admin.</p>
      </div>

      <section className="riquadro-supervisione">
        <h2>Utenti ({utenti.length})</h2>
        <table className="tabella-utenti">
          <thead>
            <tr>
              <th>Nome</th>
              <th>Email</th>
              <th>Ruolo</th>
              <th>Auto pubblicate</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {utenti.map((u) => (
              <tr key={u.id}>
                <td>
                  {u.nome} {u.cognome}
                </td>
                <td className="testo-muto">{u.email}</td>
                <td>
                  <span className={`badge-ruolo-tabella badge-ruolo-tabella--${u.ruolo.toLowerCase()}`}>
                    {u.ruolo}
                  </span>
                </td>
                <td>{autoPerAdmin.get(u.id)?.length ?? '—'}</td>
                <td className="tabella-utenti__azioni">
                  {u.ruolo === 'USER' && (
                    <button type="button" disabled={inCorso === u.id} onClick={() => cambiaRuolo(u, 'ADMIN')}>
                      Promuovi ad ADMIN
                    </button>
                  )}
                  {u.ruolo === 'ADMIN' && (
                    <button type="button" disabled={inCorso === u.id} onClick={() => cambiaRuolo(u, 'USER')}>
                      Riporta a USER
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section className="riquadro-supervisione">
        <h2>Auto per amministratore</h2>
        {[...autoPerAdmin.entries()].map(([adminId, listaAuto]) => {
          const admin = utenti.find((u) => u.id === adminId);
          const pubblicate = listaAuto.filter((a) => a.stato === 'PUBBLICATA').length;
          const bozze = listaAuto.length - pubblicate;
          return (
            <div key={adminId} className="riepilogo-admin">
              <p className="riepilogo-admin__nome">
                {admin ? `${admin.nome} ${admin.cognome}` : `Utente #${adminId}`}
                <span className="testo-muto"> — {pubblicate} pubblicate, {bozze} in bozza</span>
              </p>
              <ul className="riepilogo-admin__lista">
                {listaAuto.map((a) => (
                  <li key={a.id}>
                    {a.marca} {a.modello} — €{Number(a.prezzoVendita).toLocaleString('it-IT')}{' '}
                    <span className={a.stato === 'PUBBLICATA' ? 'etichetta-stato etichetta-stato--pubblicata' : 'etichetta-stato'}>
                      {a.stato}
                    </span>
                  </li>
                ))}
              </ul>
            </div>
          );
        })}
      </section>
    </motion.div>
  );
}
