import { getFirebaseAuth } from './firebase'
import type { Delivery, Rider, Zone, PricingRule, SystemSettings } from '@/types'

const BASE = '/api'

async function getToken(): Promise<string> {
  const user = getFirebaseAuth().currentUser
  if (!user) throw new Error('Not authenticated')
  return user.getIdToken()
}

async function request<T>(
  method: string,
  path: string,
  body?: unknown,
): Promise<{ success: boolean; data: T; message?: string }> {
  const token = await getToken()
  const res = await fetch(`${BASE}${path}`, {
    method,
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    body: body ? JSON.stringify(body) : undefined,
  })
  const json = await res.json()
  if (!res.ok || !json.success) {
    throw new Error(json.error?.message || `Request failed (${res.status})`)
  }
  return json
}

// ─── Deliveries ───────────────────────────────────────
export async function apiCreateDelivery(data: {
  deliveryType: string
  pickupAddress: string
  deliveryAddress: string
  itemName: string
  itemWeight?: number
  customerName?: string
  customerPhone?: string
  schedule?: string
}) {
  return request<Delivery>('POST', '/deliveries', data)
}

export async function apiUpdateDeliveryStatus(id: string, status: string) {
  return request<{ id: string; status: string }>('PUT', `/deliveries/${id}/status`, { status })
}

export async function apiVerifyDeliveryOtp(id: string, otp: string) {
  return request<{ id: string; status: string; otpVerified: boolean }>('POST', `/deliveries/${id}/verify`, { otp })
}

export async function apiDeleteDelivery(id: string) {
  return request<{ id: string }>('DELETE', `/deliveries/${id}`)
}

export async function apiAssignRider(deliveryId: string, riderId: string) {
  return request<{ id: string; riderId: string }>('POST', `/deliveries/${deliveryId}/assign`, { riderId })
}

// ─── Riders ────────────────────────────────────────────
export async function apiCreateRider(data: { name: string; phone?: string; bikeNumber: string }) {
  return request<Rider>('POST', '/riders', data)
}

export async function apiUpdateRider(id: string, data: Partial<Rider>) {
  return request<Rider>('PUT', `/riders/${id}`, data)
}

export async function apiDeleteRider(id: string) {
  return request<{ id: string }>('DELETE', `/riders/${id}`)
}

// ─── Zones ─────────────────────────────────────────────
export async function apiCreateZone(data: { name: string; basePrice?: number; surgeMultiplier?: number }) {
  return request<Zone>('POST', '/zones', data)
}

export async function apiUpdateZone(id: string, data: Partial<Zone>) {
  return request<Zone>('PUT', `/zones/${id}`, data)
}

export async function apiDeleteZone(id: string) {
  return request<{ id: string }>('DELETE', `/zones/${id}`)
}

// ─── Pricing ───────────────────────────────────────────
export async function apiCalculatePricing(deliveryType: string, itemWeight?: number) {
  return request<{
    basePrice: number
    perKgCharge: number
    surgeAmount: number
    serviceFee: number
    total: number
    breakdown: string
  }>('POST', '/pricing/calculate', { deliveryType, itemWeight })
}

export async function apiUpdatePricingRule(id: string, data: Partial<PricingRule>) {
  return request<PricingRule>('PUT', `/pricing/${id}`, data)
}

// ─── Settings ──────────────────────────────────────────
export async function apiUpdateSettings(data: Partial<SystemSettings>) {
  return request<Partial<SystemSettings>>('PUT', '/settings', data)
}

// ─── Transactions ──────────────────────────────────────
export async function apiApproveTransaction(id: string) {
  return request<{ id: string; status: string }>('POST', `/transactions/${id}/approve`)
}

// ─── Auth (admin setup) ────────────────────────────────
export async function apiSetupAdmin() {
  return request<{ uid: string; email: string }>('POST', '/auth/setup-admin')
}

// ─── Finance (server-calculated reads) ─────────────────
export async function apiGetRevenue() {
  return request<{
    totalRevenue: number
    totalExpenses: number
    monthlyData: { month: string; revenue: number; expenses: number }[]
  }>('GET', '/finance/revenue')
}

export async function apiGetWalletStats() {
  return request<{
    balance: number
    currency: string
    totalRevenue: number
    pendingWithdrawals: number
    customerCount: number
  }>('GET', '/finance/wallet-stats')
}

// ─── Wallet Management (Admin) ───────────────────────────
export async function apiAdminFundWallet(userId: string, amount: number, note?: string) {
  return request<{ userId: string; amount: number; reference: string }>('POST', '/wallet/admin-fund', { userId, amount, note })
}

export async function apiAdminDebitWallet(userId: string, amount: number, note?: string) {
  return request<{ userId: string; amount: number; reference: string }>('POST', '/wallet/admin-debit', { userId, amount, note })
}

// ─── Notifications (Admin) ───────────────────────────────
export async function apiAdminSendNotification(userId: string, title: string, body: string, data?: Record<string, string>) {
  return request<{ sent: boolean }>('POST', '/notifications/admin-send', { userId, title, body, data })
}

// ─── Payment Verification (for admin use) ────────────────
export async function apiVerifyPayment(reference: string) {
  return request<{ status: string; amount: number; reference: string }>('POST', '/payments/verify', { reference })
}
