'use client'
import { useState, useEffect } from 'react'
import {
  Users, Trophy, Calendar, Flag, TrendingUp, UserCheck,
  Shield, Activity, DollarSign, AlertTriangle, Plus, Eye, Handshake
} from 'lucide-react'
import DashboardWidget from '../../../components/DashboardWidget'
import { api } from '../../../lib/api'
import { auth } from '../../../lib/auth'
import { USER_ROLES } from '../../../lib/constants'

export default function AdminDashboard() {
  const [stats, setStats] = useState({
    totalUsers: 0,
    totalAthletes: 0,
    totalCoaches: 0,
    totalSponsors: 0,
    activeSponsorships: 0,
    pendingReports: 0,
    totalSports: 0,
    newUsersThisWeek: 0
  })
  const [recentUsers, setRecentUsers] = useState([])
  const [recentReports, setRecentReports] = useState([])
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    fetchDashboardData()
  }, [])

  const fetchDashboardData = async () => {
    try {
      const token = auth.getToken()

      if (!token) return

      // In a real app, you'd have admin-specific endpoints
      // For now, we'll use mock data with some API calls
      const [athletes, coaches, sports, reports] = await Promise.all([
        api.getAthletes(token).catch(() => []),
        api.getCoaches(token).catch(() => []),
        api.getSports().catch(() => []),
        api.getReports(token).catch(() => [])
      ])

      // Mock user data for demonstration
      const mockUsers = [
        { id: 1, firstName: 'John', lastName: 'Doe', role: USER_ROLES.ATHLETE, email: 'john@example.com', createdAt: new Date() },
        { id: 2, firstName: 'Jane', lastName: 'Smith', role: USER_ROLES.COACH, email: 'jane@example.com', createdAt: new Date() },
        { id: 3, firstName: 'Mike', lastName: 'Johnson', role: USER_ROLES.SPONSOR, email: 'mike@example.com', createdAt: new Date() }
      ]

      setRecentUsers(mockUsers)
      setRecentReports(reports.slice(0, 5) || [])

      setStats({
        totalUsers: (athletes.length || 0) + (coaches.length || 0) + 50, // Mock total
        totalAthletes: athletes.length || 25,
        totalCoaches: coaches.length || 12,
        totalSponsors: 8, // Mock data
        activeSponsorships: 15, // Mock data
        pendingReports: reports.filter(r => !r.reviewedAt).length || 3,
        totalSports: sports.length || 20,
        newUsersThisWeek: 12 // Mock data
      })
    } catch (error) {
      console.error('Error fetching admin dashboard data:', error)
    } finally {
      setIsLoading(false)
    }
  }

  const getRoleColor = (role) => {
    switch (role) {
      case USER_ROLES.ATHLETE:
        return 'bg-blue-100 text-blue-800'
      case USER_ROLES.COACH:
        return 'bg-green-100 text-green-800'
      case USER_ROLES.SPONSOR:
        return 'bg-purple-100 text-purple-800'
      case USER_ROLES.ADMIN:
        return 'bg-red-100 text-red-800'
      default:
        return 'bg-gray-100 text-gray-800'
    }
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Admin Dashboard</h1>
          <p className="text-gray-600 mt-1">Manage users, sports, and platform oversight</p>
        </div>
        <div className="flex space-x-3">
          <button className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg font-medium flex items-center gap-2 transition-colors">
            <Plus className="w-4 h-4" />
            Add Sport
          </button>
          <button className="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-lg font-medium flex items-center gap-2 transition-colors">
            <UserCheck className="w-4 h-4" />
            Verify Users
          </button>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <DashboardWidget
          title="Total Users"
          value={stats.totalUsers}
          icon={Users}
          trend="up"
          trendValue={`+${stats.newUsersThisWeek} this week`}
        />
        <DashboardWidget
          title="Active Athletes"
          value={stats.totalAthletes}
          icon={Activity}
          trend="up"
          trendValue="+8% from last month"
        />
        <DashboardWidget
          title="Registered Coaches"
          value={stats.totalCoaches}
          icon={UserCheck}
          trend="up"
          trendValue="+3 this month"
        />
        <DashboardWidget
          title="Active Sponsors"
          value={stats.totalSponsors}
          icon={DollarSign}
          trend="neutral"
          trendValue="Verified partners"
        />
      </div>

      {/* Secondary Stats */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <DashboardWidget
          title="Sports Categories"
          value={stats.totalSports}
          icon={Trophy}
          trend="neutral"
          trendValue="Active sports"
        />
        <DashboardWidget
          title="Active Sponsorships"
          value={stats.activeSponsorships}
          icon={Handshake}
          trend="up"
          trendValue="+5 this month"
        />
        <DashboardWidget
          title="Pending Reports"
          value={stats.pendingReports}
          icon={AlertTriangle}
          trend="down"
          trendValue="Needs attention"
          className={stats.pendingReports > 0 ? "border-l-4 border-red-500" : ""}
        />
        <DashboardWidget
          title="Platform Health"
          value="98.5%"
          icon={Shield}
          trend="up"
          trendValue="System uptime"
        />
      </div>

      {/* Main Content Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Recent Users */}
        <div className="bg-white rounded-lg shadow-md">
          <div className="p-6 border-b border-gray-200">
            <div className="flex justify-between items-center">
              <h2 className="text-xl font-semibold text-gray-900">Recent Users</h2>
              <button className="text-blue-600 hover:text-blue-500 text-sm font-medium">
                View All Users
              </button>
            </div>
          </div>
          <div className="p-6">
            <div className="space-y-4">
              {recentUsers.map((user, index) => (
                <div key={index} className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                  <div className="flex items-center space-x-3">
                    <div className="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
                      <Users className="w-5 h-5 text-blue-600" />
                    </div>
                    <div>
                      <h3 className="font-medium text-gray-900">
                        {user.firstName} {user.lastName}
                      </h3>
                      <p className="text-sm text-gray-500">{user.email}</p>
                    </div>
                  </div>
                  <div className="flex items-center space-x-2">
                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${getRoleColor(user.role)}`}>
                      {user.role.toLowerCase()}
                    </span>
                    <button className="text-gray-400 hover:text-gray-600">
                      <Eye className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Pending Reports */}
        <div className="bg-white rounded-lg shadow-md">
          <div className="p-6 border-b border-gray-200">
            <div className="flex justify-between items-center">
              <h2 className="text-xl font-semibold text-gray-900">Pending Reports</h2>
              <button className="text-blue-600 hover:text-blue-500 text-sm font-medium">
                View All Reports
              </button>
            </div>
          </div>
          <div className="p-6">
            {recentReports.length > 0 ? (
              <div className="space-y-4">
                {recentReports.map((report, index) => (
                  <div key={index} className="flex items-start space-x-3 p-4 bg-gray-50 rounded-lg">
                    <div className="w-8 h-8 bg-red-100 rounded-full flex items-center justify-center flex-shrink-0">
                      <Flag className="w-4 h-4 text-red-600" />
                    </div>
                    <div className="flex-1">
                      <h3 className="font-medium text-gray-900">{report.reason || 'Content Report'}</h3>
                      <p className="text-sm text-gray-500 mt-1">
                        {report.description?.substring(0, 80) || 'Report description...'}
                      </p>
                      <p className="text-xs text-gray-400 mt-2">
                        {new Date(report.createdAt).toLocaleDateString()}
                      </p>
                    </div>
                    <button className="text-blue-600 hover:text-blue-500 text-sm font-medium">
                      Review
                    </button>
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-center py-8">
                <Flag className="w-12 h-12 text-gray-400 mx-auto mb-4" />
                <h3 className="text-lg font-medium text-gray-900 mb-2">No pending reports</h3>
                <p className="text-gray-500">All reports have been reviewed</p>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Quick Actions */}
      <div className="bg-white rounded-lg shadow-md p-6">
        <h2 className="text-xl font-semibold text-gray-900 mb-4">Admin Actions</h2>
        <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
          <button className="flex items-center justify-center p-4 border-2 border-dashed border-gray-300 rounded-lg hover:border-blue-500 hover:bg-blue-50 transition-colors">
            <div className="text-center">
              <Users className="w-8 h-8 text-gray-400 mx-auto mb-2" />
              <span className="text-sm font-medium text-gray-600">Manage Users</span>
            </div>
          </button>
          <button className="flex items-center justify-center p-4 border-2 border-dashed border-gray-300 rounded-lg hover:border-green-500 hover:bg-green-50 transition-colors">
            <div className="text-center">
              <Trophy className="w-8 h-8 text-gray-400 mx-auto mb-2" />
              <span className="text-sm font-medium text-gray-600">Manage Sports</span>
            </div>
          </button>
          <button className="flex items-center justify-center p-4 border-2 border-dashed border-gray-300 rounded-lg hover:border-purple-500 hover:bg-purple-50 transition-colors">
            <div className="text-center">
              <Calendar className="w-8 h-8 text-gray-400 mx-auto mb-2" />
              <span className="text-sm font-medium text-gray-600">Event Management</span>
            </div>
          </button>
          <button className="flex items-center justify-center p-4 border-2 border-dashed border-gray-300 rounded-lg hover:border-red-500 hover:bg-red-50 transition-colors">
            <div className="text-center">
              <Flag className="w-8 h-8 text-gray-400 mx-auto mb-2" />
              <span className="text-sm font-medium text-gray-600">Review Reports</span>
            </div>
          </button>
          <button className="flex items-center justify-center p-4 border-2 border-dashed border-gray-300 rounded-lg hover:border-yellow-500 hover:bg-yellow-50 transition-colors">
            <div className="text-center">
              <TrendingUp className="w-8 h-8 text-gray-400 mx-auto mb-2" />
              <span className="text-sm font-medium text-gray-600">Analytics</span>
            </div>
          </button>
        </div>
      </div>
    </div>
  )
}
