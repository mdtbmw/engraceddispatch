import { useState, useEffect } from 'react'
import { Truck, TrendingUp, Users, Wallet, CheckCircle } from 'lucide-react'
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts'
import KpiCard from '@/components/KpiCard'
import StatusBadge from '@/components/StatusBadge'
import { getDeliveries, getRiders, getRevenueData } from '@/lib/services'
import type { Delivery, Rider } from '@/types'

export default function Dashboard() {
  const [deliveries, setDeliveries] = useState<Delivery[]>([])
  const [riders, setRiders] = useState<Rider[]>([])
  const [revenueData, setRevenueData] = useState<{ month: string; revenue: number }[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([
      getDeliveries(),
      getRiders(),
      getRevenueData(),
    ]).then(([d, r, rev]) => {
      setDeliveries(d)
      setRiders(r)
      setRevenueData(rev)
    }).catch(() => {}).finally(() => setLoading(false))
  }, [])

  const activeShipments = deliveries.filter((d) => d.status !== 'DELIVERED' && d.status !== 'CANCELLED').length
  const totalRevenue = deliveries.reduce((sum, d) => sum + (d.status === 'DELIVERED' ? d.totalAmount : 0), 0)
  const availableRiders = riders.filter((r) => r.status === 'active').length
  const pendingDeliveries = deliveries.filter((d) => d.status === 'PENDING').length

  if (loading) {
    return (
      <div className="flex items-center justify-center h-[60vh]">
        <div className="flex flex-col items-center gap-3">
          <div className="w-8 h-8 border-2 border-biro-blue border-t-transparent rounded-full animate-spin" />
          <p className="text-xs text-text-gray font-medium">Loading dashboard...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-8">
      <div className="grid grid-cols-4 gap-6">
        <KpiCard label="Active Shipments" value={activeShipments} desc="Currently in transit" icon={Truck} color="text-biro-blue" bg="bg-biro-blue/10" />
        <KpiCard label="Total Revenue" value={`₦${totalRevenue.toLocaleString()}`} desc="Completed deliveries inflow" icon={TrendingUp} color="text-success-green" bg="bg-success-green/10" />
        <KpiCard label="Available Riders" value={availableRiders} desc="Ready for assignment" icon={Users} color="text-purple-500" bg="bg-purple-500/10" />
        <KpiCard label="Pending Assignments" value={pendingDeliveries} desc="Awaiting rider assignment" icon={Wallet} color="text-warning-orange" bg="bg-warning-orange/10" />
      </div>

      <div className="grid grid-cols-3 gap-8">
        <div className="col-span-2 p-6 rounded-3xl bg-admin-surface border border-admin-border">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h4 className="text-sm font-bold text-white">Revenue Overview</h4>
              <p className="text-[11px] text-text-gray">Monthly booking revenue (NGN)</p>
            </div>
          </div>
          <div className="h-[280px]">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={revenueData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#151522" />
                <XAxis dataKey="month" stroke="#64748B" fontSize={11} />
                <YAxis stroke="#64748B" fontSize={11} tickFormatter={(v) => `₦${(v / 1000).toFixed(0)}k`} />
                <Tooltip
                  contentStyle={{ background: '#08080C', border: '1px solid #151522', borderRadius: 12, fontSize: 12 }}
                  labelStyle={{ color: '#E2E8F0' }}
                  formatter={(v) => [`₦${Number(v).toLocaleString()}`, 'Revenue']}
                />
                <Line type="monotone" dataKey="revenue" stroke="#5C58FF" strokeWidth={2.5} dot={{ fill: '#5C58FF', r: 4 }} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="p-6 rounded-3xl bg-admin-surface border border-admin-border flex flex-col h-[380px]">
          <div className="mb-4">
            <h4 className="text-sm font-bold text-white">Pending Assignments</h4>
            <p className="text-[11px] text-text-gray">Deliveries awaiting rider</p>
          </div>
          <div className="flex-1 overflow-y-auto flex flex-col gap-3">
            {deliveries.filter((d) => d.status === 'PENDING').length === 0 ? (
              <div className="flex-1 flex flex-col items-center justify-center text-center p-6 border border-dashed border-admin-border rounded-2xl">
                <CheckCircle className="w-8 h-8 text-success-green mb-2" />
                <p className="text-xs font-bold text-white">All orders assigned</p>
                <p className="text-[10px] text-text-gray mt-0.5">Create new dispatches from Dispatch Center</p>
              </div>
            ) : (
              deliveries.filter((d) => d.status === 'PENDING').slice(0, 5).map((d) => (
                <div key={d.id} className="p-4 rounded-2xl bg-admin-bg border border-admin-border flex flex-col gap-2">
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-extrabold text-white">{d.trackingNumber}</span>
                    <StatusBadge status={d.status} />
                  </div>
                  <p className="text-[11px] text-text-gray">{d.itemName}</p>
                  <p className="text-[10px] text-text-gray">{d.pickupAddress?.split(',')[0]} → {d.deliveryAddress?.split(',')[0]}</p>
                </div>
              ))
            )}
          </div>
        </div>
      </div>

      <div className="p-6 rounded-3xl bg-admin-surface border border-admin-border">
        <div className="flex items-center justify-between mb-4">
          <div>
            <h4 className="text-sm font-bold text-white">Recent Dispatches</h4>
            <p className="text-[11px] text-text-gray">Latest delivery operations</p>
          </div>
        </div>
        <div className="flex flex-col gap-3">
          {deliveries.slice(0, 5).map((d) => (
            <div key={d.id} className="p-4 rounded-2xl bg-admin-bg border border-admin-border flex items-center justify-between">
              <div className="flex items-center gap-4">
                <div className="w-10 h-10 rounded-xl bg-biro-blue/10 flex items-center justify-center text-biro-blue">
                  <Truck className="w-4 h-4" />
                </div>
                <div>
                  <p className="text-xs font-bold text-white">{d.trackingNumber}</p>
                  <p className="text-[10px] text-text-gray">{d.itemName} • {d.customerName}</p>
                </div>
              </div>
              <div className="flex items-center gap-4">
                <span className="text-xs font-extrabold text-white">₦{d.totalAmount?.toLocaleString()}</span>
                <StatusBadge status={d.status} />
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
