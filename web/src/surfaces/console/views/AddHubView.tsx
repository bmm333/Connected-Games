import { useState } from 'react'
import { Button, Card, Input } from '@ui'
import { AdminSectionShell } from './AdminSectionShell'

export function AddHubView() {
  const [saved, setSaved] = useState(false)
  return (
    <AdminSectionShell title="Aggiungi hub" description="Collega un nuovo hub o display al locale tramite codice di associazione.">
      <Card elevated className="mx-auto max-w-2xl">
        <form className="space-y-5" onSubmit={(event) => { event.preventDefault(); setSaved(true) }}>
          <Input label="Nome dispositivo" placeholder="Es. Totem ingresso" required />
          <Input label="Codice di associazione" placeholder="XXXX-XXXX" required />
          <label className="cg-field"><span className="cg-field-label">Tipo dispositivo</span><select className="cg-input" defaultValue="kiosk"><option value="kiosk">Kiosk</option><option value="player">Player</option><option value="display">Table display</option></select></label>
          {saved && <p role="status" className="text-sm text-[var(--cg-color-status-success)]">Dispositivo registrato. Completa il pairing dal dispositivo.</p>}
          <Button type="submit" className="w-full">Registra dispositivo</Button>
        </form>
      </Card>
    </AdminSectionShell>
  )
}
