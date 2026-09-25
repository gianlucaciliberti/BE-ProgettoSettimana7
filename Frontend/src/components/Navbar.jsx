import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

// Stili in App.css: Navbar è sempre montato dentro App, che lo importa già.

export default function Navbar() {
  const { autenticato, isAdmin, isSuperAdmin, utente, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/');
  }

  return (
    <header className="navbar">
      <Link to="/" className="navbar__brand">
        Salone<span>Auto</span>
      </Link>
      <nav className="navbar__links">
        <Link to="/">Catalogo</Link>
        {autenticato && !isAdmin && <Link to="/preferiti">Preferiti</Link>}
        {isAdmin && <Link to="/admin/auto">Gestione auto</Link>}
        {isSuperAdmin && <Link to="/admin/supervisione">Supervisione</Link>}
        {autenticato ? (
          <>
            <Link to="/profilo">{utente?.nome ?? 'Profilo'}</Link>
            {utente?.ruolo && utente.ruolo !== 'USER' && <span className="badge-ruolo">{utente.ruolo}</span>}
            <button type="button" onClick={handleLogout}>
              Esci
            </button>
          </>
        ) : (
          <>
            <Link to="/login">Accedi</Link>
            <Link to="/registrazione">Registrati</Link>
          </>
        )}
      </nav>
    </header>
  );
}
