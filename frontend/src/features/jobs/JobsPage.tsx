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
    <div className="max-w-3xl mx-auto space-y-5 fade-in">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-gray-900">Jobs de importação</h1>
          <p className="text-sm text-gray-400 mt-1">Atualiza automaticamente a cada 8 segundos.</p>
        </div>
        <button
          onClick={() => refetch()}
          disabled={isFetching}
          className="flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-semibold transition-all duration-150 active:scale-[0.98] disabled:opacity-50"
          style={{ background: 'var(--accent-subtle)', color: 'var(--accent)' }}
        >
          <RefreshCw className={`w-3.5 h-3.5 ${isFetching ? 'animate-spin' : ''}`} />
          Atualizar
        </button>
      </div>

      <div className="card overflow-hidden">
        {isLoading ? (
          <div className="flex items-center justify-center py-16 text-gray-400 gap-2 text-sm">
            <Loader2 className="w-4 h-4 animate-spin" /> Carregando...
          </div>
        ) : jobs.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-16 text-gray-400 gap-2">
            <Clock className="w-8 h-8 text-gray-200" />
            <p className="text-sm">Nenhum job encontrado.</p>
          </div>
        ) : (
          <>
            {/* Desktop */}
            <div className="hidden md:block overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-gray-100">
                    {['#', 'Tipo', 'Arquivo', 'Status', 'Iniciado em', 'Finalizado em'].map((h) => (
                      <th key={h} className="px-5 py-3 text-left text-xs font-bold text-gray-400 uppercase tracking-wider">{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-50">
                  {jobs.map((job) => {
                    const badge = statusBadge(job.status)
                    return (
                      <tr key={job.id} className="hover:bg-gray-50 transition-colors">
                        <td className="px-5 py-3 mono text-xs text-gray-400">{job.id}</td>
                        <td className="px-5 py-3 font-medium text-gray-800">{typeLabel(job.type)}</td>
                        <td className="px-5 py-3 mono text-xs text-gray-400 max-w-[180px] truncate">{job.fileName}</td>
                        <td className="px-5 py-3">
                          <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-xl text-xs font-semibold border ${badge.cls}`}>
                            {badge.icon} {badge.label}
                          </span>
                        </td>
                        <td className="px-5 py-3 text-xs text-gray-500 whitespace-nowrap">{fmt(job.startedAt)}</td>
                        <td className="px-5 py-3 text-xs text-gray-500 whitespace-nowrap">{fmt(job.finishedAt)}</td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>

            {/* Mobile */}
            <div className="md:hidden divide-y divide-gray-50">
              {jobs.map((job) => {
                const badge = statusBadge(job.status)
                return (
                  <div key={job.id} className="p-4 space-y-2">
                    <div className="flex items-center justify-between gap-2">
                      <span className="font-semibold text-gray-800 text-sm">{typeLabel(job.type)}</span>
                      <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-xl text-xs font-semibold border ${badge.cls}`}>
                        {badge.icon} {badge.label}
                      </span>
                    </div>
                    <p className="text-xs mono text-gray-400 truncate">{job.fileName}</p>
                    {job.errorMessage && <p className="text-xs text-red-600">{job.errorMessage}</p>}
                    <p className="text-xs text-gray-400">Iniciado: {fmt(job.startedAt)}</p>
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
