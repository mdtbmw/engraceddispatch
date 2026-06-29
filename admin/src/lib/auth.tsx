import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'
import { signInWithEmailAndPassword, signOut as fbSignOut, onAuthStateChanged, type User } from 'firebase/auth'
import { getFirebaseAuth } from './firebase'

export interface AuthState {
  user: User | null
  isAdmin: boolean
  loading: boolean
  error: string
  signIn: (email: string, password: string) => Promise<void>
  signOut: () => Promise<void>
}

const AuthContext = createContext<AuthState | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [isAdmin, setIsAdmin] = useState(false)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    const auth = getFirebaseAuth()
    const unsub = onAuthStateChanged(auth, async (u) => {
      setUser(u)
      if (u) {
        try {
          const result = await u.getIdTokenResult()
          setIsAdmin(!!result.claims.admin)
        } catch {
          setIsAdmin(false)
        }
      } else {
        setIsAdmin(false)
      }
      setLoading(false)
    })
    return unsub
  }, [])

  const signIn = async (email: string, password: string) => {
    setError('')
    const auth = getFirebaseAuth()
    const cred = await signInWithEmailAndPassword(auth, email, password)
    const result = await cred.user.getIdTokenResult()
    if (!result.claims.admin) {
      await fbSignOut(auth)
      throw new Error('Access denied. Admin privileges required.')
    }
  }

  const signOut = async () => {
    const auth = getFirebaseAuth()
    await fbSignOut(auth)
  }

  return (
    <AuthContext.Provider value={{ user, isAdmin, loading, error, signIn, signOut }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
