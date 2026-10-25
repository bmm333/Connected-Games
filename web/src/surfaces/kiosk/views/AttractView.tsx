import { Link } from 'react-router'
import { Card, ThemeToggle } from '@ui'

export function AttractView() {
  return (
    <div className="flex flex-col items-center justify-center min-h-[85vh] text-center p-8 space-y-8">
      <div className="absolute right-6 top-6"><ThemeToggle /></div>
      <div className="space-y-4">
        <span className="cg-badge cg-badge-neutral">
          Arena Gaming Kiosk
        </span>
        <h1 className="text-5xl md:text-6xl font-extrabold tracking-tight text-[var(--cg-color-label-primary)]">
          Tocca per Giocare
        </h1>
        <p className="text-lg text-[var(--cg-color-label-secondary)] max-w-md mx-auto">
          Inquadra il QR Code con il tuo smartphone o iscriviti direttamente al totem per partecipare al torneo.
        </p>
      </div>

      <div className="flex gap-4">
        <Link
          to="/queue"
          className="cg-button cg-button-primary inline-flex items-center"
        >
          Unisciti alla Coda
        </Link>
        <Link
          to="/leaderboard"
          className="cg-button cg-button-secondary inline-flex items-center"
        >
          Classifica Live
        </Link>
      </div>
      <Card className="max-w-md text-sm text-[var(--cg-color-label-secondary)]">
        Usa il QR Code per configurare rapidamente il totem.
      </Card>
    </div>
  )
}
