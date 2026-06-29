/**
 * Middleware: Verifies any valid Firebase ID token — no admin claim required.
 * Used for customer-facing endpoints (payments, wallet funding, etc.).
 */
import type { Request, Response, NextFunction } from 'express'
import { auth as adminAuth } from './firebase-admin'

declare global {
  namespace Express {
    interface Request {
      uid?: string
      email?: string
    }
  }
}

export async function firebaseAuthMiddleware(req: Request, res: Response, next: NextFunction) {
  const header = req.headers.authorization
  if (!header?.startsWith('Bearer ')) {
    res.status(401).json({ success: false, error: { message: 'Missing or invalid Authorization header', code: 401 } })
    return
  }

  const token = header.slice(7)
  try {
    const decoded = await adminAuth.verifyIdToken(token)
    req.uid = decoded.uid
    req.email = decoded.email
    next()
  } catch {
    res.status(401).json({ success: false, error: { message: 'Invalid or expired token', code: 401 } })
  }
}
