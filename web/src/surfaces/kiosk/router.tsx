import { HashRouter, Routes, Route } from 'react-router'
import { AttractView } from './views/AttractView'
import { QueueView } from './views/QueueView'
import { LeaderboardView } from './views/LeaderboardView'
import { KioskQrPage } from './pages/QrPage/QrPage'
import { LiveDisplayView } from './views/LiveDisplayView'
import { AppShell } from '@ui'

/**
 * Dedicated, lightweight router for the Kiosk/Totem surface.
 */
export function KioskRouter() {
  return (
    <HashRouter>
      <AppShell surface="kiosk" title="Arena display" subtitle="Stato del locale e partite in tempo reale">
        <Routes>
          <Route path="/" element={<KioskQrPage />} />
          <Route path="/attract" element={<AttractView />} />
          <Route path="/queue" element={<QueueView />} />
          <Route path="/leaderboard" element={<LeaderboardView />} />
          <Route path="/live" element={<LiveDisplayView />} />
          <Route path="/final" element={<LiveDisplayView />} />
          <Route path="/offline" element={<LiveDisplayView />} />
        </Routes>
      </AppShell>
    </HashRouter>
  )
}
