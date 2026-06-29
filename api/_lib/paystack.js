'use strict'
const PAYSTACK_SECRET = process.env.PAYSTACK_SECRET_KEY || 'sk_test_placeholder'
const PAYSTACK_API = 'https://api.paystack.co'

async function initializeTransaction(opts) {
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

async function verifyTransaction(reference) {
  const res = await fetch(`${PAYSTACK_API}/transaction/verify/${encodeURIComponent(reference)}`, {
    headers: { Authorization: `Bearer ${PAYSTACK_SECRET}` },
  })
  return res.json()
}

async function initiateTransfer(opts) {
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

function generateReference() {
  return `ESD-PAY-${Date.now()}-${Math.floor(1000 + Math.random() * 9000)}`
}

module.exports = { initializeTransaction, verifyTransaction, initiateTransfer, generateReference }
