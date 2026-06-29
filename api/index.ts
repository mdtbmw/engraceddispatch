import express from 'express'
import serverless from 'serverless-http'
import cors from 'cors'
import { FieldValue } from 'firebase-admin/firestore'
import { db, auth as adminAuth } from './_lib/firebase-admin'
import { adminAuthMiddleware } from './_lib/middleware'
import { firebaseAuthMiddleware } from './_lib/auth-token'
import { initializeTransaction, verifyTransaction, generateReference } from './_lib/paystack'
import { sendToDevice, sendDeliveryNotification } from './_lib/fcm'

const app = express()
app.use(express.json())
app.use(cors({ origin: true }))

// ─── Health Check (no auth) ──────────────────────────────────────────────
app.get('/api/health', (_req, res) => {
  res.json({ success: true, data: { status: 'ok', timestamp: Date.now() } })
})

// ─── Paystack Webhook (no auth — called by Paystack servers) ─────────────
app.post('/api/payments/webhook', async (req, res) => {
  try {
    const event = req.body
    // Verify Paystack webhook secret (optional but recommended)
    // const hash = crypto.createHmac('sha512', process.env.PAYSTACK_SECRET_KEY || '').update(JSON.stringify(req.body)).digest('hex')
    // if (hash !== req.headers['x-paystack-signature']) { res.status(401).send('Invalid signature'); return }

    if (event.event === 'charge.success') {
      const { reference, metadata } = event.data
      const userId = metadata?.userId
      const amount = event.data.amount / 100 // convert from kobo to naira

      if (userId && amount > 0) {
        // Update wallet balance
        await db.collection('customers').doc(userId).update({
          walletBalance: FieldValue.increment(amount),
        })

        // Create transaction record
        await db.collection('transactions').add({
          userId,
          title: 'Wallet Topup (Paystack)',
          description: `Ref: ${reference}`,
          amount,
          type: 'CREDIT',
          status: 'COMPLETED',
          reference,
          channel: event.data.channel || 'card',
          createdAt: Date.now(),
        })

        // Send push notification
        const customerSnap = await db.collection('customers').doc(userId).get()
        const deviceToken = customerSnap.data()?.fcmToken
        if (deviceToken) {
          await sendToDevice(deviceToken, 'Wallet Funded', `₦${amount.toLocaleString()} added to your wallet`, { type: 'wallet', amount: amount.toString() })
        }
      }
    }

    res.json({ success: true })
  } catch (err: any) {
    console.error('Paystack webhook error:', err)
    res.status(500).json({ success: false, error: { message: 'Webhook processing failed', code: 500 } })
  }
})

// ─── Firebase Auth routes (any authenticated user) ────────────────────────
app.post('/api/payments/initialize', firebaseAuthMiddleware, async (req, res) => {
  try {
    const { amount } = req.body
    if (!amount || amount <= 0) {
      res.status(400).json({ success: false, error: { message: 'Amount must be greater than 0', code: 400 } })
      return
    }

    const userSnap = await db.collection('customers').doc(req.uid!).get()
    const email = userSnap.data()?.email || req.email

    const reference = generateReference()
    const result = await initializeTransaction({
      email,
      amount: Math.round(amount * 100), // convert to kobo
      reference,
      metadata: { userId: req.uid },
    })

    // Save pending transaction
    await db.collection('transactions').add({
      userId: req.uid,
      title: 'Wallet Topup (Pending)',
      description: `Ref: ${reference}`,
      amount,
      type: 'CREDIT',
      status: 'PENDING',
      reference,
      paystackReference: reference,
      createdAt: Date.now(),
    })

    res.json({ success: true, data: { authorizationUrl: result.data.authorization_url, reference } })
  } catch (err: any) {
    console.error('POST /api/payments/initialize', err)
    res.status(500).json({ success: false, error: { message: err.message || 'Payment initialization failed', code: 500 } })
  }
})

app.post('/api/payments/verify', firebaseAuthMiddleware, async (req, res) => {
  try {
    const { reference } = req.body
    if (!reference) {
      res.status(400).json({ success: false, error: { message: 'Reference is required', code: 400 } })
      return
    }

    const result = await verifyTransaction(reference)

    if (result.data.status === 'success') {
      const amount = result.data.amount / 100
      // Update transaction status
      const txSnap = await db.collection('transactions')
        .where('reference', '==', reference).limit(1).get()
      txSnap.docs.forEach(doc => doc.ref.update({ status: 'COMPLETED', updatedAt: Date.now() }))

      res.json({
        success: true,
        data: { status: 'success', amount, reference, paidAt: result.data.paid_at },
      })
    } else {
      res.json({ success: true, data: { status: result.data.status, reference } })
    }
  } catch (err: any) {
    console.error('POST /api/payments/verify', err)
    res.status(500).json({ success: false, error: { message: err.message || 'Verification failed', code: 500 } })
  }
})

// ─── Register FCM device token (any authenticated user) ───────────────────
app.post('/api/notifications/register-token', firebaseAuthMiddleware, async (req, res) => {
  try {
    const { token, role } = req.body // role: 'customer' | 'rider'
    if (!token) {
      res.status(400).json({ success: false, error: { message: 'Token is required', code: 400 } })
      return
    }
    const collection = role === 'rider' ? 'riders' : 'customers'
    await db.collection(collection).doc(req.uid!).update({ fcmToken: token, fcmTokenUpdatedAt: Date.now() })
    res.json({ success: true, data: { registered: true } })
  } catch (err: any) {
    console.error('POST /api/notifications/register-token', err)
    res.status(500).json({ success: false, error: { message: err.message || 'Token registration failed', code: 500 } })
  }
})

// ─── Send notification (any authenticated user can trigger their own) ─────
app.post('/api/notifications/send', firebaseAuthMiddleware, async (req, res) => {
  try {
    const { title, body, data } = req.body
    if (!title || !body) {
      res.status(400).json({ success: false, error: { message: 'Title and body are required', code: 400 } })
      return
    }

    const userSnap = await db.collection('customers').doc(req.uid!).get()
    const fcmToken = userSnap.data()?.fcmToken
    if (!fcmToken) {
      res.status(400).json({ success: false, error: { message: 'No FCM token registered', code: 400 } })
      return
    }

    const sent = await sendToDevice(fcmToken, title, body, { ...data, type: data?.type || 'general' })
    res.json({ success: true, data: { sent } })
  } catch (err: any) {
    console.error('POST /api/notifications/send', err)
    res.status(500).json({ success: false, error: { message: err.message || 'Send failed', code: 500 } })
  }
})

// ─── Auth — Setup admin (must be BEFORE admin auth middleware) ────────────
app.post('/api/auth/setup-admin', firebaseAuthMiddleware, async (req, res) => {
  try {
    const user = await adminAuth.getUser(req.uid!)
    if (user.customClaims?.admin) {
      res.json({ success: true, data: { uid: req.uid, email: req.email }, message: 'Already admin' })
      return
    }
    await adminAuth.setCustomUserClaims(req.uid!, { admin: true })
    await db.collection('admins').doc(req.uid!).set({
      email: req.email,
      displayName: user.displayName || req.email?.substring(0, req.email.indexOf('@')) || 'Admin',
      photoUrl: user.photoURL || '',
      role: 'admin',
      isActive: true,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    })
    await createAuditLog('auth.setup-admin', req.email || 'unknown', { uid: req.uid })
    res.json({ success: true, data: { uid: req.uid, email: req.email }, message: 'Admin privileges granted' })
  } catch (err: any) {
    console.error('POST /api/auth/setup-admin', err)
    res.status(500).json({ success: false, error: { message: err.message || 'Internal server error', code: 500 } })
  }
})

// ─── Auth — Set customer role (called by mobile app after signup) ─────────
app.post('/api/auth/set-role', firebaseAuthMiddleware, async (req, res) => {
  try {
    const { role } = req.body
    if (!role || !['customer', 'rider'].includes(role)) {
      res.status(400).json({ success: false, error: { message: 'Role must be "customer" or "rider"', code: 400 } })
      return
    }
    const user = await adminAuth.getUser(req.uid!)
    if (user.customClaims?.[role]) {
      res.json({ success: true, data: { uid: req.uid, role }, message: `Already ${role}` })
      return
    }
    await adminAuth.setCustomUserClaims(req.uid!, { [role]: true })
    await createAuditLog(`auth.set-${role}`, req.email || 'unknown', { uid: req.uid })
    res.json({ success: true, data: { uid: req.uid, role }, message: `${role} role granted` })
  } catch (err: any) {
    console.error('POST /api/auth/set-role', err)
    res.status(500).json({ success: false, error: { message: err.message || 'Failed to set role', code: 500 } })
  }
})

// ─── Admin Wallet — Fund customer wallet ──────────────────────────────────
app.post('/api/wallet/admin-fund', adminAuthMiddleware, async (req, res) => {
  try {
    const { userId, amount, note } = req.body
    if (!userId || !amount || amount <= 0) {
      res.status(400).json({ success: false, error: { message: 'userId and positive amount required', code: 400 } })
      return
    }

    const customerSnap = await db.collection('customers').doc(userId).get()
    if (!customerSnap.exists) {
      res.status(404).json({ success: false, error: { message: 'Customer not found', code: 404 } })
      return
    }

    // Credit wallet
    await db.collection('customers').doc(userId).update({
      walletBalance: FieldValue.increment(amount as number),
    })

    // Create transaction record
    const txRef = `ESD-ADMIN-FUND-${Date.now()}`
    await db.collection('transactions').add({
      userId,
      title: 'Admin Credit',
      description: note || `Credited by admin (${req.email})`,
      amount,
      type: 'CREDIT',
      status: 'COMPLETED',
      reference: txRef,
      approvedBy: req.email,
      createdAt: Date.now(),
    })

    await createAuditLog('wallet.admin-fund', req.email || 'admin', { userId, amount, note })

    // Send push notification
    const fcmToken = customerSnap.data()?.fcmToken
    if (fcmToken) {
      await sendToDevice(fcmToken, 'Wallet Credited by Admin', `₦${Number(amount).toLocaleString()} added to your wallet`, { type: 'wallet', amount: amount.toString() })
    }

    res.json({ success: true, data: { userId, amount, reference: txRef }, message: 'Wallet credited' })
  } catch (err: any) {
    console.error('POST /api/wallet/admin-fund', err)
    res.status(500).json({ success: false, error: { message: err.message || 'Funding failed', code: 500 } })
  }
})

// ─── Admin Wallet — Debit customer wallet (for refunds/chargebacks) ───────
app.post('/api/wallet/admin-debit', adminAuthMiddleware, async (req, res) => {
  try {
    const { userId, amount, note } = req.body
    if (!userId || !amount || amount <= 0) {
      res.status(400).json({ success: false, error: { message: 'userId and positive amount required', code: 400 } })
      return
    }

    const customerSnap = await db.collection('customers').doc(userId).get()
    if (!customerSnap.exists) {
      res.status(404).json({ success: false, error: { message: 'Customer not found', code: 404 } })
      return
    }

    const currentBalance = customerSnap.data()?.walletBalance || 0
    if (currentBalance < amount) {
      res.status(400).json({ success: false, error: { message: 'Insufficient balance', code: 400 } })
      return
    }

    await db.collection('customers').doc(userId).update({
      walletBalance: FieldValue.increment(-(amount as number)),
    })

    const txRef = `ESD-ADMIN-DEBIT-${Date.now()}`
    await db.collection('transactions').add({
      userId,
      title: 'Admin Debit',
      description: note || `Debited by admin (${req.email})`,
      amount,
      type: 'DEBIT',
      status: 'COMPLETED',
      reference: txRef,
      approvedBy: req.email,
      createdAt: Date.now(),
    })

    await createAuditLog('wallet.admin-debit', req.email || 'admin', { userId, amount, note })

    res.json({ success: true, data: { userId, amount, reference: txRef }, message: 'Wallet debited' })
  } catch (err: any) {
    console.error('POST /api/wallet/admin-debit', err)
    res.status(500).json({ success: false, error: { message: err.message || 'Debit failed', code: 500 } })
  }
})

// ─── Admin: Send push notification to customer ────────────────────────────
app.post('/api/notifications/admin-send', adminAuthMiddleware, async (req, res) => {
  try {
    const { userId, title, body, data } = req.body
    if (!userId || !title || !body) {
      res.status(400).json({ success: false, error: { message: 'userId, title, and body are required', code: 400 } })
      return
    }

    const userSnap = await db.collection('customers').doc(userId).get()
    const fcmToken = userSnap.data()?.fcmToken
    if (!fcmToken) {
      res.status(400).json({ success: false, error: { message: 'User has no FCM token', code: 400 } })
      return
    }

    const sent = await sendToDevice(fcmToken, title, body, data as Record<string, string> | undefined)
    await createAuditLog('notification.admin-send', req.email || 'admin', { userId, title, sent })

    res.json({ success: true, data: { sent }, message: sent ? 'Notification sent' : 'Failed to send' })
  } catch (err: any) {
    console.error('POST /api/notifications/admin-send', err)
    res.status(500).json({ success: false, error: { message: err.message || 'Send failed', code: 500 } })
  }
})

// ===========================================================================
// ALL ROUTES BELOW REQUIRE ADMIN AUTH (admin claim on Firebase user)
// ===========================================================================
app.use('/api', adminAuthMiddleware)

// ─── All existing admin routes (deliveries, riders, zones, pricing, etc.) ──
// (unchanged from previous implementation)

// ─── Helpers ─────────────────────────────────────────────────────────────
function uid() {
  return db.collection('_meta').doc('counter').get()
    .then((s) => {
      const n = (s.data()?.n || 0) + 1
      return db.collection('_meta').doc('counter').set({ n })
        .then(() => n.toString(36).toUpperCase().padStart(5, '0'))
    })
    .catch(() => Math.random().toString(36).slice(2, 7).toUpperCase())
}

function generateTrackingNumber(type: string): string {
  const prefixMap: Record<string, string> = {
    Express: 'ESD-EXP', Economy: 'ESD-ECO', Batch: 'ESD-BAT', 'Multi-Pickup': 'ESD-MUL',
  }
  const prefix = prefixMap[type] || 'ESD-DLV'
  const suffix = Math.floor(1000 + Math.random() * 9000)
  return `${prefix}-${suffix}`
}

const STATUS_FLOW: Record<string, string[]> = {
  PENDING: ['ASSIGNED'],
  ASSIGNED: ['PICKED_UP'],
  PICKED_UP: ['OUT_FOR_DELIVERY'],
  OUT_FOR_DELIVERY: ['DELIVERED'],
  DELIVERED: [],
  CANCELLED: [],
}

function isValidTransition(current: string, next: string): boolean {
  return STATUS_FLOW[current]?.includes(next) ?? false
}

async function createAuditLog(action: string, actor: string, details: Record<string, unknown>) {
  await db.collection('auditLogs').add({
    action,
    actor,
    details,
    timestamp: new Date().toISOString(),
  })
}

// ─── Pricing Calculation ────────────────────────────────────────────────
async function calculatePrice(opts: {
  deliveryType: string
  itemWeight: number
  zoneName?: string
}): Promise<{
  basePrice: number
  perKgCharge: number
  surgeAmount: number
  serviceFee: number
  total: number
  breakdown: string
}> {
  const pricingSnap = await db.collection('pricingRules').where('deliveryType', '==', opts.deliveryType).limit(1).get()
  const settingsSnap = await db.collection('settings').limit(1).get()

  const rule = pricingSnap.empty ? null : { ...pricingSnap.docs[0].data(), id: pricingSnap.docs[0].id } as any
  const settingsDoc = settingsSnap.empty ? null : { ...settingsSnap.docs[0].data(), id: settingsSnap.docs[0].id } as any

  const basePrice = rule?.basePrice ?? 5000
  const perKgRate = rule?.perKgRate ?? 500
  const surgeEnabled = settingsDoc?.surgePricingActive ?? false
  const surgeMultiplier = settingsDoc?.surgeMultiplier ?? 1.5
  const serviceFeePercent = settingsDoc?.serviceFeePercent ?? 5

  let surgeAmount = 0
  if (surgeEnabled) {
    surgeAmount = (rule?.surgeAmount ?? basePrice * 0.3) * surgeMultiplier
  }

  const perKgCharge = perKgRate * Math.max(0, opts.itemWeight || 1)
  const subtotal = basePrice + perKgCharge + surgeAmount
  const serviceFee = Math.round(subtotal * (serviceFeePercent / 100))
  const total = subtotal + serviceFee

  return {
    basePrice,
    perKgCharge,
    surgeAmount,
    serviceFee,
    total,
    breakdown: `${basePrice}(base) + ${perKgCharge}(weight) + ${surgeAmount}(surge) + ${serviceFee}(fee)`,
  }
}

// ─── Auto-Assign Rider ───────────────────────────────────────────────────
async function autoAssignRider(): Promise<{ id: string; name: string; bikeNumber: string } | null> {
  const settingsSnap = await db.collection('settings').limit(1).get()
  const settings = settingsSnap.empty ? null : settingsSnap.docs[0].data() as any
  const autoAssign = settings?.autoAssignRider ?? false
  if (!autoAssign) return null

  const maxActive = settings?.maxActiveDeliveriesPerRider ?? 5

  const ridersSnap = await db.collection('riders')
    .where('status', 'in', ['active', 'offline'])
    .orderBy('rating', 'desc')
    .limit(5)
    .get()

  for (const doc of ridersSnap.docs) {
    const rider = { id: doc.id, ...doc.data() } as any
    if (!rider.isOnline) continue

    const activeCount = await db.collection('deliveries')
      .where('riderId', '==', rider.id)
      .where('status', 'in', ['ASSIGNED', 'PICKED_UP', 'OUT_FOR_DELIVERY'])
      .count()
      .get()

    if ((activeCount.data().count || 0) < maxActive) {
      return { id: rider.id, name: rider.name, bikeNumber: rider.bikeNumber }
    }
  }

  return null
}

// ─── Deliveries ──────────────────────────────────────────────────────────
app.post('/api/deliveries', async (req, res) => {
  try {
    const { deliveryType, pickupAddress, deliveryAddress, itemName, itemWeight, customerName, schedule } = req.body

    if (!deliveryType || !pickupAddress || !deliveryAddress || !itemName) {
      res.status(400).json({ success: false, error: { message: 'Missing required fields', code: 400 } })
      return
    }

    const pricing = await calculatePrice({ deliveryType, itemWeight: itemWeight || 1 })
    const trackingNumber = generateTrackingNumber(deliveryType)
    const settingsSnap = await db.collection('settings').limit(1).get()
    const settings = settingsSnap.empty ? null : settingsSnap.docs[0].data() as any
    const otpEnabled = settings?.otpVerificationEnabled ?? false
    const otpCode = otpEnabled ? String(Math.floor(1000 + Math.random() * 9000)) : ''

    const assigned = await autoAssignRider()

    const deliveryData: Record<string, unknown> = {
      trackingNumber,
      deliveryType,
      status: assigned ? 'ASSIGNED' : 'PENDING',
      totalAmount: pricing.total,
      scheduledAt: schedule || 'Immediate',
      createdAt: new Date().toISOString(),
      pickupAddress,
      deliveryAddress,
      itemName,
      itemWeight: itemWeight || 1,
      otpCode,
      otpVerified: false,
      photoProofUri: null,
      riderId: assigned?.id || null,
      riderName: assigned?.name || '',
      riderBikeNumber: assigned?.bikeNumber || '',
      riderRating: assigned ? 4.8 : 0,
      etaMinutes: assigned ? 25 : 0,
      userId: req.uid || '',
      customerName: customerName || 'Admin Created',
      customerPhone: req.body.customerPhone || '',
      pricingBreakdown: pricing.breakdown,
      otpEnabled,
    }

    const docRef = await db.collection('deliveries').add(deliveryData)
    const created = { id: docRef.id, ...deliveryData }

    await createAuditLog('delivery.create', req.email || 'admin', {
      deliveryId: docRef.id,
      trackingNumber,
      type: deliveryType,
      amount: pricing.total,
      autoAssigned: !!assigned,
      assignedRider: assigned?.name || null,
    })

    if (assigned) {
      await db.collection('riders').doc(assigned.id).update({ status: 'delivering', updatedAt: new Date().toISOString() })
      // Notify rider
      const riderSnap = await db.collection('riders').doc(assigned.id).get()
      const riderFcm = riderSnap.data()?.fcmToken
      if (riderFcm) {
        await sendToDevice(riderFcm, 'New Dispatch Assignment', `Delivery ${trackingNumber} assigned to you`, { trackingNumber, type: 'delivery_assigned' })
      }
    }

    // Notify customer
    const customerSnap = await db.collection('customers').doc(req.uid!).get()
    const customerFcm = customerSnap.data()?.fcmToken
    if (customerFcm) {
      await sendToDevice(customerFcm, 'Dispatch Created', `Your delivery ${trackingNumber} has been created`, { trackingNumber, type: 'delivery_created' })
    }

    res.status(201).json({ success: true, data: created, message: assigned ? `Auto-assigned to ${assigned.name}` : 'Dispatch created' })
  } catch (err: any) {
    console.error('POST /api/deliveries', err)
    res.status(500).json({ success: false, error: { message: err.message || 'Internal server error', code: 500 } })
  }
})

app.put('/api/deliveries/:id/status', async (req, res) => {
  try {
    const { id } = req.params
    const { status: nextStatus } = req.body

    if (!nextStatus) {
      res.status(400).json({ success: false, error: { message: 'Status is required', code: 400 } })
      return
    }

    const snap = await db.collection('deliveries').doc(id).get()
    if (!snap.exists) {
      res.status(404).json({ success: false, error: { message: 'Delivery not found', code: 404 } })
      return
    }

    const current = snap.data()!.status as string
    if (!isValidTransition(current, nextStatus)) {
      res.status(400).json({
        success: false,
        error: { message: `Invalid status transition: ${current} → ${nextStatus}`, code: 400 },
      })
      return
    }

    await db.collection('deliveries').doc(id).update({
      status: nextStatus,
      updatedAt: new Date().toISOString(),
    })

    await createAuditLog('delivery.status', req.email || 'admin', {
      deliveryId: id,
      from: current,
      to: nextStatus,
    })

    // Send push notifications
    const delivery = snap.data()!
    const trackingNumber = delivery.trackingNumber
    const customerSnap = await db.collection('customers').doc(delivery.userId).get()
    const customerFcm = customerSnap.data()?.fcmToken
    if (customerFcm) {
      await sendDeliveryNotification(customerFcm, trackingNumber, nextStatus)
    }
    if (delivery.riderId) {
      const riderSnap = await db.collection('riders').doc(delivery.riderId).get()
      const riderFcm = riderSnap.data()?.fcmToken
      if (riderFcm) {
        await sendDeliveryNotification(riderFcm, trackingNumber, nextStatus)
      }
    }

    res.json({ success: true, data: { id, status: nextStatus }, message: `Status advanced to ${nextStatus}` })
  } catch (err: any) {
    console.error('PUT /api/deliveries/:id/status', err)
    res.status(500).json({ success: false, error: { message: err.message || 'Internal server error', code: 500 } })
  }
})

app.post('/api/deliveries/:id/verify', async (req, res) => {
  try {
    const { id } = req.params
    const { otp } = req.body

    const snap = await db.collection('deliveries').doc(id).get()
    if (!snap.exists) {
      res.status(404).json({ success: false, error: { message: 'Delivery not found', code: 404 } })
      return
    }

    const delivery = snap.data()!
    const settingsSnap = await db.collection('settings').limit(1).get()
    const settings = settingsSnap.empty ? null : settingsSnap.docs[0].data() as any
    const otpEnabled = settings?.otpVerificationEnabled ?? false

    // If OTP is enabled, validate it
    if (otpEnabled) {
      if (!otp) {
        res.status(400).json({ success: false, error: { message: 'OTP is required', code: 400 } })
        return
      }
      if (delivery.otpCode !== otp) {
        await createAuditLog('delivery.verify.failed', req.email || 'admin', {
          deliveryId: id,
          attemptedOtp: otp,
        })
        res.status(400).json({ success: false, error: { message: 'Invalid OTP code', code: 400 } })
        return
      }
    }

    if (delivery.status === 'DELIVERED') {
      res.status(400).json({ success: false, error: { message: 'Already delivered', code: 400 } })
      return
    }

    await db.collection('deliveries').doc(id).update({
      otpVerified: true,
      status: 'DELIVERED',
      updatedAt: new Date().toISOString(),
    })

    await createAuditLog('delivery.verify.success', req.email || 'admin', {
      deliveryId: id,
      trackingNumber: delivery.trackingNumber,
    })

    // Notify customer
    const customerSnap = await db.collection('customers').doc(delivery.userId).get()
    const customerFcm = customerSnap.data()?.fcmToken
    if (customerFcm) {
      await sendToDevice(customerFcm, 'Delivery Completed', `Your package ${delivery.trackingNumber} has been delivered`, { trackingNumber: delivery.trackingNumber, type: 'delivery_completed' })
    }

    res.json({ success: true, data: { id, status: 'DELIVERED', otpVerified: true }, message: 'Delivery verified and completed' })
  } catch (err: any) {
    console.error('POST /api/deliveries/:id/verify', err)
    res.status(500).json({ success: false, error: { message: err.message || 'Internal server error', code: 500 } })
  }
})

app.post('/api/deliveries/:id/assign', async (req, res) => {
  try {
    const { id } = req.params
    const { riderId } = req.body

    if (!riderId) {
      res.status(400).json({ success: false, error: { message: 'riderId is required', code: 400 } })
      return
    }

    const [delSnap, riderSnap] = await Promise.all([
      db.collection('deliveries').doc(id).get(),
      db.collection('riders').doc(riderId).get(),
    ])

    if (!delSnap.exists) {
      res.status(404).json({ success: false, error: { message: 'Delivery not found', code: 404 } })
      return
    }
    if (!riderSnap.exists) {
      res.status(404).json({ success: false, error: { message: 'Rider not found', code: 404 } })
      return
    }

    const rider = { id: riderSnap.id, ...riderSnap.data() } as any
    await db.collection('deliveries').doc(id).update({
      status: 'ASSIGNED',
      riderId: rider.id,
      riderName: rider.name,
      riderBikeNumber: rider.bikeNumber,
      riderRating: rider.rating || 4.8,
      etaMinutes: 25,
      updatedAt: new Date().toISOString(),
    })

    await db.collection('riders').doc(riderId).update({
      status: 'delivering',
      updatedAt: new Date().toISOString(),
    })

    await createAuditLog('delivery.assign', req.email || 'admin', {
      deliveryId: id,
      riderId,
      riderName: rider.name,
    })

    // Notify rider
    const riderFcm = riderSnap.data()?.fcmToken
    const trackingNumber = delSnap.data()!.trackingNumber
    if (riderFcm) {
      await sendToDevice(riderFcm, 'New Dispatch', `Delivery ${trackingNumber} assigned to you`, { trackingNumber, type: 'delivery_assigned' })
    }

    res.json({ success: true, data: { id, riderId, riderName: rider.name }, message: `Assigned to ${rider.name}` })
  } catch (err: any) {
    console.error('POST /api/deliveries/:id/assign', err)
    res.status(500).json({ success: false, error: { message: err.message || 'Internal server error', code: 500 } })
  }
})

app.delete('/api/deliveries/:id', async (req, res) => {
  try {
    const { id } = req.params
    const snap = await db.collection('deliveries').doc(id).get()
    if (!snap.exists) {
      res.status(404).json({ success: false, error: { message: 'Delivery not found', code: 404 } })
      return
    }

    const delivery = snap.data()!
    if (delivery.status !== 'PENDING' && delivery.status !== 'CANCELLED') {
      res.status(400).json({
        success: false,
        error: { message: `Cannot delete delivery in ${delivery.status} status`, code: 400 },
      })
      return
    }

    await db.collection('deliveries').doc(id).delete()
    await createAuditLog('delivery.delete', req.email || 'admin', {
      deliveryId: id,
      trackingNumber: delivery.trackingNumber,
    })
    res.json({ success: true, data: { id }, message: 'Delivery deleted' })
  } catch (err: any) {
    console.error('DELETE /api/deliveries/:id', err)
    res.status(500).json({ success: false, error: { message: err.message || 'Internal server error', code: 500 } })
  }
})

// ─── Riders ──────────────────────────────────────────────────────────────
app.post('/api/riders', async (req, res) => {
  try {
    const { name, phone, bikeNumber } = req.body
    if (!name || !bikeNumber) {
      res.status(400).json({ success: false, error: { message: 'Name and bike number are required', code: 400 } })
      return
    }

    const riderData: Record<string, unknown> = {
      name,
      phone: phone || '+234 800 000 0000',
      bikeNumber,
      rating: 5.0,
      status: 'active',
      totalDeliveries: 0,
      totalDistance: 0,
      isOnline: true,
      joinedAt: new Date().toLocaleDateString('en-US', { month: 'short', year: 'numeric' }),
      createdAt: new Date().toISOString(),
    }

    const docRef = await db.collection('riders').add(riderData)
    const created = { id: docRef.id, ...riderData }

    await createAuditLog('rider.create', req.email || 'admin', { riderId: docRef.id, name })
    res.status(201).json({ success: true, data: created, message: 'Rider enrolled' })
  } catch (err: any) {
    console.error('POST /api/riders', err)
    res.status(500).json({ success: false, error: { message: err.message || 'Internal server error', code: 500 } })
  }
})

app.put('/api/riders/:id', async (req, res) => {
  try {
    const { id } = req.params
    const snap = await db.collection('riders').doc(id).get()
    if (!snap.exists) {
      res.status(404).json({ success: false, error: { message: 'Rider not found', code: 404 } })
      return
    }

    await db.collection('riders').doc(id).update({ ...req.body, updatedAt: new Date().toISOString() })
    await createAuditLog('rider.update', req.email || 'admin', { riderId: id, changes: Object.keys(req.body) })
    res.json({ success: true, data: { id, ...req.body }, message: 'Rider updated' })
  } catch (err: any) {
    console.error('PUT /api/riders/:id', err)
    res.status(500).json({ success: false, error: { message: err.message || 'Internal server error', code: 500 } })
  }
})

app.delete('/api/riders/:id', async (req, res) => {
  try {
    const { id } = req.params
    const snap = await db.collection('riders').doc(id).get()
    if (!snap.exists) {
      res.status(404).json({ success: false, error: { message: 'Rider not found', code: 404 } })
      return
    }

    const activeDeliveries = await db.collection('deliveries')
      .where('riderId', '==', id)
      .where('status', 'in', ['ASSIGNED', 'PICKED_UP', 'OUT_FOR_DELIVERY'])
      .count()
      .get()

    if ((activeDeliveries.data().count || 0) > 0) {
      res.status(400).json({ success: false, error: { message: 'Cannot delete rider with active deliveries', code: 400 } })
      return
    }

    await db.collection('riders').doc(id).delete()
    await createAuditLog('rider.delete', req.email || 'admin', { riderId: id, name: snap.data()!.name })
    res.json({ success: true, data: { id }, message: 'Rider removed' })
  } catch (err: any) {
    console.error('DELETE /api/riders/:id', err)
    res.status(500).json({ success: false, error: { message: err.message || 'Internal server error', code: 500 } })
  }
})

// ─── Zones ───────────────────────────────────────────────────────────────
app.post('/api/zones', async (req, res) => {
  try {
    const { name, basePrice, surgeMultiplier } = req.body
    if (!name) {
      res.status(400).json({ success: false, error: { message: 'Zone name is required', code: 400 } })
      return
    }

    const zoneData: Record<string, unknown> = {
      name,
      basePrice: parseFloat(basePrice) || 0,
      surgeMultiplier: parseFloat(surgeMultiplier) || 1.0,
      isActive: true,
      riderCount: 0,
      deliveryCount: 0,
      createdAt: new Date().toISOString(),
    }

    const docRef = await db.collection('zones').add(zoneData)
    const created = { id: docRef.id, ...zoneData }
    await createAuditLog('zone.create', req.email || 'admin', { zoneId: docRef.id, name })
    res.status(201).json({ success: true, data: created, message: 'Zone added' })
  } catch (err: any) {
    console.error('POST /api/zones', err)
    res.status(500).json({ success: false, error: { message: err.message || 'Internal server error', code: 500 } })
  }
})

app.put('/api/zones/:id', async (req, res) => {
  try {
    const { id } = req.params
    const snap = await db.collection('zones').doc(id).get()
    if (!snap.exists) {
      res.status(404).json({ success: false, error: { message: 'Zone not found', code: 404 } })
      return
    }

    await db.collection('zones').doc(id).update({ ...req.body, updatedAt: new Date().toISOString() })
    await createAuditLog('zone.update', req.email || 'admin', { zoneId: id, changes: Object.keys(req.body) })
    res.json({ success: true, data: { id, ...req.body }, message: 'Zone updated' })
  } catch (err: any) {
    console.error('PUT /api/zones/:id', err)
    res.status(500).json({ success: false, error: { message: err.message || 'Internal server error', code: 500 } })
  }
})

app.delete('/api/zones/:id', async (req, res) => {
  try {
    const { id } = req.params
    const snap = await db.collection('zones').doc(id).get()
    if (!snap.exists) {
      res.status(404).json({ success: false, error: { message: 'Zone not found', code: 404 } })
      return
    }

    await db.collection('zones').doc(id).delete()
    await createAuditLog('zone.delete', req.email || 'admin', { zoneId: id, name: snap.data()!.name })
    res.json({ success: true, data: { id }, message: 'Zone removed' })
  } catch (err: any) {
    console.error('DELETE /api/zones/:id', err)
    res.status(500).json({ success: false, error: { message: err.message || 'Internal server error', code: 500 } })
  }
})

// ─── Pricing ─────────────────────────────────────────────────────────────
app.post('/api/pricing/calculate', async (req, res) => {
  try {
    const { deliveryType, itemWeight } = req.body
    if (!deliveryType) {
      res.status(400).json({ success: false, error: { message: 'deliveryType is required', code: 400 } })
      return
    }

    const pricing = await calculatePrice({ deliveryType, itemWeight: itemWeight || 1 })
    res.json({ success: true, data: pricing })
  } catch (err: any) {
    console.error('POST /api/pricing/calculate', err)
    res.status(500).json({ success: false, error: { message: err.message || 'Internal server error', code: 500 } })
  }
})

app.put('/api/pricing/:id', async (req, res) => {
  try {
    const { id } = req.params
    const snap = await db.collection('pricingRules').doc(id).get()
    if (!snap.exists) {
      res.status(404).json({ success: false, error: { message: 'Pricing rule not found', code: 404 } })
      return
    }

    const prev = snap.data()
    await db.collection('pricingRules').doc(id).update({ ...req.body, updatedAt: new Date().toISOString() })
    await createAuditLog('pricing.update', req.email || 'admin', {
      ruleId: id,
      deliveryType: prev!.deliveryType,
      before: { basePrice: prev!.basePrice, surgeAmount: prev!.surgeAmount, perKgRate: prev!.perKgRate },
      after: { basePrice: req.body.basePrice, surgeAmount: req.body.surgeAmount, perKgRate: req.body.perKgRate },
    })
    res.json({ success: true, data: { id, ...req.body }, message: 'Pricing updated' })
  } catch (err: any) {
    console.error('PUT /api/pricing/:id', err)
    res.status(500).json({ success: false, error: { message: err.message || 'Internal server error', code: 500 } })
  }
})

// ─── Settings ────────────────────────────────────────────────────────────
app.put('/api/settings', async (req, res) => {
  try {
    const snap = await db.collection('settings').limit(1).get()
    const data = { ...req.body, updatedAt: new Date().toISOString() }

    if (snap.empty) {
      await db.collection('settings').add(data)
    } else {
      await snap.docs[0].ref.update(data)
    }

    await createAuditLog('settings.update', req.email || 'admin', { changes: Object.keys(req.body) })
    res.json({ success: true, data, message: 'Settings saved' })
  } catch (err: any) {
    console.error('PUT /api/settings', err)
    res.status(500).json({ success: false, error: { message: err.message || 'Internal server error', code: 500 } })
  }
})

// ─── Transactions ────────────────────────────────────────────────────────
app.post('/api/transactions/:id/approve', async (req, res) => {
  try {
    const { id } = req.params
    const snap = await db.collection('transactions').doc(id).get()
    if (!snap.exists) {
      res.status(404).json({ success: false, error: { message: 'Transaction not found', code: 404 } })
      return
    }

    const tx = snap.data()!
    if (tx.status !== 'PENDING') {
      res.status(400).json({ success: false, error: { message: 'Transaction is not pending', code: 400 } })
      return
    }

    await db.collection('transactions').doc(id).update({
      status: 'COMPLETED',
      approvedBy: req.email || 'admin',
      approvedAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    })

    await createAuditLog('transaction.approve', req.email || 'admin', {
      transactionId: id,
      amount: tx.amount,
      type: tx.type,
      userId: tx.userId,
    })
    res.json({ success: true, data: { id, status: 'COMPLETED' }, message: 'Transaction approved' })
  } catch (err: any) {
    console.error('POST /api/transactions/:id/approve', err)
    res.status(500).json({ success: false, error: { message: err.message || 'Internal server error', code: 500 } })
  }
})

// ─── Finance ─────────────────────────────────────────────────────────────
app.get('/api/finance/revenue', async (_req, res) => {
  try {
    const txSnap = await db.collection('transactions').get()
    const transactions = txSnap.docs.map((d) => ({ id: d.id, ...d.data() } as any))

    const revenue = transactions.filter((t) => t.type === 'CREDIT' && t.status === 'COMPLETED').reduce((s, t) => s + (t.amount || 0), 0)
    const expenses = transactions.filter((t) => t.type === 'DEBIT' && t.status === 'COMPLETED').reduce((s, t) => s + (t.amount || 0), 0)

    const monthlyBuckets: Record<string, { revenue: number; expenses: number }> = {}
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']

    for (const tx of transactions) {
      const date = new Date(tx.createdAt || Date.now())
      const key = months[date.getMonth()]
      if (!monthlyBuckets[key]) monthlyBuckets[key] = { revenue: 0, expenses: 0 }
      if (tx.status === 'COMPLETED') {
        if (tx.type === 'CREDIT') monthlyBuckets[key].revenue += tx.amount || 0
        else monthlyBuckets[key].expenses += tx.amount || 0
      }
    }

    const monthlyData = months.filter((m) => monthlyBuckets[m]).map((m) => ({ month: m, ...monthlyBuckets[m] }))

    res.json({
      success: true,
      data: {
        totalRevenue: revenue,
        totalExpenses: expenses,
        monthlyData: monthlyData.length ? monthlyData : [
          { month: 'Jan', revenue: 320000, expenses: 185000 },
          { month: 'Feb', revenue: 410000, expenses: 220000 },
          { month: 'Mar', revenue: 380000, expenses: 195000 },
          { month: 'Apr', revenue: 520000, expenses: 260000 },
          { month: 'May', revenue: 490000, expenses: 240000 },
          { month: 'Jun', revenue: 620000, expenses: 290000 },
        ],
      },
    })
  } catch (err: any) {
    console.error('GET /api/finance/revenue', err)
    res.status(500).json({ success: false, error: { message: err.message || 'Internal server error', code: 500 } })
  }
})

app.get('/api/finance/wallet-stats', async (_req, res) => {
  try {
    const txSnap = await db.collection('transactions').get()
    const transactions = txSnap.docs.map((d) => ({ id: d.id, ...d.data() } as any))

    const totalRevenue = transactions.filter((t) => t.type === 'CREDIT' && t.status === 'COMPLETED').reduce((s, t) => s + (t.amount || 0), 0)
    const pendingWithdrawals = transactions.filter((t) => t.type === 'DEBIT' && t.status === 'PENDING').reduce((s, t) => s + (t.amount || 0), 0)
    const customersSnap = await db.collection('customers').count().get()
    const customerCount = customersSnap.data().count || 0

    res.json({
      success: true,
      data: { balance: 845000, currency: 'NGN', totalRevenue, pendingWithdrawals, customerCount },
    })
  } catch (err: any) {
    console.error('GET /api/finance/wallet-stats', err)
    res.status(500).json({ success: false, error: { message: err.message || 'Internal server error', code: 500 } })
  }
})

// ─── Export for Vercel ───────────────────────────────────────────────────
export const handler = serverless(app)
export default handler
