import { useState, useEffect } from 'react'
import { MapPin, Plus } from 'lucide-react'
import { getZones } from '@/lib/services'
import { apiCreateZone, apiUpdateZone, apiDeleteZone } from '@/lib/admin-api'
import type { Zone } from '@/types'
import toast from 'react-hot-toast'

export default function Zones() {
  const [zones, setZones] = useState<Zone[]>([])
  const [loading, setLoading] = useState(true)
  const [showAdd, setShowAdd] = useState(false)
  const [form, setForm] = useState({ name: '', basePrice: '', surgeMultiplier: '1.0' })

  const loadData = () => {
    setLoading(true)
    getZones().then(setZones).catch(() => toast.error('Failed to load zones')).finally(() => setLoading(false))
  }

  useEffect(() => { loadData() }, [])

  const handleAdd = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.name) return
    try {
      const { data: z } = await apiCreateZone({ name: form.name, basePrice: parseFloat(form.basePrice) || 0, surgeMultiplier: parseFloat(form.surgeMultiplier) || 1 })
      setZones([...zones, z]); setForm({ name: '', basePrice: '', surgeMultiplier: '1.0' }); setShowAdd(false)
      toast.success('Zone added')
    } catch { toast.error('Failed to add zone') }
  }

  const toggleZone = async (id: string, current: boolean) => {
    try { await apiUpdateZone(id, { isActive: !current }); setZones((prev) => prev.map((z) => z.id === id ? { ...z, isActive: !current } : z)) }
    catch { toast.error('Failed to toggle zone') }
  }

  const handleDelete = async (id: string) => {
    try { await apiDeleteZone(id); setZones((prev) => prev.filter((z) => z.id !== id)); toast.success('Zone removed') }
    catch { toast.error('Failed to remove zone') }
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-[60vh]">
        <div className="flex flex-col items-center gap-3">
          <div className="w-8 h-8 border-2 border-biro-blue border-t-transparent rounded-full animate-spin" />
          <p className="text-xs text-text-gray font-medium">Loading zones...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-8">
      <div className="flex items-center justify-between">
        <div><h3 className="text-lg font-extrabold text-white">Delivery Zones</h3><p className="text-xs text-text-gray">Manage coverage areas, base pricing, and surge multipliers</p></div>
        <button onClick={() => setShowAdd(true)} className="flex items-center gap-2 px-5 h-11 rounded-xl bg-biro-blue text-xs font-bold text-white shadow-lg shadow-biro-blue/20 hover:scale-105 transition-all cursor-pointer"><Plus className="w-4 h-4" /> Add Zone</button>
      </div>

      <div className="grid grid-cols-3 gap-6">
        {zones.map((z) => (
          <div key={z.id} className={`p-6 rounded-2xl bg-admin-surface border flex flex-col gap-4 transition-all relative group ${z.isActive ? 'border-admin-border hover:border-biro-blue' : 'border-danger-red/30 opacity-60'}`}>
            <button onClick={() => handleDelete(z.id)} className="absolute top-3 right-3 w-6 h-6 rounded-full bg-danger-red/10 text-danger-red opacity-0 group-hover:opacity-100 flex items-center justify-center text-[10px] font-bold hover:bg-danger-red/20 transition-all cursor-pointer" title="Remove zone">✕</button>
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-biro-blue/10 flex items-center justify-center text-biro-blue"><MapPin className="w-5 h-5" /></div>
                <div><h4 className="text-sm font-extrabold text-white">{z.name}</h4><span className={`text-[10px] font-bold ${z.isActive ? 'text-success-green' : 'text-danger-red'}`}>{z.isActive ? 'ACTIVE' : 'DISABLED'}</span></div>
              </div>
              <button onClick={() => toggleZone(z.id, z.isActive)} className={`px-3 py-1 rounded text-[10px] font-bold cursor-pointer ${z.isActive ? 'bg-warning-orange/10 text-warning-orange hover:bg-warning-orange/20' : 'bg-success-green/10 text-success-green hover:bg-success-green/20'}`}>{z.isActive ? 'Disable' : 'Enable'}</button>
            </div>
            <div className="border-t border-admin-border pt-4 grid grid-cols-2 gap-4">
              <div><p className="text-[10px] text-text-gray font-medium">Base Price</p><p className="text-sm font-extrabold text-white">₦{z.basePrice?.toLocaleString()}</p></div>
              <div><p className="text-[10px] text-text-gray font-medium">Surge</p><p className="text-sm font-extrabold text-white">{z.surgeMultiplier}x</p></div>
              <div><p className="text-[10px] text-text-gray font-medium">Riders</p><p className="text-sm font-extrabold text-white">{z.riderCount}</p></div>
              <div><p className="text-[10px] text-text-gray font-medium">Deliveries</p><p className="text-sm font-extrabold text-white">{z.deliveryCount}</p></div>
            </div>
          </div>
        ))}
      </div>

      {showAdd && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50">
          <form onSubmit={handleAdd} className="w-[450px] p-8 rounded-3xl bg-admin-surface border border-admin-border flex flex-col gap-5">
            <div><h3 className="text-base font-extrabold text-white">Add Delivery Zone</h3><p className="text-xs text-text-gray">Define a new coverage area</p></div>
            <div className="flex flex-col gap-3">
              <input type="text" placeholder="Zone Name (e.g. Lekki Phase 1)" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required className="bg-admin-bg border border-admin-border rounded-xl px-4 py-3 text-xs text-white outline-none focus:border-biro-blue" />
              <input type="number" placeholder="Base Price (NGN)" value={form.basePrice} onChange={(e) => setForm({ ...form, basePrice: e.target.value })} className="bg-admin-bg border border-admin-border rounded-xl px-4 py-3 text-xs text-white outline-none focus:border-biro-blue" />
              <input type="number" step="0.1" placeholder="Surge Multiplier (e.g. 1.5)" value={form.surgeMultiplier} onChange={(e) => setForm({ ...form, surgeMultiplier: e.target.value })} className="bg-admin-bg border border-admin-border rounded-xl px-4 py-3 text-xs text-white outline-none focus:border-biro-blue" />
            </div>
            <div className="flex gap-3 justify-end mt-2">
              <button type="button" onClick={() => setShowAdd(false)} className="px-4 py-2 text-xs font-bold text-text-gray hover:text-white cursor-pointer">Cancel</button>
              <button type="submit" className="px-5 py-2.5 rounded-xl bg-biro-blue text-xs font-bold text-white hover:scale-105 transition-all cursor-pointer">Add Zone</button>
            </div>
          </form>
        </div>
      )}
    </div>
  )
}
