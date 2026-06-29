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

export async function adminAuthMiddleware(req: Request, res: Response, next: NextFunction) {
  const header = req.headers.authorization
  if (!header?.startsWith('Bearer ')) {
    res.status(401).json({ success: false, error: { message: 'Missing or invalid Authorization header', code: 401 } })
    return
  }

  const token = header.slice(7)
  try {
    const decoded = await adminAuth.verifyIdToken(token)
    if (!decoded.admin) {
      res.status(403).json({ success: false, error: { message: 'Admin privileges required', code: 403 } })
      return
    }
    req.uid = decoded.uid
    req.email = decoded.email
    next()
  } catch {
    res.status(401).json({ success: false, error: { message: 'Invalid or expired token', code: 401 } })
  }
}
