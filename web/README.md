# Connected Games Web

Applicazione React + TypeScript + Vite con superfici Player, Kiosk e Console.
L’accesso a Catalog, Match e MQTT è configurato da un unico factory in
`src/shared/communication`; le viste usano interfacce e non importano i mock.

## Avvio in modalità mock

La modalità mock è attiva per default (`VITE_USE_MOCKS=true`) e usa fixture locali
per Catalog e Match, un flusso SSE simulato e un publisher MQTT in memoria. Per
renderla esplicita, crea `web/.env.local` partendo da `.env.example` e imposta:

```dotenv
VITE_USE_MOCKS=true
```

Avvia la superficie richiesta:

```sh
npm run dev:player
npm run dev:kiosk
npm run dev:console
```

La fixture demo del catalogo contiene il codice `DEMO01` e il tavolo
`018f3f8d-9b22-7a3a-8c5b-1e2a4c6d8f13`. Per visualizzarne il punteggio live:

```text
http://localhost:5174/#/live?tableId=018f3f8d-9b22-7a3a-8c5b-1e2a4c6d8f13
```

## Avvio in modalità reale

Imposta gli URL nel file `.env.local` (non inserire credenziali o segreti nelle
variabili `VITE_*`: sono incluse nel bundle client):

```dotenv
VITE_USE_MOCKS=false
VITE_API_BASE_URL=https://api.example.test
VITE_SSE_URL=https://api.example.test
VITE_MQTT_URL=wss://broker.example.test/mqtt
```

### Servizi locali del repository

Per lo sviluppo locale, il Vite dev server può inoltrare Catalog e Match ai
servizi Spring direttamente, evitando il gateway:

```dotenv
VITE_USE_MOCKS=false
VITE_API_BASE_URL=
VITE_SSE_URL=
VITE_MQTT_URL=ws://127.0.0.1:9001
CATALOG_PROXY_TARGET=http://127.0.0.1:8102
MATCH_PROXY_TARGET=http://127.0.0.1:8103
```

Le due variabili `*_PROXY_TARGET` sono lette solo dalla configurazione Vite del
server di sviluppo; non vengono esposte al bundle. Se non hai ancora un `.env`
nella root del repository, crealo da `.env.example` e imposta in
`POSTGRES_HOST_PORT` una porta libera (per esempio `5433` se `5432` è occupata).
Poi avvia database e broker dal root:

```powershell
Copy-Item .env.example .env
docker compose up -d
```

In due terminali distinti, dalla directory `services/`, avvia Catalog e Match:

```powershell
.\gradlew.bat :catalog:bootRun --args="--spring.profiles.active=local"
```

```powershell
.\gradlew.bat :match:bootRun
```

Il profilo `local` del Catalog carica i dati demo persistiti nel database. Questo
è un backend reale, ma i record sono dati dimostrativi, non dati di produzione.
Il servizio Match non pre-semina partite attive.

- `VITE_API_BASE_URL`: origine/prefisso REST per Catalog e Match; vuoto usa
  l’origine della pagina. I path `/api/catalog` e `/api/match` derivano dai
  contratti.
- `VITE_SSE_URL`: origine/prefisso del servizio Match SSE; se omesso si usa
  `VITE_API_BASE_URL`. Il client compone `/api/match/tables/{tableId}/live`.
- `VITE_MQTT_URL`: URL WebSocket del broker, necessario quando si connette il
  simulatore Console in modalità reale.
- `VITE_USE_MOCKS`: `true` seleziona gli adattatori locali; `false` seleziona i
  client REST, SSE e MQTT. Valori diversi generano un errore di configurazione.
- `CATALOG_PROXY_TARGET` e `MATCH_PROXY_TARGET`: target opzionali per il dev
  server Vite quando si eseguono i servizi Spring locali senza gateway.

Le richieste REST hanno timeout e un retry limitato per errori di rete, HTTP 5xx
e 429. Match REST e Stats inviano il bearer token della sessione attiva; in caso
di 401 tentano il refresh con rotazione del refresh token e ritentano la richiesta.
Lo stream Match usa `fetch` con `Authorization` e riconnessione applicativa, così
anche SSE funziona con gli endpoint protetti. Stats implementa leaderboard,
riepilogo partecipante, partite del partecipante, risultato partita e riepilogo
venue. MQTT usa QoS/topic definiti dai contratti e segnala errori di connessione.
Payload non validi vengono scartati e registrati in console.

## Test e build

```sh
npm test
npm run lint
npm run build
npm run build:surfaces
```

## Limiti noti

- I mock configurabili coprono Catalog, Match REST/SSE e il publisher MQTT del
  simulatore. Identity, autenticazione e API Console rimangono reali; quindi
  `VITE_USE_MOCKS=true` non rende ogni flusso autenticato completamente offline.
  Questi servizi continuano a usare le variabili esistenti
  `VITE_AUTH_API_URL`, `VITE_IDENTITY_API_URL` e `VITE_CONSOLE_API_URL`.
- La schermata Player `/match/:id` conserva il suo punteggio locale: il route
  fornisce un `id` non qualificato e l’API Match è read-only. Non viene trattato
  come `tableId` né usato per pubblicare comandi non previsti dai contratti.
- `VITE_SSE_URL` è un’origine/prefisso, non un URL completo con `tableId`.
- Il broker MQTT deve essere accessibile dal browser via WebSocket. Non vengono
  configurate credenziali client nel frontend; l’autenticazione basata su
  certificati per hub/cloud va gestita da un client/broker adatto e non è
  implementata qui.
- EventSource gestisce automaticamente la riconnessione, senza backoff
  configurabile dall’applicazione.
