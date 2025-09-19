'use client'
import NavBar from '../components/NavBar'
import { Trophy, Users, Calendar, Target, ArrowRight, Play } from 'lucide-react'
import { useRouter } from 'next/navigation'

export default function Home() {
  const router = useRouter()

  const stats = [
    { number: '50+', label: 'Sports', bgNumber: '50' },
    { number: '200+', label: 'Athletes', bgNumber: '200' },
    { number: '5+', label: 'Years', bgNumber: '5' },
    { number: '15+', label: 'Clubs', bgNumber: '15' },
  ]

  const features = [
    {
      icon: Users,
      title: 'Connect Athletes',
      description: 'Bridge the gap between talented athletes and opportunities'
    },
    {
      icon: Trophy,
      title: 'Find Coaches',
      description: 'Match with experienced coaches to elevate your game'
    },
    {
      icon: Target,
      title: 'Sponsorship Opportunities',
      description: 'Connect sponsors with promising athletes and events'
    },
    {
      icon: Calendar,
      title: 'Event Management',
      description: 'Organize and participate in sports events and tournaments'
    }
  ]

  return (
    <div className="min-h-screen bg-gray-50">
      <NavBar />

      {/* Hero Section */}
      <section className="relative min-h-screen flex items-center justify-center overflow-hidden">
        {/* Background with sports theme */}
        <div className="absolute inset-0 bg-gradient-to-br from-blue-900 via-blue-800 to-green-700">
          <div className="absolute inset-0 bg-black/30"></div>
          {/* Sports equipment background elements */}
          <div className="absolute top-20 left-10 w-32 h-32 bg-white/10 rounded-full blur-xl"></div>
          <div className="absolute bottom-20 right-10 w-48 h-48 bg-green-400/20 rounded-full blur-2xl"></div>
          <div className="absolute top-1/2 left-1/4 w-24 h-24 bg-blue-400/20 rounded-full blur-lg"></div>
        </div>

        <div className="relative z-10 text-center px-6 max-w-6xl mx-auto">
          <h1 className="text-5xl md:text-7xl font-bold text-white mb-6 leading-tight">
            FIND YOUR
            <br />
            <span className="bg-gradient-to-r from-green-400 to-blue-400 bg-clip-text text-transparent">
              BEST SPORT
            </span>
            <br />
            <span className="text-white">MOTIVATION</span>
          </h1>

          <p className="text-xl md:text-2xl text-gray-200 mb-8 max-w-3xl mx-auto">
            Connect athletes, coaches, and sponsors in one powerful platform.
            Your journey to sports excellence starts here.
          </p>

          <div className="flex flex-col sm:flex-row gap-4 justify-center items-center">
            <button
              onClick={() => router.push('/login')}
              className="bg-green-500 hover:bg-green-600 text-white px-8 py-4 rounded-lg text-lg font-semibold transition-all duration-300 flex items-center gap-2 group"
            >
              Get Started
              <ArrowRight className="w-5 h-5 group-hover:translate-x-1 transition-transform" />
            </button>
            <button
              onClick={() => router.push('/login')}
              className="border-2 border-white text-white hover:bg-white hover:text-gray-900 px-8 py-4 rounded-lg text-lg font-semibold transition-all duration-300 flex items-center gap-2"
            >
              <Play className="w-5 h-5" />
              Watch Demo
            </button>
          </div>
        </div>

        {/* Scroll indicator */}
        <div className="absolute bottom-8 left-1/2 transform -translate-x-1/2">
          <div className="w-6 h-10 border-2 border-white/50 rounded-full flex justify-center">
            <div className="w-1 h-3 bg-white/70 rounded-full mt-2 animate-bounce"></div>
          </div>
        </div>
      </section>

      {/* About Section */}
      <section className="py-20 bg-white">
        <div className="max-w-7xl mx-auto px-6">
          <div className="grid md:grid-cols-2 gap-12 items-center">
            <div>
              <div className="flex items-center space-x-4 mb-6">
                <div className="relative">
                  <div className="w-20 h-20 bg-gradient-to-r from-blue-500 to-green-500 rounded-full flex items-center justify-center">
                    <Trophy className="w-10 h-10 text-white" />
                  </div>
                </div>
                <div>
                  <h2 className="text-4xl font-bold text-gray-900">Sports Bridge</h2>
                  <p className="text-gray-600 text-lg">One Team, One Dream.</p>
                </div>
              </div>

              <h3 className="text-3xl font-bold text-gray-900 mb-6">
                SERVE, SMASH, SCORE
              </h3>

              <p className="text-gray-600 mb-6 text-lg leading-relaxed">
                Serve, Smash, Score: SportsBridge – Where Champions Meet Opportunities on the Digital Court
              </p>

              <p className="text-gray-600 mb-6">
                Our aim? To be everywhere in sports excellence
              </p>

              <p className="text-gray-600 mb-8">
                People know us for being trustworthy and excellent in the sports community across the globe.
                We're on a journey of commitment and excellence, and we're excited to share it with you!
              </p>

              <button
                onClick={() => router.push('/login')}
                className="bg-green-500 hover:bg-green-600 text-white px-8 py-3 rounded-lg font-semibold transition-colors duration-300"
              >
                JOIN THE COMMUNITY
              </button>
            </div>

            <div className="relative">
              <div className="bg-gradient-to-br from-blue-100 to-green-100 rounded-2xl p-8 text-center">
                <h4 className="text-2xl font-bold text-gray-900 mb-4">
                  OUR PLATFORM MAKES THE WORLD'S TOP 10
                </h4>
                <p className="text-gray-600 mb-8">
                  Connecting sports communities worldwide
                </p>

                {/* Stats Grid */}
                <div className="grid grid-cols-2 gap-6">
                  {stats.map((stat, index) => (
                    <div key={index} className="relative bg-white rounded-xl p-6 shadow-lg">
                      <div className="stats-number absolute inset-0 flex items-center justify-center text-gray-100">
                        {stat.bgNumber}
                      </div>
                      <div className="relative z-10">
                        <div className="text-3xl font-bold text-gray-900 mb-2">{stat.number}</div>
                        <div className="text-gray-600 font-medium uppercase tracking-wide text-sm">{stat.label}</div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-20 bg-gray-50">
        <div className="max-w-7xl mx-auto px-6">
          <div className="text-center mb-16">
            <h2 className="text-4xl font-bold text-gray-900 mb-4">
              Why Choose Sports Bridge?
            </h2>
            <p className="text-xl text-gray-600 max-w-3xl mx-auto">
              Empowering the sports community with cutting-edge technology and seamless connections
            </p>
          </div>

          <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-8">
            {features.map((feature, index) => (
              <div key={index} className="bg-white rounded-xl p-6 shadow-lg hover:shadow-xl transition-shadow duration-300">
                <div className="w-12 h-12 bg-gradient-to-r from-blue-500 to-green-500 rounded-lg flex items-center justify-center mb-4">
                  <feature.icon className="w-6 h-6 text-white" />
                </div>
                <h3 className="text-xl font-semibold text-gray-900 mb-3">{feature.title}</h3>
                <p className="text-gray-600">{feature.description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-20 bg-gradient-to-r from-blue-600 to-green-600">
        <div className="max-w-4xl mx-auto text-center px-6">
          <h2 className="text-4xl font-bold text-white mb-6">
            Ready to Bridge Your Sports Journey?
          </h2>
          <p className="text-xl text-blue-100 mb-8">
            Join thousands of athletes, coaches, and sponsors already using Sports Bridge
          </p>
          <button
            onClick={() => router.push('/login')}
            className="bg-white text-blue-600 hover:bg-gray-100 px-8 py-4 rounded-lg text-lg font-semibold transition-colors duration-300"
          >
            Start Your Journey Today
          </button>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-gray-900 text-white py-12">
        <div className="max-w-7xl mx-auto px-6">
          <div className="grid md:grid-cols-4 gap-8">
            <div>
              <div className="flex items-center space-x-2 mb-4">
                <Trophy className="h-8 w-8 text-green-400" />
                <div>
                  <h3 className="text-xl font-bold">Sports Bridge</h3>
                  <p className="text-sm text-gray-400">One Team, One Dream.</p>
                </div>
              </div>
              <p className="text-gray-400">
                Connecting the sports community worldwide.
              </p>
            </div>

            <div>
              <h4 className="font-semibold mb-4">Platform</h4>
              <ul className="space-y-2 text-gray-400">
                <li><a href="#" className="hover:text-white">Athletes</a></li>
                <li><a href="#" className="hover:text-white">Coaches</a></li>
                <li><a href="#" className="hover:text-white">Sponsors</a></li>
                <li><a href="#" className="hover:text-white">Events</a></li>
              </ul>
            </div>

            <div>
              <h4 className="font-semibold mb-4">Company</h4>
              <ul className="space-y-2 text-gray-400">
                <li><a href="#" className="hover:text-white">About Us</a></li>
                <li><a href="#" className="hover:text-white">Careers</a></li>
                <li><a href="#" className="hover:text-white">Press</a></li>
                <li><a href="#" className="hover:text-white">Contact</a></li>
              </ul>
            </div>

            <div>
              <h4 className="font-semibold mb-4">Support</h4>
              <ul className="space-y-2 text-gray-400">
                <li><a href="#" className="hover:text-white">Help Center</a></li>
                <li><a href="#" className="hover:text-white">Privacy Policy</a></li>
                <li><a href="#" className="hover:text-white">Terms of Service</a></li>
                <li><a href="#" className="hover:text-white">Status</a></li>
              </ul>
            </div>
          </div>

          <div className="border-t border-gray-800 mt-8 pt-8 text-center text-gray-400">
            <p>&copy; 2024 Sports Bridge. All rights reserved.</p>
          </div>
        </div>
      </footer>
    </div>
  )
}
