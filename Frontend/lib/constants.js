// User Roles - mapping to backend UserRole enum
export const USER_ROLES = {
  ATHLETE: 'ATHELETE', // Note: keeping backend spelling
  COACH: 'COACH',
  SPONSOR: 'SPONSOR',
  ADMIN: 'ADMIN'
}

// Gender - mapping to backend Gender enum
export const GENDER = {
  MALE: 'MALE',
  FEMALE: 'FEMALE',
  TRANS: 'TRANS'
}

// Currency Types - mapping to backend CurrencyType enum
export const CURRENCY_TYPES = {
  INR: 'INR',
  USD: 'USD',
  EUR: 'EUR'
}

// Invitation Status - mapping to backend InvitationStatus enum
export const INVITATION_STATUS = {
  ACCEPTED: 'ACCEPTED',
  DECLINED: 'DECLINED',
  NOT_READ: 'NOT_READ'
}

// Post Types - mapping to backend PostType enum
export const POST_TYPES = {
  DAILYLOG: 'DAILYLOG',
  EVENT: 'EVENT',
  SPONSORSHIP: 'SPONSORSHIP',
  COACHING: 'COACHING',
  FRESHER: 'FRESHER',
  BEGINNER: 'BEGINNER'
}

// Dashboard navigation for different roles
export const DASHBOARD_NAVIGATION = {
  [USER_ROLES.ADMIN]: [
    { name: 'Overview', href: '/dashboard/admin', icon: 'LayoutDashboard' },
    { name: 'Users', href: '/dashboard/admin/users', icon: 'Users' },
    { name: 'Sports', href: '/dashboard/admin/sports', icon: 'Trophy' },
    { name: 'Events', href: '/dashboard/admin/events', icon: 'Calendar' },
    { name: 'Reports', href: '/dashboard/admin/reports', icon: 'Flag' }
  ],
  [USER_ROLES.ATHLETE]: [
    { name: 'Overview', href: '/dashboard/athlete', icon: 'LayoutDashboard' },
    { name: 'Training Logs', href: '/dashboard/athlete/logs', icon: 'BookOpen' },
    { name: 'Achievements', href: '/dashboard/athlete/achievements', icon: 'Trophy' },
    { name: 'Find Coach', href: '/dashboard/athlete/coach', icon: 'UserCheck' },
    { name: 'Events', href: '/dashboard/athlete/events', icon: 'Calendar' },
    { name: 'Sponsorships', href: '/dashboard/athlete/sponsorships', icon: 'DollarSign' },
    { name: 'Insights', href: '/dashboard/athlete/insights', icon: 'TrendingUp' }
  ],
  [USER_ROLES.COACH]: [
    { name: 'Overview', href: '/dashboard/coach', icon: 'LayoutDashboard' },
    { name: 'Students', href: '/dashboard/coach/students', icon: 'Users' },
    { name: 'Events', href: '/dashboard/coach/events', icon: 'Calendar' },
    { name: 'Insights', href: '/dashboard/coach/insights', icon: 'TrendingUp' }
  ],
  [USER_ROLES.SPONSOR]: [
    { name: 'Overview', href: '/dashboard/sponsor', icon: 'LayoutDashboard' },
    { name: 'Explore Athletes', href: '/dashboard/sponsor/explore', icon: 'Search' },
    { name: 'Sponsorships', href: '/dashboard/sponsor/sponsorships', icon: 'Handshake' },
    { name: 'Events', href: '/dashboard/sponsor/events', icon: 'Calendar' }
  ]
}

// Common sports list (can be fetched from API but useful for forms)
export const COMMON_SPORTS = [
  'Football', 'Basketball', 'Cricket', 'Tennis', 'Swimming', 'Athletics',
  'Badminton', 'Volleyball', 'Hockey', 'Wrestling', 'Boxing', 'Cycling',
  'Weightlifting', 'Gymnastics', 'Table Tennis', 'Archery', 'Shooting',
  'Rowing', 'Sailing', 'Equestrian'
]

// Training types for daily logs
export const TRAINING_TYPES = [
  'Cardio', 'Strength Training', 'Skill Practice', 'Match/Competition',
  'Recovery', 'Conditioning', 'Technique', 'Strategy', 'Flexibility', 'Endurance'
]

// Achievement categories
export const ACHIEVEMENT_CATEGORIES = [
  'Gold Medal', 'Silver Medal', 'Bronze Medal', 'First Place', 'Second Place',
  'Third Place', 'Participation', 'Best Performance', 'Most Improved', 'Team Champion'
]

// Countries list (subset for forms)
export const COUNTRIES = [
  'India', 'United States', 'United Kingdom', 'Canada', 'Australia',
  'Germany', 'France', 'Japan', 'South Korea', 'Brazil', 'China',
  'Russia', 'Italy', 'Spain', 'Netherlands', 'Sweden', 'Norway'
]

// Indian states for location selection
export const INDIAN_STATES = [
  'Andhra Pradesh', 'Arunachal Pradesh', 'Assam', 'Bihar', 'Chhattisgarh',
  'Goa', 'Gujarat', 'Haryana', 'Himachal Pradesh', 'Jharkhand', 'Karnataka',
  'Kerala', 'Madhya Pradesh', 'Maharashtra', 'Manipur', 'Meghalaya',
  'Mizoram', 'Nagaland', 'Odisha', 'Punjab', 'Rajasthan', 'Sikkim',
  'Tamil Nadu', 'Telangana', 'Tripura', 'Uttar Pradesh', 'Uttarakhand',
  'West Bengal', 'Delhi', 'Chandigarh', 'Jammu and Kashmir', 'Ladakh'
]

// Budget ranges for sponsors
export const BUDGET_RANGES = [
  '₹10,000 - ₹50,000',
  '₹50,000 - ₹1,00,000',
  '₹1,00,000 - ₹5,00,000',
  '₹5,00,000 - ₹10,00,000',
  '₹10,00,000+'
]

// Industry types for sponsors
export const INDUSTRY_TYPES = [
  'Technology', 'Sports Equipment', 'Healthcare', 'Finance', 'Education',
  'Retail', 'Manufacturing', 'Entertainment', 'Food & Beverage',
  'Automotive', 'Real Estate', 'Telecommunications', 'Other'
]

// Experience levels for coaches
export const EXPERIENCE_LEVELS = [
  '0-1 years', '2-5 years', '6-10 years', '11-15 years', '16-20 years', '20+ years'
]

// Chart colors for data visualization
export const CHART_COLORS = {
  primary: '#3B82F6',
  secondary: '#10B981',
  accent: '#F59E0B',
  danger: '#EF4444',
  warning: '#F97316',
  info: '#06B6D4',
  success: '#22C55E',
  muted: '#6B7280'
}

// API endpoints paths
export const API_ENDPOINTS = {
  AUTH: '/auth',
  USERS: '/users',
  ATHLETES: '/athletes',
  COACHES: '/coaches',
  SPONSORS: '/sponsors',
  SPORTS: '/sports',
  POSTS: '/posts',
  ACHIEVEMENTS: '/achievements',
  DAILY_LOGS: '/daily-logs',
  SPONSORSHIPS: '/sponsorships',
  INVITATIONS: '/invitations',
  REPORTS: '/reports'
}
