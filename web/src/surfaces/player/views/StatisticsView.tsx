import { Link } from 'react-router'
import { Card, ThemeToggle } from '@ui'

export function StatisticsView() {
  return (
    <div className="space-y-6">
      <header className="flex items-center justify-between border-b border-[var(--cg-color-border-subtle)] pb-4">
        <div><Link to="/lobby" className="text-sm text-[var(--cg-color-accent-label)]">← Lobby</Link><h1 className="mt-2 text-2xl font-bold">Le tue statistiche</h1></div><ThemeToggle />
      </header>
      <Card elevated><p className="text-sm text-[var(--cg-color-label-secondary)]">Partite giocate</p><p className="mt-2 text-5xl font-bold">12</p><p className="mt-2 text-sm text-[var(--cg-color-status-success)]">+3 questa settimana</p></Card>
      <div className="grid grid-cols-2 gap-4"><Card><p className="text-sm text-[var(--cg-color-label-secondary)]">Vittorie</p><p className="mt-2 text-2xl font-bold">8</p></Card><Card><p className="text-sm text-[var(--cg-color-label-secondary)]">Win rate</p><p className="mt-2 text-2xl font-bold">67%</p></Card></div>
      <Card><h2 className="font-semibold">Andamento</h2><div className="mt-6 flex h-32 items-end gap-2">{[35, 55, 44, 78, 62, 90, 72].map((height, index) => <div key={index} className="flex-1 rounded-t bg-[var(--cg-color-accent)]" style={{ height: `${height}%` }} />)}</div></Card>
    </div>
  )
}
