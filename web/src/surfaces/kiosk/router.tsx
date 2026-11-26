import { HashRouter, Routes, Route } from 'react-router'
import { AttractView } from './views/AttractView'
import { QueueView } from './views/QueueView'
import { LeaderboardView } from './views/LeaderboardView'
import { KioskQrPage } from './pages/QrPage/QrPage'
import { LiveDisplayView } from './views/LiveDisplayView'
import { AppShell } from '@ui'
import type { MatchRestClient, MatchSseClient } from '@domain/simulator'
import type { StatsRepository } from '@domain/stats/StatsRepository'

type KioskMatchService = MatchRestClient & MatchSseClient

/**
 * Dedicated, lightweight router for the Kiosk/Totem surface.
 */
export function KioskRouter({
  matches,
  stats,
}: {
  matches: KioskMatchService
  stats: StatsRepository
}) {
  return (
    <HashRouter>
      <AppShell surface="kiosk" title="Arena display" subtitle="Stato del locale e partite in tempo reale">
        <Routes>
          <Route path="/" element={<KioskQrPage />} />
          <Route path="/attract" element={<AttractView />} />
          <Route path="/queue" element={<QueueView />} />
          <Route path="/leaderboard" element={<LeaderboardView stats={stats} />} />
          <Route path="/live" element={<LiveDisplayView matches={matches} />} />
          <Route path="/final" element={<LiveDisplayView matches={matches} />} />
          <Route path="/offline" element={<LiveDisplayView matches={matches} />} />
        </Routes>
      </AppShell>
    </HashRouter>
  )
}
