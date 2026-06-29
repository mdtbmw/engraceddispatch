/**
 * Paystack payment integration for wallet funding & withdrawal.
 * Docs: https://paystack.com/docs/api
 */

const PAYSTACK_SECRET = process.env.PAYSTACK_SECRET_KEY || 'sk_test_placeholder'
const PAYSTACK_API = 'https://api.paystack.co'

interface PaystackInitResponse {
  status: boolean
  message: string
  data: {
    authorization_url: string
    access_code: string
    reference: string
  }
}

interface PaystackVerifyResponse {
  status: boolean
  message: string
  data: {
    id: number
    status: string
    reference: string
    amount: number
    channel: string
    currency: string
    paid_at: string
    created_at: string
    customer: {
      id: number
      email: string
      customer_code: string
    }
    metadata: Record<string, unknown>
  }
}

interface PaystackTransferResponse {
  status: boolean
  message: string
  data: {
    reference: string
    amount: number
    recipient: { name: string }
    status: string
    transfer_code: string
  }
}

export async function initializeTransaction(opts: {
  email: string
  amount: number // in kobo (e.g., 500000 = ₦5,000)
  reference: string
  metadata?: Record<string, unknown>
}): Promise<PaystackInitResponse> {
  const res = await fetch(`${PAYSTACK_API}/transaction/initialize`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${PAYSTACK_SECRET}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      email: opts.email,
      amount: opts.amount,
      reference: opts.reference,
      metadata: opts.metadata || {},
      callback_url: `${process.env.VITE_ADMIN_URL || 'https://admin.engraceddispatch.com'}/wallet/callback`,
    }),
  })
  return res.json()
}

export async function verifyTransaction(reference: string): Promise<PaystackVerifyResponse> {
  const res = await fetch(`${PAYSTACK_API}/transaction/verify/${encodeURIComponent(reference)}`, {
    headers: { Authorization: `Bearer ${PAYSTACK_SECRET}` },
  })
  return res.json()
}

export async function initiateTransfer(opts: {
  amount: number // in kobo
  recipient: string // recipient code
  reason: string
  reference: string
}): Promise<PaystackTransferResponse> {
  const res = await fetch(`${PAYSTACK_API}/transfer`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${PAYSTACK_SECRET}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      source: 'balance',
      amount: opts.amount,
      recipient: opts.recipient,
      reason: opts.reason,
      reference: opts.reference,
    }),
  })
  return res.json()
}

export function generateReference(): string {
  return `ESD-PAY-${Date.now()}-${Math.floor(1000 + Math.random() * 9000)}`
}
