import type { LucideIcon } from 'lucide-react'

interface KpiCardProps {
  label: string
  value: string | number
  desc: string
  icon: LucideIcon
  color: string
  bg: string
}

export default function KpiCard({ label, value, desc, icon: Icon, color, bg }: KpiCardProps) {
  return (
    <div className="p-6 rounded-2xl bg-admin-surface border border-admin-border flex items-center justify-between hover:border-biro-blue transition-all duration-300">
      <div>
        <p className="text-xs text-text-gray font-semibold tracking-wider uppercase">{label}</p>
        <h3 className="text-2xl font-extrabold text-white mt-1.5">{value}</h3>
        <p className="text-[10px] text-text-gray mt-1">{desc}</p>
      </div>
      <div className={`w-12 h-12 rounded-xl ${bg} ${color} flex items-center justify-center`}>
        <Icon className="w-5 h-5" />
      </div>
    </div>
  )
}
