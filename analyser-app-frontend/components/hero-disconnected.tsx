"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Github, Loader2 } from "lucide-react"

interface HeroDisconnectedProps {
  onConnectionSuccess: () => void
}

export function HeroDisconnected({ onConnectionSuccess }: HeroDisconnectedProps) {
  const [isConnecting, setIsConnecting] = useState(false)

  const handleConnectGithub = async () => {
    setIsConnecting(true)

    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_BASE}/api/v1.0.0/auth/github/oauth`, {
        method: "GET",
        headers: {
          "Authorization": `Bearer ${localStorage.getItem("auth_token")}`,
        },
      })

      if (!response.ok) {
        throw new Error("Failed to get GitHub OAuth URL")
      }

      const oauthUrl = await response.text()
      console.log("[v0] Redirecting to GitHub OAuth:", oauthUrl)

      // Redirect user to GitHub OAuth
      window.location.href = oauthUrl
    } catch (error) {
      console.error("Error initiating GitHub connection:", error)
      setIsConnecting(false)
    }
  }

  return (
    <div className="flex-1 flex items-center justify-center p-8">
      <div className="w-full max-w-2xl text-center space-y-8">
        <div className="space-y-4">
          <h1 className="text-4xl font-bold text-foreground text-balance">Connect Your GitHub Account</h1>
          <p className="text-xl text-muted-foreground text-pretty">
            Link your GitHub account to start analyzing repositories and gain insights into contributor activity
          </p>
        </div>

        <Card className="p-8">
          <CardContent className="space-y-6">
            <div className="flex justify-center">
              <Github className="h-16 w-16 text-muted-foreground" />
            </div>

            <div className="space-y-4">
              <h3 className="text-lg font-semibold text-foreground">Why connect GitHub?</h3>
              <ul className="text-left space-y-2 text-muted-foreground">
                <li className="flex items-center">
                  <div className="w-2 h-2 bg-primary rounded-full mr-3"></div>
                  Access your repositories for analysis
                </li>
                <li className="flex items-center">
                  <div className="w-2 h-2 bg-primary rounded-full mr-3"></div>
                  Get detailed contributor insights
                </li>
                <li className="flex items-center">
                  <div className="w-2 h-2 bg-primary rounded-full mr-3"></div>
                  Track repository analytics over time
                </li>
              </ul>
            </div>

            <Button onClick={handleConnectGithub} disabled={isConnecting} size="lg" className="w-full">
              {isConnecting ? (
                <>
                  <Loader2 className="mr-2 h-5 w-5 animate-spin" />
                  Connecting...
                </>
              ) : (
                <>
                  <Github className="mr-2 h-5 w-5" />
                  Connect to GitHub
                </>
              )}
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}

