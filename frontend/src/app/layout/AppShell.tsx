import { useState, ReactNode } from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '@/features/auth/AuthContext'
import {
  AlertTriangle, History, FileText, Upload, LogOut, Menu, X, BarChart2
} from 'lucide-react'

const nav = [
  { to: '/vektor/divergences/new',     label: 'Registrar',  icon: AlertTriangle },
  { to: '/vektor/divergences/history', label: 'Histórico',  icon: History },
  { to: '/vektor/divergences/report',  label: 'Relatório',  icon: FileText },
  { to: '/vektor/uploads',             label: 'Importar',   icon: Upload },
]

function SidebarContent({ onClose }: { onClose?: () => void }) {
  const { logout } = useAuth()
  return (
    <div className="flex flex-col h-full">
      <div className="flex items-center gap-3 px-6 py-5 border-b border-gray-800">
        <div className="w-8 h-8 bg-blue-500 rounded-lg flex items-center justify-center flex-shrink-0">
          <BarChart2 className="w-4 h-4 text-white" />
        </div>
        <span className="font-semibold text-white text-sm">VektorContext</span>
        {onClose && (
          <button onClick={onClose} className="ml-auto text-gray-400 hover:text-white">
            <X className="w-5 h-5" />
          </button>
        )}
      </div>

      <nav className="flex-1 px-3 py-4 space-y-1">
        {nav.map(({ to, label, icon: Icon }) => (
          <NavLink
            key={to}
            to={to}
            onClick={onClose}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm transition-colors ${
                isActive
                  ? 'bg-blue-600 text-white'
                  : 'text-gray-400 hover:text-white hover:bg-gray-800'
              }`
            }
          >
            <Icon className="w-4 h-4 flex-shrink-0" />
            {label}
          </NavLink>
        ))}
      </nav>

      <div className="px-3 py-4 border-t border-gray-800">
        <button
          onClick={logout}
          className="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm text-gray-400 hover:text-white hover:bg-gray-800 w-full transition-colors"
        >
          <LogOut className="w-4 h-4" />
          Sair
        </button>
      </div>
    </div>
  )
}

export default function AppShell({ children }: { children: ReactNode }) {
  const [mobileOpen, setMobileOpen] = useState(false)

  return (
    <div className="flex h-screen bg-gray-50">
      {/* Desktop sidebar */}
      <aside className="hidden lg:flex flex-col w-56 bg-gray-900 flex-shrink-0">
        <SidebarContent />
      </aside>

      {/* Mobile overlay */}
      {mobileOpen && (
        <div className="lg:hidden fixed inset-0 z-40 flex">
          <div className="fixed inset-0 bg-black/50" onClick={() => setMobileOpen(false)} />
          <aside className="relative z-50 flex flex-col w-56 bg-gray-900">
            <SidebarContent onClose={() => setMobileOpen(false)} />
          </aside>
        </div>
      )}

      {/* Main content */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Mobile top bar */}
        <header className="lg:hidden flex items-center gap-3 px-4 py-3 bg-white border-b border-gray-200">
          <button onClick={() => setMobileOpen(true)} className="text-gray-600 hover:text-gray-900">
            <Menu className="w-5 h-5" />
          </button>
          <span className="font-semibold text-gray-900 text-sm">VektorContext</span>
        </header>

        <main className="flex-1 overflow-auto p-4 lg:p-6">
          {children}
        </main>
      </div>
    </div>
  )
}
