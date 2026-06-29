import { useState, useEffect } from 'react'
import { Phone, MapPin, Plus, Star } from 'lucide-react'
import { getRiders } from '@/lib/services'
import { apiCreateRider, apiDeleteRider } from '@/lib/admin-api'
import type { Rider } from '@/types'
import toast from 'react-hot-toast'

export default function Riders() {
  const [riders, setRiders] = useState<Rider[]>([])
  const [loading, setLoading] = useState(true)
  const [showAdd, setShowAdd] = useState(false)
  const [form, setForm] = useState({ name: '', phone: '', bike: '' })

  const loadData = () => {
    setLoading(true)
    getRiders().then(setRiders).catch(() => toast.error('Failed to load riders')).finally(() => setLoading(false))
  }

  useEffect(() => { loadData() }, [])

  const handleAdd = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.name || !form.bike) return
    try {
      const { data: created } = await apiCreateRider({ name: form.name, phone: form.phone || '+234 800 000 0000', bikeNumber: form.bike })
      setRiders([created, ...riders])
      setForm({ name: '', phone: '', bike: '' }); setShowAdd(false)
      toast.success('Rider enrolled')
    } catch { toast.error('Failed to add rider') }
  }

  const handleDelete = async (id: string) => {
    try { await apiDeleteRider(id); setRiders((prev) => prev.filter((r) => r.id !== id)); toast.success('Rider removed') }
    catch { toast.error('Failed to remove rider') }
  }

  const statusStyle = (s: string) => {
    if (s === 'delivering') return 'bg-indigo-500/10 text-indigo-500'
    if (s === 'active') return 'bg-success-green/10 text-success-green'
    return 'bg-danger-red/10 text-red-400'
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-[60vh]">
        <div className="flex flex-col items-center gap-3">
          <div className="w-8 h-8 border-2 border-biro-blue border-t-transparent rounded-full animate-spin" />
          <p className="text-xs text-text-gray font-medium">Loading riders...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-8">
      <div className="flex items-center justify-between">
        <div><h3 className="text-lg font-extrabold text-white">Active Riders Registry</h3><p className="text-xs text-text-gray">Manage system dispatchers, ratings, and vehicle numbers</p></div>
        <button onClick={() => setShowAdd(true)} className="flex items-center gap-2 px-5 h-11 rounded-xl bg-biro-blue text-xs font-bold text-white shadow-lg shadow-biro-blue/20 hover:scale-105 transition-all cursor-pointer"><Plus className="w-4 h-4" /> Add Rider</button>
      </div>

      <div className="grid grid-cols-3 gap-6">
        {riders.map((r) => (
          <div key={r.id} className="p-6 rounded-2xl bg-admin-surface border border-admin-border flex flex-col gap-4 hover:border-biro-blue transition-all relative group">
            <button onClick={() => handleDelete(r.id)} className="absolute top-3 right-3 w-6 h-6 rounded-full bg-danger-red/10 text-danger-red opacity-0 group-hover:opacity-100 flex items-center justify-center text-[10px] font-bold hover:bg-danger-red/20 transition-all cursor-pointer" title="Remove rider">✕</button>
            <div className="flex items-center justify-between">
              <div>
                <h4 className="text-sm font-extrabold text-white">{r.name}</h4>
                <span className={`text-[10px] font-bold mt-1 inline-block px-2 py-0.5 rounded ${statusStyle(r.status)}`}>{r.status.toUpperCase()}</span>
              </div>
              <div className="text-right">
                <p className="text-xs font-bold text-white flex items-center gap-1"><Star className="w-3 h-3 text-royal-gold" /> {r.rating}</p>
                <p className="text-[10px] text-text-gray mt-0.5">{r.totalDeliveries} deliveries</p>
              </div>
            </div>
            <div className="border-t border-admin-border pt-4 flex flex-col gap-2">
              <div className="flex items-center gap-2 text-text-gray text-xs"><Phone className="w-3.5 h-3.5" /> <span>{r.phone}</span></div>
              <div className="flex items-center gap-2 text-text-gray text-xs"><MapPin className="w-3.5 h-3.5" /> <span>Bike: {r.bikeNumber}</span></div>
              {r.assignedZone && <div className="flex items-center gap-2 text-text-gray text-xs"><MapPin className="w-3.5 h-3.5" /> <span>Zone: {r.assignedZone}</span></div>}
            </div>
          </div>
        ))}
      </div>

      {showAdd && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50">
          <form onSubmit={handleAdd} className="w-[450px] p-8 rounded-3xl bg-admin-surface border border-admin-border flex flex-col gap-5">
            <div><h3 className="text-base font-extrabold text-white">Add New Dispatch Rider</h3><p className="text-xs text-text-gray">Enroll a rider into the dispatch system</p></div>
            <div className="flex flex-col gap-3">
              <input type="text" placeholder="Rider Full Name" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required className="bg-admin-bg border border-admin-border rounded-xl px-4 py-3 text-xs text-white outline-none focus:border-biro-blue" />
              <input type="text" placeholder="Rider Phone Number" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} className="bg-admin-bg border border-admin-border rounded-xl px-4 py-3 text-xs text-white outline-none focus:border-biro-blue" />
              <input type="text" placeholder="Motorcycle Plate (e.g. LAG-1029-YZ)" value={form.bike} onChange={(e) => setForm({ ...form, bike: e.target.value })} required className="bg-admin-bg border border-admin-border rounded-xl px-4 py-3 text-xs text-white outline-none focus:border-biro-blue" />
            </div>
            <div className="flex gap-3 justify-end mt-2">
              <button type="button" onClick={() => setShowAdd(false)} className="px-4 py-2 text-xs font-bold text-text-gray hover:text-white cursor-pointer">Cancel</button>
              <button type="submit" className="px-5 py-2.5 rounded-xl bg-biro-blue text-xs font-bold text-white hover:scale-105 transition-all cursor-pointer">Enroll Rider</button>
            </div>
          </form>
        </div>
      )}
    </div>
  )
}
