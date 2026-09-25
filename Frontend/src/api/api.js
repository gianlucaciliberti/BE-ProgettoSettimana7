// Ogni chiamata al backend passa da qui: un solo posto che sa la base URL,
// aggiunge il token e traduce gli errori HTTP in eccezioni leggibili.

const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export class ApiError extends Error {
  constructor(status, message) {
    super(message);
    this.status = status;
  }
}

function tokenHeader() {
  const token = localStorage.getItem('token');
  return token ? { Authorization: `Bearer ${token}` } : {};
}

async function request(path, { method = 'GET', body, query } = {}) {
  const url = new URL(path, BASE_URL);
  if (query) {
    Object.entries(query)
      .filter(([, value]) => value !== undefined && value !== null && value !== '')
      .forEach(([chiave, valore]) => url.searchParams.set(chiave, valore));
  }

  const risposta = await fetch(url, {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...tokenHeader(),
    },
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  if (!risposta.ok) {
    let messaggio = `Errore ${risposta.status}`;
    try {
      const dati = await risposta.json();
      messaggio = dati.message || dati.error || messaggio;
    } catch {
      // Corpo non JSON (o assente): resta il messaggio generico.
    }
    throw new ApiError(risposta.status, messaggio);
  }

  if (risposta.status === 204) {
    return null;
  }
  return risposta.json();
}

export const api = {
  // Auth / profilo
  registrazione: (dati) => request('/api/auth/registrazione', { method: 'POST', body: dati }),
  login: (dati) => request('/api/auth/login', { method: 'POST', body: dati }),
  profilo: () => request('/api/auth/profilo'),
  aggiornaProfilo: (dati) => request('/api/auth/profilo', { method: 'PUT', body: dati }),
  eliminaAccount: () => request('/api/auth/profilo', { method: 'DELETE' }),

  // Supervisione utenti (solo SUPERADMIN)
  utentiLista: () => request('/api/auth/utenti'),
  utenteCambiaRuolo: (id, ruolo) => request(`/api/auth/utenti/${id}/ruolo`, { method: 'PATCH', body: { ruolo } }),

  // Catalogo auto
  catalogo: (query) => request('/api/auto', { query }),
  autoDettaglio: (id) => request(`/api/auto/${id}`),

  // Gestione auto (admin)
  autoAdminLista: () => request('/api/auto/admin'),
  autoAdminDettaglio: (id) => request(`/api/auto/admin/${id}`),
  autoCrea: (dati) => request('/api/auto', { method: 'POST', body: dati }),
  autoAggiorna: (id, dati) => request(`/api/auto/${id}`, { method: 'PUT', body: dati }),
  autoCambiaPrezzo: (id, dati) => request(`/api/auto/${id}/prezzo`, { method: 'PATCH', body: dati }),
  autoElimina: (id) => request(`/api/auto/${id}`, { method: 'DELETE' }),

  // Preferiti e avvisi
  preferitiLista: () => request('/api/preferiti'),
  preferitoAggiungi: (autoId) => request('/api/preferiti', { method: 'POST', body: { autoId } }),
  preferitoRimuovi: (id) => request(`/api/preferiti/${id}`, { method: 'DELETE' }),
  /** 404 (ApiError) se l'auto non è (ancora) tra i preferiti dell'utente loggato. */
  preferitoPerAuto: (autoId) => request(`/api/preferiti/per-auto/${autoId}`),
  avvisiLista: (preferitoId) => request(`/api/preferiti/${preferitoId}/avvisi`),
  avvisoCrea: (preferitoId, sogliaPrezzo) =>
    request(`/api/preferiti/${preferitoId}/avvisi`, { method: 'POST', body: { sogliaPrezzo } }),
  avvisoAggiorna: (id, sogliaPrezzo) =>
    request(`/api/avvisi/${id}`, { method: 'PUT', body: { sogliaPrezzo } }),
  avvisoRimuovi: (id) => request(`/api/avvisi/${id}`, { method: 'DELETE' }),
};
