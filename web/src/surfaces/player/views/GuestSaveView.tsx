import { useState } from 'react'
import { Link } from 'react-router'
import { Button, Card, Input } from '@ui'

export function GuestSaveView() {
  const [saved, setSaved] = useState(false)
  return (
    <div className="space-y-6">
      <header><Link to="/lobby" className="text-sm text-[var(--cg-color-accent-label)]">← Lobby</Link><h1 className="mt-3 text-2xl font-bold">Salva il tuo profilo</h1><p className="mt-2 text-sm text-[var(--cg-color-label-secondary)]">Conserva risultati e statistiche anche su altri dispositivi.</p></header>
      <Card elevated><form className="space-y-4" onSubmit={(event) => { event.preventDefault(); setSaved(true) }}><Input label="Nome" placeholder="Come vuoi essere chiamato?" required /><Input label="Email" type="email" placeholder="nome@email.it" required />{saved && <p role="status" className="text-sm text-[var(--cg-color-status-success)]">Profilo salvato correttamente.</p>}<Button type="submit" className="w-full">Salva e continua</Button></form></Card>
    </div>
  )
}
