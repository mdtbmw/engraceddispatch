import type { ReactNode } from 'react'

export interface Column<T> {
  key: string
  header: string
  render: (item: T) => ReactNode
  className?: string
}

interface DataTableProps<T> {
  columns: Column<T>[]
  data: T[]
  onRowClick?: (item: T) => void
}

export default function DataTable<T extends { id: string | number }>({ columns, data, onRowClick }: DataTableProps<T>) {
  return (
    <div className="overflow-x-auto">
      <table className="w-full text-left border-collapse">
        <thead>
          <tr className="border-b border-admin-border text-xs text-text-gray font-bold">
            {columns.map((col) => (
              <th key={col.key} className={`pb-4 ${col.className || ''}`}>{col.header}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {data.length === 0 ? (
            <tr>
              <td colSpan={columns.length} className="py-16 text-center">
                <p className="text-sm text-text-gray font-medium">No data found</p>
              </td>
            </tr>
          ) : (
            data.map((item) => (
              <tr
                key={item.id}
                onClick={() => onRowClick?.(item)}
                className={`border-b border-[#1A1A29]/40 text-xs text-[#E2E8F0] transition-all ${onRowClick ? 'cursor-pointer' : ''} hover:bg-[#10101A]/35`}
              >
                {columns.map((col) => (
                  <td key={col.key} className={`py-4 ${col.className || ''}`}>{col.render(item)}</td>
                ))}
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  )
}
