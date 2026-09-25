import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { api } from '../api/api.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('token'));
  const [utente, setUtente] = useState(null);
  const [caricamento, setCaricamento] = useState(Boolean(token));

  useEffect(() => {
    if (!token) {
      setCaricamento(false);
      return;
    }
    api
      .profilo()
      .then(setUtente)
      .catch(() => {
        // Token scaduto o non valido: si torna allo stato "non loggato".
        localStorage.removeItem('token');
        setToken(null);
        setUtente(null);
      })
      .finally(() => setCaricamento(false));
  }, [token]);

  const login = useCallback(async (credenziali) => {
    const risposta = await api.login(credenziali);
    localStorage.setItem('token', risposta.token);
    setToken(risposta.token);
  }, []);

  const registrazione = useCallback(async (dati) => {
    const risposta = await api.registrazione(dati);
    localStorage.setItem('token', risposta.token);
    setToken(risposta.token);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    setToken(null);
    setUtente(null);
  }, []);

  const valore = useMemo(
    () => ({
      utente,
      caricamento,
      autenticato: Boolean(token),
      isAdmin: utente?.ruolo === 'ADMIN' || utente?.ruolo === 'SUPERADMIN',
      isSuperAdmin: utente?.ruolo === 'SUPERADMIN',
      login,
      registrazione,
      logout,
      aggiornaUtente: setUtente,
    }),
    [utente, caricamento, token, login, registrazione, logout],
  );

  return <AuthContext.Provider value={valore}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const contesto = useContext(AuthContext);
  if (!contesto) {
    throw new Error('useAuth va usato dentro AuthProvider');
  }
  return contesto;
}

/**
 * Per le azioni riservate agli utenti loggati tentate da un ospite (es.
 * "aggiungi ai preferiti" dal catalogo pubblico): manda alla pagina di
 * login portando il messaggio da mostrare e la pagina di provenienza, per
 * tornarci dopo l'accesso.
 */
export function useRichiediAccesso() {
  const navigate = useNavigate();
  const location = useLocation();

  return useCallback(
    (messaggio) => {
      navigate('/login', { state: { from: location, messaggio } });
    },
    [navigate, location],
  );
}
