'use client'
import { useState } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import { Eye, EyeOff, Trophy, Mail, Lock } from 'lucide-react'
import { api } from '../../lib/api'
import { auth } from '../../lib/auth'
import { USER_ROLES } from '../../lib/constants'

export default function LoginPage() {
  const [formData, setFormData] = useState({
    email: '',
    password: ''
  })
  const [showPassword, setShowPassword] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')
  const router = useRouter()

  const handleChange = (e) => {
    setFormData(prev => ({
      ...prev,
      [e.target.name]: e.target.value
    }))
    setError('')
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setIsLoading(true)
    setError('')

    try {
      const response = await api.login(formData)

      if (response.token && response.user) {
        auth.setToken(response.token)
        auth.setUser(response.user)

        // Redirect based on user role
        switch (response.user.role) {
          case USER_ROLES.ADMIN:
            router.push('/dashboard/admin')
            break
          case USER_ROLES.ATHLETE:
            router.push('/dashboard/athlete')
            break
          case USER_ROLES.COACH:
            router.push('/dashboard/coach')
            break
          case USER_ROLES.SPONSOR:
            router.push('/dashboard/sponsor')
            break
          default:
            router.push('/dashboard')
        }
      } else {
        setError('Invalid login credentials')
      }
    } catch (error) {
      setError('Login failed. Please check your credentials.')
    } finally {
      setIsLoading(false)
    }
  }

  const handleDemoLogin = async (email, password, role) => {
    setIsLoading(true)
    setError('')

    try {
      const response = await api.login({ email, password })

      if (response.token && response.user) {
        auth.setToken(response.token)
        auth.setUser(response.user)

        // Redirect based on user role from the response
        switch (response.user.role) {
          case USER_ROLES.ADMIN:
            router.push('/dashboard/admin')
            break
          case USER_ROLES.ATHLETE:
            router.push('/dashboard/athlete')
            break
          case USER_ROLES.COACH:
            router.push('/dashboard/coach')
            break
          case USER_ROLES.SPONSOR:
            router.push('/dashboard/sponsor')
            break
          default:
            router.push('/dashboard')
        }
      } else {
        setError('Demo login failed')
      }
    } catch (error) {
      setError('Demo login failed. Please try again.')
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-900 via-blue-800 to-green-700 flex items-center justify-center px-4">
      <div className="max-w-md w-full">
        {/* Logo */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center space-x-2 mb-4">
            <Trophy className="h-10 w-10 text-white" />
            <div>
              <h1 className="text-2xl font-bold text-white">Sports Bridge</h1>
              <p className="text-sm text-blue-100">One Team, One Dream.</p>
            </div>
          </div>
        </div>

        {/* Login Form */}
        <div className="bg-white rounded-2xl shadow-2xl p-8">
          <div className="text-center mb-6">
            <h2 className="text-3xl font-bold text-gray-900">Welcome Back</h2>
            <p className="text-gray-600 mt-2">Sign in to your account</p>
          </div>

          {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-6">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Email */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Email Address
              </label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                <input
                  type="email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900 placeholder-gray-500"
                  placeholder="Enter your email"
                  required
                />
              </div>
            </div>

            {/* Password */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Password
              </label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                <input
                  type={showPassword ? 'text' : 'password'}
                  name="password"
                  value={formData.password}
                  onChange={handleChange}
                  className="w-full pl-10 pr-12 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900 placeholder-gray-500"
                  placeholder="Enter your password"
                  required
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                >
                  {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                </button>
              </div>
            </div>

            {/* Submit Button */}
            <button
              type="submit"
              disabled={isLoading}
              className="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 text-white py-3 px-4 rounded-lg font-semibold transition-colors duration-300"
            >
              {isLoading ? 'Signing In...' : 'Sign In'}
            </button>
          </form>

          {/* Links */}
          <div className="mt-6 text-center space-y-2">
            <Link
              href="/forgot-password"
              className="text-blue-600 hover:text-blue-500 text-sm font-medium"
            >
              Forgot your password?
            </Link>
            <div className="text-gray-700 text-sm">
              Don't have an account?{' '}
              <Link
                href="/register"
                className="text-blue-600 hover:text-blue-500 font-medium"
              >
                Sign up
              </Link>
            </div>
          </div>
        </div>

        {/* Demo Credentials */}
        <div className="mt-6 bg-white bg-opacity-20 backdrop-blur-sm rounded-lg p-4 border border-white border-opacity-30">
          <h3 className="text-white font-semibold mb-3 text-center">Quick Demo Login:</h3>
          <div className="space-y-2">
            <button
              onClick={() => handleDemoLogin('admin@sportsbridge.com', 'admin123', 'admin')}
              disabled={isLoading}
              className="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 text-white py-2 px-4 rounded-lg text-sm font-medium transition-colors duration-300 flex items-center justify-between"
            >
              <span>Login as Admin</span>
              <span className="text-blue-200">admin@sportsbridge.com</span>
            </button>

            <button
              onClick={() => handleDemoLogin('athlete@sportsbridge.com', 'athlete123', 'athlete')}
              disabled={isLoading}
              className="w-full bg-green-600 hover:bg-green-700 disabled:bg-green-400 text-white py-2 px-4 rounded-lg text-sm font-medium transition-colors duration-300 flex items-center justify-between"
            >
              <span>Login as Athlete</span>
              <span className="text-green-200">athlete@sportsbridge.com</span>
            </button>

            <button
              onClick={() => handleDemoLogin('coach@sportsbridge.com', 'coach123', 'coach')}
              disabled={isLoading}
              className="w-full bg-yellow-600 hover:bg-yellow-700 disabled:bg-yellow-400 text-white py-2 px-4 rounded-lg text-sm font-medium transition-colors duration-300 flex items-center justify-between"
            >
              <span>Login as Coach</span>
              <span className="text-yellow-200">coach@sportsbridge.com</span>
            </button>

            <button
              onClick={() => handleDemoLogin('sponsor@sportsbridge.com', 'sponsor123', 'sponsor')}
              disabled={isLoading}
              className="w-full bg-purple-600 hover:bg-purple-700 disabled:bg-purple-400 text-white py-2 px-4 rounded-lg text-sm font-medium transition-colors duration-300 flex items-center justify-between"
            >
              <span>Login as Sponsor</span>
              <span className="text-purple-200">sponsor@sportsbridge.com</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
