"use client"

import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { Navbar } from "@/components/navbar"
import { AuthGuard } from "@/components/auth-guard"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import { Eye, Calendar, Users, GitCommit } from "lucide-react"

interface Analysis {
  id: number
  repoUrl: string
  status: "IN_PROGRESS" | "COMPLETED" | "FAILED"
  result?: {
    contributors?: number
    commits?: number
    analyzedAt?: string
  } | null
}

export default function HistoryPage() {
  const router = useRouter()
  const [analyses, setAnalyses] = useState<Analysis[]>([])
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    fetchAnalyses()
  }, [])

  const fetchAnalyses = async () => {
    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_BASE}/api/v1.0.0/analyze/allProjects`, {
        headers: {
          "Authorization": `Bearer ${localStorage.getItem("auth_token")}`,
          "Content-Type": "application/json"
        }
      })
      if (response.ok) {
        const data = await response.json()
        setAnalyses(data)
      } else {
        console.error("Failed to fetch analyses")
      }
    } catch (error) {
      console.error("Network error:", error)
    } finally {
      setIsLoading(false)
    }
  }

  const handleViewAnalysis = (analysisId: number) => {
    router.push(`/analyzer?id=${analysisId}`)
  }

  const formatDate = (dateString?: string | null) => {
    if (!dateString) return "-"
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    })
  }

  const extractRepoName = (repoUrl: string) => {
    try {
      return repoUrl.replace("https://github.com/", "")
    } catch {
      return repoUrl
    }
  }

  const handleBackToHero = () => {
    router.push("/hero")
  }

  if (isLoading) {
    return (
      <AuthGuard>
        <div className="min-h-screen bg-background">
          <Navbar />
          <div className="flex items-center justify-center h-[calc(100vh-4rem)]">
            <div className="text-center">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto mb-4"></div>
              <p className="text-muted-foreground">Loading your analysis history...</p>
            </div>
          </div>
        </div>
      </AuthGuard>
    )
  }

  return (
    <AuthGuard>
      <div className="min-h-screen bg-background">
        <Navbar />

        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="mb-6">
            <div className="space-y-2">
              <h1 className="text-3xl font-bold text-foreground">Analysis History</h1>
              <p className="text-muted-foreground">View all your previously analyzed repositories and their insights</p>
            </div>
          </div>

          <Card>
            <CardHeader>
              <CardTitle className="flex items-center">
                <Calendar className="mr-2 h-5 w-5" />
                Repository Analysis History
              </CardTitle>
              <CardDescription>{analyses.length} repositories analyzed</CardDescription>
            </CardHeader>
            <CardContent>
              {analyses.length === 0 ? (
                <div className="text-center py-12">
                  <div className="text-muted-foreground mb-4">
                    <Calendar className="h-12 w-12 mx-auto mb-4 opacity-50" />
                    <p className="text-lg">No repositories analyzed yet</p>
                    <p className="text-sm">Start by analyzing your first repository from the dashboard</p>
                  </div>
                  <Button onClick={handleBackToHero}>Go to Dashboard</Button>
                </div>
              ) : (
                <div className="overflow-x-auto">
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>Repository</TableHead>
                        <TableHead>URL</TableHead>
                        <TableHead>Contributors</TableHead>
                        <TableHead>Commits</TableHead>
                        <TableHead>Analyzed</TableHead>
                        <TableHead className="text-right">Actions</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {analyses.map((analysis) => {
                        const repoName = extractRepoName(analysis.repoUrl)
                        const contributors = analysis.result?.contributors ?? "-"
                        const commits = analysis.result?.commits ?? "-"
                        const analyzedAt = analysis.result?.analyzedAt ?? null

                        return (
                          <TableRow key={analysis.id}>
                            <TableCell className="font-medium">
                              <div className="flex items-center">
                                <div className="w-2 h-2 bg-primary rounded-full mr-3"></div>
                                {repoName}
                              </div>
                            </TableCell>
                            <TableCell>
                              <a
                                href={analysis.repoUrl}
                                target="_blank"
                                rel="noopener noreferrer"
                                className="text-primary hover:underline text-sm"
                              >
                                {repoName}
                              </a>
                            </TableCell>
                            <TableCell>
                              <Badge variant="secondary" className="flex items-center w-fit">
                                <Users className="mr-1 h-3 w-3" />
                                {contributors}
                              </Badge>
                            </TableCell>
                            <TableCell>
                              <Badge variant="outline" className="flex items-center w-fit">
                                <GitCommit className="mr-1 h-3 w-3" />
                                {commits}
                              </Badge>
                            </TableCell>
                            <TableCell className="text-muted-foreground text-sm">{formatDate(analyzedAt)}</TableCell>
                            <TableCell className="text-right">
                              {analysis.status === "IN_PROGRESS" && (
                                <Button variant="outline" size="sm" disabled>
                                  In Progress…
                                </Button>
                              )}
                              {analysis.status === "COMPLETED" && (
                                <Button
                                  variant="outline"
                                  size="sm"
                                  onClick={() => handleViewAnalysis(analysis.id)}
                                  className="flex items-center hover:bg-primary hover:text-white"
                                >
                                  <Eye className="mr-2 h-4 w-4" />
                                  View Analysis
                                </Button>
                              )}
                              {analysis.status === "FAILED" && (
                                <Button
                                  variant="outline"
                                  size="sm"
                                  className="flex items-center bg-red-100 text-red-700 hover:bg-red-200 hover:text-red-800"
                                >
                                  Failed
                                </Button>
                              )}
                            </TableCell>
                          </TableRow>
                        )
                      })}
                    </TableBody>
                  </Table>
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </AuthGuard>
  )
}
