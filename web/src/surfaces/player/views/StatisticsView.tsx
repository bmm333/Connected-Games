import { Link } from 'react-router'
import { Card, ThemeToggle } from '@ui'

export function StatisticsView() {
  return (
    <div className="cg-mobile-page space-y-8">
      <header className="flex items-start justify-between gap-4">
        <div><Link to="/lobby" className="text-sm font-semibold text-[var(--cg-color-accent-label)]">← Lobby</Link><p className="mt-4 text-xs font-semibold uppercase tracking-[0.14em] text-[var(--cg-color-accent-label)]">Il tuo rendimento</p><h1 className="mt-1 text-3xl font-bold">Le tue statistiche</h1><p className="mt-1 text-sm text-[var(--cg-color-label-secondary)]">Segui i tuoi progressi e punta al prossimo traguardo.</p></div><ThemeToggle />
      </header>
      <div className="grid gap-4 sm:grid-cols-3"><Card elevated><p className="text-sm text-[var(--cg-color-label-secondary)]">Partite giocate</p><p className="mt-2 text-4xl font-bold">12</p><p className="mt-2 text-sm text-[var(--cg-color-status-success)]">+3 questa settimana</p></Card><Card><p className="text-sm text-[var(--cg-color-label-secondary)]">Vittorie</p><p className="mt-2 text-3xl font-bold">8</p><p className="mt-2 text-sm text-[var(--cg-color-label-secondary)]">67% delle partite</p></Card><Card><p className="text-sm text-[var(--cg-color-label-secondary)]">Serie migliore</p><p className="mt-2 text-3xl font-bold">4 <span className="text-base font-medium">win</span></p><p className="mt-2 text-sm text-[var(--cg-color-label-secondary)]">Record personale</p></Card></div>
      <Card><div className="flex items-start justify-between gap-4"><div><h2 className="font-semibold">Andamento settimanale</h2><p className="mt-1 text-sm text-[var(--cg-color-label-secondary)]">Le tue ultime 7 sessioni</p></div><span className="cg-badge cg-badge-success">+12%</span></div><div className="mt-8 flex h-40 items-end gap-2">{[35, 55, 44, 78, 62, 90, 72].map((height, index) => <div key={index} className="flex flex-1 flex-col items-center gap-2"><div className="w-full rounded-t bg-[var(--cg-color-accent)] transition-opacity hover:opacity-80" style={{ height: `${height}%` }} /><span className="text-[0.65rem] text-[var(--cg-color-label-tertiary)]">{['Lun', 'Mar', 'Mer', 'Gio', 'Ven', 'Sab', 'Dom'][index]}</span></div>)}</div></Card>
      <Card className="flex items-center justify-between gap-4"><div><h2 className="font-semibold">Prossimo obiettivo</h2><p className="mt-1 text-sm text-[var(--cg-color-label-secondary)]">Vinci ancora 2 partite per raggiungere 10 vittorie.</p></div><span className="text-2xl font-bold text-[var(--cg-color-accent-label)]">80%</span></Card>
    </div>
  )
}
