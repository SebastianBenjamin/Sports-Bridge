const API_BASE_URL = 'http://localhost:8080/api'

// Mock user data for testing when backend is not available
const MOCK_USERS = {
  'admin@sportsbridge.com': {
    password: 'admin123',
    user: { id: 1, email: 'admin@sportsbridge.com', role: 'ADMIN', firstName: 'Admin', lastName: 'User' },
    token: 'mock-admin-token'
  },
  'athlete@sportsbridge.com': {
    password: 'athlete123',
    user: { id: 2, email: 'athlete@sportsbridge.com', role: 'ATHELETE', firstName: 'John', lastName: 'Athlete' },
    token: 'mock-athlete-token'
  },
  'coach@sportsbridge.com': {
    password: 'coach123',
    user: { id: 3, email: 'coach@sportsbridge.com', role: 'COACH', firstName: 'Jane', lastName: 'Coach' },
    token: 'mock-coach-token'
  },
  'sponsor@sportsbridge.com': {
    password: 'sponsor123',
    user: { id: 4, email: 'sponsor@sportsbridge.com', role: 'SPONSOR', firstName: 'Bob', lastName: 'Sponsor' },
    token: 'mock-sponsor-token'
  }
}

// Check if backend is available
const isBackendAvailable = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/health`, {
      method: 'GET',
      signal: AbortSignal.timeout(3000)
    })
    return response.ok
  } catch (error) {
    return false
  }
}

export const api = {
  // Auth endpoints
  login: async (credentials) => {
    try {
      // First try the real backend
      const response = await fetch(`${API_BASE_URL}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(credentials),
        signal: AbortSignal.timeout(5000)
      })

      if (response.ok) {
        return response.json()
      } else {
        throw new Error('Backend login failed')
      }
    } catch (error) {
      // If backend fails, use mock authentication
      console.warn('Backend not available, using mock authentication:', error.message)

      const mockUser = MOCK_USERS[credentials.email]
      if (mockUser && mockUser.password === credentials.password) {
        // Simulate network delay
        await new Promise(resolve => setTimeout(resolve, 500))
        return {
          token: mockUser.token,
          user: mockUser.user
        }
      } else {
        throw new Error('Invalid credentials')
      }
    }
  },

  register: async (userData) => {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(userData),
        signal: AbortSignal.timeout(5000)
      })

      if (response.ok) {
        return response.json()
      } else {
        throw new Error('Backend registration failed')
      }
    } catch (error) {
      // If backend fails, use mock registration
      console.warn('Backend not available, using mock registration:', error.message)

      // Simulate network delay
      await new Promise(resolve => setTimeout(resolve, 1000))

      // Create mock user response
      const newUser = {
        id: Date.now(),
        email: userData.email,
        role: userData.role,
        firstName: userData.firstName,
        lastName: userData.lastName
      }

      return {
        token: `mock-${userData.role.toLowerCase()}-${Date.now()}`,
        user: newUser
      }
    }
  },

  // User endpoints
  getProfile: async (token) => {
    const response = await fetch(`${API_BASE_URL}/users/profile`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    return response.json()
  },

  updateProfile: async (token, profileData) => {
    const response = await fetch(`${API_BASE_URL}/users/profile`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(profileData)
    })
    return response.json()
  },

  // Athletes endpoints
  getAthletes: async (token) => {
    const response = await fetch(`${API_BASE_URL}/athletes`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    return response.json()
  },

  getAthleteById: async (token, id) => {
    const response = await fetch(`${API_BASE_URL}/athletes/${id}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    return response.json()
  },

  // Coaches endpoints
  getCoaches: async (token) => {
    const response = await fetch(`${API_BASE_URL}/coaches`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    return response.json()
  },

  // Sports endpoints
  getSports: async () => {
    const response = await fetch(`${API_BASE_URL}/sports`)
    return response.json()
  },

  // Posts endpoints
  getPosts: async (token) => {
    const response = await fetch(`${API_BASE_URL}/posts`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    return response.json()
  },

  createPost: async (token, postData) => {
    const response = await fetch(`${API_BASE_URL}/posts`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(postData)
    })
    return response.json()
  },

  // Achievements endpoints
  getAchievements: async (token, athleteId) => {
    const response = await fetch(`${API_BASE_URL}/achievements/athlete/${athleteId}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    return response.json()
  },

  createAchievement: async (token, achievementData) => {
    const response = await fetch(`${API_BASE_URL}/achievements`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(achievementData)
    })
    return response.json()
  },

  // Daily Logs endpoints
  getDailyLogs: async (token, athleteId) => {
    const response = await fetch(`${API_BASE_URL}/daily-logs/athlete/${athleteId}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    return response.json()
  },

  createDailyLog: async (token, logData) => {
    const response = await fetch(`${API_BASE_URL}/daily-logs`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(logData)
    })
    return response.json()
  },

  // Sponsorships endpoints
  getSponsorships: async (token) => {
    const response = await fetch(`${API_BASE_URL}/sponsorships`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    return response.json()
  },

  createSponsorship: async (token, sponsorshipData) => {
    const response = await fetch(`${API_BASE_URL}/sponsorships`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(sponsorshipData)
    })
    return response.json()
  },

  // Invitations endpoints
  getInvitations: async (token) => {
    const response = await fetch(`${API_BASE_URL}/invitations`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    return response.json()
  },

  sendInvitation: async (token, invitationData) => {
    const response = await fetch(`${API_BASE_URL}/invitations`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(invitationData)
    })
    return response.json()
  },

  respondToInvitation: async (token, invitationId, status) => {
    const response = await fetch(`${API_BASE_URL}/invitations/${invitationId}/respond`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ status })
    })
    return response.json()
  },

  // Reports endpoints
  getReports: async (token) => {
    const response = await fetch(`${API_BASE_URL}/reports`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    return response.json()
  },

  createReport: async (token, reportData) => {
    const response = await fetch(`${API_BASE_URL}/reports`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(reportData)
    })
    return response.json()
  }
}
