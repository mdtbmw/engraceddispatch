import { NavLink, useNavigate } from 'react-router-dom'
import { LayoutDashboard, Truck, Users, MapPin, Receipt, UserCog, Settings, Wallet, LogOut, type LucideIcon } from 'lucide-react'
import { useAuth } from '@/lib/auth'

interface NavItem {
  to: string
  label: string
  icon: LucideIcon
}

const navItems: NavItem[] = [
  { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/dispatch', label: 'Dispatch Center', icon: Truck },
  { to: '/riders', label: 'Riders Registry', icon: Users },
  { to: '/zones', label: 'Delivery Zones', icon: MapPin },
  { to: '/pricing', label: 'Pricing Rules', icon: Receipt },
  { to: '/finance', label: 'Finance', icon: Wallet },
  { to: '/users', label: 'Customers', icon: UserCog },
  { to: '/settings', label: 'Settings', icon: Settings },
]

export default function Sidebar() {
  const { signOut, user } = useAuth()
  const navigate = useNavigate()

  const handleLogout = async () => {
    await signOut()
    navigate('/login')
  }

  return (
    <aside className="w-[260px] border-r border-admin-border bg-admin-surface flex flex-col justify-between p-6 z-10 shrink-0">
      <div>
        <div className="flex items-center gap-3 mb-10">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-biro-blue to-[#8B5CF6] flex items-center justify-center shadow-lg shadow-biro-blue/20">
            <Truck className="w-5 h-5 text-white" />
          </div>
          <div>
            <h1 className="text-sm font-extrabold tracking-wider text-white">ENGRACED</h1>
            <p className="text-[10px] font-bold text-[#8B5CF6] tracking-widest">CONTROL PANEL</p>
          </div>
        </div>

        <nav className="flex flex-col gap-2">
          {navItems.map((item) => {
            const Icon = item.icon
            return (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.to === '/dashboard'}
                className={({ isActive }) =>
                  `flex items-center gap-4 px-4 py-3.5 rounded-xl text-sm font-semibold transition-all duration-300 ${
                    isActive
                      ? 'bg-biro-blue text-white shadow-lg shadow-biro-blue/10'
                      : 'text-text-gray hover:text-white hover:bg-admin-hover'
                  }`
                }
              >
                <Icon className="w-4.5 h-4.5" />
                {item.label}
              </NavLink>
            )
          })}
        </nav>
      </div>

      <div className="flex flex-col gap-4">
        <div className="p-4 rounded-2xl bg-admin-hover border border-admin-border">
          <p className="text-[11px] text-text-gray font-medium">Firebase</p>
          <div className="flex items-center gap-2 mt-1.5">
            <span className="w-2.5 h-2.5 rounded-full bg-success-green animate-pulse" />
            <span className="text-xs font-bold text-white">Connected</span>
          </div>
          {user && (
            <p className="text-[9px] text-text-gray mt-1.5 truncate">{user.email}</p>
          )}
        </div>
        <button
          onClick={handleLogout}
          className="flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-semibold text-danger-red hover:bg-danger-red/10 transition-all cursor-pointer"
        >
          <LogOut className="w-4.5 h-4.5" />
          Sign Out
        </button>
      </div>
    </aside>
  )
}
