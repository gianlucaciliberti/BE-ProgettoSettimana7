# Ruoli e comportamento dell'applicazione

Questo documento spiega chi può fare cosa nel Salone Auto, e come si comporta l'app nei casi meno ovvi: soglie di prezzo, avvisi via mail, accessi non autorizzati.

## I ruoli

Ci sono quattro livelli, dal meno al più privilegiato:

| | Ospite | USER | ADMIN | SUPERADMIN |
|---|---|---|---|---|
| Sfogliare il catalogo pubblico | ✅ | ✅ | ✅ | ✅ |
| Registrarsi / accedere | — | ✅ | ✅ | ✅ |
| Aggiungere auto ai preferiti | ❌ | ✅ | ✅ | ✅ |
| Fissare soglie di prezzo | ❌ | ✅ | ✅ | ✅ |
| Modificare il proprio profilo | ❌ | ✅ | ✅ | ✅ |
| Eliminare il proprio account | ❌ | ✅ | ✅ | ✅ |
| Creare/pubblicare auto | ❌ | ❌ | ✅ (solo le proprie) | ✅ (tutte) |
| Vedere le bozze e il prezzo d'acquisto | ❌ | ❌ | ✅ (solo le proprie) | ✅ (tutte) |
| Modificare/eliminare auto altrui | ❌ | ❌ | ❌ | ✅ |
| Vedere l'elenco di tutti gli utenti | ❌ | ❌ | ❌ | ✅ |
| Promuovere/retrocedere USER ↔ ADMIN | ❌ | ❌ | ❌ | ✅ |

Il **SUPERADMIN** è un account unico e fittizio (utente "Amministratore Sistema"), creato in automatico all'avvio dell'applicazione leggendo `ADMIN_EMAIL`/`ADMIN_PASSWORD`. Non si crea registrandosi, e non si può assegnare ad altri dal pannello di supervisione: quel campo accetta solo USER e ADMIN.

---

## Ospite (nessun accesso)

Un visitatore senza account può:
- sfogliare il catalogo (solo le auto **pubblicate**, mai le bozze)
- cercare e ordinare i risultati
- aprire la pagina di dettaglio di un'auto e vedere tutte le foto

Se prova ad **aggiungere un'auto ai preferiti**, l'azione non parte: viene portato alla pagina di login con un banner ("Devi accedere all'account per aggiungere questa auto ai preferiti") e un link diretto alla registrazione. Una volta effettuato l'accesso (o la registrazione), torna automaticamente alla pagina da cui era partito — non deve ricominciare la navigazione da capo.

## USER (utente registrato)

Oltre a tutto ciò che può fare un ospite:

- **Preferiti**: può aggiungere/rimuovere auto dai preferiti (solo tra quelle pubblicate — una bozza non è nemmeno raggiungibile). Non può aggiungere due volte la stessa auto: se ci prova, l'app la tratta semplicemente come "già salvata".
- **Soglie di prezzo**: su ogni auto nei preferiti può fissare **più soglie contemporaneamente** (es. "avvisami sotto 20.000€" e, separatamente, "avvisami sotto 18.000€"). Ogni soglia è indipendente e **a perdere**: una volta scattata (l'auto è scesa sotto quel prezzo), quella soglia specifica ha esaurito la sua funzione per sempre, anche se il prezzo dovesse poi risalire e riscendere sotto lo stesso valore. Per un'altra notifica sulla stessa auto serve fissare una nuova soglia. Una soglia già scattata non si può più modificare (l'app risponde con un errore se si prova); una soglia non ancora scattata invece sì.
- **Profilo**: può cambiare nome e cognome (non l'email, non la password — non c'è ancora una funzione per quello).
- **Eliminazione account**: cancella anche tutti i suoi preferiti e le sue soglie. Da quel momento in poi non può più arrivare nessuna mail legata a quell'account, nemmeno per soglie che sarebbero scattate dopo.

## ADMIN

Oltre a tutto ciò che può fare uno USER (un ADMIN è anche un utente normale: può avere preferiti e soglie come chiunque altro):

- **Crea auto**: marca, modello, descrizione, prezzo di vendita, prezzo d'acquisto, foto. Una nuova auto nasce sempre in **bozza**.
- **Pubblica/ritira**: passa un'auto da bozza a pubblicata (visibile a tutti) e viceversa.
- **Modifica**: dati, prezzo, foto — ma solo delle auto che ha creato lui. Se prova a modificare o eliminare un'auto creata da un **altro** ADMIN, l'operazione viene rifiutata (errore 403), e nella gestione auto non la vede nemmeno elencata.
- **Vede il prezzo d'acquisto** delle proprie auto — un dato che il catalogo pubblico non mostra mai.
- **Cambia prezzo**: questa è l'azione che può far scattare gli avvisi degli utenti che hanno quell'auto nei preferiti (vedi sotto).

## SUPERADMIN

Oltre a tutto ciò che può fare un ADMIN, ma **senza il limite della proprietà**: può vedere, modificare, pubblicare ed eliminare le auto create da qualsiasi ADMIN, bozze comprese.

In più, dal pannello di **Supervisione** (unica sezione riservata a questo ruolo):
- vede l'elenco di tutti gli utenti registrati, con ruolo ed email
- può promuovere un USER ad ADMIN, o riportare un ADMIN a USER
- vede tutte le auto raggruppate per amministratore, con il conteggio di quante sono pubblicate e quante ancora in bozza

Non può promuovere nessuno a SUPERADMIN, né modificare l'account SUPERADMIN stesso da questo pannello: è protetto perché ce n'è uno solo.

---

## Come funziona l'avviso di prezzo, nel dettaglio

1. Un ADMIN cambia il prezzo di un'auto (o la modifica cambiando anche il prezzo).
2. L'applicazione controlla tutte le soglie **non ancora scattate** fissate su quell'auto da qualunque utente.
3. Una soglia scatta solo se il prezzo **attraversa** quella soglia in questo preciso cambiamento: prima era sopra, ora è arrivato uguale o sotto. Se il prezzo era già sotto la soglia (ad esempio l'utente ha fissato la soglia quando l'auto costava già meno), non scatta nulla finché non risale sopra e poi riscende — la soglia reagisce solo ai movimenti futuri, non allo stato in cui si trovava quando è stata creata.
4. Se scatta, parte una mail all'utente, con il link dell'auto e un link per **disattivare quella specifica soglia** (un token casuale, non serve essere loggati per usarlo — basta cliccare dalla mail).
5. La mail parte solo dopo che il salvataggio del nuovo prezzo è andato a buon fine, e non blocca l'admin: se Gmail è lento o irraggiungibile, il salvataggio del prezzo non ne risente.
6. Ogni soglia manda **una e una sola volta**: anche in caso di più modifiche di prezzo ravvicinate, non si rischia il doppione.

## Cosa NON succede

Per completezza, alcuni comportamenti scelti deliberatamente:

- Un utente che tenta di modificare/vedere i preferiti o gli avvisi di **un altro utente** (indovinando o cambiando un numero nell'indirizzo) riceve "non trovato", non "non autorizzato" — così non sa nemmeno se quella risorsa esiste.
- Nessuno può scegliersi il ruolo in fase di registrazione: si parte sempre come USER, la promozione la decide solo il SUPERADMIN.
- Cambiare il prezzo di un'auto è un'azione riservata agli ADMIN/SUPERADMIN: un USER che ci provasse (anche forzando la richiesta) riceve un rifiuto, non un errore generico.
