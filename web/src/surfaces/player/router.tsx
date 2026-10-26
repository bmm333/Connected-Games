import { HashRouter, Routes, Route } from 'react-router'
import { LobbyView } from './views/LobbyView'
import { MatchView } from './views/MatchView'
import { ProfileView } from './views/ProfileView'
import { TableLookupView } from './views/TableLookupView'
import { PlayerQrPage } from './pages/QrPage/QrPage'
import { StatisticsView } from './views/StatisticsView'
import { GuestSaveView } from './views/GuestSaveView'
import { AppShell } from '@ui'

/**
 * Dedicated, lightweight router for the Player surface.
 * Uses HashRouter for robust behavior in Capacitor WebViews & PWA offline states.
 */
export function PlayerRouter() {
  return (
    <HashRouter>
      <AppShell surface="player" title="Player arena" subtitle="Gioca, controlla i risultati e migliora il tuo punteggio">
        <Routes>
          <Route path="/" element={<PlayerQrPage />} />
          <Route path="/lobby" element={<LobbyView />} />
          <Route path="/match/:id" element={<MatchView />} />
          <Route path="/profile" element={<ProfileView />} />
          <Route path="/statistics" element={<StatisticsView />} />
          <Route path="/guest/save" element={<GuestSaveView />} />
          <Route path="/table/:code" element={<TableLookupView />} />
        </Routes>
      </AppShell>
    </HashRouter>
  )
}
