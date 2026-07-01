import { useState, ReactNode } from 'react'
import { NavLink, useLocation } from 'react-router-dom'
import { useAuth } from '@/features/auth/AuthContext'
import {
  AlertTriangle, History, FileText, Upload, LogOut,
  Menu, X, BarChart2, Clock, Package, TrendingUp, ScanLine, ClipboardList, MessageSquare
} from 'lucide-react'
import { cn } from '@/lib/utils'
import { Button } from '@/components/ui/button'

const nav = [
  { to: '/vektor/divergences/new',     label: 'Registrar',       icon: AlertTriangle, group: 'Divergências' },
  { to: '/vektor/divergences/history', label: 'Histórico',       icon: History,       group: 'Divergências' },
  { to: '/vektor/divergences/report',  label: 'Relatório',       icon: FileText,      group: 'Divergências' },
  { to: '/vektor/uploads',             label: 'Importar',        icon: Upload,        group: 'Sistema' },
  { to: '/vektor/jobs',                label: 'Jobs',            icon: Clock,         group: 'Sistema' },
  { to: '/vektor/old-pending',         label: 'Pendências',      icon: Package,       group: 'Sistema' },
  { to: '/vektor/rankings',            label: 'Rankings',        icon: TrendingUp,    group: 'Análise' },
  { to: '/vektor/pending-by-barcode',  label: 'Buscar Código',   icon: ScanLine,      group: 'Sistema' },
  { to: '/vektor/counting',            label: 'Contagem',        icon: ClipboardList, group: 'Sistema' },
  { to: '/vektor/request-pending',     label: 'Pedir Pendência', icon: MessageSquare, group: 'Sistema' },
]

const groups = ['Divergências', 'Sistema', 'Análise']

const routeTitles: Record<string, string> = {
  '/vektor': 'Início',
  '/vektor/divergences/new': 'Registrar divergência',
  '/vektor/divergences/history': 'Histórico',
  '/vektor/divergences/report': 'Relatório',
  '/vektor/uploads': 'Importar arquivos',
  '/vektor/jobs': 'Jobs de importação',
  '/vektor/old-pending': 'Pendências antigas',
  '/vektor/rankings': 'Rankings',
  '/vektor/pending-by-barcode': 'Buscar por código',
  '/vektor/counting': 'Contagem',
  '/vektor/request-pending': 'Pedir pendência',
}

function SidebarContent({ onClose }: { onClose?: () => void }) {
  const { logout } = useAuth()

  return (
    <div className="flex h-full flex-col bg-sidebar">
      <div className="flex items-center gap-3 border-b border-sidebar px-4 py-4">
        <NavLink to="/vektor" onClick={onClose} className="flex min-w-0 flex-1 items-center gap-3 no-underline">
          <div className="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-md bg-sidebar-primary text-sidebar-primary-foreground shadow-sm">
            <BarChart2 className="h-4 w-4" />
          </div>
          <div className="min-w-0">
            <span className="block truncate text-sm font-semibold text-sidebar-foreground">VektorContext</span>
            <span className="block truncate text-[11px] text-sidebar-muted">Sistema de divergências</span>
          </div>
        </NavLink>
        {onClose && (
          <Button variant="ghost" size="icon" onClick={onClose} className="text-sidebar-muted hover:text-sidebar-foreground">
            <X className="h-4 w-4" />
          </Button>
        )}
      </div>

      <nav className="flex-1 space-y-5 overflow-y-auto px-3 py-4">
        {groups.map((group) => (
          <div key={group}>
            <div className="mb-1.5 px-3 text-[10px] font-semibold uppercase tracking-widest text-sidebar-muted">
              {group}
            </div>
            <div className="space-y-0.5">
              {nav.filter((n) => n.group === group).map(({ to, label, icon: Icon }) => (
                <NavLink
                  key={to}
                  to={to}
                  onClick={onClose}
                  className={({ isActive }) =>
                    cn(isActive ? 'sidebar-nav-link-active' : 'sidebar-nav-link')
                  }
                >
                  <Icon className="h-4 w-4 flex-shrink-0" />
                  {label}
                </NavLink>
              ))}
            </div>
          </div>
        ))}
      </nav>

      <div className="border-t border-sidebar p-3">
        <button
          onClick={logout}
          className="sidebar-nav-link w-full text-left"
        >
          <LogOut className="h-4 w-4" />
          Sair
        </button>
      </div>
    </div>
  )
}

export default function AppShell({ children }: { children: ReactNode }) {
  const [mobileOpen, setMobileOpen] = useState(false)
  const location = useLocation()
  const pageTitle = routeTitles[location.pathname] ?? 'VektorContext'

  return (
    <div className="flex min-h-screen bg-muted/30">
      <aside className="hidden lg:fixed lg:inset-y-0 lg:flex lg:w-sidebar lg:flex-col border-r border-sidebar">
        <SidebarContent />
      </aside>

      {mobileOpen && (
        <div className="fixed inset-0 z-50 flex lg:hidden">
          <div className="fixed inset-0 bg-black/40 backdrop-blur-sm" onClick={() => setMobileOpen(false)} />
          <aside className="relative z-50 flex w-sidebar flex-col border-r border-sidebar shadow-xl">
            <SidebarContent onClose={() => setMobileOpen(false)} />
          </aside>
        </div>
      )}

      <div className="flex min-h-screen flex-1 flex-col lg:pl-sidebar">
        <header className="sticky top-0 z-40 flex h-header items-center gap-3 border-b bg-background/95 px-4 backdrop-blur supports-[backdrop-filter]:bg-background/80 lg:px-6">
          <Button
            variant="ghost"
            size="icon"
            className="lg:hidden"
            onClick={() => setMobileOpen(true)}
            aria-label="Abrir menu"
          >
            <Menu className="h-5 w-5" />
          </Button>

          <div className="flex min-w-0 flex-1 items-center gap-3">
            <div className="flex h-8 w-8 items-center justify-center rounded-md bg-primary/10 text-primary lg:hidden">
              <BarChart2 className="h-4 w-4" />
            </div>
            <div className="min-w-0">
              <p className="truncate text-sm font-semibold text-foreground">{pageTitle}</p>
              <p className="hidden truncate text-xs text-muted-foreground sm:block">VektorContext</p>
            </div>
          </div>
        </header>

        <main className="flex-1 overflow-auto p-4 lg:p-6">
          <div className="mx-auto w-full max-w-5xl">
            {children}
          </div>
        </main>
      </div>
    </div>
  )
}
