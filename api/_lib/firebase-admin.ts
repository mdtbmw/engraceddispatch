import { initializeApp, cert, getApps } from 'firebase-admin/app'
import { getFirestore } from 'firebase-admin/firestore'
import { getAuth } from 'firebase-admin/auth'

function init() {
  const base64 = process.env.FIREBASE_SERVICE_ACCOUNT_BASE64
  if (!base64) throw new Error('FIREBASE_SERVICE_ACCOUNT_BASE64 env var not set')

  if (getApps().length) return

  const serviceAccount = JSON.parse(
    Buffer.from(base64, 'base64').toString('utf-8'),
  )

  initializeApp({
    credential: cert(serviceAccount),
  })
}

init()

export const db = getFirestore()
export const auth = getAuth()
