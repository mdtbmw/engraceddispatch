import { useState } from 'react'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'
import Sidebar from './Sidebar'
import Header from './Header'

export default function AppLayout() {
  const [searchQuery, setSearchQuery] = useState('')
  const location = useLocation()
  const navigate = useNavigate()

  const pathActionMap: Record<string, string> = {
    '/riders': 'Add Rider',
    '/dispatch': 'New Dispatch',
  }

  const actionLabel = pathActionMap[location.pathname] || 'New Dispatch'

  const handleAction = () => {
    if (location.pathname === '/dispatch') {
      window.dispatchEvent(new CustomEvent('open-create-dispatch'))
    } else if (location.pathname === '/riders') {
      window.dispatchEvent(new CustomEvent('open-add-rider'))
    } else {
      navigate('/dispatch')
    }
  }

  const handleSimulate = () => {
    window.dispatchEvent(new CustomEvent('simulate-booking'))
  }

  return (
    <div className="min-h-screen bg-admin-bg flex relative">
      <div className="glow-orb glow-blue w-[400px] h-[400px] top-[-100px] left-[-100px]" />
      <div className="glow-orb glow-purple w-[300px] h-[300px] bottom-[-50px] right-[-50px]" />

      <Sidebar />

      <main className="flex-1 flex flex-col min-h-screen z-10">
        <Header
          searchQuery={searchQuery}
          onSearchChange={setSearchQuery}
          actionLabel={actionLabel}
          onAction={handleAction}
          onSimulate={handleSimulate}
        />

        <div className="p-10 flex-1 overflow-y-auto">
          <Outlet />
        </div>
      </main>
    </div>
  )
}
