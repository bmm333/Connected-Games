import { useMemo, useState } from 'react'
import { Badge, Card } from '@ui'
import type { Actuator, MqttMessage, SimulationDatasetName } from '@domain/simulator'
import type { createMqttClientService } from '@infrastructure/realtime/MqttClientService'
import { AdminSectionShell } from './AdminSectionShell'

type DatasetTab = SimulationDatasetName

const datasetLabels: Record<DatasetTab, string> = {
  devices: 'Dispositivi',
  telemetry: 'Telemetria',
  'scheduled-events': 'Eventi schedulati',
  'system-config': 'Configurazione',
}

const initialRows: Record<DatasetTab, readonly Record<string, string>[]> = {
  devices: [
    { id: 'table-02', name: 'Tavolo 02', type: 'foosball', status: 'online', lastSeen: '20:24' },
    { id: 'kiosk-01', name: 'Kiosk sala', type: 'display', status: 'online', lastSeen: '20:24' },
  ],
  telemetry: [
    { id: 'tel-100', deviceId: 'table-02', metric: 'score.home', value: '7', recordedAt: '20:24' },
    { id: 'tel-099', deviceId: 'table-02', metric: 'latency', value: '18 ms', recordedAt: '20:23' },
  ],
  'scheduled-events': [
    { id: 'evt-01', topic: 'matches/quick-match/start', payload: '{ "table": "02" }', scheduledAt: '20:30', status: 'pending' },
    { id: 'evt-02', topic: 'venue/screens/refresh', payload: '{ "screen": "main" }', scheduledAt: '20:45', status: 'sent' },
  ],
  'system-config': [
    { key: 'mqtt.topicPrefix', value: 'connected-games', category: 'realtime', editable: 'true', updatedAt: 'oggi' },
    { key: 'match.targetScore', value: '10', category: 'match', editable: 'true', updatedAt: 'ieri' },
  ],
}

const initialActuators: Actuator[] = [
  { id: 'table-02-led', name: 'LED Tavolo 02', type: 'switch', value: true, enabled: true, lastUpdated: new Date() },
  { id: 'table-02-volume', name: 'Volume Tavolo 02', type: 'slider', value: 70, min: 0, max: 100, step: 10, enabled: true, lastUpdated: new Date() },
  { id: 'screen-refresh', name: 'Aggiorna display sala', type: 'trigger', value: 'idle', enabled: true, lastUpdated: new Date() },
]

type MqttService = ReturnType<typeof createMqttClientService>
let mqttServicePromise: Promise<MqttService> | null = null

function getMqttService(): Promise<MqttService> {
  mqttServicePromise ??= import('@infrastructure/realtime/MqttClientService').then(
    ({ createMqttClientService }) => createMqttClientService(),
  )
  return mqttServicePromise
}

function formatValue(value: string) {
  return value.length > 34 ? `${value.slice(0, 34)}…` : value
}

export function SimulatorView() {
  const [dataset, setDataset] = useState<DatasetTab>('devices')
  const [actuators, setActuators] = useState(initialActuators)
  const [messages, setMessages] = useState<MqttMessage[]>([])
  const [mqttStatus, setMqttStatus] = useState<'offline' | 'connecting' | 'connected' | 'error'>('offline')
  const [error, setError] = useState<string | null>(null)

  const rows = initialRows[dataset]
  const columns = useMemo(() => Object.keys(rows[0] ?? {}), [rows])

  async function connectMqtt() {
    setError(null)
    setMqttStatus('connecting')
    try {
      const mqttService = await getMqttService()
      await mqttService.connect({
        url: import.meta.env.VITE_MQTT_URL ?? 'ws://localhost:9001',
        clientId: `connected-games-simulator-${crypto.randomUUID()}`,
      })
      setMqttStatus('connected')
    } catch (reason) {
      setMqttStatus('error')
      setError(reason instanceof Error ? reason.message : 'Connessione MQTT non riuscita')
    }
  }

  async function publishDemoEvent() {
    setError(null)
    const message: MqttMessage = {
      id: crypto.randomUUID(),
      topic: 'simulator/events/demo',
      payload: { type: 'score.updated', tableId: 'table-02', score: '7-5' },
      qos: 1 as const,
      retain: false,
      timestamp: new Date(),
      direction: 'outbound' as const,
    }
    try {
      const mqttService = await getMqttService()
      await mqttService.publish(message.topic, message.payload, { qos: message.qos })
      setMessages((current) => [message, ...current].slice(0, 8))
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Pubblicazione MQTT non riuscita')
    }
  }

  async function updateActuator(actuator: Actuator, value: Actuator['value']) {
    setError(null)
    const updated = { ...actuator, value, lastUpdated: new Date() }
    setActuators((current) => current.map((item) => item.id === actuator.id ? updated : item))
    const mqttService = await getMqttService()
    if (!mqttService.isConnected()) {
      setMessages((current) => [{
        id: crypto.randomUUID(),
        topic: `simulator/actuators/${actuator.id}/set`,
        payload: { actuatorId: actuator.id, value },
        qos: 1 as const,
        retain: false,
        timestamp: new Date(),
        direction: 'outbound' as const,
      }, ...current].slice(0, 8))
      return
    }
    try {
      await mqttService.publish(`simulator/actuators/${encodeURIComponent(actuator.id)}/set`, { actuatorId: actuator.id, value }, { qos: 1 })
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Aggiornamento attuatore non riuscito')
    }
  }

  return (
    <AdminSectionShell title="Web Simulator" description="Esplora i dati simulati, pubblica eventi MQTT e controlla lo stato degli attuatori.">
      <div className="grid gap-4 sm:grid-cols-3">
        <Card elevated><p className="text-xs text-[var(--cg-color-label-secondary)]">Dataset simulati</p><p className="mt-2 text-3xl font-bold">4</p><p className="mt-1 text-xs text-[var(--cg-color-label-tertiary)]">Persistenza locale pronta</p></Card>
        <Card elevated><p className="text-xs text-[var(--cg-color-label-secondary)]">Messaggi sessione</p><p className="mt-2 text-3xl font-bold">{messages.length}</p><p className="mt-1 text-xs text-[var(--cg-color-label-tertiary)]">Eventi in uscita</p></Card>
        <Card elevated><p className="text-xs text-[var(--cg-color-label-secondary)]">Broker MQTT</p><p className="mt-2 text-3xl font-bold">{mqttStatus === 'connected' ? 'OK' : '—'}</p><p className="mt-1 text-xs text-[var(--cg-color-label-tertiary)]">{mqttStatus === 'error' ? 'Errore connessione' : 'ws://localhost:9001'}</p></Card>
      </div>

      <Card elevated className="space-y-4">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <h2 className="text-lg font-bold">Tabelle dati</h2>
            <p className="text-sm text-[var(--cg-color-label-secondary)]">Quattro sorgenti disponibili per testare il flusso di simulazione.</p>
          </div>
          <div className="flex gap-2">
            <Badge tone={mqttStatus === 'connected' ? 'success' : mqttStatus === 'error' ? 'warning' : 'neutral'}>{mqttStatus}</Badge>
            <button type="button" className="cg-button cg-button-secondary text-sm" onClick={connectMqtt} disabled={mqttStatus === 'connecting'}>{mqttStatus === 'connecting' ? 'Connessione…' : 'Connetti MQTT'}</button>
          </div>
        </div>
        <div className="flex flex-wrap gap-2">
          {(Object.keys(datasetLabels) as DatasetTab[]).map((item) => <button key={item} type="button" onClick={() => setDataset(item)} className={`rounded-lg px-3 py-2 text-sm font-semibold ${dataset === item ? 'bg-[var(--cg-color-accent)] text-white' : 'bg-[var(--cg-color-bg-grouped)] text-[var(--cg-color-label-secondary)]'}`}>{datasetLabels[item]}</button>)}
        </div>
        <div className="overflow-x-auto">
          <table className="w-full min-w-[560px] text-left text-sm">
            <thead className="border-b border-[var(--cg-color-border-subtle)] text-xs uppercase text-[var(--cg-color-label-secondary)]"><tr>{columns.map((column) => <th key={column} className="p-3">{column}</th>)}</tr></thead>
            <tbody>{rows.map((row, index) => <tr key={row.id ?? row.key ?? index} className="border-b border-[var(--cg-color-border-subtle)] last:border-0">{columns.map((column) => <td key={column} className="p-3 text-[var(--cg-color-label-secondary)]">{formatValue(row[column] ?? '—')}</td>)}</tr>)}</tbody>
          </table>
        </div>
      </Card>

      <div className="grid gap-4 lg:grid-cols-2">
        <Card elevated className="space-y-4">
          <div><h2 className="text-lg font-bold">Stato attuatori</h2><p className="text-sm text-[var(--cg-color-label-secondary)]">I comandi vengono pubblicati su MQTT quando il broker è connesso.</p></div>
          <div className="space-y-3">{actuators.map((actuator) => <div key={actuator.id} className="flex flex-wrap items-center justify-between gap-3 rounded-xl bg-[var(--cg-color-bg-grouped)] p-3"><div><p className="font-semibold">{actuator.name}</p><p className="text-xs text-[var(--cg-color-label-tertiary)]">{actuator.id} · {String(actuator.value)}</p></div>{actuator.type === 'switch' && <button type="button" className="cg-button cg-button-secondary text-sm" onClick={() => void updateActuator(actuator, !actuator.value)}>{actuator.value ? 'Attivo' : 'Spento'}</button>}{actuator.type === 'slider' && <input aria-label={actuator.name} type="range" min={actuator.min} max={actuator.max} step={actuator.step} value={Number(actuator.value)} onChange={(event) => void updateActuator(actuator, Number(event.target.value))} />}{actuator.type === 'trigger' && <button type="button" className="cg-button cg-button-secondary text-sm" onClick={() => void updateActuator(actuator, 'triggered')}>Esegui</button>}</div>)}</div>
        </Card>
        <Card elevated className="space-y-4">
          <div className="flex items-center justify-between gap-3"><div><h2 className="text-lg font-bold">Eventi MQTT</h2><p className="text-sm text-[var(--cg-color-label-secondary)]">Verifica topic e payload durante lo sviluppo.</p></div><button type="button" className="cg-button cg-button-primary text-sm" onClick={() => void publishDemoEvent()}>Pubblica demo</button></div>
          {messages.length === 0 ? <p className="rounded-xl bg-[var(--cg-color-bg-grouped)] p-4 text-sm text-[var(--cg-color-label-secondary)]">Nessun messaggio in questa sessione.</p> : <ul className="space-y-2">{messages.map((message) => <li key={message.id} className="rounded-xl bg-[var(--cg-color-bg-grouped)] p-3 text-sm"><div className="flex justify-between gap-3"><code className="text-[var(--cg-color-accent-label)]">{message.topic}</code><time className="text-xs text-[var(--cg-color-label-tertiary)]">{message.timestamp.toLocaleTimeString('it-IT')}</time></div><pre className="mt-2 overflow-x-auto text-xs text-[var(--cg-color-label-secondary)]">{JSON.stringify(message.payload)}</pre></li>)}</ul>}
        </Card>
      </div>
      {error && <p role="alert" className="rounded-xl border border-[var(--cg-color-status-warning)]/30 bg-[var(--cg-color-status-warning)]/10 p-3 text-sm text-[var(--cg-color-status-warning)]">{error}</p>}
    </AdminSectionShell>
  )
}
