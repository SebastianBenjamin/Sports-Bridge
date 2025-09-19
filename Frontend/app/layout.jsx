import '../styles/globals.css'
import { Inter } from 'next/font/google'

const inter = Inter({ subsets: ['latin'] })

export const metadata = {
  title: 'Sports Bridge - Connect Athletes, Coaches & Sponsors',
  description: 'The ultimate platform connecting athletes, coaches, and sponsors in the sports community.',
}

export default function RootLayout({ children }) {
  return (
    <html lang="en">
      <body className={inter.className}>
        {children}
      </body>
    </html>
  )
}
