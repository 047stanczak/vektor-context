import { useQuery } from '@tanstack/react-query'
import { fetchJobs } from './api'
import { ImportJob } from '@/types'
import { CheckCircle, XCircle, Loader2, Clock, RefreshCw } from 'lucide-react'

function statusBadge(status: ImportJob['status']) {
  if (status === 'SUCCESS')    return { icon: <CheckCircle className="w-3.5 h-3.5" />, label: 'Concluído',   cls: 'bg-green-50 text-green-700 border-green-100' }
  if (status === 'ERROR')      return { icon: <XCircle className="w-3.5 h-3.5" />,     label: 'Erro',        cls: 'bg-red-50 text-red-700 border-red-100' }
  return                              { icon: <Loader2 className="w-3.5 h-3.5 animate-spin" />, label: 'Processando', cls: 'bg-blue-50 text-blue-700 border-blue-100' }
}

function typeLabel(type: string) {
  const map: Record<string, string> = {
    PRODUCTS: 'Produtos',
    SEPARATED_PRODUCTS: 'Produtos Separados',
    SEPARATION_OPERATIONS: 'Operações de Separação',
    SEPARATION_PRODUCTS: 'Produtos de Separação',
  }
  return map[type] ?? type
}

function fmt(iso: string | null) {
  if (!iso) return '—'
  return new Date(iso).toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' })
}

export default function JobsPage() {
  const { data: jobs = [], isLoading, refetch, isFetching } = useQuery({
    queryKey: ['jobs'],
    queryFn: fetchJobs,
    refetchInterval: 8000,
  })

  return (
    <div className="space-y-5 fade-in">
      <div className="page-header">
        <div>
          <h1 className="page-title">Jobs de importação</h1>
          <p className="page-description mt-1">Atualiza automaticamente a cada 8 segundos.</p>
        </div>
        <button
          onClick={() => refetch()}
          disabled={isFetching}
          className="btn-accent-soft"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${isFetching ? 'animate-spin' : ''}`} />
          Atualizar
        </button>
      </div>

      <div className="card overflow-hidden">
        {isLoading ? (
          <div className="loading-state">
            <Loader2 className="w-4 h-4 animate-spin" /> Carregando...
          </div>
        ) : jobs.length === 0 ? (
          <div className="empty-state">
            <Clock className="w-8 h-8 text-muted-foreground/30" />
            <p className="text-sm">Nenhum job encontrado.</p>
          </div>
        ) : (
          <>
            {/* Desktop */}
            <div className="hidden md:block overflow-x-auto">
              <table className="data-table">
                <thead>
                  <tr>
                    {['#', 'Tipo', 'Arquivo', 'Status', 'Iniciado em', 'Finalizado em'].map((h) => (
                      <th key={h}>{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {jobs.map((job) => {
                    const badge = statusBadge(job.status)
                    return (
                      <tr key={job.id}>
                        <td className="mono text-xs text-muted-foreground">{job.id}</td>
                        <td className="font-medium">{typeLabel(job.type)}</td>
                        <td className="mono text-xs text-muted-foreground max-w-[180px] truncate">{job.fileName}</td>
                        <td>
                          <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs font-semibold border ${badge.cls}`}>
                            {badge.icon} {badge.label}
                          </span>
                        </td>
                        <td className="text-xs text-muted-foreground whitespace-nowrap">{fmt(job.startedAt)}</td>
                        <td className="text-xs text-muted-foreground whitespace-nowrap">{fmt(job.finishedAt)}</td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>

            {/* Mobile */}
            <div className="md:hidden divide-y">
              {jobs.map((job) => {
                const badge = statusBadge(job.status)
                return (
                  <div key={job.id} className="p-4 space-y-2">
                    <div className="flex items-center justify-between gap-2">
                      <span className="font-semibold text-sm">{typeLabel(job.type)}</span>
                      <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs font-semibold border ${badge.cls}`}>
                        {badge.icon} {badge.label}
                      </span>
                    </div>
                    <p className="text-xs mono text-muted-foreground truncate">{job.fileName}</p>
                    {job.errorMessage && <p className="text-xs text-destructive">{job.errorMessage}</p>}
                    <p className="text-xs text-muted-foreground">Iniciado: {fmt(job.startedAt)}</p>
                  </div>
                )
              })}
            </div>
          </>
        )}
      </div>
    </div>
  )
}
