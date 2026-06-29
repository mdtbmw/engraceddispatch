'use strict'
const { initializeApp, cert, getApps } = require('firebase-admin/app')
const { getFirestore, FieldValue } = require('firebase-admin/firestore')
const { getAuth } = require('firebase-admin/auth')

let _db = null, _auth = null

function init() {
  if (_db) return
  if (getApps().length) { _db = getFirestore(); _auth = getAuth(); return }
  const raw = process.env.FIREBASE_SERVICE_ACCOUNT_BASE64
  if (!raw) {
    console.warn('[firebase] FIREBASE_SERVICE_ACCOUNT_BASE64 not set — running in degraded mode')
    return
  }
  try {
    const sa = JSON.parse(Buffer.from(raw, 'base64').toString('utf8'))
    initializeApp({ credential: cert(sa) })
    _db = getFirestore()
    _auth = getAuth()
    console.log('[firebase] Admin SDK initialized')
  } catch (e) {
    console.error('[firebase] Init failed:', e.message)
  }
}

init()

function getDb() {
  if (!_db) throw new Error('Firebase not initialized: missing/invalid FIREBASE_SERVICE_ACCOUNT_BASE64')
  return _db
}

function getAuthInst() {
  if (!_auth) throw new Error('Firebase not initialized: missing/invalid FIREBASE_SERVICE_ACCOUNT_BASE64')
  return _auth
}

module.exports = {
  db: new Proxy({}, {
    get(_, prop) {
      const instance = getDb()
      const val = instance[prop]
      return typeof val === 'function' ? val.bind(instance) : val
    }
  }),
  auth: new Proxy({}, {
    get(_, prop) {
      const instance = getAuthInst()
      const val = instance[prop]
      return typeof val === 'function' ? val.bind(instance) : val
    }
  }),
  FieldValue,
}
