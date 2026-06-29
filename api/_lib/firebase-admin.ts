import { initializeApp, cert, getApps } from 'firebase-admin/app'
import { getFirestore } from 'firebase-admin/firestore'
import { getAuth } from 'firebase-admin/auth'

function init() {
  if (getApps().length) return
  const base64 = process.env.FIREBASE_SERVICE_ACCOUNT_BASE64
  if (!base64) {
    console.warn('FIREBASE_SERVICE_ACCOUNT_BASE64 not set — Firestore/Auth calls will fail at runtime')
    return
  }
  try {
    const serviceAccount = JSON.parse(Buffer.from(base64, 'base64').toString('utf-8'))
    initializeApp({ credential: cert(serviceAccount) })
  } catch (err) {
    console.error('Firebase Admin init failed:', err)
  }
}

init()

export const db = getFirestore()
export const auth = getAuth()
