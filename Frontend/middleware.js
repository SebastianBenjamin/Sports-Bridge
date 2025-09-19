import { NextResponse } from 'next/server'

export function middleware(request) {
  // Get the pathname of the request (e.g. /, /dashboard, /login)
  const path = request.nextUrl.pathname

  // Check if the path is for login, register or public routes
  const isPublicPath = path === '/login' || path === '/register' || path === '/'

  // Get token from request
  const token = request.cookies.get('token')?.value

  // If it's a public path and user has token, redirect to dashboard
  if (isPublicPath && token && path !== '/') {
    // Don't redirect to a specific dashboard, let the login page handle the role-based routing
    return NextResponse.next()
  }

  // If it's a protected path and user doesn't have token, redirect to login
  if (!isPublicPath && !token) {
    return NextResponse.redirect(new URL('/login', request.nextUrl))
  }

  return NextResponse.next()
}

// Configure which paths the middleware should run on
export const config = {
  matcher: [
    '/((?!api|_next/static|_next/image|favicon.ico).*)',
  ]
}
