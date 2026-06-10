import { createContext, useContext, useState, useEffect, ReactNode } from 'react'
import api from '@/lib/axios'

interface AuthContextValue {
  isAuthenticated: boolean
  isChecking: boolean
  login: () => void
  logout: () => void
}

const AuthContext = createContext<AuthContextValue>({
  isAuthenticated: false,
  isChecking: true,
  login: () => {},
  logout: () => {},
})

export function AuthProvider({ children }: { children: ReactNode }) {
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [isChecking, setIsChecking] = useState(true)

  useEffect(() => {
    api.get('/separation-operations/separators')
      .then(() => setIsAuthenticated(true))
      .catch(() => setIsAuthenticated(false))
      .finally(() => setIsChecking(false))
  }, [])

  const login = () => setIsAuthenticated(true)

  const logout = async () => {
    try {
      await api.post('/logout')
    } catch {}
    setIsAuthenticated(false)
    window.location.href = '/vektor/login'
  }

  return (
    <AuthContext.Provider value={{ isAuthenticated, isChecking, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}