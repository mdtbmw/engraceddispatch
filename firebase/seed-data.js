/**
 * Firebase Seed Script
 * Run with: node firebase/seed-data.js
 * Requires: FIREBASE_SERVICE_ACCOUNT env var or serviceAccountKey.json
 *
 * This script seeds initial data into Firestore for development.
 */

const { initializeApp, cert } = require('firebase-admin/app')
const { getFirestore, FieldValue } = require('firebase-admin/firestore')
const { getAuth } = require('firebase-admin/auth')
const path = require('path')

// Load service account from firebase/service-account.json
const serviceAccount = require(path.join(__dirname, 'service-account.json'))

initializeApp({
  credential: cert(serviceAccount),
})

const db = getFirestore()
const auth = getAuth()

async function createAdminUser() {
  const email = 'admin@engraced.com'
  const password = 'Admin@123'
  try {
    const user = await auth.createUser({ email, password, displayName: 'System Admin' })
    await auth.setCustomUserClaims(user.uid, { admin: true })
    console.log(`✅ Admin user created: ${email} / ${password}`)
  } catch (err) {
    if (err.code === 'auth/email-already-exists') {
      const user = await auth.getUserByEmail(email)
      await auth.setCustomUserClaims(user.uid, { admin: true })
      console.log(`✅ Admin claim set on existing user: ${email}`)
    } else if (err.code === 'auth/configuration-not-found') {
      console.warn('⚠️  Firebase Auth not enabled. Enable Email/Password in Firebase Console → Authentication → Sign-in method, then run: node firebase/seed-data.js')
      console.warn('   Admin user will need to be created manually:')
      console.warn(`   Email: ${email}  Password: ${password}`)
    } else {
      throw err
    }
  }
}

async function seed() {
  console.log('🌱 Seeding Firestore...\n')

  // ─── Admin User (may skip if Auth not configured) ─
  await createAdminUser()

  // ─── Settings ────────────────────────────────
  await db.collection('settings').doc('global').set({
    autoAssignRider: true,
    maxDeliveryRadiusKm: 15,
    surgePricingActive: true,
    surgeMultiplier: 1.5,
    serviceFeePercent: 5,
    minWalletBalance: 500,
    riderAssignmentTimeoutMinutes: 30,
    maxActiveDeliveriesPerRider: 5,
    updatedAt: FieldValue.serverTimestamp(),
  })
  console.log('✅ Settings')

  // ─── Pricing Rules ───────────────────────────
  const pricingData = [
    { deliveryType: 'Express', basePrice: 1500, surgeAmount: 525, perKgRate: 200, isActive: true },
    { deliveryType: 'Economy', basePrice: 800, surgeAmount: 0, perKgRate: 100, isActive: true },
    { deliveryType: 'Batch', basePrice: 2500, surgeAmount: 500, perKgRate: 150, isActive: true },
    { deliveryType: 'Multi-Pickup', basePrice: 3500, surgeAmount: 750, perKgRate: 250, isActive: true },
  ]
  for (const rule of pricingData) {
    await db.collection('pricingRules').add({
      ...rule,
      createdAt: FieldValue.serverTimestamp(),
    })
  }
  console.log('✅ Pricing Rules')

  // ─── Zones ───────────────────────────────────
  const zonesData = [
    { name: 'Lekki Phase 1', basePrice: 1200, surgeMultiplier: 1.5, isActive: true, riderCount: 4, deliveryCount: 89 },
    { name: 'Ikeja', basePrice: 800, surgeMultiplier: 1.2, isActive: true, riderCount: 6, deliveryCount: 145 },
    { name: 'Victoria Island', basePrice: 1500, surgeMultiplier: 2.0, isActive: true, riderCount: 3, deliveryCount: 67 },
    { name: 'Surulere', basePrice: 600, surgeMultiplier: 1.0, isActive: true, riderCount: 5, deliveryCount: 112 },
    { name: 'Maryland', basePrice: 700, surgeMultiplier: 1.1, isActive: true, riderCount: 4, deliveryCount: 98 },
  ]
  for (const zone of zonesData) {
    await db.collection('zones').add({
      ...zone,
      createdAt: FieldValue.serverTimestamp(),
    })
  }
  console.log('✅ Zones')

  // ─── Riders ──────────────────────────────────
  const ridersData = [
    { name: 'Sani Ibrahim', email: 'sani@engraced.com', phone: '+234 803 111 2222', bikeNumber: 'LAG-5832-BK', rating: 4.8, status: 'active', totalDeliveries: 42, totalDistance: 320, isOnline: true, assignedZone: 'Lekki', joinedAt: 'Jan 2024' },
    { name: 'Chukwuemeka Obi', email: 'emeka@engraced.com', phone: '+234 812 333 4444', bikeNumber: 'LAG-3291-YZ', rating: 4.9, status: 'active', totalDeliveries: 58, totalDistance: 510, isOnline: true, assignedZone: 'Ikeja', joinedAt: 'Mar 2023' },
    { name: 'Tunde Bakare', email: 'tunde@engraced.com', phone: '+234 905 555 6666', bikeNumber: 'LAG-7453-MN', rating: 4.7, status: 'active', totalDeliveries: 31, totalDistance: 280, isOnline: true, assignedZone: 'Surulere', joinedAt: 'Jun 2024' },
  ]
  for (const rider of ridersData) {
    await db.collection('riders').add({
      ...rider,
      createdAt: FieldValue.serverTimestamp(),
    })
  }
  console.log('✅ Riders')

  // ─── Customers ───────────────────────────────
  const customersData = [
    { fullName: 'Alabi Johnson', email: 'alabi@email.com', phone: '+234 803 111 0000', photoUrl: '', isVerified: true, rating: 4.5, totalDeliveries: 12, role: 'customer', createdAt: 'Jan 2024' },
    { fullName: 'Fatima Bello', email: 'fatima@email.com', phone: '+234 812 333 0000', photoUrl: '', isVerified: true, rating: 4.8, totalDeliveries: 8, role: 'customer', createdAt: 'Mar 2024' },
    { fullName: 'David Okafor', email: 'david@email.com', phone: '+234 905 555 0000', photoUrl: '', isVerified: false, rating: 4.2, totalDeliveries: 3, role: 'customer', createdAt: 'Jun 2024' },
  ]
  for (const customer of customersData) {
    await db.collection('customers').add({
      ...customer,
      createdAt: FieldValue.serverTimestamp(),
    })
  }
  console.log('✅ Customers')

  // ─── Transactions ────────────────────────────
  const transactionsData = [
    { title: 'Wallet Funding', description: 'Customer deposit', amount: 25000, type: 'CREDIT', status: 'COMPLETED', reference: 'TXN-001', userId: '', userName: 'Alabi Johnson' },
    { title: 'Delivery ESD-ECO-4123', description: 'Booking payment', amount: 800, type: 'CREDIT', status: 'COMPLETED', reference: 'TXN-002', userId: '', userName: 'Fatima Bello' },
    { title: 'Withdrawal Request', description: 'Bank transfer', amount: 45000, type: 'DEBIT', status: 'PENDING', reference: 'TXN-003', userId: '', userName: 'Alabi Johnson' },
  ]
  for (const tx of transactionsData) {
    await db.collection('transactions').add({
      ...tx,
      createdAt: FieldValue.serverTimestamp(),
    })
  }
  console.log('✅ Transactions')

  console.log('\n🎉 Seed complete! All collections populated.')
}

seed().catch((err) => {
  console.error('❌ Seed failed:', err)
  process.exit(1)
})
