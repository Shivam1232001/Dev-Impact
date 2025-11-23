export interface User {
  username: string
  token: string
  displayName?: string
}

export const getStoredUser = (): User | null => {
  if (typeof window === "undefined") return null

  const token = localStorage.getItem("auth_token")
  const userData = localStorage.getItem("user_data")

  if (token && userData) {
    return JSON.parse(userData)
  }

  return null
}

export const storeUser = (user: User): void => {
  if (typeof window === "undefined") return

  localStorage.setItem("auth_token", user.token)
  localStorage.setItem("user_data", JSON.stringify(user))
}

export const clearAuth = (): void => {
  if (typeof window === "undefined") return

  localStorage.removeItem("auth_token")
  localStorage.removeItem("user_data")
}

export const isAuthenticated = (): boolean => {
  return getStoredUser() !== null
}

// ✅ Login user
export const loginUser = async (
  username: string,
  password: string,
): Promise<{ success: boolean; user?: User; message: string }> => {
  try {
    const response = await fetch(`${process.env.NEXT_PUBLIC_API_BASE}/api/v1.0.0/auth/login`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ username, password }),
    })

    const data = await response.json()
    if (response.ok) {
      const user: User = {
        username,
        token: data.token,
        displayName: data.displayName || username,
      }

      storeUser(user)

      return {
        success: true,
        user,
        message: data.message || "Login successful",
      }
    } else {
      return {
        success: false,
        message: data.message || "Login failed",
      }
    }
  } catch (error) {
    return {
      success: false,
      message: "Unauthorized. Please try again.",
    }
  }
}

// ✅ Register user
export const registerUser = async (
  firstName: string,
  lastName: string,
  username: string,
  password: string,
): Promise<{ success: boolean; user?: User; message: string }> => {
  try {
    const response = await fetch(`${process.env.NEXT_PUBLIC_API_BASE}/api/v1.0.0/auth/register`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ firstName, lastName, username, password }),
    })

    const data = await response.json()
    
    if (response.ok) {
      const user: User = {
        username,
        token: data.token,
        displayName: data.displayName || `${firstName} ${lastName}`,
      }

      storeUser(user)

      return {
        success: true,
        user,
        message: data.message || "Registration successful",
      }
    } else {
      return {
        success: false,
        message: data.message || "Registration failed",
      }
    }
  } catch (error) {
    return {
      success: false,
      message: "Network error. Please try again.",
    }
  }
}



