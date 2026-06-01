import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { DivergenceRecord } from '@/types'
import { fetchByDate, deleteDivergence } from './api'
import { today, formatDate, formatNum, tipoBadgeClass } from '@/lib/utils'
import EditDivergenceDialog from './components/EditDivergenceDialog'
import { Search, Pencil, Trash2 } from 'lucide-react'

export default function DivergenceHistoryPage() {
  const [date, setDate] = useState(today())
  const [searched, setSearched] = useState(today())
  const [editRecord, setEditRecord] = useState<DivergenceRecord | null>(null)
  const [confirmDelete, setConfirmDelete] = useState<number | null>(null)
  const qc = useQueryClient()

  const qKey = ['divergences', searched]

  const { data, isLoading, isFetching } = useQuery({
    queryKey: qKey,
    queryFn: () => fetchByDate(searched),
    enabled: !!searched,
    select: (res) => res.data ?? [],
  })

  const delMut = useMutation({
    mutationFn: (id: number) => deleteDivergence(id),
    onSuccess: () => {
      toast.success('Divergência excluída')
      qc.invalidateQueries({ queryKey: qKey })
      setConfirmDelete(null)
    },
    onError: () => toast.error('Erro ao excluir'),
  })

  const records: DivergenceRecord[] = data ?? []

  return (
    <div className="max-w-5xl mx-auto space-y-5">
      <h1 className="text-xl font-semibold text-gray-900">Histórico</h1>

      {/* Search bar */}
      <div className="bg-white rounded-xl border border-gray-200 p-4">
        <div className="flex gap-3">
          <input type="date" className="field flex-1 max-w-xs" value={date} onChange={(e) => setDate(e.target.value)} />
          <button onClick={() => setSearched(date)}
            className="flex items-center gap-2 bg-blue-600 text-white rounded-lg px-4 py-2 text-sm font-medium hover:bg-blue-700 transition-colors">
            <Search className="w-4 h-4" /> Buscar
          </button>
        </div>
      </div>

      {/* Results */}
      <div className="bg-white rounded-xl border border-gray-200 p-5">
        <div className="flex items-center justify-between mb-4">
          <h2 className="font-medium text-gray-900">
            {isLoading || isFetching ? 'Carregando...' : `${records.length} registro(s) em ${formatDate(searched)}`}
          </h2>
        </div>

        {/* Desktop table */}
        <div className="hidden md:block overflow-x-auto">
          {records.length === 0 && !isLoading ? (
            <div className="text-center py-10 text-gray-400 text-sm">Nenhuma divergência encontrada para essa data.</div>
          ) : (
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-gray-200 text-left">
                  {['Produto', 'Loja', 'Tipo', 'Qtde', 'Estoque', 'Separador', 'Criado em', ''].map((h) => (
                    <th key={h} className="pb-2 font-medium text-gray-500 pr-3">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {records.map((r) => (
                  <tr key={r.id} className="border-b border-gray-100 hover:bg-gray-50">
                    <td className="py-2.5 pr-3">
                      <div className="font-medium text-gray-900">{r.productCode}</div>
                      {r.productDescription && <div className="text-xs text-gray-500 truncate max-w-[180px]">{r.productDescription}</div>}
                    </td>
                    <td className="py-2.5 pr-3 text-gray-700">{r.storeCode}</td>
                    <td className="py-2.5 pr-3">
                      <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${tipoBadgeClass(r.tipo)}`}>{r.tipo}</span>
                    </td>
                    <td className="py-2.5 pr-3 text-gray-700">{formatNum(r.quantity)}</td>
                    <td className="py-2.5 pr-3 text-gray-500">{formatNum(r.currentStock)}</td>
                    <td className="py-2.5 pr-3 text-gray-700 truncate max-w-[140px]">{r.separatorName ?? '—'}</td>
                    <td className="py-2.5 pr-3 text-gray-500 text-xs whitespace-nowrap">
                      {new Date(r.createdAt).toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' })}
                    </td>
                    <td className="py-2.5">
                      <div className="flex gap-1">
                        <button onClick={() => setEditRecord(r)} className="p-1 text-gray-400 hover:text-blue-600 rounded transition-colors"><Pencil className="w-3.5 h-3.5" /></button>
                        <button onClick={() => setConfirmDelete(r.id)} className="p-1 text-gray-400 hover:text-red-500 rounded transition-colors"><Trash2 className="w-3.5 h-3.5" /></button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        {/* Mobile cards */}
        <div className="md:hidden space-y-2">
          {records.length === 0 && !isLoading && (
            <div className="text-center py-10 text-gray-400 text-sm">Nenhuma divergência encontrada.</div>
          )}
          {records.map((r) => (
            <div key={r.id} className="bg-gray-50 rounded-lg p-3 text-sm">
              <div className="flex justify-between items-start">
                <div className="min-w-0">
                  <div className="font-medium text-gray-900">{r.productCode} — Loja {r.storeCode}</div>
                  {r.productDescription && <div className="text-xs text-gray-500 truncate">{r.productDescription}</div>}
                  <div className="flex gap-2 mt-1 flex-wrap">
                    <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${tipoBadgeClass(r.tipo)}`}>{r.tipo}</span>
                    <span className="text-gray-600">Qtde: {formatNum(r.quantity)}</span>
                    <span className="text-gray-500">Est: {formatNum(r.currentStock)}</span>
                  </div>
                  {r.separatorName && <div className="text-xs text-gray-500 mt-1">{r.separatorName}</div>}
                </div>
                <div className="flex gap-1 flex-shrink-0">
                  <button onClick={() => setEditRecord(r)} className="p-1 text-gray-400 hover:text-blue-600"><Pencil className="w-4 h-4" /></button>
                  <button onClick={() => setConfirmDelete(r.id)} className="p-1 text-gray-400 hover:text-red-500"><Trash2 className="w-4 h-4" /></button>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      <EditDivergenceDialog record={editRecord} onClose={() => setEditRecord(null)} queryKey={qKey} />

      {/* Confirm delete */}
      {confirmDelete !== null && (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
          <div className="fixed inset-0 bg-black/40" onClick={() => setConfirmDelete(null)} />
          <div className="relative bg-white rounded-xl shadow-xl p-6 w-full max-w-sm mx-4">
            <h3 className="font-semibold text-gray-900 mb-2">Confirmar exclusão</h3>
            <p className="text-sm text-gray-600 mb-5">Deseja excluir a divergência #{confirmDelete}? Essa ação não pode ser desfeita.</p>
            <div className="flex gap-2">
              <button onClick={() => setConfirmDelete(null)} className="flex-1 bg-gray-100 text-gray-800 rounded-lg py-2 text-sm font-medium hover:bg-gray-200 transition-colors">Cancelar</button>
              <button onClick={() => delMut.mutate(confirmDelete!)} disabled={delMut.isPending}
                className="flex-1 bg-red-600 text-white rounded-lg py-2 text-sm font-medium hover:bg-red-700 disabled:opacity-50 transition-colors">
                {delMut.isPending ? 'Excluindo...' : 'Excluir'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
