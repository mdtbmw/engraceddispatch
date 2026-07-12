import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'

export function middleware(request: NextRequest) {
  const adminAuth = request.cookies.get('admin_auth')
  if (!adminAuth || adminAuth.value !== 'true') {
    const url = new URL('/', request.url)
    url.searchParams.set('redirect', request.nextUrl.pathname)
    return NextResponse.redirect(url)
  }
  const response = NextResponse.next()
  // Enforce secure cookie behavior
  response.headers.set('X-Robots-Tag', 'noindex, nofollow')
  response.headers.set('X-Frame-Options', 'DENY')
  response.headers.set('Referrer-Policy', 'strict-origin-when-cross-origin')
  return response
}

export const config = {
  matcher: '/engdadmin/:path+',
}
