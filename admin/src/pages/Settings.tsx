import { useState, useEffect } from 'react'
import { Save } from 'lucide-react'
import { getSettings } from '@/lib/services'
import { apiUpdateSettings } from '@/lib/admin-api'
import type { SystemSettings } from '@/types'
import toast from 'react-hot-toast'

export default function Settings() {
  const [settings, setSettings] = useState<SystemSettings | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    getSettings().then(setSettings).catch(() => {}).finally(() => setLoading(false))
  }, [])

  const toggle = (key: keyof SystemSettings) => {
    if (!settings || typeof settings[key] !== 'boolean') return
    setSettings({ ...settings, [key]: !settings[key] as boolean })
  }

  const update = (key: keyof SystemSettings, val: number) => {
    if (!settings) return
    setSettings({ ...settings, [key]: val })
  }

  const handleSave = async () => {
    if (!settings) return
    setSaving(true)
    try { await apiUpdateSettings(settings); toast.success('Settings saved') }
    catch { toast.error('Failed to save settings') }
    finally { setSaving(false) }
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-[60vh]">
        <div className="flex flex-col items-center gap-3">
          <div className="w-8 h-8 border-2 border-biro-blue border-t-transparent rounded-full animate-spin" />
          <p className="text-xs text-text-gray font-medium">Loading settings...</p>
        </div>
      </div>
    )
  }

  if (!settings) return null

  return (
    <div className="flex flex-col gap-8 max-w-3xl">
      <div><h3 className="text-lg font-extrabold text-white">System Settings</h3><p className="text-xs text-text-gray">Configure dispatch operations, pricing, and rider assignment rules</p></div>

      <div className="p-6 rounded-3xl bg-admin-surface border border-admin-border flex flex-col gap-6">
        <h4 className="text-xs font-bold text-text-gray uppercase tracking-wider">Dispatch Operations</h4>
        <ToggleRow label="Auto-Assign Riders" desc="When ON, system automatically assigns the nearest available rider. When OFF, dispatch center shows manual rider selection." enabled={settings.autoAssignRider} onToggle={() => toggle('autoAssignRider')} />
        <ToggleRow label="Surge Pricing" desc="Enable dynamic surge pricing during peak hours and high-demand periods" enabled={settings.surgePricingActive} onToggle={() => toggle('surgePricingActive')} />

        <div className="border-t border-admin-border pt-6 flex flex-col gap-4">
          <h4 className="text-xs font-bold text-text-gray uppercase tracking-wider">Thresholds & Limits</h4>
          <NumberRow label="Max Delivery Radius (KM)" value={settings.maxDeliveryRadiusKm} onChange={(v) => update('maxDeliveryRadiusKm', v)} />
          <NumberRow label="Surge Multiplier" value={settings.surgeMultiplier} onChange={(v) => update('surgeMultiplier', v)} step={0.1} />
          <NumberRow label="Service Fee (%)" value={settings.serviceFeePercent} onChange={(v) => update('serviceFeePercent', v)} />
          <NumberRow label="Min Wallet Balance (NGN)" value={settings.minWalletBalance} onChange={(v) => update('minWalletBalance', v)} />
          <NumberRow label="Rider Assignment Timeout (min)" value={settings.riderAssignmentTimeoutMinutes} onChange={(v) => update('riderAssignmentTimeoutMinutes', v)} />
          <NumberRow label="Max Active Deliveries Per Rider" value={settings.maxActiveDeliveriesPerRider} onChange={(v) => update('maxActiveDeliveriesPerRider', v)} />
        </div>
      </div>

      <button onClick={handleSave} disabled={saving} className="self-start flex items-center gap-2 px-6 py-3 rounded-xl bg-biro-blue text-xs font-bold text-white shadow-lg shadow-biro-blue/20 hover:scale-105 transition-all disabled:opacity-50 cursor-pointer">
        <Save className="w-4 h-4" /> {saving ? 'Saving...' : 'Save Settings'}
      </button>
    </div>
  )
}

function ToggleRow({ label, desc, enabled, onToggle }: { label: string; desc: string; enabled: boolean; onToggle: () => void }) {
  return (
    <div className="flex items-center justify-between py-2">
      <div><p className="text-sm font-bold text-white">{label}</p><p className="text-[11px] text-text-gray mt-0.5 max-w-lg">{desc}</p></div>
      <button onClick={onToggle} className={`relative w-12 h-6 rounded-full transition-all cursor-pointer ${enabled ? 'bg-biro-blue' : 'bg-admin-border'}`}>
        <span className={`absolute top-0.5 w-5 h-5 bg-white rounded-full shadow transition-all ${enabled ? 'left-6' : 'left-0.5'}`} />
      </button>
    </div>
  )
}

function NumberRow({ label, value, onChange, step }: { label: string; value: number; onChange: (v: number) => void; step?: number }) {
  return (
    <div className="flex items-center justify-between py-2">
      <p className="text-xs font-semibold text-white">{label}</p>
      <input type="number" value={value} step={step || 1} onChange={(e) => onChange(parseFloat(e.target.value) || 0)} className="w-24 text-center bg-admin-bg border border-admin-border rounded-lg px-3 py-2 text-xs text-white font-bold outline-none focus:border-biro-blue" />
    </div>
  )
}
