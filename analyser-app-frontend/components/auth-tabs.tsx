"use client"

import type React from "react"
import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Loader2, Github } from "lucide-react"
import { loginUser, registerUser, storeUser, isAuthenticated } from "@/lib/auth"

export function AuthTabs() {
  const router = useRouter()
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState("")
  const [success, setSuccess] = useState("")

  useEffect(() => {
    if (isAuthenticated()) {
      console.log("[v0] User already authenticated, redirecting to hero...")
      router.push("/hero")
    }
  }, [router])

  const [loginForm, setLoginForm] = useState({
    email: "",
    password: "",
  })

  const [registerForm, setRegisterForm] = useState({
    firstName: "",
    lastName: "",
    email: "",
    password: "",
  })

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault()
    setIsLoading(true)
    setError("")
    setSuccess("")

    console.log("[v0] Attempting login...")
    const result = await loginUser(loginForm.email, loginForm.password)

    if (result.success && result.user) {
      console.log("[v0] Login successful, storing user data...")
      storeUser(result.user)
      setSuccess(result.message)
      setTimeout(() => {
        router.push("/hero")
      }, 1000)
    } else {
      console.log("[v0] Login failed:", result.message)
      setError(result.message)
    }

    setIsLoading(false)
  }

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault()
    setIsLoading(true)
    setError("")
    setSuccess("")

    console.log("[v0] Attempting registration...")
    const result = await registerUser(
      registerForm.firstName,
      registerForm.lastName,
      registerForm.email,
      registerForm.password,
    )

    if (result.success && result.user) {
      console.log("[v0] Registration successful, storing user data...")
      storeUser(result.user)
      setSuccess(result.message)
      setTimeout(() => {
        router.push("/hero")
      }, 1000)
    } else {
      console.log("[v0] Registration failed:", result.message)
      setError(result.message)
    }

    setIsLoading(false)
  }

  return (
    <div className="min-h-screen flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <div className="flex items-center justify-center mb-6">
            <div className="glass-card p-4 rounded-2xl">
              <Github className="h-12 w-12 text-primary mx-auto mb-2" />
            </div>
          </div>
          <h1 className="text-3xl font-bold text-foreground mb-2">RepoAnalyzer</h1>
          <p className="text-muted-foreground text-lg">Analyze GitHub repositories and contributor insights</p>
        </div>

        <div className="glass-card rounded-2xl p-6">
          <Tabs defaultValue="login" className="w-full">
            <TabsList className="grid w-full grid-cols-2 glass mb-6">
              <TabsTrigger
                value="login"
                className="data-[state=active]:bg-primary data-[state=active]:text-primary-foreground"
              >
                Login
              </TabsTrigger>
              <TabsTrigger
                value="register"
                className="data-[state=active]:bg-primary data-[state=active]:text-primary-foreground"
              >
                Register
              </TabsTrigger>
            </TabsList>

            <TabsContent value="login">
              <div className="space-y-6">
                <div className="text-center">
                  <h2 className="text-2xl font-bold text-foreground mb-2">Welcome back</h2>
                  <p className="text-muted-foreground">Sign in to your account to continue</p>
                </div>

                <form onSubmit={handleLogin} className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="login-email" className="text-foreground">
                      Email
                    </Label>
                    <Input
                      id="login-email"
                      type="email"
                      placeholder="john.doe@example.com"
                      value={loginForm.email}
                      onChange={(e) => setLoginForm({ ...loginForm, email: e.target.value })}
                      className="glass border-border/50 focus:border-primary"
                      required
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="login-password" className="text-foreground">
                      Password
                    </Label>
                    <Input
                      id="login-password"
                      type="password"
                      placeholder="Enter your password"
                      value={loginForm.password}
                      onChange={(e) => setLoginForm({ ...loginForm, password: e.target.value })}
                      className="glass border-border/50 focus:border-primary"
                      required
                    />
                  </div>

                  {error && (
                    <Alert variant="destructive" className="glass-card border-destructive/50">
                      <AlertDescription>{error}</AlertDescription>
                    </Alert>
                  )}

                  {success && (
                    <Alert className="glass-card border-primary/50 text-primary">
                      <AlertDescription>{success}</AlertDescription>
                    </Alert>
                  )}

                  <Button
                    type="submit"
                    className="w-full bg-primary hover:bg-primary/90 text-primary-foreground shadow-lg"
                    disabled={isLoading}
                  >
                    {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                    Sign In
                  </Button>
                </form>

                <div className="mt-6 p-4 glass rounded-lg text-center text-sm text-muted-foreground">
                  <strong>Demo credentials:</strong>
                  <br />
                  john.doe@example.com / password123
                </div>
              </div>
            </TabsContent>

            <TabsContent value="register">
              <div className="space-y-6">
                <div className="text-center">
                  <h2 className="text-2xl font-bold text-foreground mb-2">Create account</h2>
                  <p className="text-muted-foreground">Sign up to start analyzing repositories</p>
                </div>

                <form onSubmit={handleRegister} className="space-y-4">
                  <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label htmlFor="register-firstName" className="text-foreground">
                        First Name
                      </Label>
                      <Input
                        id="register-firstName"
                        placeholder="John"
                        value={registerForm.firstName}
                        onChange={(e) => setRegisterForm({ ...registerForm, firstName: e.target.value })}
                        className="glass border-border/50 focus:border-primary"
                        required
                      />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="register-lastName" className="text-foreground">
                        Last Name
                      </Label>
                      <Input
                        id="register-lastName"
                        placeholder="Doe"
                        value={registerForm.lastName}
                        onChange={(e) => setRegisterForm({ ...registerForm, lastName: e.target.value })}
                        className="glass border-border/50 focus:border-primary"
                        required
                      />
                    </div>
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="register-email" className="text-foreground">
                      Email
                    </Label>
                    <Input
                      id="register-email"
                      type="email"
                      placeholder="john.doe@example.com"
                      value={registerForm.email}
                      onChange={(e) => setRegisterForm({ ...registerForm, email: e.target.value })}
                      className="glass border-border/50 focus:border-primary"
                      required
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="register-password" className="text-foreground">
                      Password
                    </Label>
                    <Input
                      id="register-password"
                      type="password"
                      placeholder="Create a password"
                      value={registerForm.password}
                      onChange={(e) => setRegisterForm({ ...registerForm, password: e.target.value })}
                      className="glass border-border/50 focus:border-primary"
                      required
                    />
                  </div>

                  {error && (
                    <Alert variant="destructive" className="glass-card border-destructive/50">
                      <AlertDescription>{error}</AlertDescription>
                    </Alert>
                  )}

                  {success && (
                    <Alert className="glass-card border-primary/50 text-primary">
                      <AlertDescription>{success}</AlertDescription>
                    </Alert>
                  )}

                  <Button
                    type="submit"
                    className="w-full bg-primary hover:bg-primary/90 text-primary-foreground shadow-lg"
                    disabled={isLoading}
                  >
                    {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                    Create Account
                  </Button>
                </form>
              </div>
            </TabsContent>
          </Tabs>
        </div>
      </div>
    </div>
  )
}
