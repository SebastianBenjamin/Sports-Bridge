import { TrendingUp, TrendingDown, Minus } from 'lucide-react'

export default function DashboardWidget({
  title,
  value,
  icon: Icon,
  trend,
  trendValue,
  className = '',
  onClick
}) {
  const getTrendIcon = () => {
    if (trend === 'up') return <TrendingUp className="w-4 h-4 text-green-500" />
    if (trend === 'down') return <TrendingDown className="w-4 h-4 text-red-500" />
    return <Minus className="w-4 h-4 text-gray-500" />
  }

  const getTrendColor = () => {
    if (trend === 'up') return 'text-green-600'
    if (trend === 'down') return 'text-red-600'
    return 'text-gray-600'
  }

  return (
    <div
      className={`bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow cursor-pointer ${className}`}
      onClick={onClick}
    >
      <div className="flex items-center justify-between">
        <div className="flex-1">
          <p className="text-sm font-medium text-gray-600">{title}</p>
          <p className="text-2xl font-bold text-gray-900 mt-2">{value}</p>
          {trendValue && (
            <div className="flex items-center mt-2">
              {getTrendIcon()}
              <span className={`text-sm font-medium ml-1 ${getTrendColor()}`}>
                {trendValue}
              </span>
            </div>
          )}
        </div>
        {Icon && (
          <div className="bg-blue-50 p-3 rounded-full">
            <Icon className="w-6 h-6 text-blue-600" />
          </div>
        )}
      </div>
    </div>
  )
}
