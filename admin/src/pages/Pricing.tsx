import { useState, useEffect } from 'react'
import { Receipt, Edit3 } from 'lucide-react'
import { getPricingRules } from '@/lib/services'
import { apiUpdatePricingRule } from '@/lib/admin-api'
import type { PricingRule } from '@/types'
import toast from 'react-hot-toast'

export default function Pricing() {
  const [rules, setRules] = useState<PricingRule[]>([])
  const [loading, setLoading] = useState(true)
  const [editRule, setEditRule] = useState<PricingRule | null>(null)
  const [form, setForm] = useState({ basePrice: '', surgeAmount: '', perKgRate: '' })

  const loadData = () => {
    setLoading(true)
    getPricingRules().then(setRules).catch(() => toast.error('Failed to load pricing')).finally(() => setLoading(false))
  }

  useEffect(() => { loadData() }, [])

  const handleEdit = (rule: PricingRule) => {
    setEditRule(rule); setForm({ basePrice: String(rule.basePrice), surgeAmount: String(rule.surgeAmount), perKgRate: String(rule.perKgRate) })
  }

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!editRule) return
    try {
      await apiUpdatePricingRule(editRule.id, { basePrice: parseFloat(form.basePrice) || 0, surgeAmount: parseFloat(form.surgeAmount) || 0, perKgRate: parseFloat(form.perKgRate) || 0 })
      setRules((prev) => prev.map((r) => r.id === editRule.id ? { ...r, basePrice: parseFloat(form.basePrice) || 0, surgeAmount: parseFloat(form.surgeAmount) || 0, perKgRate: parseFloat(form.perKgRate) || 0 } : r))
      setEditRule(null); toast.success('Pricing updated')
    } catch { toast.error('Failed to save') }
  }

  const toggleRule = async (id: string, current: boolean) => {
    try { await apiUpdatePricingRule(id, { isActive: !current }); setRules((prev) => prev.map((r) => r.id === id ? { ...r, isActive: !current } : r)) }
    catch { toast.error('Failed to toggle') }
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-[60vh]">
        <div className="flex flex-col items-center gap-3">
          <div className="w-8 h-8 border-2 border-biro-blue border-t-transparent rounded-full animate-spin" />
          <p className="text-xs text-text-gray font-medium">Loading pricing rules...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-8">
      <div><h3 className="text-lg font-extrabold text-white">Pricing & Rate Rules</h3><p className="text-xs text-text-gray">Manage delivery type base pricing and dynamic surcharges</p></div>

      <div className="grid grid-cols-2 gap-6">
        {rules.map((r) => (
          <div key={r.id} className={`p-6 rounded-2xl bg-admin-surface border transition-all ${r.isActive ? 'border-admin-border hover:border-biro-blue' : 'border-danger-red/30 opacity-60'}`}>
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-biro-blue/10 flex items-center justify-center text-biro-blue"><Receipt className="w-5 h-5" /></div>
                <div><h4 className="text-sm font-extrabold text-white">{r.deliveryType}</h4><span className={`text-[10px] font-bold ${r.isActive ? 'text-success-green' : 'text-danger-red'}`}>{r.isActive ? 'ACTIVE' : 'DISABLED'}</span></div>
              </div>
              <div className="flex gap-2">
                <button onClick={() => handleEdit(r)} className="p-2 rounded-lg bg-admin-hover hover:bg-admin-active text-text-gray hover:text-white transition-all cursor-pointer" title="Edit"><Edit3 className="w-4 h-4" /></button>
                <button onClick={() => toggleRule(r.id, r.isActive)} className={`px-3 py-1.5 rounded text-[10px] font-bold cursor-pointer ${r.isActive ? 'bg-warning-orange/10 text-warning-orange' : 'bg-success-green/10 text-success-green'}`}>{r.isActive ? 'Disable' : 'Enable'}</button>
              </div>
            </div>
            <div className="border-t border-admin-border pt-4 grid grid-cols-3 gap-4">
              <div><p className="text-[10px] text-text-gray font-medium">Base Price</p><p className="text-sm font-extrabold text-white">₦{r.basePrice?.toLocaleString()}</p></div>
              <div><p className="text-[10px] text-text-gray font-medium">Surge</p><p className="text-sm font-extrabold text-white">₦{r.surgeAmount?.toLocaleString()}</p></div>
              <div><p className="text-[10px] text-text-gray font-medium">Per KG</p><p className="text-sm font-extrabold text-white">₦{r.perKgRate?.toLocaleString()}</p></div>
            </div>
          </div>
        ))}
      </div>

      {editRule && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50">
          <form onSubmit={handleSave} className="w-[450px] p-8 rounded-3xl bg-admin-surface border border-admin-border flex flex-col gap-5">
            <div><h3 className="text-base font-extrabold text-white">Edit Pricing — {editRule.deliveryType}</h3><p className="text-xs text-text-gray">Update rate parameters</p></div>
            <div className="flex flex-col gap-3">
              <div><label className="text-[10px] text-text-gray font-bold mb-1 block">Base Price (NGN)</label><input type="number" value={form.basePrice} onChange={(e) => setForm({ ...form, basePrice: e.target.value })} className="w-full bg-admin-bg border border-admin-border rounded-xl px-4 py-3 text-xs text-white outline-none focus:border-biro-blue" /></div>
              <div><label className="text-[10px] text-text-gray font-bold mb-1 block">Surge Amount (NGN)</label><input type="number" value={form.surgeAmount} onChange={(e) => setForm({ ...form, surgeAmount: e.target.value })} className="w-full bg-admin-bg border border-admin-border rounded-xl px-4 py-3 text-xs text-white outline-none focus:border-biro-blue" /></div>
              <div><label className="text-[10px] text-text-gray font-bold mb-1 block">Per KG Rate (NGN)</label><input type="number" step="0.1" value={form.perKgRate} onChange={(e) => setForm({ ...form, perKgRate: e.target.value })} className="w-full bg-admin-bg border border-admin-border rounded-xl px-4 py-3 text-xs text-white outline-none focus:border-biro-blue" /></div>
            </div>
            <div className="flex gap-3 justify-end mt-2">
              <button type="button" onClick={() => setEditRule(null)} className="px-4 py-2 text-xs font-bold text-text-gray hover:text-white cursor-pointer">Cancel</button>
              <button type="submit" className="px-5 py-2.5 rounded-xl bg-biro-blue text-xs font-bold text-white hover:scale-105 transition-all cursor-pointer">Save Changes</button>
            </div>
          </form>
        </div>
      )}
    </div>
  )
}
