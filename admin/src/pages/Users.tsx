import { useState, useEffect } from 'react'
import { Mail, Phone } from 'lucide-react'
import StatusBadge from '@/components/StatusBadge'
import { getCustomers } from '@/lib/services'
import type { User } from '@/types'

export default function Users() {
  const [customers, setCustomers] = useState<User[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    getCustomers().then(setCustomers).catch(() => {}).finally(() => setLoading(false))
  }, [])

  if (loading) {
    return (
      <div className="flex items-center justify-center h-[60vh]">
        <div className="flex flex-col items-center gap-3">
          <div className="w-8 h-8 border-2 border-biro-blue border-t-transparent rounded-full animate-spin" />
          <p className="text-xs text-text-gray font-medium">Loading customers...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-8">
      <div><h3 className="text-lg font-extrabold text-white">Customer Management</h3><p className="text-xs text-text-gray">View registered customers, their activity, and verification status</p></div>
      <div className="p-6 rounded-3xl bg-admin-surface border border-admin-border">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-admin-border text-xs text-text-gray font-bold">
                <th className="pb-4">CUSTOMER</th><th className="pb-4">CONTACT</th><th className="pb-4">DELIVERIES</th><th className="pb-4">RATING</th><th className="pb-4">VERIFIED</th><th className="pb-4">MEMBER SINCE</th>
              </tr>
            </thead>
            <tbody>
              {customers.map((u) => (
                <tr key={u.id} className="border-b border-[#1A1A29]/40 text-xs text-[#E2E8F0] hover:bg-[#10101A]/35 transition-all">
                  <td className="py-4"><p className="font-bold text-white">{u.fullName}</p></td>
                  <td className="py-4">
                    <div className="flex flex-col gap-1">
                      <div className="flex items-center gap-1.5 text-text-gray"><Mail className="w-3 h-3" /> {u.email}</div>
                      <div className="flex items-center gap-1.5 text-text-gray"><Phone className="w-3 h-3" /> {u.phone}</div>
                    </div>
                  </td>
                  <td className="py-4 text-white font-bold">{u.totalDeliveries}</td>
                  <td className="py-4"><span className="text-royal-gold font-bold">⭐ {u.rating}</span></td>
                  <td className="py-4"><StatusBadge status={u.isVerified ? 'VERIFIED' : 'PENDING'} /></td>
                  <td className="py-4 text-text-gray">{u.createdAt}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
