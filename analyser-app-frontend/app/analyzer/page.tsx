"use client"

import { useState, useEffect } from "react"
import { useRouter, useSearchParams } from "next/navigation"
import { Navbar } from "@/components/navbar"
import { AuthGuard } from "@/components/auth-guard"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Progress } from "@/components/ui/progress"
import { ArrowLeft, GitCommit, Users, TrendingUp, ExternalLink } from "lucide-react"

interface Contributor {
  name: string
  impact_summary: string
  contribution_percentage: number
}

interface Statistic {
  author: string
  lines_changed: number
  total_commits: number
}

interface AISummary {
  contributors: Contributor[]
  overall_summary: string
}

interface AnalysisResult {
  ai_summary: AISummary
  statistics: Statistic[]
}

interface AnalysisData {
  id: number
  repoUrl: string
  status: string
  result: AnalysisResult
}

export default function AnalyzerPage() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const [analysisData, setAnalysisData] = useState<AnalysisData | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const analysisId = searchParams.get("id")

  useEffect(() => {
    if (!analysisId) {
      router.push("/history")
      return
    }

    fetchAnalysisData(analysisId)
  }, [router, analysisId])

  const fetchAnalysisData = async (id: string) => {
    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_BASE}/api/v1.0.0/analyze/project/${id}`, {
        headers: {
          "Authorization": `Bearer ${localStorage.getItem("auth_token")}`,
          "Content-Type": "application/json"
        }
      })
      
      if (response.ok) {
        const data = await response.json()
        console.log("Fetched data:", data) // Debug log
        setAnalysisData(data)
      } else {
        console.error("Failed to fetch analysis data")
        router.push("/history")
      }
    } catch (error) {
      console.error("Network error:", error)
      router.push("/history")
    } finally {
      setIsLoading(false)
    }
  }

  const extractRepoName = (repoUrl: string) => {
    try {
      return repoUrl.replace("https://github.com/", "")
    } catch {
      return repoUrl
    }
  }

  if (isLoading) {
    return (
      <AuthGuard>
        <div className="min-h-screen bg-background">
          <Navbar />
          <div className="flex items-center justify-center h-[calc(100vh-4rem)]">
            <div className="text-center">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto mb-4"></div>
              <p className="text-muted-foreground">Analyzing repository data...</p>
            </div>
          </div>
        </div>
      </AuthGuard>
    )
  }

  if (!analysisData || !analysisData.result) {
    return (
      <AuthGuard>
        <div className="min-h-screen bg-background">
          <Navbar />
          <div className="flex items-center justify-center h-[calc(100vh-4rem)]">
            <div className="text-center">
              <p className="text-muted-foreground mb-4">Failed to load analysis data</p>
              <Button onClick={() => router.push("/history")}>Back to History</Button>
            </div>
          </div>
        </div>
      </AuthGuard>
    )
  }

  const { result, repoUrl } = analysisData
  const { ai_summary, statistics } = result
  
  const totalCommits = statistics?.reduce((sum, stat) => sum + stat.total_commits, 0) || 0
  const totalLinesChanged = statistics?.reduce((sum, stat) => sum + stat.lines_changed, 0) || 0
  const repoName = extractRepoName(repoUrl)

  return (
    <AuthGuard>
      <div className="min-h-screen bg-background">
        <Navbar />

        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="mb-6">
            <Button variant="ghost" onClick={() => router.push("/history")} className="mb-4">
              <ArrowLeft className="mr-2 h-4 w-4" />
              Back to History
            </Button>
          </div>

          {/* Repository Header */}
          <Card className="mb-8">
            <CardHeader>
              <div className="flex items-start justify-between">
                <div className="space-y-2">
                  <CardTitle className="text-2xl">{repoName}</CardTitle>
                  <CardDescription>AI-powered repository analysis</CardDescription>
                  <a
                    href={repoUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-primary hover:underline text-sm flex items-center"
                  >
                    {repoUrl}
                    <ExternalLink className="ml-1 h-3 w-3" />
                  </a>
                </div>
              </div>
            </CardHeader>
          </Card>

          {/* Overall Summary */}
          <Card className="mb-8">
            <CardHeader>
              <CardTitle className="text-2xl">Project Analysis</CardTitle>
              <CardDescription>AI-powered insights and contributor analysis</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="prose prose-sm max-w-none">
                <p className="text-foreground leading-relaxed">{ai_summary?.overall_summary || "No summary available"}</p>
              </div>
            </CardContent>
          </Card>

          {/* Summary Stats */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Total Contributors</CardTitle>
                <Users className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{ai_summary?.contributors?.length || 0}</div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Total Commits</CardTitle>
                <GitCommit className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{totalCommits.toLocaleString()}</div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Total Lines Changed</CardTitle>
                <TrendingUp className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{totalLinesChanged.toLocaleString()}</div>
              </CardContent>
            </Card>
          </div>

          {/* Contributor Impact Analysis */}
          <Card className="mb-8">
            <CardHeader>
              <CardTitle className="flex items-center">
                <Users className="mr-2 h-5 w-5" />
                Contributor Impact Analysis
              </CardTitle>
              <CardDescription>AI-generated impact summaries and contribution percentages</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-6">
                {ai_summary?.contributors?.map((contributor, index) => {
                  const stats = statistics?.find(s => s.author === contributor.name)
                  
                  return (
                    <div key={index} className="p-6 border border-border rounded-lg space-y-4">
                      <div className="flex items-start justify-between">
                        <div>
                          <h3 className="font-semibold text-lg text-foreground">{contributor.name}</h3>
                          <Badge variant="secondary" className="mt-2">
                            {contributor.contribution_percentage}% contribution
                          </Badge>
                        </div>
                        {stats && (
                          <div className="text-right space-y-1">
                            <div className="text-sm text-muted-foreground">
                              <span className="font-medium text-foreground">{stats.total_commits}</span> commits
                            </div>
                            <div className="text-sm text-muted-foreground">
                              <span className="font-medium text-foreground">{stats.lines_changed.toLocaleString()}</span> lines
                            </div>
                          </div>
                        )}
                      </div>

                      <div className="space-y-2">
                        <div className="flex justify-between text-xs text-muted-foreground">
                          <span>Impact Level</span>
                          <span>{contributor.contribution_percentage}%</span>
                        </div>
                        <Progress 
                          value={contributor.contribution_percentage} 
                          className="h-2"
                        />
                      </div>

                      <div className="pt-2 border-t border-border">
                        <p className="text-sm text-foreground leading-relaxed">
                          {contributor.impact_summary}
                        </p>
                      </div>
                    </div>
                  )
                })}
              </div>
            </CardContent>
          </Card>

          {/* Statistics Table */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center">
                <TrendingUp className="mr-2 h-5 w-5" />
                Detailed Statistics
              </CardTitle>
              <CardDescription>Quantitative breakdown of each contributor's activity</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead>
                    <tr className="border-b border-border">
                      <th className="text-left py-3 px-4 font-medium text-sm text-muted-foreground">Author</th>
                      <th className="text-right py-3 px-4 font-medium text-sm text-muted-foreground">Commits</th>
                      <th className="text-right py-3 px-4 font-medium text-sm text-muted-foreground">Lines Changed</th>
                      <th className="text-right py-3 px-4 font-medium text-sm text-muted-foreground">Avg Lines/Commit</th>
                    </tr>
                  </thead>
                  <tbody>
                    {statistics?.map((stat, index) => (
                      <tr key={index} className="border-b border-border last:border-0">
                        <td className="py-3 px-4 font-medium">{stat.author}</td>
                        <td className="py-3 px-4 text-right">{stat.total_commits}</td>
                        <td className="py-3 px-4 text-right">{stat.lines_changed.toLocaleString()}</td>
                        <td className="py-3 px-4 text-right">
                          {Math.round(stat.lines_changed / stat.total_commits)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </AuthGuard>
  )
}