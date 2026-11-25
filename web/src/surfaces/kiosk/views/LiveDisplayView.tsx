import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router'
import type { Match, MatchRestClient, MatchSseClient, MatchStreamStatus } from '@domain/simulator'
import { Badge, Card, ThemeToggle } from '@ui'

type MatchService = MatchRestClient & MatchSseClient

export function LiveDisplayView({ matches }: { matches: MatchService }) {
  const [searchParams] = useSearchParams()
  const tableId = searchParams.get('tableId') ?? ''
  const [snapshot, setSnapshot] = useState<{
    tableId: string
    match?: Match
    loading: boolean
    error?: string
  }>({ tableId: '', loading: false })
  const [streamStatus, setStreamStatus] = useState<{
    tableId: string
    status: MatchStreamStatus
  }>({ tableId: '', status: 'disconnected' })

  useEffect(() => {
    let cancelled = false
    let streamUpdated = false
    if (!tableId) return

    let unsubscribe: (() => void) | undefined
    void Promise.resolve()
      .then(() => matches.subscribe(
        tableId,
        (nextMatch) => {
          streamUpdated = true
          if (!cancelled) {
            setSnapshot((current) => ({
              tableId,
              match: nextMatch,
              loading: current.tableId !== tableId || current.loading,
            }))
          }
        },
        (status) => {
          if (!cancelled) setStreamStatus({ tableId, status })
        },
      ))
      .then((dispose) => {
        if (cancelled) dispose()
        else unsubscribe = dispose
      })
      .catch((reason: unknown) => {
        if (!cancelled) {
          setSnapshot({
            tableId,
            loading: false,
            error: reason instanceof Error ? reason.message : 'Connessione live non riuscita',
          })
        }
      })

    void matches.getCurrentMatch(tableId)
      .then((currentMatch) => {
        if (!cancelled) {
          setSnapshot((current) => ({
            tableId,
            match: streamUpdated && current.tableId === tableId
              ? current.match
              : currentMatch,
            loading: false,
            ...(current.error ? { error: current.error } : {}),
          }))
        }
      })
      .catch((reason: unknown) => {
        if (!cancelled) {
          setSnapshot((current) => ({
            tableId,
            match: current.tableId === tableId ? current.match : undefined,
            loading: false,
            error: reason instanceof Error ? reason.message : 'Impossibile caricare la partita',
          }))
        }
      })

    return () => {
      cancelled = true
      unsubscribe?.()
    }
  }, [matches, tableId])

  const loading = Boolean(tableId) && (
    snapshot.tableId !== tableId || snapshot.loading
  )
  const match = snapshot.tableId === tableId ? snapshot.match : undefined
  const error = snapshot.tableId === tableId ? snapshot.error : undefined
  const connection = streamStatus.tableId === tableId
    ? streamStatus.status
    : tableId ? 'connecting' : 'disconnected'
  const final = match?.status === 'COMPLETED' || match?.status === 'CANCELLED'
  const offline = connection !== 'connected'
  const red = match?.sides.find((side) => side.side === 'RED')
  const blue = match?.sides.find((side) => side.side === 'BLUE')

  return (
    <main className="min-h-[78vh] p-6 sm:p-10">
      <header className="flex items-start justify-between gap-4">
        <div>
          <p className="text-xs font-semibold uppercase tracking-[0.14em] text-[var(--cg-color-accent-label)]">
            Partita · {tableId || 'nessun tavolo'}
          </p>
          <h1 className="mt-2 text-3xl font-bold sm:text-5xl">{final ? 'Finale' : 'In diretta'}</h1>
        </div>
        <div className="flex items-center gap-3">
          <Badge tone={offline ? 'warning' : final ? 'neutral' : 'success'}>
            {connection === 'connecting' ? 'Connessione…' : connection === 'retrying' ? 'Riconnessione…' : connection === 'error' ? 'Errore stream' : connection === 'disconnected' ? 'Disconnesso' : final ? 'Terminata' : 'LIVE'}
          </Badge>
          <ThemeToggle />
        </div>
      </header>

      <Card elevated className="mx-auto mt-12 max-w-4xl p-6 sm:mt-16 sm:p-14">
        {loading ? (
          <p role="status" className="text-center text-sm text-[var(--cg-color-label-secondary)]">Caricamento partita…</p>
        ) : error ? (
          <p role="alert" className="text-center text-sm text-[var(--cg-color-status-warning)]">{error}</p>
        ) : !tableId ? (
          <p className="text-center text-sm text-[var(--cg-color-label-secondary)]">Seleziona un tavolo aggiungendo `?tableId=…` all’URL.</p>
        ) : !match ? (
          <p className="text-center text-sm text-[var(--cg-color-label-secondary)]">Nessuna partita attiva per questo tavolo.</p>
        ) : (
          <>
            <p className="text-center text-sm text-[var(--cg-color-label-secondary)]">
              {match.mode === 'TEAM' ? 'Squadre' : 'Giocatori'} · Tavolo {match.tableId}
            </p>
            <div className="mt-10 grid grid-cols-[1fr_auto_1fr] items-center gap-3 text-center sm:gap-12">
              <div>
                <p className="text-lg font-semibold sm:text-3xl">{red?.displayName ?? 'Rosso'}</p>
                <strong className="mt-3 block text-7xl tracking-tight sm:text-9xl">{red?.score ?? 0}</strong>
              </div>
              <span className="text-3xl text-[var(--cg-color-label-tertiary)]">—</span>
              <div>
                <p className="text-lg font-semibold sm:text-3xl">{blue?.displayName ?? 'Blu'}</p>
                <strong className="mt-3 block text-7xl tracking-tight sm:text-9xl">{blue?.score ?? 0}</strong>
              </div>
            </div>
            <div className="mt-8 flex justify-center">
              <Badge tone={final ? 'neutral' : 'blue'}>
                {final ? 'Partita conclusa' : `Primo a ${match.pointsToWin} punti`}
              </Badge>
            </div>
          </>
        )}
      </Card>
      <Link to="/" className="mx-auto mt-8 block w-fit text-sm text-[var(--cg-color-accent-label)]">
        ← Torna alla schermata iniziale
      </Link>
    </main>
  )
}
