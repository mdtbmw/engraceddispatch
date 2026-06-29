import { useState, useEffect } from 'react'
import { Check, Copy, Plus } from 'lucide-react'
import StatusBadge from '@/components/StatusBadge'
import { getDeliveries, getActiveRiders } from '@/lib/services'
import {
  apiCreateDelivery, apiUpdateDeliveryStatus, apiVerifyDeliveryOtp,
  apiDeleteDelivery, apiAssignRider,
} from '@/lib/admin-api'
import type { Delivery, Rider } from '@/types'
import toast from 'react-hot-toast'

export default function DispatchCenter() {
  const [deliveries, setDeliveries] = useState<Delivery[]>([])
  const [riders, setRiders] = useState<Rider[]>([])
  const [loading, setLoading] = useState(true)
  const [selectedDelivery, setSelectedDelivery] = useState<Delivery | null>(null)
  const [otpValue, setOtpValue] = useState('')
  const [otpError, setOtpError] = useState('')
  const [showCreate, setShowCreate] = useState(false)
  const [newForm, setNewForm] = useState({ pickup: '', delivery: '', item: '', weight: '1.0', type: 'Express' })

  const loadData = () => {
    setLoading(true)
    Promise.all([getDeliveries(), getActiveRiders()])
      .then(([d, r]) => { setDeliveries(d); setRiders(r) })
      .catch(() => toast.error('Failed to load data'))
      .finally(() => setLoading(false))
  }

  useEffect(() => { loadData() }, [])

  const handleAssign = async (deliveryId: string, _riderName: string, rider: Rider) => {
    try {
      await apiAssignRider(deliveryId, rider.id)
      setDeliveries((prev) => prev.map((d) => d.id === deliveryId ? { ...d, status: 'ASSIGNED' as const, riderName: rider.name, riderBikeNumber: rider.bikeNumber, etaMinutes: 25 } : d))
      toast.success(`Assigned to ${rider.name}`)
    } catch { toast.error('Failed to assign rider') }
  }

  const handleAdvance = async (id: string, currentStatus: string) => {
    const flow: Record<string, string> = { ASSIGNED: 'PICKED_UP', PICKED_UP: 'OUT_FOR_DELIVERY', OUT_FOR_DELIVERY: 'DELIVERED' }
    const next = flow[currentStatus] || currentStatus
    try {
      await apiUpdateDeliveryStatus(id, next)
      setDeliveries((prev) => prev.map((d) => d.id === id ? { ...d, status: next as Delivery['status'] } : d))
      toast.success(`Status advanced to ${next.replace('_', ' ')}`)
    } catch { toast.error('Failed to advance status') }
  }

  const handleVerifyOtp = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!selectedDelivery) return
    try {
      await apiVerifyDeliveryOtp(selectedDelivery.id, otpValue)
      setDeliveries((prev) => prev.map((d) => d.id === selectedDelivery.id ? { ...d, status: 'DELIVERED', otpVerified: true } : d))
      setOtpValue(''); setOtpError(''); setSelectedDelivery(null)
      toast.success('Delivery verified!')
    } catch {
      setOtpError('Invalid 4-digit OTP code')
    }
  }

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault()
    try {
      const { data: created } = await apiCreateDelivery({
        deliveryType: newForm.type,
        pickupAddress: newForm.pickup,
        deliveryAddress: newForm.delivery,
        itemName: newForm.item,
        itemWeight: parseFloat(newForm.weight) || 1,
        customerName: 'Admin Created',
      })
      setDeliveries([created, ...deliveries])
      setShowCreate(false)
      setNewForm({ pickup: '', delivery: '', item: '', weight: '1.0', type: 'Express' })
      toast.success('Dispatch created')
    } catch { toast.error('Failed to create dispatch') }
  }

  const handleDelete = async (id: string) => {
    try {
      await apiDeleteDelivery(id)
      setDeliveries((prev) => prev.filter((d) => d.id !== id))
      toast.success('Dispatch deleted')
    } catch { toast.error('Failed to delete') }
  }

  const copyCode = (code: string) => { navigator.clipboard.writeText(code); toast.success('OTP copied') }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-[60vh]">
        <div className="flex flex-col items-center gap-3">
          <div className="w-8 h-8 border-2 border-biro-blue border-t-transparent rounded-full animate-spin" />
          <p className="text-xs text-text-gray font-medium">Loading dispatches...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="p-6 rounded-3xl bg-admin-surface border border-admin-border flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <div>
          <h3 className="text-lg font-extrabold text-white">Dispatch Operations Control</h3>
          <p className="text-xs text-text-gray">View timelines, assign riders, and verify OTP handovers</p>
        </div>
        <button onClick={() => setShowCreate(true)} className="flex items-center gap-2 px-5 h-11 rounded-xl bg-biro-blue text-xs font-bold text-white shadow-lg shadow-biro-blue/20 hover:scale-105 transition-all cursor-pointer">
          <Plus className="w-4 h-4" /> New Dispatch
        </button>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="border-b border-admin-border text-xs text-text-gray font-bold">
              <th className="pb-4">TRACKING / TYPE</th><th className="pb-4">ITEM</th><th className="pb-4">ROUTE</th><th className="pb-4">RIDER</th><th className="pb-4">STATUS</th><th className="pb-4 text-right">ACTIONS</th>
            </tr>
          </thead>
          <tbody>
            {deliveries.map((d) => (
              <tr key={d.id} className="border-b border-[#1A1A29]/40 text-xs text-[#E2E8F0] hover:bg-[#10101A]/35 transition-all">
                <td className="py-4">
                  <p className="font-extrabold text-white">{d.trackingNumber}</p>
                  <span className="text-[10px] font-bold text-biro-blue bg-biro-blue/10 px-2 py-0.5 rounded inline-block mt-1">{d.deliveryType}</span>
                </td>
                <td className="py-4"><p className="font-semibold">{d.itemName}</p><p className="text-text-gray text-[10px] mt-0.5">{d.itemWeight} KG</p></td>
                <td className="py-4">
                  <p className="font-medium">{d.pickupAddress?.split(',')[0]} &rarr;</p>
                  <p className="text-text-gray">{d.deliveryAddress?.split(',')[0]}</p>
                </td>
                <td className="py-4">
                  {d.riderName ? (
                    <div><p className="font-bold text-white">{d.riderName}</p><p className="text-text-gray text-[10px]">{d.riderBikeNumber}</p></div>
                  ) : (
                    <span className="text-warning-orange font-bold">Unassigned</span>
                  )}
                </td>
                <td className="py-4"><StatusBadge status={d.status} /></td>
                <td className="py-4 text-right">
                  <div className="flex gap-2 justify-end">
                    {d.status === 'PENDING' ? (
                      <select onChange={(e) => { if (e.target.value) { const r = riders.find((x) => x.id === e.target.value); if (r) handleAssign(d.id, r.name, r) } }} className="bg-admin-bg border border-admin-border text-[10px] text-white rounded px-1.5 py-1 outline-none font-bold" defaultValue="">
                        <option value="" disabled>Assign...</option>
                        {riders.map((r) => (<option key={r.id} value={r.id}>{r.name}</option>))}
                      </select>
                    ) : d.status !== 'DELIVERED' && d.status !== 'CANCELLED' ? (
                      <>
                        <button onClick={() => handleAdvance(d.id, d.status)} className="px-2 py-1 rounded bg-admin-hover border border-admin-border text-[10px] font-bold text-white hover:bg-admin-active cursor-pointer">Advance</button>
                        <button onClick={() => { setSelectedDelivery(d); setOtpValue(''); setOtpError('') }} className="px-2.5 py-1 rounded bg-biro-blue text-[10px] font-bold text-white hover:bg-biro-blue/80 cursor-pointer">Verify OTP</button>
                      </>
                    ) : (
                      <span className="text-text-gray font-bold flex items-center gap-1"><Check className="w-3.5 h-3.5 text-success-green" /> Done</span>
                    )}
                    {d.status === 'PENDING' && (
                      <button onClick={() => handleDelete(d.id)} className="px-2 py-1 rounded bg-danger-red/10 border border-danger-red/20 text-[10px] font-bold text-danger-red hover:bg-danger-red/20 cursor-pointer">Delete</button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {showCreate && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50">
          <form onSubmit={handleCreate} className="w-[450px] p-8 rounded-3xl bg-admin-surface border border-admin-border flex flex-col gap-5">
            <div><h3 className="text-base font-extrabold text-white">Create Custom Dispatch</h3><p className="text-xs text-text-gray">Manually schedule a pickup/delivery order</p></div>
            <div className="flex flex-col gap-3">
              <input type="text" placeholder="Pickup Location" value={newForm.pickup} onChange={(e) => setNewForm({ ...newForm, pickup: e.target.value })} required className="bg-admin-bg border border-admin-border rounded-xl px-4 py-3 text-xs text-white outline-none focus:border-biro-blue" />
              <input type="text" placeholder="Delivery Destination" value={newForm.delivery} onChange={(e) => setNewForm({ ...newForm, delivery: e.target.value })} required className="bg-admin-bg border border-admin-border rounded-xl px-4 py-3 text-xs text-white outline-none focus:border-biro-blue" />
              <input type="text" placeholder="Item Name" value={newForm.item} onChange={(e) => setNewForm({ ...newForm, item: e.target.value })} required className="bg-admin-bg border border-admin-border rounded-xl px-4 py-3 text-xs text-white outline-none focus:border-biro-blue" />
              <div className="grid grid-cols-2 gap-3">
                <input type="number" step="0.1" placeholder="Weight (KG)" value={newForm.weight} onChange={(e) => setNewForm({ ...newForm, weight: e.target.value })} className="bg-admin-bg border border-admin-border rounded-xl px-4 py-3 text-xs text-white outline-none" />
                <select value={newForm.type} onChange={(e) => setNewForm({ ...newForm, type: e.target.value })} className="bg-admin-bg border border-admin-border rounded-xl px-4 py-3 text-xs text-white outline-none font-bold">
                  <option value="Express">Express</option><option value="Economy">Economy</option><option value="Batch">Batch</option>
                </select>
              </div>
            </div>
            <div className="flex gap-3 justify-end mt-2">
              <button type="button" onClick={() => setShowCreate(false)} className="px-4 py-2 text-xs font-bold text-text-gray hover:text-white cursor-pointer">Cancel</button>
              <button type="submit" className="px-5 py-2.5 rounded-xl bg-biro-blue text-xs font-bold text-white hover:scale-105 transition-all cursor-pointer">Create Booking</button>
            </div>
          </form>
        </div>
      )}

      {selectedDelivery && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50">
          <form onSubmit={handleVerifyOtp} className="w-[400px] p-8 rounded-3xl bg-admin-surface border border-admin-border flex flex-col gap-5">
            <div className="text-center">
              <div className="w-12 h-12 bg-biro-blue/10 rounded-full flex items-center justify-center mx-auto mb-3"><Check className="w-6 h-6 text-biro-blue" /></div>
              <h3 className="text-base font-extrabold text-white">OTP Handover Verification</h3>
              <p className="text-xs text-text-gray mt-1">Enter customer's 4-digit code to complete delivery</p>
            </div>
            <div className="p-4 rounded-xl bg-admin-bg border border-admin-border text-center flex items-center justify-center gap-3">
              <span className="text-xl font-extrabold text-white tracking-widest">{selectedDelivery.otpCode}</span>
              <button type="button" onClick={() => copyCode(selectedDelivery.otpCode)} className="p-1.5 rounded-lg bg-admin-hover hover:bg-admin-active text-text-gray hover:text-white transition-all cursor-pointer" title="Copy OTP"><Copy className="w-3.5 h-3.5" /></button>
            </div>
            <input type="text" maxLength={4} placeholder="Enter 4-Digit OTP" value={otpValue} onChange={(e) => setOtpValue(e.target.value)} className="bg-admin-bg border border-admin-border rounded-xl px-4 py-3.5 text-center text-sm text-white font-extrabold tracking-widest outline-none focus:border-biro-blue" />
            {otpError && <p className="text-[11px] text-danger-red font-medium text-center">{otpError}</p>}
            <div className="flex gap-3 justify-end mt-2">
              <button type="button" onClick={() => { setSelectedDelivery(null); setOtpError('') }} className="px-4 py-2 text-xs font-bold text-text-gray hover:text-white cursor-pointer">Cancel</button>
              <button type="submit" className="px-5 py-2.5 rounded-xl bg-biro-blue text-xs font-bold text-white hover:scale-105 transition-all cursor-pointer">Verify & Complete</button>
            </div>
          </form>
        </div>
      )}
    </div>
  )
}
