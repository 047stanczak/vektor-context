import { useState, FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { loginApi } from './api'
import { useAuth } from './AuthContext'
import { BarChart2, Lock, User } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'

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
    <div className="login-page">
      <div className="login-glow" />

      <Card className="login-card fade-in relative">
        <CardHeader className="items-center text-center">
          <div className="mb-2 flex h-12 w-12 items-center justify-center rounded-lg bg-primary text-primary-foreground shadow-md">
            <BarChart2 className="h-6 w-6" />
          </div>
          <CardTitle className="text-xl">VektorContext</CardTitle>
          <CardDescription>Sistema de divergências</CardDescription>
        </CardHeader>

        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="codeUser">Usuário</Label>
              <div className="field-icon-wrap">
                <User className="field-icon" />
                <Input
                  id="codeUser"
                  className="pl-9"
                  value={codeUser}
                  onChange={(e) => setCodeUser(e.target.value)}
                  placeholder="Código do usuário"
                  autoFocus
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="password">Senha</Label>
              <div className="field-icon-wrap">
                <Lock className="field-icon" />
                <Input
                  id="password"
                  type="password"
                  className="pl-9"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                />
              </div>
            </div>

            <Button type="submit" className="w-full" disabled={loading}>
              {loading ? 'Entrando...' : 'Entrar'}
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
