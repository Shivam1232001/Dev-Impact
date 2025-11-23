"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Card, CardContent } from "@/components/ui/card"
import { Loader2, Search } from "lucide-react"
import { useToast } from "@/hooks/use-toast"

export function HeroConnected() {
  const [repoUrl, setRepoUrl] = useState("")
  const [isAnalyzing, setIsAnalyzing] = useState(false)
  const { toast } = useToast()

  const handleAnalyze = async () => {
    if (!repoUrl.trim()) return

    setIsAnalyzing(true)

    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_BASE}/api/v1.0.0/analyze/repo?repoUrl=${encodeURIComponent(repoUrl)}`,
        {
          method: "POST",
          headers: {
            "Authorization": `Bearer ${localStorage.getItem("auth_token")}`,
          },
        }
      )

      const data = await response.json()
      console.log("Analyze response:", data)

      if (data.status === "success") {
        toast({
          title: "Analysis Complete",
          description: data.message || "Repository analysis completed successfully!",
          variant: "success",
          duration: 4000,
        })
        setRepoUrl("") // clear input on success
      } else {
        toast({
          title: "Analysis Failed",
          description: data.message || "Failed to analyze repository.",
          variant: "destructive",
          duration: 4000,
        })
      }
    } catch (error) {
      console.error("Network error:", error)
      toast({
        title: "Analysis Failed",
        description: "Network error. Please try again.",
        variant: "destructive",
        duration: 4000,
      })
    } finally {
      setIsAnalyzing(false)
    }
  }

  return (
    <div className="flex-1 flex items-center justify-center p-8">
      <div className="w-full max-w-2xl text-center space-y-8">
        <div className="space-y-4">
          <h1 className="text-4xl font-bold text-foreground text-balance">Analyze GitHub Repository</h1>
          <p className="text-xl text-muted-foreground text-pretty">
            Enter a GitHub repository URL to get detailed contributor insights and analytics
          </p>
        </div>

        <Card className="p-8">
          <CardContent className="space-y-6">
            <div className="flex gap-4">
              <Input
                placeholder="https://github.com/username/repository"
                value={repoUrl}
                onChange={(e) => setRepoUrl(e.target.value)}
                className="flex-1 text-lg h-12"
                disabled={isAnalyzing}
              />
              <Button onClick={handleAnalyze} disabled={!repoUrl.trim() || isAnalyzing} size="lg" className="px-8">
                {isAnalyzing ? (
                  <>
                    <Loader2 className="mr-2 h-5 w-5 animate-spin" />
                    Analyzing...
                  </>
                ) : (
                  <>
                    <Search className="mr-2 h-5 w-5" />
                    Analyze
                  </>
                )}
              </Button>
            </div>

            <div className="text-sm text-muted-foreground">Example: https://github.com/vercel/next.js</div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}

