import { useState } from 'react'
import { Badge, Card, Input } from '@ui'
import { useDataTable } from '../hooks/useConsoleQueries'
import { AdminMetric, AdminSectionShell } from './AdminSectionShell'

export function TablesView() {
  const [filter, setFilter] = useState('')
  const [page, setPage] = useState(1)
  const tableData = useDataTable({
    table: 'tables',
    page,
    pageSize: 20,
    filters: filter.trim() === '' ? [] : [{ column: 'name', value: filter }],
  })

  const totalRows = tableData.data?.total ?? 0
  const visibleRows = tableData.data?.rows.length ?? 0

  return (
    <AdminSectionShell title="Tavoli" description="Gestisci le postazioni e lo stato dei giochi installati.">
      <div className="grid gap-4 sm:grid-cols-3">
        <AdminMetric label="Postazioni totali" value={tableData.isPending ? '…' : String(totalRows)} detail="Dal catalogo console" />
        <AdminMetric label="Visualizzate" value={tableData.isPending ? '…' : String(visibleRows)} detail={`Pagina ${page}`} />
        <AdminMetric label="Filtro attivo" value={filter.trim() || 'Nessuno'} detail="Ricerca per nome" />
      </div>
      <Input value={filter} onChange={(event) => { setFilter(event.target.value); setPage(1) }} placeholder="Filtra tavoli…" aria-label="Filtra tavoli" />
      <Card elevated className="overflow-x-auto p-0">
        {tableData.isPending ? <p className="p-6">Caricamento…</p> : tableData.isError ? <p className="p-6">Impossibile caricare i tavoli.</p> : <table className="w-full min-w-[600px] text-left text-sm">
          <thead className="border-b border-[var(--cg-color-border-subtle)] text-[var(--cg-color-label-secondary)]">
            <tr>
              {tableData.data.columns.map((col) => <th key={col.key} className="p-4">{col.label}</th>)}
              <th className="p-4 text-right">ID</th>
            </tr>
          </thead>
          <tbody>
            {tableData.data.rows.map((row) => (
              <tr key={row.id} className="border-b border-[var(--cg-color-border-subtle)] last:border-0">
                {tableData.data.columns.map((column) => <td key={column.key} className="p-4">{String(row.values[column.key] ?? '—')}</td>)}
                <td className="p-4 text-right"><Badge>{row.id}</Badge></td>
              </tr>
            ))}
          </tbody>
        </table>}
      </Card>
      <div className="flex items-center justify-between text-sm">
        <span>{totalRows} risultati</span>
        <button type="button" disabled={page === 1} onClick={() => setPage((current) => current - 1)}>Pagina precedente</button>
        <button type="button" disabled={!tableData.data || page * 20 >= tableData.data.total} onClick={() => setPage((current) => current + 1)}>Pagina successiva</button>
      </div>
    </AdminSectionShell>
  )
}
