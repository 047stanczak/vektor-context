import { useState, FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { loginApi } from './api'
import { useAuth } from './AuthContext'
import { BarChart2, Lock, User } from 'lucide-react'

export default function LoginPage() {
  const [codeUser, setCodeUser] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()
  const { login } = useAuth()

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    if (!codeUser || !password) { toast.error('Preencha todos os campos'); return }
    setLoading(true)
    try {
      const res = await loginApi(codeUser, password)
      if (res.success) {
        login()
        navigate('/vektor', { replace: true })
      } else {
        toast.error(res.message)
      }
    } catch (err: any) {
      toast.error(err.response?.data?.message ?? 'Erro ao fazer login')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center p-4"
      style={{ background: 'linear-gradient(135deg, #0f1117 0%, #1a1f2e 50%, #0f1117 100%)' }}>

      {/* Background glow */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-1/3 left-1/2 -translate-x-1/2 -translate-y-1/2 w-96 h-96 rounded-full"
          style={{ background: 'radial-gradient(circle, rgba(79,126,248,0.12) 0%, transparent 70%)' }} />
      </div>

      <div className="relative w-full max-w-sm fade-in">
        {/* Card */}
        <div className="rounded-2xl p-8"
          style={{
            background: 'rgba(255,255,255,0.04)',
            border: '1px solid rgba(255,255,255,0.08)',
            backdropFilter: 'blur(20px)',
          }}>

          {/* Logo */}
          <div className="flex flex-col items-center mb-8">
            <div className="w-12 h-12 rounded-2xl flex items-center justify-center mb-4"
              style={{ background: 'var(--accent)', boxShadow: '0 0 24px rgba(79,126,248,0.5)' }}>
              <BarChart2 className="w-6 h-6 text-white" />
            </div>
            <h1 className="text-xl font-bold text-white">VektorContext</h1>
            <p className="text-sm mt-1" style={{ color: 'rgba(255,255,255,0.4)' }}>Sistema de divergências</p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider mb-1.5"
                style={{ color: 'rgba(255,255,255,0.5)' }}>Usuário</label>
              <div className="relative">
                <User className="absolute left-3.5 top-2.5 w-4 h-4" style={{ color: 'rgba(255,255,255,0.3)' }} />
                <input
                  className="w-full rounded-xl px-3.5 py-2.5 pl-10 text-sm text-white placeholder:text-gray-600 focus:outline-none transition-all duration-150"
                  style={{
                    background: 'rgba(255,255,255,0.06)',
                    border: '1px solid rgba(255,255,255,0.1)',
                  }}
                  onFocus={(e) => (e.target.style.borderColor = 'rgba(79,126,248,0.6)')}
                  onBlur={(e) => (e.target.style.borderColor = 'rgba(255,255,255,0.1)')}
                  value={codeUser}
                  onChange={(e) => setCodeUser(e.target.value)}
                  placeholder="Código do usuário"
                  autoFocus
                />
              </div>
            </div>
            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider mb-1.5"
                style={{ color: 'rgba(255,255,255,0.5)' }}>Senha</label>
              <div className="relative">
                <Lock className="absolute left-3.5 top-2.5 w-4 h-4" style={{ color: 'rgba(255,255,255,0.3)' }} />
                <input
                  type="password"
                  className="w-full rounded-xl px-3.5 py-2.5 pl-10 text-sm text-white placeholder:text-gray-600 focus:outline-none transition-all duration-150"
                  style={{
                    background: 'rgba(255,255,255,0.06)',
                    border: '1px solid rgba(255,255,255,0.1)',
                  }}
                  onFocus={(e) => (e.target.style.borderColor = 'rgba(79,126,248,0.6)')}
                  onBlur={(e) => (e.target.style.borderColor = 'rgba(255,255,255,0.1)')}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                />
              </div>
            </div>
            <button
              type="submit"
              disabled={loading}
              className="w-full rounded-xl py-2.5 text-sm font-bold text-white transition-all duration-150 active:scale-[0.98] mt-2"
              style={{ background: loading ? 'rgba(79,126,248,0.5)' : 'var(--accent)', boxShadow: '0 4px 20px rgba(79,126,248,0.35)' }}
            >
              {loading ? 'Entrando...' : 'Entrar'}
            </button>
          </form>
        </div>
      </div>
    </div>
  )
}
