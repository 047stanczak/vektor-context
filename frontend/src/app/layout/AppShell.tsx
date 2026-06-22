import { useState, ReactNode } from 'react'
import { NavLink } from 'react-router-dom'
import { useAuth } from '@/features/auth/AuthContext'
import {
  AlertTriangle, History, FileText, Upload, LogOut,
  Menu, X, BarChart2, Clock, Package, TrendingUp, ScanLine, ClipboardList
} from 'lucide-react'

const nav = [
  { to: '/vektor/divergences/new',     label: 'Registrar',    icon: AlertTriangle, group: 'Divergências' },
  { to: '/vektor/divergences/history', label: 'Histórico',    icon: History,       group: 'Divergências' },
  { to: '/vektor/divergences/report',  label: 'Relatório',    icon: FileText,      group: 'Divergências' },
  { to: '/vektor/uploads',             label: 'Importar',     icon: Upload,        group: 'Sistema' },
  { to: '/vektor/jobs',                label: 'Jobs',         icon: Clock,         group: 'Sistema' },
  { to: '/vektor/old-pending',         label: 'Pendências',   icon: Package,       group: 'Sistema' },
  { to: '/vektor/rankings',            label: 'Rankings',     icon: TrendingUp,    group: 'Análise' },
  { to: '/vektor/pending-by-barcode',  label: 'Buscar Código', icon: ScanLine,        group: 'Sistema' },
  { to: '/vektor/counting',            label: 'Contagem',      icon: ClipboardList,   group: 'Sistema' },
]

const groups = ['Divergências', 'Sistema', 'Análise']

function SidebarContent({ onClose }: { onClose?: () => void }) {
  const { logout } = useAuth()

  return (
    <div className="flex flex-col h-full" style={{ background: 'var(--sidebar-bg)' }}>
      <div className="flex items-center gap-3 px-5 py-5" style={{ borderBottom: '1px solid var(--sidebar-border)' }}>
        <div className="w-8 h-8 rounded-xl flex items-center justify-center flex-shrink-0"
          style={{ background: 'var(--accent)', boxShadow: '0 0 16px rgba(79,126,248,0.4)' }}>
          <BarChart2 className="w-4 h-4 text-white" />
        </div>
        <span className="font-bold text-white text-sm tracking-tight">VektorContext</span>
        {onClose && (
          <button onClick={onClose} className="ml-auto text-gray-500 hover:text-white transition-colors">
            <X className="w-4 h-4" />
          </button>
        )}
      </div>

      <nav className="flex-1 px-3 py-4 space-y-5 overflow-y-auto">
        {groups.map((group) => (
          <div key={group}>
            <div className="px-3 mb-1.5 text-[10px] font-bold uppercase tracking-widest" style={{ color: 'rgba(255,255,255,0.25)' }}>
              {group}
            </div>
            <div className="space-y-0.5">
              {nav.filter((n) => n.group === group).map(({ to, label, icon: Icon }) => (
                <NavLink
                  key={to}
                  to={to}
                  onClick={onClose}
                  className={({ isActive }) =>
                    `flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all duration-150 ${
                      isActive ? 'text-white' : 'hover:text-white'
                    }`
                  }
                  style={({ isActive }) => isActive
                    ? { background: 'var(--accent)', boxShadow: '0 2px 12px rgba(79,126,248,0.35)', color: 'white' }
                    : { color: 'rgba(255,255,255,0.45)' }
                  }
                >
                  <Icon className="w-4 h-4 flex-shrink-0" />
                  {label}
                </NavLink>
              ))}
            </div>
          </div>
        ))}
      </nav>

      <div className="px-3 py-4" style={{ borderTop: '1px solid var(--sidebar-border)' }}>
        <button
          onClick={logout}
          className="flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium w-full transition-all duration-150 hover:text-white"
          style={{ color: 'rgba(255,255,255,0.4)' }}
          onMouseEnter={(e) => (e.currentTarget.style.background = 'rgba(255,255,255,0.06)')}
          onMouseLeave={(e) => (e.currentTarget.style.background = 'transparent')}
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
    <div className="flex h-screen" style={{ background: '#f5f6fa' }}>
      <aside className="hidden lg:flex flex-col w-56 flex-shrink-0" style={{ background: 'var(--sidebar-bg)' }}>
        <SidebarContent />
      </aside>

      {mobileOpen && (
        <div className="lg:hidden fixed inset-0 z-40 flex">
          <div className="fixed inset-0 bg-black/60 backdrop-blur-sm" onClick={() => setMobileOpen(false)} />
          <aside className="relative z-50 flex flex-col w-56" style={{ background: 'var(--sidebar-bg)' }}>
            <SidebarContent onClose={() => setMobileOpen(false)} />
          </aside>
        </div>
      )}

      <div className="flex-1 flex flex-col overflow-hidden">
        <header className="lg:hidden flex items-center gap-3 px-4 py-3 bg-white border-b border-gray-100">
          <button onClick={() => setMobileOpen(true)} className="text-gray-500 hover:text-gray-900 transition-colors">
            <Menu className="w-5 h-5" />
          </button>
          <span className="font-bold text-gray-900 text-sm">VektorContext</span>
        </header>

        <main className="flex-1 overflow-auto p-4 lg:p-7">
          {children}
        </main>
      </div>
    </div>
  )
}