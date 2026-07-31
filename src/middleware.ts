import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'

export async function middleware(request: NextRequest) {
  const adminToken = request.cookies.get('admin_token')
  if (!adminToken || !adminToken.value) {
    const url = new URL('/', request.url)
    url.searchParams.set('redirect', request.nextUrl.pathname)
    return NextResponse.redirect(url)
  }
  try {
    const origin = request.nextUrl.origin
    const verifyRes = await fetch(`${origin}/api/auth/verify`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ idToken: adminToken.value }),
    })
    if (!verifyRes.ok) {
      const url = new URL('/', request.url)
      url.searchParams.set('redirect', request.nextUrl.pathname)
      const resp = NextResponse.redirect(url)
      resp.cookies.delete('admin_token')
      return resp
    }
    const data = await verifyRes.json()
    if (!data.valid) {
      const url = new URL('/', request.url)
      url.searchParams.set('redirect', request.nextUrl.pathname)
      const resp = NextResponse.redirect(url)
      resp.cookies.delete('admin_token')
      return resp
    }
    const response = NextResponse.next()
    response.headers.set('X-Robots-Tag', 'noindex, nofollow')
    response.headers.set('X-Frame-Options', 'DENY')
    response.headers.set('Referrer-Policy', 'strict-origin-when-cross-origin')
    response.headers.set('X-Admin-Role', data.role)
    return response
  } catch {
    const url = new URL('/', request.url)
    url.searchParams.set('redirect', request.nextUrl.pathname)
    const resp = NextResponse.redirect(url)
    resp.cookies.delete('admin_token')
    return resp
  }
}

export const config = {
  matcher: '/engdadmin/:path+',
}
