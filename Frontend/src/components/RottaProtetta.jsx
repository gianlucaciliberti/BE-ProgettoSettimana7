import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

/** Richiede solo che l'utente sia loggato. */
export function RottaAutenticata({ children }) {
  const { autenticato, caricamento } = useAuth();
  if (caricamento) return null;
  return autenticato ? children : <Navigate to="/login" replace />;
}

/**
 * Richiede ADMIN o SUPERADMIN. Il controllo vero però resta sempre sul
 * backend (qui è solo UX: nasconde pagine che l'API rifiuterebbe comunque).
 */
export function RottaAdmin({ children }) {
  const { autenticato, isAdmin, caricamento } = useAuth();
  if (caricamento) return null;
  if (!autenticato) return <Navigate to="/login" replace />;
  return isAdmin ? children : <Navigate to="/" replace />;
}
