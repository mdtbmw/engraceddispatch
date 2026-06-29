'use strict'
const { auth: adminAuth } = require('./firebase')

async function adminAuthMiddleware(req, res, next) {
  const header = req.headers.authorization
  if (!header || !header.startsWith('Bearer ')) {
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

module.exports = { adminAuthMiddleware }
