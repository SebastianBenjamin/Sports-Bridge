'use client'

export const auth = {
  // Token management
  getToken: () => {
    if (typeof window !== 'undefined') {
      return localStorage.getItem('sportsbridge_token')
    }
    return null
  },

  setToken: (token) => {
    if (typeof window !== 'undefined') {
      localStorage.setItem('sportsbridge_token', token)
      // Also set in cookies for middleware
      document.cookie = `token=${token}; path=/; max-age=86400; SameSite=strict`
    }
  },

  removeToken: () => {
    if (typeof window !== 'undefined') {
      localStorage.removeItem('sportsbridge_token')
      localStorage.removeItem('sportsbridge_user')
      // Also remove from cookies
      document.cookie = 'token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT'
    }
  },

  // User data management
  getUser: () => {
    if (typeof window !== 'undefined') {
      const user = localStorage.getItem('sportsbridge_user')
      return user ? JSON.parse(user) : null
    }
    return null
  },

  setUser: (user) => {
    if (typeof window !== 'undefined') {
      localStorage.setItem('sportsbridge_user', JSON.stringify(user))
    }
  },

  // Check if user is authenticated
  isAuthenticated: () => {
    return !!auth.getToken()
  },

  // Check user role
  hasRole: (role) => {
    const user = auth.getUser()
    return user && user.role === role
  },

  // Logout
  logout: () => {
    auth.removeToken()
    if (typeof window !== 'undefined') {
      window.location.href = '/login'
    }
  },

  // Check if token is expired (basic check - you might want to implement JWT decoding)
  isTokenExpired: () => {
    const token = auth.getToken()
    if (!token) return true

    try {
      // Basic implementation - you might want to decode JWT and check exp
      // For now, assume token is valid if it exists
      return false
    } catch (error) {
      return true
    }
  }
}
