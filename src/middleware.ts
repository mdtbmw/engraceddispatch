import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'

// The admin portal handles its own authentication (Firebase client auth gate
// in AdminDashboard sets the admin_token cookie after a successful admin login).
// Middleware only adds security headers and must NOT block the page, otherwise
// the built-in login screen can never render.
export function middleware(request: NextRequest) {
  const response = NextResponse.next()
  response.headers.set('X-Robots-Tag', 'noindex, nofollow')
  response.headers.set('X-Frame-Options', 'DENY')
  response.headers.set('Referrer-Policy', 'strict-origin-when-cross-origin')
  response.headers.set('Permissions-Policy', 'camera=(), microphone=(), geolocation=(self)')
  return response
}

export const config = {
  matcher: ['/engdadmin', '/engdadmin/:path*', '/engadmin', '/engadmin/:path*'],
}
