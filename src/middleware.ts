import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'

function verifyJwtAtEdge(token: string): boolean {
  try {
    const parts = token.split('.');
    if (parts.length !== 3) return false;
    
    // Decode the Base64URL-encoded payload (2nd part of JWT)
    const base64Url = parts[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = atob(base64);
    const payload = JSON.parse(jsonPayload);
    
    // Check token expiration (exp is in seconds since epoch)
    const currentTime = Math.floor(Date.now() / 1000);
    if (payload.exp && payload.exp < currentTime) {
      console.warn('[Middleware JWT Verification] Token is expired.');
      return false;
    }
    
    // Check user role from Firebase custom claims
    const role = payload.role;
    if (role === 'admin' || role === 'super_admin' || role === 'dispatcher') {
      return true;
    }
    
    console.warn(`[Middleware JWT Verification] Unauthorized role: ${role}`);
    return false;
  } catch (e) {
    console.error('[Middleware JWT Verification Exception]', e);
    return false;
  }
}

export function middleware(request: NextRequest) {
  const tokenCookie = request.cookies.get('admin_token');
  
  let authorized = false;
  if (tokenCookie && tokenCookie.value) {
    authorized = verifyJwtAtEdge(tokenCookie.value);
  }

  if (!authorized) {
    const url = new URL('/', request.url)
    url.searchParams.set('redirect', request.nextUrl.pathname)
    return NextResponse.redirect(url)
  }
  
  const response = NextResponse.next()
  response.headers.set('X-Robots-Tag', 'noindex, nofollow')
  response.headers.set('X-Frame-Options', 'DENY')
  response.headers.set('Referrer-Policy', 'strict-origin-when-cross-origin')
  return response
}

export const config = {
  matcher: '/engdadmin/:path+',
}
