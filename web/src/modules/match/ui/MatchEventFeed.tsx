import { Radio, Trophy, Zap } from 'lucide-react'

export type MatchEvent = {
  readonly id: string
  readonly time: string
  readonly message: string
  readonly tone: 'score' | 'system' | 'info'
}

interface MatchEventFeedProps {
  events: readonly MatchEvent[]
  title?: string
}

const eventIcons = {
  score: Trophy,
  system: Radio,
  info: Zap,
} as const

export function MatchEventFeed({ events, title = 'Feed partita' }: MatchEventFeedProps) {
  return (
    <section className="space-y-4 overflow-hidden rounded-2xl border border-[var(--cg-color-border-subtle)] bg-[var(--cg-color-bg-elevated)] p-5 shadow-sm">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-xs font-semibold uppercase tracking-[0.14em] text-[var(--cg-color-accent-label)]">Eventi live</p>
          <h2 className="mt-1 text-lg font-bold">{title}</h2>
        </div>
        <span className="inline-flex items-center gap-2 text-xs font-semibold text-[var(--cg-color-status-success)]">
          <span className="h-2 w-2 animate-pulse rounded-full bg-current" aria-hidden="true" /> Aggiornato ora
        </span>
      </div>
      <ol className="space-y-3" aria-label={title}>
        {events.map((event) => {
          const Icon = eventIcons[event.tone]
          return (
            <li key={event.id} className="flex items-start gap-3 rounded-xl bg-[var(--cg-color-bg-grouped)] px-3 py-3">
              <span className="mt-0.5 rounded-lg bg-[var(--cg-color-bg-elevated)] p-2 text-[var(--cg-color-accent-label)]">
                <Icon size={15} aria-hidden="true" />
              </span>
              <p className="min-w-0 flex-1 text-sm">{event.message}</p>
              <time className="shrink-0 font-mono text-xs text-[var(--cg-color-label-tertiary)]">{event.time}</time>
            </li>
          )
        })}
      </ol>
    </section>
  )
}
