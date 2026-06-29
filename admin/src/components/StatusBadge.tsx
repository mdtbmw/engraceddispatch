interface StatusBadgeProps {
  status: string
}

const statusStyles: Record<string, string> = {
  DELIVERED: 'bg-success-green/10 text-success-green',
  COMPLETED: 'bg-success-green/10 text-success-green',
  PENDING: 'bg-warning-orange/10 text-warning-orange',
  ASSIGNED: 'bg-biro-blue/10 text-biro-blue',
  PICKED_UP: 'bg-indigo-500/10 text-indigo-500',
  OUT_FOR_DELIVERY: 'bg-indigo-500/10 text-indigo-500',
  CANCELLED: 'bg-danger-red/10 text-danger-red',
  FAILED: 'bg-danger-red/10 text-danger-red',
  ACTIVE: 'bg-success-green/10 text-success-green',
  OFFLINE: 'bg-danger-red/10 text-danger-red',
  DELIVERING: 'bg-indigo-500/10 text-indigo-500',
  CREDIT: 'bg-success-green/10 text-success-green',
  DEBIT: 'bg-biro-blue/10 text-biro-blue',
  VERIFIED: 'bg-success-green/10 text-success-green',
}

export default function StatusBadge({ status }: StatusBadgeProps) {
  const style = statusStyles[status] || 'bg-admin-hover text-text-gray'
  return (
    <span className={`px-2.5 py-1 rounded-full text-[10px] font-extrabold ${style}`}>
      {status.replace(/_/g, ' ')}
    </span>
  )
}
