'use client'
import { useState } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import { Eye, EyeOff, Trophy, Mail, Lock, User, Phone, MapPin, CreditCard, Calendar, ChevronRight, ChevronLeft, Check } from 'lucide-react'
import { api } from '../../lib/api'
import { auth } from '../../lib/auth'
import { USER_ROLES, GENDER, COMMON_SPORTS, INDIAN_STATES } from '../../lib/constants'

export default function RegisterPage() {
  const [currentStep, setCurrentStep] = useState(1)
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')
  const [formData, setFormData] = useState({
    // Step 1: Basic Info
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: '',
    role: '',

    // Step 2: Personal Details
    phone: '',
    dateOfBirth: '',
    gender: '',
    aadharNumber: '',

    // Step 3: Location & Additional Info
    address: '',
    city: '',
    state: '',
    pincode: '',
    country: 'India',

    // Step 4: Role-specific Info
    sports: [], // For Athletes and Coaches
    experience: '', // For Coaches
    companyName: '', // For Sponsors
    budget: '' // For Sponsors
  })
  const router = useRouter()

  const steps = [
    { id: 1, title: 'Basic Information', description: 'Email, password and role' },
    { id: 2, title: 'Personal Details', description: 'Personal info and Aadhar verification' },
    { id: 3, title: 'Location Details', description: 'Address and contact information' },
    { id: 4, title: 'Additional Information', description: 'Role-specific details' }
  ]

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target

    if (type === 'checkbox') {
      if (name === 'sports') {
        setFormData(prev => ({
          ...prev,
          sports: checked
            ? [...prev.sports, value]
            : prev.sports.filter(sport => sport !== value)
        }))
      }
    } else {
      setFormData(prev => ({
        ...prev,
        [name]: value
      }))
    }
    setError('')
  }

  const validateAadhar = (aadharNumber) => {
    // Basic Aadhar validation - 12 digits
    const aadharPattern = /^\d{12}$/
    return aadharPattern.test(aadharNumber.replace(/\s/g, ''))
  }

  const validateStep = (step) => {
    switch (step) {
      case 1:
        if (!formData.firstName || !formData.lastName || !formData.email || !formData.password || !formData.role) {
          setError('Please fill all required fields')
          return false
        }
        if (formData.password !== formData.confirmPassword) {
          setError('Passwords do not match')
          return false
        }
        if (formData.password.length < 6) {
          setError('Password must be at least 6 characters long')
          return false
        }
        break
      case 2:
        if (!formData.phone || !formData.dateOfBirth || !formData.gender || !formData.aadharNumber) {
          setError('Please fill all required fields')
          return false
        }
        if (!validateAadhar(formData.aadharNumber)) {
          setError('Please enter a valid 12-digit Aadhar number')
          return false
        }
        break
      case 3:
        if (!formData.address || !formData.city || !formData.state || !formData.pincode) {
          setError('Please fill all required fields')
          return false
        }
        break
      case 4:
        if (formData.role === USER_ROLES.ATHLETE || formData.role === USER_ROLES.COACH) {
          if (formData.sports.length === 0) {
            setError('Please select at least one sport')
            return false
          }
        }
        if (formData.role === USER_ROLES.COACH && !formData.experience) {
          setError('Please enter your coaching experience')
          return false
        }
        if (formData.role === USER_ROLES.SPONSOR && (!formData.companyName || !formData.budget)) {
          setError('Please fill company name and budget details')
          return false
        }
        break
    }
    return true
  }

  const nextStep = () => {
    if (validateStep(currentStep)) {
      setCurrentStep(prev => Math.min(prev + 1, 4))
      setError('')
    }
  }

  const prevStep = () => {
    setCurrentStep(prev => Math.max(prev - 1, 1))
    setError('')
  }

  const handleSubmit = async (e) => {
    e.preventDefault()

    if (!validateStep(4)) return

    setIsLoading(true)
    setError('')

    try {
      // Prepare data for backend
      const registrationData = {
        firstName: formData.firstName,
        lastName: formData.lastName,
        email: formData.email,
        password: formData.password,
        role: formData.role,
        phone: formData.phone,
        dateOfBirth: formData.dateOfBirth,
        gender: formData.gender,
        aadharNumber: formData.aadharNumber.replace(/\s/g, ''),
        address: formData.address,
        city: formData.city,
        state: formData.state,
        pincode: formData.pincode,
        country: formData.country,
        sports: formData.sports,
        experience: formData.experience,
        companyName: formData.companyName,
        budget: formData.budget
      }

      const response = await api.register(registrationData)

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
        setError('Registration failed. Please try again.')
      }
    } catch (error) {
      setError('Registration failed. Please check your information and try again.')
    } finally {
      setIsLoading(false)
    }
  }

  const formatAadhar = (value) => {
    // Format Aadhar number with spaces (XXXX XXXX XXXX)
    const numbers = value.replace(/\D/g, '')
    const formatted = numbers.replace(/(\d{4})(\d{4})(\d{4})/, '$1 $2 $3')
    return formatted
  }

  const handleAadharChange = (e) => {
    const formatted = formatAadhar(e.target.value)
    setFormData(prev => ({ ...prev, aadharNumber: formatted }))
    setError('')
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-900 via-blue-800 to-green-700 flex items-center justify-center px-4 py-8">
      <div className="max-w-2xl w-full">
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

        {/* Progress Steps */}
        <div className="mb-8">
          <div className="flex items-center justify-between">
            {steps.map((step, index) => (
              <div key={step.id} className="flex items-center">
                <div className={`flex items-center justify-center w-10 h-10 rounded-full ${
                  currentStep >= step.id 
                    ? 'bg-white text-blue-600' 
                    : 'bg-blue-600 bg-opacity-50 text-white'
                }`}>
                  {currentStep > step.id ? (
                    <Check className="w-6 h-6" />
                  ) : (
                    <span className="text-sm font-semibold">{step.id}</span>
                  )}
                </div>
                {index < steps.length - 1 && (
                  <div className={`h-1 w-16 mx-2 ${
                    currentStep > step.id ? 'bg-white' : 'bg-blue-600 bg-opacity-50'
                  }`} />
                )}
              </div>
            ))}
          </div>
          <div className="mt-4 text-center">
            <h3 className="text-white font-semibold">{steps[currentStep - 1].title}</h3>
            <p className="text-blue-100 text-sm">{steps[currentStep - 1].description}</p>
          </div>
        </div>

        {/* Registration Form */}
        <div className="bg-white rounded-2xl shadow-2xl p-8">
          <div className="text-center mb-6">
            <h2 className="text-3xl font-bold text-gray-900">Create Account</h2>
            <p className="text-gray-600 mt-2">Join the Sports Bridge community</p>
          </div>

          {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-6">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Step 1: Basic Information */}
            {currentStep === 1 && (
              <div className="space-y-6">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      First Name *
                    </label>
                    <input
                      type="text"
                      name="firstName"
                      value={formData.firstName}
                      onChange={handleChange}
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900"
                      placeholder="Enter first name"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Last Name *
                    </label>
                    <input
                      type="text"
                      name="lastName"
                      value={formData.lastName}
                      onChange={handleChange}
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900"
                      placeholder="Enter last name"
                      required
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Email Address *
                  </label>
                  <div className="relative">
                    <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                    <input
                      type="email"
                      name="email"
                      value={formData.email}
                      onChange={handleChange}
                      className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900"
                      placeholder="Enter your email"
                      required
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Password *
                  </label>
                  <div className="relative">
                    <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                    <input
                      type={showPassword ? 'text' : 'password'}
                      name="password"
                      value={formData.password}
                      onChange={handleChange}
                      className="w-full pl-10 pr-12 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900"
                      placeholder="Enter password"
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

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Confirm Password *
                  </label>
                  <div className="relative">
                    <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                    <input
                      type={showConfirmPassword ? 'text' : 'password'}
                      name="confirmPassword"
                      value={formData.confirmPassword}
                      onChange={handleChange}
                      className="w-full pl-10 pr-12 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900"
                      placeholder="Confirm password"
                      required
                    />
                    <button
                      type="button"
                      onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                      className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                    >
                      {showConfirmPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                    </button>
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    I want to join as *
                  </label>
                  <div className="grid grid-cols-2 gap-4">
                    {Object.entries(USER_ROLES).filter(([key, value]) => value !== 'ADMIN').map(([key, value]) => (
                      <label key={value} className="relative">
                        <input
                          type="radio"
                          name="role"
                          value={value}
                          checked={formData.role === value}
                          onChange={handleChange}
                          className="sr-only"
                        />
                        <div className={`p-4 border-2 rounded-lg cursor-pointer transition-all ${
                          formData.role === value
                            ? 'border-blue-500 bg-blue-50 text-blue-700'
                            : 'border-gray-300 hover:border-gray-400 text-gray-700'
                        }`}>
                          <div className="text-center">
                            <User className="w-8 h-8 mx-auto mb-2" />
                            <div className="font-semibold">{key}</div>
                            <div className="text-sm text-gray-500">
                              {key === 'ATHLETE' && 'Compete and grow'}
                              {key === 'COACH' && 'Train and mentor'}
                              {key === 'SPONSOR' && 'Support athletes'}
                            </div>
                          </div>
                        </div>
                      </label>
                    ))}
                  </div>
                </div>
              </div>
            )}

            {/* Step 2: Personal Details */}
            {currentStep === 2 && (
              <div className="space-y-6">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Phone Number *
                  </label>
                  <div className="relative">
                    <Phone className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                    <input
                      type="tel"
                      name="phone"
                      value={formData.phone}
                      onChange={handleChange}
                      className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900"
                      placeholder="Enter phone number"
                      required
                    />
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Date of Birth *
                    </label>
                    <div className="relative">
                      <Calendar className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                      <input
                        type="date"
                        name="dateOfBirth"
                        value={formData.dateOfBirth}
                        onChange={handleChange}
                        className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900"
                        required
                      />
                    </div>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Gender *
                    </label>
                    <select
                      name="gender"
                      value={formData.gender}
                      onChange={handleChange}
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900"
                      required
                    >
                      <option value="">Select gender</option>
                      {Object.entries(GENDER).map(([key, value]) => (
                        <option key={value} value={value}>{key}</option>
                      ))}
                    </select>
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Aadhar Card Number * <span className="text-sm text-blue-600">(For verification purposes)</span>
                  </label>
                  <div className="relative">
                    <CreditCard className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                    <input
                      type="text"
                      name="aadharNumber"
                      value={formData.aadharNumber}
                      onChange={handleAadharChange}
                      className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900"
                      placeholder="XXXX XXXX XXXX"
                      maxLength="14"
                      required
                    />
                  </div>
                  <p className="text-sm text-gray-500 mt-1">
                    Your Aadhar details are securely stored and used only for identity verification.
                  </p>
                </div>
              </div>
            )}

            {/* Step 3: Location Details */}
            {currentStep === 3 && (
              <div className="space-y-6">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Address *
                  </label>
                  <div className="relative">
                    <MapPin className="absolute left-3 top-3 text-gray-400 w-5 h-5" />
                    <textarea
                      name="address"
                      value={formData.address}
                      onChange={handleChange}
                      rows="3"
                      className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900"
                      placeholder="Enter your complete address"
                      required
                    />
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      City *
                    </label>
                    <input
                      type="text"
                      name="city"
                      value={formData.city}
                      onChange={handleChange}
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900"
                      placeholder="Enter city"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      State *
                    </label>
                    <select
                      name="state"
                      value={formData.state}
                      onChange={handleChange}
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900"
                      required
                    >
                      <option value="">Select state</option>
                      {INDIAN_STATES.map(state => (
                        <option key={state} value={state}>{state}</option>
                      ))}
                    </select>
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      PIN Code *
                    </label>
                    <input
                      type="text"
                      name="pincode"
                      value={formData.pincode}
                      onChange={handleChange}
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900"
                      placeholder="Enter PIN code"
                      maxLength="6"
                      pattern="[0-9]{6}"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Country
                    </label>
                    <input
                      type="text"
                      name="country"
                      value={formData.country}
                      onChange={handleChange}
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900 bg-gray-50"
                      disabled
                    />
                  </div>
                </div>
              </div>
            )}

            {/* Step 4: Role-specific Information */}
            {currentStep === 4 && (
              <div className="space-y-6">
                {(formData.role === USER_ROLES.ATHLETE || formData.role === USER_ROLES.COACH) && (
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Sports * <span className="text-sm text-gray-600">(Select all that apply)</span>
                    </label>
                    <div className="grid grid-cols-3 gap-3 max-h-48 overflow-y-auto border border-gray-300 rounded-lg p-4">
                      {COMMON_SPORTS.map(sport => (
                        <label key={sport} className="flex items-center space-x-2 cursor-pointer">
                          <input
                            type="checkbox"
                            name="sports"
                            value={sport}
                            checked={formData.sports.includes(sport)}
                            onChange={handleChange}
                            className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
                          />
                          <span className="text-sm text-gray-700">{sport}</span>
                        </label>
                      ))}
                    </div>
                  </div>
                )}

                {formData.role === USER_ROLES.COACH && (
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Coaching Experience *
                    </label>
                    <textarea
                      name="experience"
                      value={formData.experience}
                      onChange={handleChange}
                      rows="4"
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900"
                      placeholder="Describe your coaching experience, certifications, and achievements..."
                      required
                    />
                  </div>
                )}

                {formData.role === USER_ROLES.SPONSOR && (
                  <div className="space-y-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Company/Organization Name *
                      </label>
                      <input
                        type="text"
                        name="companyName"
                        value={formData.companyName}
                        onChange={handleChange}
                        className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900"
                        placeholder="Enter company name"
                        required
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Sponsorship Budget Range *
                      </label>
                      <select
                        name="budget"
                        value={formData.budget}
                        onChange={handleChange}
                        className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900"
                        required
                      >
                        <option value="">Select budget range</option>
                        <option value="Under 1 Lakh">Under ₹1 Lakh</option>
                        <option value="1-5 Lakhs">₹1-5 Lakhs</option>
                        <option value="5-10 Lakhs">₹5-10 Lakhs</option>
                        <option value="10-25 Lakhs">₹10-25 Lakhs</option>
                        <option value="25+ Lakhs">₹25+ Lakhs</option>
                      </select>
                    </div>
                  </div>
                )}
              </div>
            )}

            {/* Navigation Buttons */}
            <div className="flex justify-between pt-6">
              {currentStep > 1 && (
                <button
                  type="button"
                  onClick={prevStep}
                  className="flex items-center space-x-2 px-6 py-3 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors duration-300 text-gray-700"
                >
                  <ChevronLeft className="w-5 h-5" />
                  <span>Previous</span>
                </button>
              )}

              {currentStep < 4 ? (
                <button
                  type="button"
                  onClick={nextStep}
                  className="flex items-center space-x-2 ml-auto px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors duration-300"
                >
                  <span>Next</span>
                  <ChevronRight className="w-5 h-5" />
                </button>
              ) : (
                <button
                  type="submit"
                  disabled={isLoading}
                  className="ml-auto px-8 py-3 bg-green-600 hover:bg-green-700 disabled:bg-green-400 text-white rounded-lg font-semibold transition-colors duration-300"
                >
                  {isLoading ? 'Creating Account...' : 'Create Account'}
                </button>
              )}
            </div>
          </form>

          {/* Login Link */}
          <div className="mt-6 text-center">
            <div className="text-gray-600 text-sm">
              Already have an account?{' '}
              <Link
                href="/login"
                className="text-blue-600 hover:text-blue-500 font-medium"
              >
                Sign in
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
