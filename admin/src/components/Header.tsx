import { Search, Plus } from 'lucide-react'

interface HeaderProps {
  searchQuery: string
  onSearchChange: (val: string) => void
  actionLabel: string
  onAction: () => void
  onSimulate: () => void
}

export default function Header({ searchQuery, onSearchChange, actionLabel, onAction, onSimulate }: HeaderProps) {
  return (
    <header className="h-[80px] border-b border-admin-border bg-admin-surface/80 backdrop-blur-md px-10 flex items-center justify-between shrink-0">
      <div className="flex items-center gap-4 w-[400px]">
        <Search className="w-5 h-5 text-text-gray" />
        <input
          type="text"
          placeholder="Search tracking numbers, items, or locations..."
          value={searchQuery}
          onChange={(e) => onSearchChange(e.target.value)}
          className="bg-transparent border-none outline-none text-sm text-white w-full placeholder-text-gray"
        />
      </div>

      <div className="flex items-center gap-4">
        <button
          onClick={onSimulate}
          className="flex items-center gap-2 px-4 h-11 rounded-xl bg-admin-hover border border-admin-border text-xs font-bold text-white hover:bg-admin-active transition-all cursor-pointer"
        >
          <RefreshCwIcon />
          Simulate Booking
        </button>
        <button
          onClick={onAction}
          className="flex items-center gap-2 px-5 h-11 rounded-xl bg-biro-blue text-xs font-bold text-white shadow-lg shadow-biro-blue/20 hover:scale-105 transition-all cursor-pointer"
        >
          <Plus className="w-4 h-4" />
          {actionLabel}
        </button>
      </div>
    </header>
  )
}

function RefreshCwIcon() {
  return (
    <svg className="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M21 2v6h-6" />
      <path d="M3 12a9 9 0 0 1 15-6.7L21 8" />
      <path d="M3 22v-6h6" />
      <path d="M21 12a9 9 0 0 1-15 6.7L3 16" />
    </svg>
  )
}
