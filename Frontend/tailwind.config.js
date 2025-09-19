/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './pages/**/*.{js,ts,jsx,tsx,mdx}',
    './components/**/*.{js,ts,jsx,tsx,mdx}',
    './app/**/*.{js,ts,jsx,tsx,mdx}',
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#f0f9ff',
          500: '#0ea5e9',
          600: '#0284c7',
          700: '#0369a1',
        },
        accent: {
          500: '#10b981',
          600: '#059669',
        }
      },
      backgroundImage: {
        'hero-gradient': 'linear-gradient(135deg, rgba(14, 165, 233, 0.8), rgba(16, 185, 129, 0.8))',
      }
    },
  },
  plugins: [],
}

