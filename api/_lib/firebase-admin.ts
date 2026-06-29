import { initializeApp, cert, getApps } from 'firebase-admin/app'
import { getFirestore as _getFirestore, type Firestore } from 'firebase-admin/firestore'
import { getAuth as _getAuth, type Auth } from 'firebase-admin/auth'

let _db: Firestore | null = null
let _auth: Auth | null = null

function init() {
  if (_db) return
  if (getApps().length) { _db = _getFirestore(); _auth = _getAuth(); return }
  const base64 = process.env.FIREBASE_SERVICE_ACCOUNT_BASE64
  if (!base64) {
    console.warn('FIREBASE_SERVICE_ACCOUNT_BASE64 not set — Firestore/Auth calls will fail at runtime')
    return
  }
  try {
    const serviceAccount = JSON.parse(Buffer.from(base64, 'base64').toString('utf-8'))
    initializeApp({ credential: cert(serviceAccount) })
    _db = _getFirestore()
    _auth = _getAuth()
  } catch (err) {
    console.error('Firebase Admin init failed:', err)
  }
}

init()

function getDb(): Firestore {
  if (!_db) throw new Error('Firebase not initialized. Set FIREBASE_SERVICE_ACCOUNT_BASE64 env var.')
  return _db
}

function getAuthInst(): Auth {
  if (!_auth) throw new Error('Firebase not initialized. Set FIREBASE_SERVICE_ACCOUNT_BASE64 env var.')
  return _auth
}

export const db = new Proxy({} as Firestore, {
  get(_, prop: string | symbol) {
    const instance = getDb()
    const value = (instance as any)[prop]
    return typeof value === 'function' ? value.bind(instance) : value
  },
})

export const auth = new Proxy({} as Auth, {
  get(_, prop: string | symbol) {
    const instance = getAuthInst()
    const value = (instance as any)[prop]
    return typeof value === 'function' ? value.bind(instance) : value
  },
})
