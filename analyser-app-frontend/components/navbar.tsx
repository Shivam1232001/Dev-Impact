"use client"

import { useState } from "react"
import { useRouter, usePathname } from "next/navigation"
import { Button } from "@/components/ui/button"
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Github, ChevronDown, LogOut, History, Home, Menu, X } from "lucide-react"
import { getStoredUser, clearAuth } from "@/lib/auth"
import { cn } from "@/lib/utils"

interface NavbarProps {
  showUserMenu?: boolean
}

export function Navbar({ showUserMenu = true }: NavbarProps) {
  const router = useRouter()
  const pathname = usePathname()
  const user = getStoredUser()
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false)

  const handleLogout = () => {
    clearAuth()
    router.push("/")
  }

  const handleHistory = () => {
    router.push("/history")
    setIsMobileMenuOpen(false)
  }

  const handleHome = () => {
    router.push("/hero")
    setIsMobileMenuOpen(false)
  }

  const navItems = [
    { name: "Dashboard", href: "/hero", icon: Home },
    { name: "History", href: "/history", icon: History },
  ]

  // 🔹 Derive display name + initials safely
  const displayName = user?.displayName || user?.username || "User"

  const initials = user?.displayName?.[0] || user?.username?.[0] || "U"

  return (
    <nav className="glass-nav sticky top-0 z-40">
      <div className="w-full px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <div className="flex items-center flex-shrink-0">
            <button onClick={handleHome} className="flex items-center hover:opacity-80 transition-opacity">
              <Github className="h-8 w-8 text-primary mr-3" />
              <span className="text-xl font-bold text-foreground">RepoAnalyzer</span>
            </button>
          </div>

          {showUserMenu && user && (
            <div className="hidden md:flex items-center space-x-6 flex-shrink-0">
              <nav className="flex space-x-2">
                {navItems.map((item) => {
                  const Icon = item.icon
                  const isActive = pathname === item.href

                  return (
                    <button
                      key={item.name}
                      onClick={() => router.push(item.href)}
                      className={cn(
                        "flex items-center px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200",
                        isActive
                          ? "bg-primary text-primary-foreground shadow-lg"
                          : "text-muted-foreground hover:text-foreground hover:bg-secondary/50 glass transition-glass",
                      )}
                    >
                      <Icon className="mr-2 h-4 w-4" />
                      {item.name}
                    </button>
                  )
                })}
              </nav>

              <div className="flex items-center space-x-4 pl-4 border-l border-border">
                <span className="text-sm text-muted-foreground">{displayName}</span>

                <DropdownMenu>
                  <DropdownMenuTrigger asChild>
                    <div className="flex items-center cursor-pointer select-none">
                      <Avatar className="h-8 w-8">
                        <AvatarImage src="/developer-avatar.png" alt={displayName} />
                        <AvatarFallback className="bg-primary text-primary-foreground">
                          {initials}
                        </AvatarFallback>
                      </Avatar>
                      <ChevronDown className="h-4 w-4 ml-2" />
                    </div>
                  </DropdownMenuTrigger>

                  <DropdownMenuContent
                    align="end"
                    className="w-48 glass-card z-[9999]" // ensure it overlays navbar
                  >
                    <DropdownMenuItem onClick={handleHistory} className="hover:bg-secondary/50">
                      <History className="mr-2 h-4 w-4" />
                      My History
                    </DropdownMenuItem>
                    <DropdownMenuItem
                      onClick={handleLogout}
                      className="hover:bg-destructive/20 text-destructive"
                    >
                      <LogOut className="mr-2 h-4 w-4" />
                      Logout
                    </DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>

              </div>
            </div>
          )}

          {/* Mobile menu button */}
          {showUserMenu && user && (
            <div className="md:hidden">
              <Button
                variant="ghost"
                size="sm"
                onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
                className="glass"
              >
                {isMobileMenuOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
              </Button>
            </div>
          )}
        </div>

        {/* Mobile Navigation */}
        {isMobileMenuOpen && showUserMenu && user && (
          <div className="md:hidden border-t border-border">
            <div className="px-2 pt-2 pb-3 space-y-1">
              {navItems.map((item) => {
                const Icon = item.icon
                const isActive = pathname === item.href

                return (
                  <button
                    key={item.name}
                    onClick={() => {
                      router.push(item.href)
                      setIsMobileMenuOpen(false)
                    }}
                    className={cn(
                      "flex items-center w-full px-3 py-2 rounded-lg text-sm font-medium transition-all duration-200",
                      isActive
                        ? "bg-primary text-primary-foreground shadow-lg"
                        : "text-muted-foreground hover:text-foreground hover:bg-secondary/50 glass transition-glass",
                    )}
                  >
                    <Icon className="mr-2 h-4 w-4" />
                    {item.name}
                  </button>
                )
              })}

              <div className="border-t border-border pt-3 mt-3">
                <div className="flex items-center px-3 py-2 glass-card rounded-lg">
                  <Avatar className="h-8 w-8 mr-3">
                    <AvatarImage src="/developer-avatar.png" alt={displayName} />
                    <AvatarFallback className="bg-primary text-primary-foreground">{initials}</AvatarFallback>
                  </Avatar>
                  <div>
                    <p className="text-sm font-medium text-foreground">{displayName}</p>
                    <p className="text-xs text-muted-foreground">{user.username}</p>
                  </div>
                </div>

                <button
                  onClick={handleLogout}
                  className="flex items-center w-full px-3 py-2 mt-2 rounded-lg text-sm font-medium text-destructive hover:bg-destructive/20 transition-all duration-200"
                >
                  <LogOut className="mr-2 h-4 w-4" />
                  Logout
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </nav>
  )
}

