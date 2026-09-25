import { Route, Routes } from 'react-router-dom';
import Navbar from './components/Navbar.jsx';
import { RottaAdmin, RottaAutenticata } from './components/RottaProtetta.jsx';
import { AdminAutoForm, AdminAutoLista } from './pages/AdminAuto.jsx';
import { Preferiti, Profilo } from './pages/AreaUtente.jsx';
import { Login, Registrazione } from './pages/Autenticazione.jsx';
import { Catalogo, DettaglioAuto } from './pages/Catalogo.jsx';
import { CookiePolicy, PrivacyPolicy } from './pages/Policy.jsx';
import './App.css';

export default function App() {
  return (
    <div className="layout">
      <Navbar />
      <main className="main">
        <Routes>
          <Route path="/" element={<Catalogo />} />
          <Route path="/auto/:id" element={<DettaglioAuto />} />
          <Route path="/login" element={<Login />} />
          <Route path="/registrazione" element={<Registrazione />} />
          <Route path="/privacy" element={<PrivacyPolicy />} />
          <Route path="/cookie" element={<CookiePolicy />} />

          <Route
            path="/profilo"
            element={
              <RottaAutenticata>
                <Profilo />
              </RottaAutenticata>
            }
          />
          <Route
            path="/preferiti"
            element={
              <RottaAutenticata>
                <Preferiti />
              </RottaAutenticata>
            }
          />

          <Route
            path="/admin/auto"
            element={
              <RottaAdmin>
                <AdminAutoLista />
              </RottaAdmin>
            }
          />
          <Route
            path="/admin/auto/nuova"
            element={
              <RottaAdmin>
                <AdminAutoForm />
              </RottaAdmin>
            }
          />
          <Route
            path="/admin/auto/:id/modifica"
            element={
              <RottaAdmin>
                <AdminAutoForm />
              </RottaAdmin>
            }
          />
        </Routes>
      </main>
    </div>
  );
}
