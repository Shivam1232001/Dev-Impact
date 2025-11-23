"use client"

import { useState, useEffect } from "react"
import { useRouter, useSearchParams } from "next/navigation"
import { Navbar } from "@/components/navbar"
import { HeroConnected } from "@/components/hero-connected"
import { HeroDisconnected } from "@/components/hero-disconnected"
import { AuthGuard } from "@/components/auth-guard"

export default function HeroPage() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const [isGithubConnected, setIsGithubConnected] = useState(false)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    console.log("[v0] Hero page mounted, checking GitHub connection...")

    const authSuccess = searchParams.get("auth") === "success"
    const tokenStored = searchParams.get("token_stored") === "true"

    if (authSuccess && tokenStored) {
      console.log("[v0] Auth success detected, setting GitHub as connected")
      setIsGithubConnected(true)
      window.history.replaceState({}, "", "/hero")
      setIsLoading(false)
    } else {
      checkGithubConnection()
    }
  }, [searchParams])

  const checkGithubConnection = async () => {
    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_BASE}/api/v1.0.0/auth/github/status`, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${localStorage.getItem("auth_token") || ""}`,
        },
      })

      if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`)
      }

      const data: boolean = await response.json()
      console.log("GitHub connection status response:", data)
      setIsGithubConnected(data)
    } catch (error) {
      console.error("Failed to check GitHub connection status:", error)
      setIsGithubConnected(false)
    } finally {
      setIsLoading(false)
    }
  }

  const handleConnectionSuccess = () => {
    console.log("[v0] GitHub connection successful")
    setIsGithubConnected(true)
  }

  if (isLoading) {
    return (
      <AuthGuard>
        <div className="min-h-screen flex flex-col">
          <Navbar />
          <div className="flex items-center justify-center flex-1">
            <div className="text-center glass-card p-8 rounded-2xl">
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
              <p className="text-muted-foreground text-lg">
                Loading dashboard...
              </p>
            </div>
          </div>
        </div>
      </AuthGuard>
    )
  }

  return (
    <AuthGuard>
      <div className="min-h-screen flex flex-col">
        <Navbar />
        {isGithubConnected ? (
          <HeroConnected />
        ) : (
          <HeroDisconnected onConnectionSuccess={handleConnectionSuccess} />
        )}
      </div>
    </AuthGuard>
  )
}

