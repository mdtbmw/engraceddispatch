import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from '@/lib/auth'
import Layout from '@/components/Layout'
import ErrorBoundary from '@/components/ErrorBoundary'
import Login from '@/pages/Login'
import Dashboard from '@/pages/Dashboard'
import DispatchCenter from '@/pages/DispatchCenter'
import Riders from '@/pages/Riders'
import Zones from '@/pages/Zones'
import Pricing from '@/pages/Pricing'
import Users from '@/pages/Users'
import Settings from '@/pages/Settings'
import Finance from '@/pages/Finance'
import { Toaster } from 'react-hot-toast'

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { user, loading } = useAuth()

  if (loading) {
    return (
      <div className="min-h-screen bg-admin-bg flex items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <div className="w-8 h-8 border-2 border-biro-blue border-t-transparent rounded-full animate-spin" />
          <p className="text-xs text-text-gray font-medium">Verifying access...</p>
        </div>
      </div>
    )
  }

  if (!user) {
    return <Navigate to="/login" replace />
  }

  return <>{children}</>
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Toaster
          position="top-right"
          toastOptions={{
            style: { background: '#08080C', color: '#E2E8F0', border: '1px solid #151522', borderRadius: 12, fontSize: 12 },
          }}
        />
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <ErrorBoundary>
                  <Layout />
                </ErrorBoundary>
              </ProtectedRoute>
            }
          >
            <Route index element={<Navigate to="/dashboard" replace />} />
            <Route path="dashboard" element={<Dashboard />} />
            <Route path="dispatch" element={<DispatchCenter />} />
            <Route path="riders" element={<Riders />} />
            <Route path="zones" element={<Zones />} />
            <Route path="pricing" element={<Pricing />} />
            <Route path="users" element={<Users />} />
            <Route path="settings" element={<Settings />} />
            <Route path="finance" element={<Finance />} />
          </Route>
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  )
}
