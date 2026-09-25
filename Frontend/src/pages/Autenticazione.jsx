import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { ApiError } from '../api/api.js';
import { useAuth } from '../context/AuthContext.jsx';
import './pages.css';

export function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errore, setErrore] = useState('');
  const [inviando, setInviando] = useState(false);

  // Se si arriva qui reindirizzati da un'azione riservata (es. "aggiungi ai
  // preferiti" da ospite), c'è un messaggio da mostrare e una pagina da
  // riprendere dopo l'accesso.
  const messaggioAccesso = location.state?.messaggio;
  const destinazione = location.state?.from?.pathname ?? '/';

  async function handleSubmit(evento) {
    evento.preventDefault();
    setErrore('');
    setInviando(true);
    try {
      await login({ email, password });
      navigate(destinazione, { replace: true });
    } catch (err) {
      setErrore(err instanceof ApiError ? err.message : 'Accesso non riuscito, riprova.');
    } finally {
      setInviando(false);
    }
  }

  return (
    <div className="pagina-form">
      <h1>Accedi</h1>

      {messaggioAccesso && (
        <div className="banner">
          <p>{messaggioAccesso}</p>
          <Link to="/registrazione" state={location.state}>
            Vuoi procedere con la registrazione?
          </Link>
        </div>
      )}

      <form className="form" onSubmit={handleSubmit}>
        <label className="form__campo">
          Email
          <input
            type="email"
            required
            autoComplete="email"
            value={email}
            onChange={(evento) => setEmail(evento.target.value)}
          />
        </label>
        <label className="form__campo">
          Password
          <input
            type="password"
            required
            autoComplete="current-password"
            value={password}
            onChange={(evento) => setPassword(evento.target.value)}
          />
        </label>

        {errore && <p className="form__errore">{errore}</p>}

        <button type="submit" disabled={inviando}>
          {inviando ? 'Accesso in corso…' : 'Accedi'}
        </button>
      </form>

      <p>
        Non hai un account? <Link to="/registrazione">Registrati</Link>
      </p>
      <p>
        <Link to="/">Continua senza account</Link>
      </p>
    </div>
  );
}

export function Registrazione() {
  const { registrazione } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [dati, setDati] = useState({ email: '', password: '', nome: '', cognome: '' });
  const [errore, setErrore] = useState('');
  const [inviando, setInviando] = useState(false);

  const destinazione = location.state?.from?.pathname ?? '/';

  function aggiornaCampo(campo) {
    return (evento) => setDati((precedente) => ({ ...precedente, [campo]: evento.target.value }));
  }

  async function handleSubmit(evento) {
    evento.preventDefault();
    setErrore('');
    setInviando(true);
    try {
      await registrazione(dati);
      navigate(destinazione, { replace: true });
    } catch (err) {
      setErrore(err instanceof ApiError ? err.message : 'Registrazione non riuscita, riprova.');
    } finally {
      setInviando(false);
    }
  }

  return (
    <div className="pagina-form">
      <h1>Registrati</h1>

      <form className="form" onSubmit={handleSubmit}>
        <label className="form__campo">
          Nome
          <input required autoComplete="given-name" value={dati.nome} onChange={aggiornaCampo('nome')} />
        </label>
        <label className="form__campo">
          Cognome
          <input required autoComplete="family-name" value={dati.cognome} onChange={aggiornaCampo('cognome')} />
        </label>
        <label className="form__campo">
          Email
          <input
            type="email"
            required
            autoComplete="email"
            value={dati.email}
            onChange={aggiornaCampo('email')}
          />
        </label>
        <label className="form__campo">
          Password
          <input
            type="password"
            required
            minLength={8}
            autoComplete="new-password"
            value={dati.password}
            onChange={aggiornaCampo('password')}
          />
        </label>

        {errore && <p className="form__errore">{errore}</p>}

        <button type="submit" disabled={inviando}>
          {inviando ? 'Registrazione in corso…' : 'Registrati'}
        </button>
      </form>

      <p>
        Hai già un account? <Link to="/login">Accedi</Link>
      </p>
      <p>
        <Link to="/">Continua senza account</Link>
      </p>
    </div>
  );
}
