import { NextResponse } from "next/server"
import type { NextRequest } from "next/server"

export function middleware(request: NextRequest) {
  // Get the pathname of the request (e.g. /, /hero, /history)
  const { pathname } = request.nextUrl

  // Define protected routes
  const protectedRoutes = ["/hero", "/history", "/analyzer"]

  // Check if the current path is a protected route
  const isProtectedRoute = protectedRoutes.some((route) => pathname.startsWith(route))

  if (isProtectedRoute) {
    // Check for auth token in cookies or headers
    // Since we're using localStorage for demo, we'll let the client-side handle auth
    // In a real app, you'd check for JWT tokens in cookies here

    // For now, we'll let all requests through and handle auth on client-side
    return NextResponse.next()
  }

  return NextResponse.next()
}

export const config = {
  matcher: [
    /*
     * Match all request paths except for the ones starting with:
     * - api (API routes)
     * - _next/static (static files)
     * - _next/image (image optimization files)
     * - favicon.ico (favicon file)
     */
    "/((?!api|_next/static|_next/image|favicon.ico).*)",
  ],
}
