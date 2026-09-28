"use client";
import React, { useEffect, useMemo, useState } from "react";
import { collection, deleteDoc, doc, onSnapshot, addDoc, updateDoc, setDoc, Timestamp, writeBatch } from "firebase/firestore";
import { MapPin, Plus, Trash2, Edit3, Save, X, Search, RefreshCw, AlertTriangle, Check } from "lucide-react";
import { RegistryEntry, isBeninCityCoord } from "./LiveTrackingMap";

type ToastFn = (type: "success" | "error" | "info", message: string) => void;

interface Props {
  db: any;
  addLog: (action: string, details: string) => Promise<void> | void;
  addToast: ToastFn;
}

const DEFAULT_GEO = { lat: 6.3350, lng: 5.6037 };

const inputCls = "w-full h-10 bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3.5 text-xs text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFB800]/40";
const labelCls = "block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 mb-1 uppercase tracking-wider";

const SEED_COORDS: Record<string, { lat: number; lng: number }> = {
  "ring road": { lat: 6.3350, lng: 5.6037 },
  "king's square": { lat: 6.3350, lng: 5.6037 },
  "kings square": { lat: 6.3350, lng: 5.6037 },
  "oba market": { lat: 6.3365, lng: 5.6040 },
  "oba palace": { lat: 6.3325, lng: 5.6010 },
  "ugbowo": { lat: 6.3980, lng: 5.6120 },
  "uniben": { lat: 6.4020, lng: 5.6140 },
  "ubth": { lat: 6.3910, lng: 5.6105 },
  "gra": { lat: 6.3150, lng: 5.6180 },
  "boundary road": { lat: 6.3120, lng: 5.6190 },
  "ihama": { lat: 6.3180, lng: 5.6160 },
  "airport road": { lat: 6.3080, lng: 5.5980 },
  "airport": { lat: 6.3170, lng: 5.5995 },
  "ikpoba hill": { lat: 6.3520, lng: 5.6550 },
  "ramat park": { lat: 6.3540, lng: 5.6580 },
  "sapele road": { lat: 6.3000, lng: 5.6350 },
  "country home": { lat: 6.2950, lng: 5.6380 },
  "new benin": { lat: 6.3450, lng: 5.6310 },
  "uselu": { lat: 6.3680, lng: 5.6150 },
  "ekenwan": { lat: 6.3200, lng: 5.5800 },
  "siluko": { lat: 6.3580, lng: 5.5950 },
  "aduwawa": { lat: 6.3750, lng: 5.6700 },
  "upper sakponba": { lat: 6.3150, lng: 5.6550 },
  "st saviour": { lat: 6.3050, lng: 5.6600 },
  "ugbor": { lat: 6.2850, lng: 5.6150 },
  "etete": { lat: 6.2980, lng: 5.6180 },
  "textile mill": { lat: 6.3620, lng: 5.6020 },
  "mission road": { lat: 6.3390, lng: 5.6080 },
  "akpakpava": { lat: 6.3380, lng: 5.6140 },
  "adesuwa": { lat: 6.3130, lng: 5.6150 },
  "benin": { lat: 6.3350, lng: 5.6037 },
};

const ACRONYMS: Record<string, string> = { gra: "GRA", uniben: "UNIBEN", ubth: "UBTH" };

function landmarkName(key: string): string {
  return key.split(" ").map(w => ACRONYMS[w] || (w.charAt(0).toUpperCase() + w.slice(1))).join(" ");
}

interface FormState {
  id: string | null;
  name: string;
  tags: string;
  lat: string;
  lng: string;
  zone: string;
  active: boolean;
}

const emptyForm: FormState = { id: null, name: "", tags: "", lat: "", lng: "", zone: "Benin City", active: true };

export default function AddressBookTab({ db, addLog, addToast }: Props) {
  const [entries, setEntries] = useState<RegistryEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [form, setForm] = useState<FormState | null>(null);
  const [saving, setSaving] = useState(false);
  const [seeding, setSeeding] = useState(false);
  const [formError, setFormError] = useState("");
  const [pendingDelete, setPendingDelete] = useState<RegistryEntry | null>(null);
  const [geo, setGeo] = useState(DEFAULT_GEO);
  const [geoLat, setGeoLat] = useState(String(DEFAULT_GEO.lat));
  const [geoLng, setGeoLng] = useState(String(DEFAULT_GEO.lng));
  const [geoError, setGeoError] = useState("");
  const [savingGeo, setSavingGeo] = useState(false);

  useEffect(() => {
    const unsub = onSnapshot(collection(db, "address_registry"), snap => {
      const list: RegistryEntry[] = [];
      snap.forEach(d => {
        const x = d.data();
        list.push({
          id: d.id,
          name: x.name || "",
          tags: Array.isArray(x.tags) ? x.tags : [],
          lat: typeof x.lat === "number" ? x.lat : Number(x.lat),
          lng: typeof x.lng === "number" ? x.lng : Number(x.lng),
          zone: x.zone || "Benin City",
          active: x.active !== false,
        });
      });
      list.sort((a, b) => a.name.localeCompare(b.name));
      setEntries(list);
      setLoading(false);
    }, () => setLoading(false));
    return () => unsub();
  }, [db]);

  useEffect(() => {
    const unsub = onSnapshot(doc(db, "system_config", "geo"), snap => {
      if (snap.exists()) {
        const x = snap.data();
        const lat = typeof x.centerLat === "number" ? x.centerLat : DEFAULT_GEO.lat;
        const lng = typeof x.centerLng === "number" ? x.centerLng : DEFAULT_GEO.lng;
        setGeo({ lat, lng });
        setGeoLat(String(lat));
        setGeoLng(String(lng));
      }
    }, () => {});
    return () => unsub();
  }, [db]);

  const filtered = useMemo(() => {
    const q = search.toLowerCase().trim();
    if (!q) return entries;
    return entries.filter(e =>
      (e.name || "").toLowerCase().includes(q) ||
      (e.zone || "").toLowerCase().includes(q) ||
      (e.tags || []).some(t => String(t).toLowerCase().includes(q))
    );
  }, [entries, search]);

  const openAdd = () => { setForm({ ...emptyForm }); setFormError(""); };
  const openEdit = (e: RegistryEntry) => {
    setForm({ id: e.id, name: e.name, tags: (e.tags || []).join(", "), lat: String(e.lat), lng: String(e.lng), zone: e.zone || "Benin City", active: e.active !== false });
    setFormError("");
  };
  const closeForm = () => { setForm(null); setFormError(""); };

  const saveEntry = async () => {
    if (!form) return;
    const name = form.name.trim();
    if (!name) { setFormError("Enter a name for this address."); return; }
    const lat = Number(form.lat);
    const lng = Number(form.lng);
    if (!form.lat.trim() || !form.lng.trim() || isNaN(lat) || isNaN(lng)) {
      setFormError("Enter numeric latitude and longitude values.");
      return;
    }
    if (!isBeninCityCoord(lat, lng)) {
      setFormError("Coordinates must be within Benin City.");
      addToast("error", "Coordinates must be within Benin City.");
      return;
    }
    const tags = form.tags.split(",").map(t => t.trim().toLowerCase()).filter(Boolean);
    const data = {
      name,
      tags,
      lat,
      lng,
      zone: form.zone.trim() || "Benin City",
      active: form.active,
      updatedAt: Timestamp.now(),
    };
    setSaving(true);
    try {
      if (form.id) {
        await updateDoc(doc(db, "address_registry", form.id), data);
        await addLog("Address Registry", `Updated: ${name}`);
        addToast("success", "Address updated.");
      } else {
        await addDoc(collection(db, "address_registry"), data);
        await addLog("Address Registry", `Created: ${name}`);
        addToast("success", "Address added.");
      }
      closeForm();
    } catch (e: any) {
      addToast("error", "Could not save this address: " + (e?.message || "Unknown error"));
    } finally {
      setSaving(false);
    }
  };

  const confirmDelete = async () => {
    if (!pendingDelete) return;
    try {
      await deleteDoc(doc(db, "address_registry", pendingDelete.id));
      await addLog("Address Registry", `Deleted: ${pendingDelete.name}`);
      addToast("success", "Address removed.");
    } catch (e: any) {
      addToast("error", "Could not delete this address: " + (e?.message || "Unknown error"));
    } finally {
      setPendingDelete(null);
    }
  };

  const toggleActive = async (e: RegistryEntry) => {
    const next = e.active === false;
    try {
      await updateDoc(doc(db, "address_registry", e.id), { active: next, updatedAt: Timestamp.now() });
      await addLog("Address Registry", `${next ? "Activated" : "Deactivated"}: ${e.name}`);
    } catch (err: any) {
      addToast("error", "Could not update this address: " + (err?.message || "Unknown error"));
    }
  };

  const seedDefaults = async () => {
    if (entries.length > 0) return;
    setSeeding(true);
    try {
      const batch = writeBatch(db);
      const keys = Object.keys(SEED_COORDS);
      keys.forEach(key => {
        batch.set(doc(collection(db, "address_registry")), {
          name: landmarkName(key),
          tags: [key],
          lat: SEED_COORDS[key].lat,
          lng: SEED_COORDS[key].lng,
          zone: "Benin City",
          active: true,
          updatedAt: Timestamp.now(),
        });
      });
      await batch.commit();
      await addLog("Address Registry", `Loaded ${keys.length} default landmarks`);
      addToast("success", `${keys.length} default landmarks loaded.`);
    } catch (e: any) {
      addToast("error", "Could not load default landmarks: " + (e?.message || "Unknown error"));
    } finally {
      setSeeding(false);
    }
  };

  const saveGeo = async () => {
    const lat = Number(geoLat);
    const lng = Number(geoLng);
    if (!geoLat.trim() || !geoLng.trim() || isNaN(lat) || isNaN(lng)) {
      setGeoError("Enter numeric latitude and longitude values.");
      return;
    }
    if (!isBeninCityCoord(lat, lng)) {
      setGeoError("Coordinates must be within Benin City.");
      addToast("error", "Coordinates must be within Benin City.");
      return;
    }
    setGeoError("");
    setSavingGeo(true);
    try {
      await setDoc(doc(db, "system_config", "geo"), { centerLat: lat, centerLng: lng, updatedAt: Timestamp.now() }, { merge: true });
      setGeo({ lat, lng });
      await addLog("Address Registry", `Updated default map center: ${lat}, ${lng}`);
      addToast("success", "Default map center saved.");
    } catch (e: any) {
      addToast("error", "Could not save the map center: " + (e?.message || "Unknown error"));
    } finally {
      setSavingGeo(false);
    }
  };

  return <div className="tab-content space-y-6">
    <div className="flex items-center justify-between flex-wrap gap-4">
      <div>
        <h1 className="text-xl font-black text-[#111] dark:text-white flex items-center gap-2">
          <MapPin className="w-5 h-5 text-[#FFB800]" /> Address Book
        </h1>
        <p className="text-xs text-gray-600 dark:text-gray-400 mt-1">
          {entries.length} landmark{entries.length === 1 ? "" : "s"} with coordinates used for dispatch routing
        </p>
      </div>
      <div className="flex items-center gap-2.5 flex-wrap">
        <button
          onClick={seedDefaults}
          disabled={seeding || loading || entries.length > 0}
          title={entries.length > 0 ? "Default landmarks are already loaded" : "Load default landmarks"}
          className="h-10 px-3.5 bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300 rounded-xl text-xs font-bold hover:bg-gray-200 dark:hover:bg-gray-700 disabled:opacity-40 disabled:cursor-not-allowed transition-colors flex items-center gap-1.5 cursor-pointer"
        >
          <RefreshCw size={14} className={seeding ? "animate-spin" : ""} /> {seeding ? "Loading..." : "Load default landmarks"}
        </button>
        <button onClick={openAdd} className="h-10 px-4 bg-[#FFB800] hover:bg-[#FFB800]/90 text-[#111] rounded-xl text-xs font-black shadow-xs flex items-center gap-1.5 transition-all cursor-pointer">
          <Plus size={14} /> Add Address
        </button>
      </div>
    </div>

    {/* Default map center */}
    <div className="bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-5 shadow-sm space-y-3">
      <div className="flex items-center gap-2 pb-2 border-b border-black/10 dark:border-white/10">
        <MapPin className="w-4 h-4 text-[#FFB800]" />
        <span className="text-xs font-black text-[#111] dark:text-white uppercase tracking-wide">Default map center (Benin City)</span>
      </div>
      <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-3 items-end">
        <div>
          <label className={labelCls}>Center Latitude</label>
          <input type="number" step="0.0001" value={geoLat} onChange={e => setGeoLat(e.target.value)} className={inputCls} />
        </div>
        <div>
          <label className={labelCls}>Center Longitude</label>
          <input type="number" step="0.0001" value={geoLng} onChange={e => setGeoLng(e.target.value)} className={inputCls} />
        </div>
        <div className="flex items-center gap-2">
          <button onClick={saveGeo} disabled={savingGeo} className="h-10 px-5 bg-[#FFB800] hover:bg-[#FFB800]/90 text-[#111] rounded-xl text-xs font-black shadow-xs flex items-center gap-1.5 disabled:opacity-50 transition-all cursor-pointer">
            {savingGeo ? <RefreshCw size={14} className="animate-spin" /> : <Save size={14} />} Save Center
          </button>
          <span className="text-[10px] text-gray-500 dark:text-gray-400 font-medium">Shown when no live positions exist.</span>
        </div>
      </div>
      {geoError && <p className="text-[11px] font-bold text-red-500">{geoError}</p>}
      <p className="text-[10px] text-gray-500 dark:text-gray-400">Current center: {geo.lat.toFixed(4)}, {geo.lng.toFixed(4)}</p>
    </div>

    {/* Search */}
    <div className="relative group flex items-center w-full sm:max-w-sm">
      <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400 dark:text-gray-500 group-focus-within:text-[#FFB800] pointer-events-none transition-colors z-10" />
      <input
        type="text"
        value={search}
        onChange={e => setSearch(e.target.value)}
        placeholder="Search name, zone or tag..."
        className="h-10 w-full !pl-12 !pr-9 bg-white dark:bg-[#1c1c1c] border border-gray-300 dark:border-white/15 rounded-xl text-xs text-gray-900 dark:text-white placeholder:text-gray-400 dark:placeholder:text-gray-500 focus:outline-none focus:ring-2 focus:ring-[#FFB800]/30 focus:border-[#FFB800] transition-all shadow-2xs"
      />
      {search && <button type="button" onClick={() => setSearch("")} className="absolute right-3 top-1/2 -translate-y-1/2 p-1 text-gray-400 hover:text-gray-700 dark:hover:text-white rounded-md transition-colors cursor-pointer z-10" title="Clear search"><X className="w-3.5 h-3.5" /></button>}
    </div>

    {/* Registry list */}
    {loading ? (
      <div className="bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-10 text-center">
        <div className="animate-pulse text-xs font-bold text-black/40 dark:text-white/40">Loading addresses...</div>
      </div>
    ) : entries.length === 0 ? (
      <div className="bg-white dark:bg-[#1a1a1a] border border-dashed border-black/15 dark:border-white/15 rounded-3xl p-10 text-center space-y-2">
        <div className="w-12 h-12 rounded-2xl bg-[#FFB800]/15 text-[#FFB800] flex items-center justify-center mx-auto"><MapPin className="w-6 h-6" /></div>
        <p className="text-sm font-black text-[#111] dark:text-white">No addresses yet — load defaults or add one</p>
        <p className="text-xs text-gray-500 dark:text-gray-400 max-w-sm mx-auto">Coordinates stored here are what the dispatch map and new shipments use to plot pickup and drop-off points.</p>
        <div className="flex items-center justify-center gap-2 pt-1 flex-wrap">
          <button onClick={seedDefaults} disabled={seeding} className="h-10 px-4 bg-[#FFB800] hover:bg-[#FFB800]/90 text-[#111] rounded-xl text-xs font-black flex items-center gap-1.5 disabled:opacity-50 transition-all cursor-pointer">
            {seeding ? <RefreshCw size={14} className="animate-spin" /> : <RefreshCw size={14} />} Load default landmarks
          </button>
          <button onClick={openAdd} className="h-10 px-4 bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300 rounded-xl text-xs font-bold hover:bg-gray-200 dark:hover:bg-gray-700 transition-colors cursor-pointer">Add Address</button>
        </div>
      </div>
    ) : filtered.length === 0 ? (
      <div className="bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-8 text-center space-y-1">
        <p className="text-sm font-black text-[#111] dark:text-white">No addresses match your search</p>
        <p className="text-xs text-gray-500 dark:text-gray-400">Try a different landmark name, zone or tag.</p>
        <button onClick={() => setSearch("")} className="mt-2 h-9 px-4 bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300 rounded-xl text-xs font-bold hover:bg-gray-200 dark:hover:bg-gray-700 transition-colors cursor-pointer">Clear search</button>
      </div>
    ) : (
      <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
        {filtered.map((e, i) => (
          <div key={e.id} className={"bg-white dark:bg-[#1a1a1a] border rounded-3xl p-4 shadow-sm flex flex-col gap-2.5 transition-all " + (e.active !== false ? "border-black/10 dark:border-white/10 hover:border-[#FFB800]/50" : "border-black/5 dark:border-white/5 opacity-70")}>
            <div className="flex items-start justify-between gap-3">
              <div className="min-w-0">
                <p className="text-xs font-black text-[#111] dark:text-white truncate">{e.name || "Untitled"}</p>
                <p className="text-[10px] text-gray-500 dark:text-gray-400 font-medium truncate">{e.zone || "Benin City"}</p>
              </div>
              <div className="flex items-center gap-1 shrink-0">
                <button onClick={() => openEdit(e)} title="Edit address" aria-label={`Edit ${e.name}`} className="p-2 min-w-[36px] min-h-[36px] text-[#FFB800] hover:bg-[#FFB800]/10 rounded-lg transition-colors cursor-pointer"><Edit3 size={14} /></button>
                <button onClick={() => setPendingDelete(e)} title="Delete address" aria-label={`Delete ${e.name}`} className="p-2 min-w-[36px] min-h-[36px] text-red-500 hover:bg-red-50 dark:hover:bg-red-900/30 rounded-lg transition-colors cursor-pointer"><Trash2 size={14} /></button>
              </div>
            </div>
            <div className="flex flex-wrap gap-1">
              {(e.tags || []).slice(0, 4).map(t => (
                <span key={t} className="text-[9px] font-bold text-black/50 dark:text-white/50 bg-gray-100 dark:bg-[#222] px-2 py-0.5 rounded-full">{t}</span>
              ))}
            </div>
            <div className="flex items-center justify-between gap-2 pt-2 border-t border-black/5 dark:border-white/5 mt-auto">
              <span className="text-[10px] font-mono font-bold text-gray-600 dark:text-gray-300">{Number(e.lat).toFixed(4)}, {Number(e.lng).toFixed(4)}</span>
              <button
                onClick={() => toggleActive(e)}
                aria-label={`${e.active !== false ? "Deactivate" : "Activate"} ${e.name}`}
                className={"relative h-6 w-11 rounded-full transition-colors cursor-pointer shrink-0 " + (e.active !== false ? "bg-[#FFB800]" : "bg-gray-300 dark:bg-gray-700")}
              >
                <span className={"absolute top-0.5 h-5 w-5 rounded-full bg-white shadow transition-all " + (e.active !== false ? "left-[22px]" : "left-0.5")} />
              </button>
            </div>
            <span className={"text-[9px] font-black uppercase tracking-wider " + (e.active !== false ? "text-emerald-600 dark:text-emerald-400" : "text-gray-400")}>{e.active !== false ? "Active" : "Inactive"}</span>
          </div>
        ))}
      </div>
    )}

    {/* Add / Edit modal */}
    {form && <div className="fixed inset-0 bg-black/50 backdrop-blur-md flex items-center justify-center p-4 z-50" onClick={closeForm}>
      <div className="animate-scale-in bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-6 w-full max-w-lg shadow-2xl space-y-5" onClick={e => e.stopPropagation()}>
        <div className="flex items-center justify-between border-b border-black/10 dark:border-white/10 pb-3">
          <h3 className="text-base font-black text-[#111] dark:text-white flex items-center gap-2"><MapPin className="w-5 h-5 text-[#FFB800]" /> {form.id ? "Edit Address" : "Add Address"}</h3>
          <button onClick={closeForm} aria-label="Close" className="text-xs font-bold text-gray-500 dark:text-gray-400 hover:text-red-500 cursor-pointer p-2 rounded-lg"><X size={18} /></button>
        </div>

        <div className="grid sm:grid-cols-2 gap-3">
          <div className="sm:col-span-2">
            <label className={labelCls}>Landmark / Address Name *</label>
            <input type="text" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} placeholder="e.g. Ring Road, King's Square" className={inputCls} />
          </div>
          <div className="sm:col-span-2">
            <label className={labelCls}>Search Tags (comma separated)</label>
            <input type="text" value={form.tags} onChange={e => setForm({ ...form, tags: e.target.value })} placeholder="e.g. ring road, city center" className={inputCls} />
          </div>
          <div>
            <label className={labelCls}>Latitude *</label>
            <input type="number" step="0.0001" value={form.lat} onChange={e => setForm({ ...form, lat: e.target.value })} placeholder="6.3350" className={inputCls} />
          </div>
          <div>
            <label className={labelCls}>Longitude *</label>
            <input type="number" step="0.0001" value={form.lng} onChange={e => setForm({ ...form, lng: e.target.value })} placeholder="5.6037" className={inputCls} />
          </div>
          <div>
            <label className={labelCls}>Zone</label>
            <input type="text" value={form.zone} onChange={e => setForm({ ...form, zone: e.target.value })} className={inputCls} />
          </div>
          <div className="flex items-end">
            <label className="flex items-center gap-2.5 cursor-pointer select-none pb-2">
              <input type="checkbox" checked={form.active} onChange={e => setForm({ ...form, active: e.target.checked })} className="w-4 h-4 accent-[#FFB800] cursor-pointer" />
              <span className="text-xs font-bold text-[#111] dark:text-white">Active (used for routing)</span>
            </label>
          </div>
        </div>

        {formError && <p className="text-[11px] font-bold text-red-500 flex items-center gap-1.5"><AlertTriangle size={13} /> {formError}</p>}

        <div className="flex items-center justify-end gap-3 pt-2 border-t border-black/10 dark:border-white/10">
          <button onClick={closeForm} className="h-10 px-4 bg-gray-100 dark:bg-gray-800 text-gray-800 dark:text-gray-200 rounded-xl text-xs font-bold hover:bg-gray-200 dark:hover:bg-gray-700 cursor-pointer transition-colors">Cancel</button>
          <button onClick={saveEntry} disabled={saving} className="h-10 px-5 bg-[#FFB800] hover:bg-[#FFB800]/90 text-[#111] rounded-xl text-xs font-black disabled:opacity-50 transition-all shadow-xs flex items-center gap-1.5 cursor-pointer">
            {saving ? <RefreshCw size={14} className="animate-spin" /> : <Save size={14} />} {saving ? "Saving..." : "Save"}
          </button>
        </div>
      </div>
    </div>}

    {/* Delete confirmation */}
    {pendingDelete && <div className="fixed inset-0 bg-black/60 backdrop-blur-md flex items-center justify-center p-4 z-50" onClick={() => setPendingDelete(null)}>
      <div className="animate-scale-in bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-6 w-full max-w-md shadow-2xl space-y-4" onClick={e => e.stopPropagation()}>
        <h3 className="text-base font-black text-[#111] dark:text-white flex items-center gap-2"><AlertTriangle className="w-5 h-5 text-red-500" /> Delete this address?</h3>
        <p className="text-xs text-gray-600 dark:text-gray-300 font-medium leading-relaxed">
          <span className="font-black text-[#111] dark:text-white">{pendingDelete.name}</span> will be removed from the Address Book. Shipments that only rely on this landmark will no longer plot a point until coordinates are set again.
        </p>
        <div className="flex items-center justify-end gap-3 pt-2">
          <button onClick={() => setPendingDelete(null)} className="h-10 px-4 bg-gray-100 dark:bg-white/10 text-gray-700 dark:text-gray-300 rounded-xl text-xs font-bold hover:bg-gray-200 dark:hover:bg-white/15 cursor-pointer transition-colors">Cancel</button>
          <button onClick={confirmDelete} className="h-10 px-5 bg-red-600 hover:bg-red-700 text-white rounded-xl text-xs font-black transition-colors cursor-pointer shadow-xs flex items-center gap-1.5"><Check size={14} /> Delete</button>
        </div>
      </div>
    </div>}
  </div>;
}
