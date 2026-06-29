import {
  collection, doc, getDocs, getDoc,
  query, where, orderBy,
  type DocumentData,
} from 'firebase/firestore'
import { getFirestoreDb } from './firebase'
import type { Delivery, Rider, Zone, PricingRule, User, SystemSettings, Transaction } from '@/types'

function db() { return getFirestoreDb() }

function snapToData<T>(snap: DocumentData): T {
  const d = snap.data()
  return { ...d, id: snap.id } as T
}

// ─── Deliveries (read-only) ─────────────────────────
export async function getDeliveries(): Promise<Delivery[]> {
  const q = query(collection(db(), 'deliveries'), orderBy('createdAt', 'desc'))
  const snap = await getDocs(q)
  return snap.docs.map((d) => snapToData<Delivery>(d))
}

export async function getDelivery(id: string): Promise<Delivery | null> {
  const snap = await getDoc(doc(db(), 'deliveries', id))
  return snap.exists() ? snapToData<Delivery>(snap) : null
}

// ─── Riders (read-only) ─────────────────────────────
export async function getRiders(): Promise<Rider[]> {
  const q = query(collection(db(), 'riders'), orderBy('joinedAt', 'desc'))
  const snap = await getDocs(q)
  return snap.docs.map((d) => snapToData<Rider>(d))
}

export async function getActiveRiders(): Promise<Rider[]> {
  const q = query(collection(db(), 'riders'), where('status', '==', 'active'), orderBy('rating', 'desc'))
  const snap = await getDocs(q)
  return snap.docs.map((d) => snapToData<Rider>(d))
}

// ─── Zones (read-only) ──────────────────────────────
export async function getZones(): Promise<Zone[]> {
  const q = query(collection(db(), 'zones'), orderBy('name'))
  const snap = await getDocs(q)
  return snap.docs.map((d) => snapToData<Zone>(d))
}

// ─── Pricing (read-only) ────────────────────────────
export async function getPricingRules(): Promise<PricingRule[]> {
  const snap = await getDocs(collection(db(), 'pricingRules'))
  return snap.docs.map((d) => snapToData<PricingRule>(d))
}

// ─── Users / Customers (read-only) ──────────────────
export async function getCustomers(): Promise<User[]> {
  const q = query(collection(db(), 'customers'), orderBy('createdAt', 'desc'))
  const snap = await getDocs(q)
  return snap.docs.map((d) => snapToData<User>(d))
}

// ─── Settings (read-only) ───────────────────────────
export async function getSettings(): Promise<SystemSettings | null> {
  const snap = await getDocs(collection(db(), 'settings'))
  if (snap.empty) return null
  return snapToData<SystemSettings>(snap.docs[0])
}

// ─── Transactions (read-only) ───────────────────────
export async function getTransactions(): Promise<Transaction[]> {
  const q = query(collection(db(), 'transactions'), orderBy('createdAt', 'desc'))
  const snap = await getDocs(q)
  return snap.docs.map((d) => snapToData<Transaction>(d))
}

// ─── Static / fallback data ─────────────────────────
export async function getRevenueData(): Promise<{ month: string; revenue: number; expenses: number }[]> {
  return [
    { month: 'Jan', revenue: 320000, expenses: 185000 },
    { month: 'Feb', revenue: 410000, expenses: 220000 },
    { month: 'Mar', revenue: 380000, expenses: 195000 },
    { month: 'Apr', revenue: 520000, expenses: 260000 },
    { month: 'May', revenue: 490000, expenses: 240000 },
    { month: 'Jun', revenue: 620000, expenses: 290000 },
  ]
}
