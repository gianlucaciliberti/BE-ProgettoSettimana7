# Salone Auto

Mini salone di automobili online: catalogo pubblico, preferiti con soglie di prezzo, avvisi via mail, gestione auto per gli admin, pannello di supervisione per il SuperAdmin.

## 🔗 Il sito online

- **Frontend**: https://salone-auto-frontend-r9sy.onrender.com
- **Backend** (health check): https://salone-auto-backend-yvdt.onrender.com/actuator/health

> ⚠️ Il piano gratuito di Render mette in pausa i servizi dopo un periodo di inattività: al primo accesso possono servire **fino a un minuto** prima che il sito risponda. Non è un guasto, basta aspettare e ricaricare.

## Cosa può fare ciascun ruolo

Vedi **[RUOLI-E-COMPORTAMENTO.md](./RUOLI-E-COMPORTAMENTO.md)** per la spiegazione dettagliata di cosa può fare ogni ruolo (Ospite, USER, ADMIN, SUPERADMIN) e come si comporta l'applicazione nei casi particolari (soglie di prezzo, avvisi via mail, accessi non autorizzati, ecc.).

## Stack

- **Backend**: Java 21, Spring Boot 3, Spring Security (JWT), Spring Data JPA, PostgreSQL, invio mail via Gmail SMTP
- **Frontend**: React, Vite, React Router, [motion](https://motion.dev/)
- **Deploy**: Render (Docker per il backend, sito statico per il frontend, Postgres gestito)


