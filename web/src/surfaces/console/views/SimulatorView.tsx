import { useEffect, useState } from 'react'
import type {
  MatchEventEnvelope,
  MatchSide,
  SimulatorTable,
} from '@domain/simulator'
import { Badge, Card } from '@ui'
import { useConsoleDependencies } from '../dependencies'
import { AdminSectionShell } from './AdminSectionShell'

export function SimulatorView() {
  const { simulator } = useConsoleDependencies()
  const [venues, setVenues] = useState<readonly { id: string; name: string; city: string }[]>([])
  const [venueId, setVenueId] = useState('')
  const [tables, setTables] = useState<readonly SimulatorTable[]>([])
  const [activeTables, setActiveTables] = useState<ReadonlySet<string>>(new Set())
  const [events, setEvents] = useState<readonly MatchEventEnvelope[]>([])
  const [acks, setAcks] = useState<readonly string[]>([])
  const [mqttStatus, setMqttStatus] = useState<'offline' | 'connecting' | 'connected' | 'error'>('offline')
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    void simulator.listVenues()
      .then((loadedVenues) => {
        if (cancelled) return
        setVenues(loadedVenues)
        setVenueId(loadedVenues[0]?.id ?? '')
      })
      .catch((reason: unknown) => {
        if (!cancelled) setError(reason instanceof Error ? reason.message : 'Impossibile caricare i locali')
      })
    return () => {
      cancelled = true
    }
  }, [simulator])

  useEffect(() => {
    if (!venueId) {
      return
    }
    let cancelled = false
    void simulator.listTables(venueId)
      .then((loadedTables) => {
        if (!cancelled) setTables(loadedTables)
      })
      .catch((reason: unknown) => {
        if (!cancelled) setError(reason instanceof Error ? reason.message : 'Impossibile caricare i tavoli')
      })
    return () => {
      cancelled = true
    }
  }, [simulator, venueId])

  async function connectMqtt() {
    setError(null)
    setMqttStatus('connecting')
    try {
      await simulator.connect()
      if (venueId) {
        await simulator.subscribeAcks(venueId, (payload) => {
          setAcks((current) => [JSON.stringify(payload), ...current].slice(0, 8))
        })
      }
      setMqttStatus('connected')
    } catch (reason) {
      setMqttStatus('error')
      setError(reason instanceof Error ? reason.message : 'Connessione MQTT non riuscita')
    }
  }

  async function publish(action: () => Promise<MatchEventEnvelope | readonly MatchEventEnvelope[]>) {
    setError(null)
    try {
      const result = await action()
      const published = Array.isArray(result) ? result : [result]
      setEvents((current) => [...published.toReversed(), ...current].slice(0, 12))
      if (published.some((event) => event.type === 'MatchEnded')) {
        setActiveTables((current) => {
          const next = new Set(current)
          published.forEach((event) => next.delete(event.gameId))
          return next
        })
      }
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Pubblicazione evento non riuscita')
    }
  }

  function startMatch(table: SimulatorTable) {
    void publish(async () => {
      const event = await simulator.startMatch(table)
      setActiveTables((current) => new Set(current).add(table.table.id))
      return event
    })
  }

  function scoreGoal(tableId: string, side: MatchSide) {
    void publish(() => simulator.scoreGoal(tableId, side))
  }

  function endMatch(tableId: string) {
    void publish(async () => {
      const event = await simulator.endMatch(tableId, 'CANCELLED')
      setActiveTables((current) => {
        const next = new Set(current)
        next.delete(tableId)
        return next
      })
      return event
    })
  }

  return (
    <AdminSectionShell title="Match Simulator" description="Simula un hub e pubblica gli eventi di partita definiti nei contratti AsyncAPI.">
      <Card elevated className="space-y-4">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <h2 className="text-lg font-bold">Connessione hub</h2>
            <p className="text-sm text-[var(--cg-color-label-secondary)]">Gli ack vengono ascoltati sul topic del locale selezionato.</p>
          </div>
          <div className="flex items-center gap-2">
            <Badge tone={mqttStatus === 'connected' ? 'success' : mqttStatus === 'error' ? 'warning' : 'neutral'}>{mqttStatus}</Badge>
            <button type="button" className="cg-button cg-button-secondary text-sm" onClick={() => void connectMqtt()} disabled={mqttStatus === 'connecting'}>
              {mqttStatus === 'connecting' ? 'Connessione…' : 'Connetti MQTT'}
            </button>
          </div>
        </div>
        <label className="block text-sm font-semibold">
          Locale
          <select className="mt-2 block w-full rounded-lg border border-[var(--cg-color-border-subtle)] bg-[var(--cg-color-bg-grouped)] p-2" value={venueId} onChange={(event) => setVenueId(event.target.value)}>
            {venues.map((venue) => <option key={venue.id} value={venue.id}>{venue.name} · {venue.city}</option>)}
          </select>
        </label>
      </Card>

      <Card elevated className="space-y-4">
        <div>
          <h2 className="text-lg font-bold">Tavoli dal catalogo</h2>
          <p className="text-sm text-[var(--cg-color-label-secondary)]">La lista proviene dall’API Catalog; il simulatore non definisce più dispositivi locali.</p>
        </div>
        {!venueId || tables.length === 0 ? <p className="rounded-xl bg-[var(--cg-color-bg-grouped)] p-4 text-sm text-[var(--cg-color-label-secondary)]">Nessun tavolo disponibile.</p> : <div className="grid gap-3 sm:grid-cols-2">{tables.map((table) => {
          const active = activeTables.has(table.table.id)
          return <div key={table.table.id} className="rounded-xl bg-[var(--cg-color-bg-grouped)] p-4">
            <div className="flex items-start justify-between gap-3">
              <div><p className="font-semibold">{table.table.name}</p><p className="text-xs text-[var(--cg-color-label-tertiary)]">{table.gameType.name} · {table.table.status}</p></div>
              <Badge tone={active ? 'success' : 'neutral'}>{active ? 'match live' : 'disponibile'}</Badge>
            </div>
            <div className="mt-3 flex flex-wrap gap-2">
              {!active && <button type="button" className="cg-button cg-button-primary text-sm" onClick={() => startMatch(table)}>Start match</button>}
              {active && <><button type="button" className="cg-button cg-button-secondary text-sm" onClick={() => scoreGoal(table.table.id, 'RED')}>Goal red</button><button type="button" className="cg-button cg-button-secondary text-sm" onClick={() => scoreGoal(table.table.id, 'BLUE')}>Goal blue</button><button type="button" className="cg-button cg-button-secondary text-sm" onClick={() => endMatch(table.table.id)}>Cancel match</button></>}
            </div>
          </div>
        })}</div>}
      </Card>

      <div className="grid gap-4 lg:grid-cols-2">
        <Card elevated className="space-y-3">
          <h2 className="text-lg font-bold">Eventi pubblicati</h2>
          {events.length === 0 ? <p className="text-sm text-[var(--cg-color-label-secondary)]">Nessun evento pubblicato.</p> : <ul className="space-y-2">{events.map((event) => <li key={event.eventId} className="rounded-xl bg-[var(--cg-color-bg-grouped)] p-3 text-xs"><code>{event.type} · seq {event.seq}</code><pre className="mt-2 overflow-x-auto">{JSON.stringify(event)}</pre></li>)}</ul>}
        </Card>
        <Card elevated className="space-y-3">
          <h2 className="text-lg font-bold">Ack hub</h2>
          {acks.length === 0 ? <p className="text-sm text-[var(--cg-color-label-secondary)]">Nessun ack ricevuto.</p> : <ul className="space-y-2">{acks.map((ack, index) => <li key={`${ack}-${index}`} className="rounded-xl bg-[var(--cg-color-bg-grouped)] p-3 text-xs"><pre className="overflow-x-auto">{ack}</pre></li>)}</ul>}
        </Card>
      </div>
      {error && <p role="alert" className="rounded-xl border border-[var(--cg-color-status-warning)]/30 bg-[var(--cg-color-status-warning)]/10 p-3 text-sm text-[var(--cg-color-status-warning)]">{error}</p>}
    </AdminSectionShell>
  )
}
