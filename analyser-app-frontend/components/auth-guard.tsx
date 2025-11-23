"use client"

import type React from "react"
import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { isAuthenticated } from "@/lib/auth"

interface AuthGuardProps {
  children: React.ReactNode
  fallback?: React.ReactNode
}

export function AuthGuard({ children, fallback }: AuthGuardProps) {
  const router = useRouter()
  const [isLoading, setIsLoading] = useState(true)
  const [isAuth, setIsAuth] = useState(false)

  useEffect(() => {
    const checkAuth = () => {
      console.log("[v0] Checking authentication status...")
      const authenticated = isAuthenticated()
      console.log("[v0] Authentication result:", authenticated)

      setIsAuth(authenticated)

      if (!authenticated) {
        console.log("[v0] User not authenticated, redirecting to login...")
        router.push("/")
      } else {
        console.log("[v0] User authenticated, allowing access")
      }

      setIsLoading(false)
    }

    const timer = setTimeout(checkAuth, 100)
    return () => clearTimeout(timer)
  }, [router])

  if (isLoading) {
    return (
      fallback || (
        <div className="min-h-screen flex items-center justify-center">
          <div className="text-center glass-card p-8 rounded-2xl">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
            <p className="text-muted-foreground text-lg">Loading...</p>
          </div>
        </div>
      )
    )
  }

  if (!isAuth) {
    return null
  }

  return <>{children}</>
}
