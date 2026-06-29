import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Truck, UserPlus, LogIn } from 'lucide-react'
import { createUserWithEmailAndPassword } from 'firebase/auth'
import { getFirebaseAuth } from '@/lib/firebase'
import { useAuth } from '@/lib/auth'
import { apiSetupAdmin } from '@/lib/admin-api'

export default function Login() {
  const navigate = useNavigate()
  const { signIn } = useAuth()
  const [mode, setMode] = useState<'login' | 'signup'>('login')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    if (!email || !password) { setError('Please fill in all fields'); return }
    setLoading(true)
    try {
      await signIn(email, password)
      navigate('/dashboard')
    } catch (err: any) {
      const msg = err?.message || 'Invalid credentials'
      if (msg.includes('auth/invalid-credential')) setError('Invalid email or password')
      else if (msg.includes('Access denied')) setError('Access denied. Admin privileges required.')
      else setError(msg)
    } finally { setLoading(false) }
  }

  const handleSignup = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    if (!email || !password) { setError('Please fill in all fields'); return }
    if (password !== confirm) { setError('Passwords do not match'); return }
    if (password.length < 6) { setError('Password must be at least 6 characters'); return }
    setLoading(true)
    try {
      await createUserWithEmailAndPassword(getFirebaseAuth(), email, password)
      await apiSetupAdmin()
      await getFirebaseAuth().currentUser?.getIdToken(true)
      navigate('/dashboard')
    } catch (err: any) {
      const code = err?.code || ''
      if (code === 'auth/email-already-in-use') setError('Email already in use by another account')
      else if (code === 'auth/weak-password') setError('Password must be at least 6 characters')
      else if (code === 'auth/configuration-not-found') setError('Firebase Auth not enabled. Enable Email/Password in Firebase Console.')
      else setError(err?.message || 'Signup failed')
    } finally { setLoading(false) }
  }

  return (
    <div className="min-h-screen bg-admin-bg flex items-center justify-center relative">
      <div className="glow-orb glow-blue w-[500px] h-[500px] top-[-150px] left-[-150px]" />
      <div className="glow-orb glow-gold w-[400px] h-[400px] bottom-[-100px] right-[-100px]" />

      <div className="w-full max-w-[420px] z-10">
        <div className="text-center mb-10">
          <div className="w-16 h-16 rounded-2xl bg-gradient-to-tr from-biro-blue to-[#8B5CF6] flex items-center justify-center mx-auto mb-4 shadow-xl shadow-biro-blue/30">
            <Truck className="w-8 h-8 text-white" />
          </div>
          <h1 className="text-xl font-extrabold tracking-wider text-white">ENGRACED DISPATCH</h1>
          <p className="text-xs font-bold text-[#8B5CF6] tracking-widest mt-1">CONTROL PANEL</p>
        </div>

        <form onSubmit={mode === 'login' ? handleLogin : handleSignup} className="p-8 rounded-3xl bg-admin-surface border border-admin-border flex flex-col gap-5">
          <div className="flex gap-2 mb-1">
            <button type="button" onClick={() => setMode('login')} className={`flex-1 py-2.5 rounded-xl text-xs font-bold transition-all cursor-pointer ${mode === 'login' ? 'bg-biro-blue text-white shadow-lg shadow-biro-blue/20' : 'bg-admin-bg text-text-gray hover:text-white'}`}>
              <LogIn className="w-3.5 h-3.5 inline mr-1.5" />Sign In
            </button>
            <button type="button" onClick={() => setMode('signup')} className={`flex-1 py-2.5 rounded-xl text-xs font-bold transition-all cursor-pointer ${mode === 'signup' ? 'bg-biro-blue text-white shadow-lg shadow-biro-blue/20' : 'bg-admin-bg text-text-gray hover:text-white'}`}>
              <UserPlus className="w-3.5 h-3.5 inline mr-1.5" />Create Admin
            </button>
          </div>

          <div className="border-t border-admin-border pt-4">
            <h3 className="text-base font-extrabold text-white">{mode === 'login' ? 'Admin Login' : 'First-Time Setup'}</h3>
            <p className="text-xs text-text-gray mt-1">
              {mode === 'login' ? 'Sign in with your admin account' : 'Create the first admin account for this system'}
            </p>
          </div>

          {error && (
            <div className="p-3 rounded-xl bg-danger-red/10 border border-danger-red/20">
              <p className="text-xs font-medium text-danger-red">{error}</p>
            </div>
          )}

          <div className="flex flex-col gap-3">
            <input type="email" placeholder="Email address" value={email} onChange={(e) => setEmail(e.target.value)} className="bg-admin-bg border border-admin-border rounded-xl px-4 py-3.5 text-xs text-white outline-none focus:border-biro-blue transition-all" />
            <input type="password" placeholder="Password" value={password} onChange={(e) => setPassword(e.target.value)} className="bg-admin-bg border border-admin-border rounded-xl px-4 py-3.5 text-xs text-white outline-none focus:border-biro-blue transition-all" />
            {mode === 'signup' && (
              <input type="password" placeholder="Confirm password" value={confirm} onChange={(e) => setConfirm(e.target.value)} className="bg-admin-bg border border-admin-border rounded-xl px-4 py-3.5 text-xs text-white outline-none focus:border-biro-blue transition-all" />
            )}
          </div>

          <button type="submit" disabled={loading} className="w-full py-3.5 rounded-xl bg-biro-blue text-xs font-bold text-white shadow-lg shadow-biro-blue/20 hover:scale-[1.02] transition-all disabled:opacity-50 cursor-pointer">
            {loading ? 'Processing...' : mode === 'login' ? 'Sign In' : 'Create Admin Account'}
          </button>
        </form>
      </div>
    </div>
  )
}
