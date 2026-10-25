import type { ReactNode, TableHTMLAttributes } from 'react'

export interface TableColumn<T> {
  key: string
  header: string
  render: (row: T) => ReactNode
}

export interface DataTableProps<T> extends TableHTMLAttributes<HTMLTableElement> {
  columns: TableColumn<T>[]
  rows: T[]
  rowKey: (row: T, index: number) => string
  emptyMessage?: string
}

export function DataTable<T>({
  columns,
  rows,
  rowKey,
  emptyMessage = 'Nessun dato disponibile',
  className = '',
  ...props
}: DataTableProps<T>) {
  return (
    <div className="cg-table-wrapper">
      <table className={`cg-table ${className}`.trim()} {...props}>
        <thead>
          <tr>
            {columns.map((column) => <th key={column.key} scope="col">{column.header}</th>)}
          </tr>
        </thead>
        <tbody>
          {rows.length === 0 ? (
            <tr><td colSpan={columns.length}>{emptyMessage}</td></tr>
          ) : rows.map((row, index) => (
            <tr key={rowKey(row, index)}>
              {columns.map((column) => <td key={column.key}>{column.render(row)}</td>)}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
