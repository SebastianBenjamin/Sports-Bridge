'use client'
import { useState, useEffect } from 'react'
import {
  Trophy, Activity, Calendar, Target, Clock, TrendingUp,
  Plus, BookOpen, Award, Users, DollarSign
} from 'lucide-react'
import DashboardWidget from '../../../components/DashboardWidget'
import { api } from '../../../lib/api'
import { auth } from '../../../lib/auth'

export default function AthleteDashboard() {
  const [stats, setStats] = useState({
    totalTrainingSessions: 0,
    thisWeekSessions: 0,
    totalAchievements: 0,
    activeSponsorships: 0,
    upcomingEvents: 0
  })
  const [recentLogs, setRecentLogs] = useState([])
  const [achievements, setAchievements] = useState([])
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    fetchDashboardData()
  }, [])

  const fetchDashboardData = async () => {
    try {
      const token = auth.getToken()
      const user = auth.getUser()

      if (!token || !user) return

      // Fetch data in parallel
      const [logsResponse, achievementsResponse, sponsorshipsResponse] = await Promise.all([
        api.getDailyLogs(token, user.id),
        api.getAchievements(token, user.id),
        api.getSponsorships(token)
      ])

      setRecentLogs(logsResponse.slice(0, 5) || [])
      setAchievements(achievementsResponse.slice(0, 3) || [])

      // Calculate stats
      setStats({
        totalTrainingSessions: logsResponse.length || 0,
        thisWeekSessions: getThisWeekSessions(logsResponse),
        totalAchievements: achievementsResponse.length || 0,
        activeSponsorships: sponsorshipsResponse.filter(s => s.status === 'ACCEPTED').length || 0,
        upcomingEvents: 3 // Mock data - replace with actual API call
      })
    } catch (error) {
      console.error('Error fetching dashboard data:', error)
    } finally {
      setIsLoading(false)
    }
  }

  const getThisWeekSessions = (logs) => {
    const oneWeekAgo = new Date()
    oneWeekAgo.setDate(oneWeekAgo.getDate() - 7)

    return logs.filter(log =>
      new Date(log.createdAt) >= oneWeekAgo
    ).length
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
          <h1 className="text-3xl font-bold text-gray-900">Athlete Dashboard</h1>
          <p className="text-gray-600 mt-1">Track your training, achievements, and opportunities</p>
        </div>
        <button className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg font-medium flex items-center gap-2 transition-colors">
          <Plus className="w-4 h-4" />
          Log Training
        </button>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-6">
        <DashboardWidget
          title="Total Sessions"
          value={stats.totalTrainingSessions}
          icon={Activity}
          trend="up"
          trendValue="+12% from last month"
        />
        <DashboardWidget
          title="This Week"
          value={stats.thisWeekSessions}
          icon={Clock}
          trend="up"
          trendValue="+3 from last week"
        />
        <DashboardWidget
          title="Achievements"
          value={stats.totalAchievements}
          icon={Trophy}
          trend="up"
          trendValue="+2 this month"
        />
        <DashboardWidget
          title="Sponsorships"
          value={stats.activeSponsorships}
          icon={DollarSign}
          trend="neutral"
          trendValue="Active deals"
        />
        <DashboardWidget
          title="Upcoming Events"
          value={stats.upcomingEvents}
          icon={Calendar}
          trend="neutral"
          trendValue="Next 30 days"
        />
      </div>

      {/* Main Content Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Recent Training Logs */}
        <div className="lg:col-span-2 bg-white rounded-lg shadow-md">
          <div className="p-6 border-b border-gray-200">
            <div className="flex justify-between items-center">
              <h2 className="text-xl font-semibold text-gray-900">Recent Training Sessions</h2>
              <button className="text-blue-600 hover:text-blue-500 text-sm font-medium">
                View All
              </button>
            </div>
          </div>
          <div className="p-6">
            {recentLogs.length > 0 ? (
              <div className="space-y-4">
                {recentLogs.map((log, index) => (
                  <div key={index} className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                    <div className="flex items-center space-x-3">
                      <div className="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
                        <Activity className="w-5 h-5 text-blue-600" />
                      </div>
                      <div>
                        <h3 className="font-medium text-gray-900">{log.trainingType || 'Training Session'}</h3>
                        <p className="text-sm text-gray-500">
                          {log.trainingDurationMinutes || 60} minutes • {log.sport?.name || 'General'}
                        </p>
                      </div>
                    </div>
                    <div className="text-sm text-gray-500">
                      {new Date(log.createdAt).toLocaleDateString()}
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-center py-8">
                <BookOpen className="w-12 h-12 text-gray-400 mx-auto mb-4" />
                <h3 className="text-lg font-medium text-gray-900 mb-2">No training logs yet</h3>
                <p className="text-gray-500 mb-4">Start tracking your training sessions to see your progress</p>
                <button className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg font-medium">
                  Log Your First Session
                </button>
              </div>
            )}
          </div>
        </div>

        {/* Recent Achievements */}
        <div className="bg-white rounded-lg shadow-md">
          <div className="p-6 border-b border-gray-200">
            <h2 className="text-xl font-semibold text-gray-900">Recent Achievements</h2>
          </div>
          <div className="p-6">
            {achievements.length > 0 ? (
              <div className="space-y-4">
                {achievements.map((achievement, index) => (
                  <div key={index} className="flex items-start space-x-3">
                    <div className="w-8 h-8 bg-yellow-100 rounded-full flex items-center justify-center flex-shrink-0">
                      <Award className="w-4 h-4 text-yellow-600" />
                    </div>
                    <div className="flex-1">
                      <h3 className="font-medium text-gray-900">{achievement.title}</h3>
                      <p className="text-sm text-gray-500">{achievement.competitionName}</p>
                      <p className="text-xs text-gray-400 mt-1">
                        {new Date(achievement.achievementDate).toLocaleDateString()}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-center py-8">
                <Trophy className="w-12 h-12 text-gray-400 mx-auto mb-4" />
                <h3 className="text-lg font-medium text-gray-900 mb-2">No achievements yet</h3>
                <p className="text-gray-500">Your achievements will appear here</p>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Quick Actions */}
      <div className="bg-white rounded-lg shadow-md p-6">
        <h2 className="text-xl font-semibold text-gray-900 mb-4">Quick Actions</h2>
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <button className="flex items-center justify-center p-4 border-2 border-dashed border-gray-300 rounded-lg hover:border-blue-500 hover:bg-blue-50 transition-colors">
            <div className="text-center">
              <Plus className="w-8 h-8 text-gray-400 mx-auto mb-2" />
              <span className="text-sm font-medium text-gray-600">Log Training</span>
            </div>
          </button>
          <button className="flex items-center justify-center p-4 border-2 border-dashed border-gray-300 rounded-lg hover:border-green-500 hover:bg-green-50 transition-colors">
            <div className="text-center">
              <Award className="w-8 h-8 text-gray-400 mx-auto mb-2" />
              <span className="text-sm font-medium text-gray-600">Add Achievement</span>
            </div>
          </button>
          <button className="flex items-center justify-center p-4 border-2 border-dashed border-gray-300 rounded-lg hover:border-purple-500 hover:bg-purple-50 transition-colors">
            <div className="text-center">
              <Users className="w-8 h-8 text-gray-400 mx-auto mb-2" />
              <span className="text-sm font-medium text-gray-600">Find Coach</span>
            </div>
          </button>
          <button className="flex items-center justify-center p-4 border-2 border-dashed border-gray-300 rounded-lg hover:border-yellow-500 hover:bg-yellow-50 transition-colors">
            <div className="text-center">
              <Target className="w-8 h-8 text-gray-400 mx-auto mb-2" />
              <span className="text-sm font-medium text-gray-600">Set Goals</span>
            </div>
          </button>
        </div>
      </div>
    </div>
  )
}
