import { Link, useParams } from 'react-router'
import { useResolveTableByCode } from '../hooks/useResolveTableByCode'

export function TableLookupView() {
  const { code = '' } = useParams()
  const lookup = useResolveTableByCode(code)

  if (lookup.isPending) return <p>Caricamento tavolo...</p>
  if (lookup.isError) {
    return (
      <div className="space-y-4">
        <p role="alert">Impossibile trovare il tavolo.</p>
        <Link className="text-purple-400" to="/">Torna alla lobby</Link>
      </div>
    )
  }

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold">{lookup.data.venue.name}</h1>
      <p>{lookup.data.table.name} · {lookup.data.gameType.name}</p>
      <Link className="text-purple-400" to="/">Torna alla lobby</Link>
    </div>
  )
}
