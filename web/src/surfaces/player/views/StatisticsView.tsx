import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router'
import { Card, ThemeToggle } from '@ui'
import { getPlayerDependencies } from '../composition-root'

const dependencies = getPlayerDependencies()

export function StatisticsView() {
  const sessionQuery = useQuery({
    queryKey: ['auth', 'session'],
    queryFn: () => dependencies.auth.getStoredSession(),
  })

  const participantId = sessionQuery.data?.id

  const summaryQuery = useQuery({
    queryKey: ['stats', 'participant', participantId],
    queryFn: () => dependencies.stats.getParticipantSummary(participantId!),
    enabled: Boolean(participantId),
  })

  const summary = summaryQuery.data
  const winRate = summary && summary.matches > 0
    ? Math.round((summary.wins / summary.matches) * 100)
    : 0

  return (
    <div className="cg-mobile-page space-y-8">
      <header className="flex items-start justify-between gap-4">
        <div>
          <Link to="/lobby" className="text-sm font-semibold text-[var(--cg-color-accent-label)]">← Lobby</Link>
          <p className="mt-4 text-xs font-semibold uppercase tracking-[0.14em] text-[var(--cg-color-accent-label)]">Il tuo rendimento</p>
          <h1 className="mt-1 text-3xl font-bold">Le tue statistiche</h1>
          <p className="mt-1 text-sm text-[var(--cg-color-label-secondary)]">Segui i tuoi progressi e i risultati ufficiali.</p>
        </div>
        <ThemeToggle />
      </header>

      {!participantId && !sessionQuery.isPending ? (
        <Card elevated>
          <p className="text-sm text-[var(--cg-color-label-secondary)]">Accedi o scansiona un tavolo per visualizzare le statistiche.</p>
        </Card>
      ) : summaryQuery.isPending ? (
        <Card elevated><p className="text-sm text-[var(--cg-color-label-secondary)]">Caricamento statistiche…</p></Card>
      ) : summaryQuery.isError ? (
        <Card elevated><p role="alert" className="text-sm text-[var(--cg-color-status-warning)]">Impossibile caricare le statistiche.</p></Card>
      ) : !summary ? (
        <Card elevated><p className="text-sm text-[var(--cg-color-label-secondary)]">Nessuna partita completata finora.</p></Card>
      ) : (
        <>
          <div className="grid gap-4 sm:grid-cols-3">
            <Card elevated>
              <p className="text-sm text-[var(--cg-color-label-secondary)]">Partite giocate</p>
              <p className="mt-2 text-4xl font-bold">{summary.matches}</p>
              <p className="mt-2 text-sm text-[var(--cg-color-label-secondary)]">{summary.draws} pareggi</p>
            </Card>
            <Card>
              <p className="text-sm text-[var(--cg-color-label-secondary)]">Vittorie</p>
              <p className="mt-2 text-3xl font-bold">{summary.wins}</p>
              <p className="mt-2 text-sm text-[var(--cg-color-label-secondary)]">{winRate}% delle partite</p>
            </Card>
            <Card>
              <p className="text-sm text-[var(--cg-color-label-secondary)]">Punti fatti / subiti</p>
              <p className="mt-2 text-3xl font-bold">{summary.pointsFor} <span className="text-base font-medium">/ {summary.pointsAgainst}</span></p>
              <p className="mt-2 text-sm text-[var(--cg-color-label-secondary)]">Sconfitte: {summary.losses}</p>
            </Card>
          </div>

          {summary.byGameType.length > 0 && (
            <Card>
              <h2 className="font-semibold">Per tipologia di gioco</h2>
              <div className="mt-4 space-y-3">
                {summary.byGameType.map((gt) => (
                  <div key={gt.gameTypeId} className="flex items-center justify-between text-sm border-b border-[var(--cg-color-border-subtle)] pb-2 last:border-0">
                    <span className="font-mono text-xs">{gt.gameTypeId}</span>
                    <span>{gt.wins}V / {gt.losses}P ({gt.matches} totali)</span>
                  </div>
                ))}
              </div>
            </Card>
          )}
        </>
      )}
    </div>
  )
}
