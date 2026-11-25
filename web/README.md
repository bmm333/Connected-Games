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

- `VITE_API_BASE_URL`: origine/prefisso REST per Catalog e Match; vuoto usa
  l’origine della pagina. I path `/api/catalog` e `/api/match` derivano dai
  contratti.
- `VITE_SSE_URL`: origine/prefisso del servizio Match SSE; se omesso si usa
  `VITE_API_BASE_URL`. Il client compone `/api/match/tables/{tableId}/live`.
- `VITE_MQTT_URL`: URL WebSocket del broker, necessario quando si connette il
  simulatore Console in modalità reale.
- `VITE_USE_MOCKS`: `true` seleziona gli adattatori locali; `false` seleziona i
  client REST, SSE e MQTT. Valori diversi generano un errore di configurazione.

Le richieste REST hanno timeout e un retry limitato per errori di rete, HTTP 5xx
e 429. EventSource usa la riconnessione nativa del browser e ne espone lo stato;
MQTT usa QoS/topic definiti dai contratti e segnala errori di connessione.
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
