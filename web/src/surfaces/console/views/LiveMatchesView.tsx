import { Badge, Card } from '@ui'
import { AdminMetric, AdminSectionShell } from './AdminSectionShell'
import { MatchEventFeed, type MatchEvent } from '@match/ui/MatchEventFeed'

const matches = [
  ['Tavolo 02', 'Rossi', 'Blu', '7', '5', '12:42'],
  ['Tavolo 05', 'Pixel Masters', 'Dragon Squad', '3', '3', '08:17'],
  ['Tavolo 08', 'Marco V.', 'Giulia R.', '1', '0', '03:55'],
]

const events: MatchEvent[] = [
  { id: 'goal-1', time: '20:24', message: 'Rossi segna · Tavolo 02 passa a 7-5', tone: 'score' },
  { id: 'goal-2', time: '20:22', message: 'Pixel Masters pareggia · Tavolo 05', tone: 'score' },
  { id: 'device-1', time: '20:20', message: 'Tavolo 08 connesso al broker MQTT', tone: 'system' },
  { id: 'match-1', time: '20:18', message: 'Nuova partita avviata al Tavolo 08', tone: 'info' },
]

export function LiveMatchesView() {
  return (
    <AdminSectionShell title="Partite live" description="Controlla in tempo reale le partite attive nel locale.">
      <div className="grid gap-4 sm:grid-cols-3">
        <AdminMetric label="Partite in corso" value="8" detail="Aggiornato ora" />
        <AdminMetric label="Giocatori online" value="24" detail="4 tavoli completati" />
        <AdminMetric label="Durata media" value="11:24" detail="-2:10 rispetto a ieri" />
      </div>
      <div className="grid gap-4 lg:grid-cols-2">
        {matches.map(([table, home, away, homeScore, awayScore, time]) => (
          <Card elevated key={table} className="space-y-5">
            <div className="flex items-center justify-between"><span className="font-semibold">{table}</span><Badge tone="success">LIVE</Badge></div>
            <div className="flex items-center justify-between text-lg"><span>{home}</span><strong>{homeScore}</strong></div>
            <div className="flex items-center justify-between text-lg"><span>{away}</span><strong>{awayScore}</strong></div>
            <p className="text-sm text-[var(--cg-color-label-secondary)]">In corso da {time}</p>
          </Card>
        ))}
      </div>
      <MatchEventFeed events={events} title="Feed attività venue" />
    </AdminSectionShell>
  )
}
