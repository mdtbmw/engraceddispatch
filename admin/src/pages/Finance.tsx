import { useState, useEffect } from 'react'
import { TrendingUp, Users, Wallet, ArrowDownLeft, ArrowUpRight, Check } from 'lucide-react'
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts'
import KpiCard from '@/components/KpiCard'
import StatusBadge from '@/components/StatusBadge'
import { getTransactions, getRevenueData } from '@/lib/services'
import { apiApproveTransaction, apiGetWalletStats, apiGetRevenue } from '@/lib/admin-api'
import type { Transaction, WalletStats } from '@/types'

export default function Finance() {
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [stats, setStats] = useState<WalletStats | null>(null)
  const [revenueData, setRevenueData] = useState<{ month: string; revenue: number; expenses: number }[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([
      getTransactions(),
      apiGetWalletStats().then((r) => r.data).catch(() => null),
      getRevenueData(),
    ])
      .then(([tx, s, rev]) => { setTransactions(tx); setStats(s); setRevenueData(rev) })
      .catch(() => {}).finally(() => setLoading(false))
  }, [])

  const handleApprove = async (txId: string) => {
    try { await apiApproveTransaction(txId); setTransactions((prev) => prev.map((t) => t.id === txId ? { ...t, status: 'COMPLETED' } : t)) }
    catch {}
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-[60vh]">
        <div className="flex flex-col items-center gap-3">
          <div className="w-8 h-8 border-2 border-biro-blue border-t-transparent rounded-full animate-spin" />
          <p className="text-xs text-text-gray font-medium">Loading finance data...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-8">
      <div><h3 className="text-lg font-extrabold text-white">Finance & Revenue</h3><p className="text-xs text-text-gray">Company revenue reporting and transaction monitoring</p></div>

      <div className="grid grid-cols-4 gap-6">
        <KpiCard label="Total Revenue" value={`₦${(stats?.totalRevenue || 0).toLocaleString()}`} desc="All-time bookings inflow" icon={TrendingUp} color="text-success-green" bg="bg-success-green/10" />
        <KpiCard label="Active Customers" value={stats?.customerCount || 0} desc="Registered users" icon={Users} color="text-biro-blue" bg="bg-biro-blue/10" />
        <KpiCard label="Wallet Balance" value={`₦${(stats?.balance || 0).toLocaleString()}`} desc="Current float" icon={Wallet} color="text-royal-gold" bg="bg-royal-gold/10" />
        <KpiCard label="Pending Withdrawals" value={`₦${(stats?.pendingWithdrawals || 0).toLocaleString()}`} desc="Awaiting approval" icon={TrendingUp} color="text-warning-orange" bg="bg-warning-orange/10" />
      </div>

      <div className="grid grid-cols-3 gap-8">
        <div className="col-span-2 p-6 rounded-3xl bg-admin-surface border border-admin-border">
          <div className="mb-6"><h4 className="text-sm font-bold text-white">Revenue vs Expenses</h4><p className="text-[11px] text-text-gray">Monthly comparison (NGN)</p></div>
          <div className="h-[280px]">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={revenueData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#151522" />
                <XAxis dataKey="month" stroke="#64748B" fontSize={11} />
                <YAxis stroke="#64748B" fontSize={11} tickFormatter={(v) => `₦${(v / 1000).toFixed(0)}k`} />
                <Tooltip contentStyle={{ background: '#08080C', border: '1px solid #151522', borderRadius: 12, fontSize: 12 }} labelStyle={{ color: '#E2E8F0' }} />
                <Bar dataKey="revenue" fill="#5C58FF" radius={[6, 6, 0, 0]} name="Revenue" />
                <Bar dataKey="expenses" fill="#10B981" radius={[6, 6, 0, 0]} name="Expenses" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="p-6 rounded-3xl bg-admin-surface border border-admin-border flex flex-col h-[380px]">
          <div className="mb-4"><h4 className="text-sm font-bold text-white">Recent Transactions</h4><p className="text-[11px] text-text-gray">Latest financial activity</p></div>
          <div className="flex-1 overflow-y-auto flex flex-col gap-3">
            {transactions.map((t) => (
              <div key={t.id} className="p-4 rounded-xl bg-admin-bg border border-admin-border flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <div className={`w-10 h-10 rounded-lg flex items-center justify-center ${t.type === 'CREDIT' ? 'bg-success-green/10 text-success-green' : 'bg-biro-blue/10 text-biro-blue'}`}>
                    {t.type === 'CREDIT' ? <ArrowDownLeft className="w-4 h-4" /> : <ArrowUpRight className="w-4 h-4" />}
                  </div>
                  <div><p className="text-xs font-bold text-white">{t.title}</p><p className="text-[10px] text-text-gray">{t.userName}</p></div>
                </div>
                <div className="flex items-center gap-3">
                  <span className="text-xs font-extrabold text-white">₦{t.amount?.toLocaleString()}</span>
                  {t.status === 'PENDING' ? (
                    <button onClick={() => handleApprove(t.id)} className="px-2.5 py-1.5 rounded bg-success-green hover:bg-emerald-600 text-[10px] font-bold text-white transition-all cursor-pointer"><Check className="w-3 h-3" /></button>
                  ) : (
                    <StatusBadge status={t.status} />
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}
