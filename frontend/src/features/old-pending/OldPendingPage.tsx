import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { fetchOldPending, fetchOldPendingWithStock } from './api'
import { SeparationProduct } from '@/types'
import { Package, Loader2, RefreshCw, LayoutList, Layers } from 'lucide-react'
import { formatNum } from '@/lib/utils'

type Mode = 'all' | 'with-stock'

function fmt(iso: string) {
  return new Date(iso).toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', year: '2-digit', hour: '2-digit', minute: '2-digit' })
}

export default function OldPendingPage() {
  const [mode, setMode] = useState<Mode>('all')

  const { data: items = [], isLoading, refetch, isFetching } = useQuery({
    queryKey: ['old-pending', mode],
    queryFn: mode === 'all' ? fetchOldPending : fetchOldPendingWithStock,
  })

  return (
    <div className="max-w-4xl mx-auto space-y-5 fade-in">
      <div className="flex items-start justify-between flex-wrap gap-3">
        <div>
          <h1 className="text-xl font-bold text-gray-900">Pendências antigas</h1>
          <p className="text-sm text-gray-400 mt-1">Produtos de separação com status pendente.</p>
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

      {/* Mode toggle */}
      <div className="flex gap-2">
        <button
          onClick={() => setMode('all')}
          className={`flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-semibold border transition-all duration-150 ${
            mode === 'all'
              ? 'text-white border-transparent'
              : 'bg-white text-gray-500 border-gray-200 hover:border-gray-300'
          }`}
          style={mode === 'all' ? { background: 'var(--accent)' } : {}}
        >
          <LayoutList className="w-3.5 h-3.5" /> Todos pendentes
        </button>
        <button
          onClick={() => setMode('with-stock')}
          className={`flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-semibold border transition-all duration-150 ${
            mode === 'with-stock'
              ? 'text-white border-transparent'
              : 'bg-white text-gray-500 border-gray-200 hover:border-gray-300'
          }`}
          style={mode === 'with-stock' ? { background: 'var(--accent)' } : {}}
        >
          <Layers className="w-3.5 h-3.5" /> Com estoque
        </button>
      </div>

      <div className="card overflow-hidden">
        {isLoading ? (
          <div className="flex items-center justify-center py-16 text-gray-400 gap-2 text-sm">
            <Loader2 className="w-4 h-4 animate-spin" /> Carregando...
          </div>
        ) : items.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-16 text-gray-400 gap-2">
            <Package className="w-8 h-8 text-gray-200" />
            <p className="text-sm">Nenhuma pendência encontrada.</p>
          </div>
        ) : (
          <>
            {/* Desktop */}
            <div className="hidden md:block overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-gray-100">
                    {['Produto', 'Descrição', 'Loja', 'Qtde', 'Qtde Emb', 'Status', 'Criado em'].map((h) => (
                      <th key={h} className="px-5 py-3 text-left text-xs font-bold text-gray-400 uppercase tracking-wider">{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-50">
                  {items.map((item) => (
                    <tr key={item.id} className="hover:bg-gray-50 transition-colors">
                      <td className="px-5 py-3 mono text-xs font-medium text-gray-800">{item.productCode}</td>
                      <td className="px-5 py-3 text-gray-700 max-w-[220px] truncate text-xs">{item.productDescription ?? '—'}</td>
                      <td className="px-5 py-3 text-gray-600 text-xs">{item.storeCode ?? '—'}</td>
                      <td className="px-5 py-3 text-gray-700 text-xs">{formatNum(item.quantity)}</td>
                      <td className="px-5 py-3 text-gray-500 text-xs">{formatNum(item.quantityEmb)}</td>
                      <td className="px-5 py-3">
                        <span className="inline-flex items-center px-2.5 py-1 rounded-xl text-xs font-semibold bg-amber-50 text-amber-700 border border-amber-100">
                          {item.status ?? 'PENDENTE'}
                        </span>
                      </td>
                      <td className="px-5 py-3 text-xs text-gray-400 whitespace-nowrap">{fmt(item.createdAt)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Mobile */}
            <div className="md:hidden divide-y divide-gray-50">
              {items.map((item) => (
                <div key={item.id} className="p-4 space-y-1">
                  <div className="flex items-center justify-between gap-2">
                    <span className="mono text-sm font-semibold text-gray-800">{item.productCode}</span>
                    <span className="inline-flex items-center px-2.5 py-1 rounded-xl text-xs font-semibold bg-amber-50 text-amber-700 border border-amber-100">
                      {item.status ?? 'PENDENTE'}
                    </span>
                  </div>
                  {item.productDescription && <p className="text-xs text-gray-500 truncate">{item.productDescription}</p>}
                  <div className="flex gap-3 text-xs text-gray-400 flex-wrap">
                    <span>Loja: {item.storeCode ?? '—'}</span>
                    <span>Qtde: {formatNum(item.quantity)}</span>
                    <span>Emb: {formatNum(item.quantityEmb)}</span>
                  </div>
                  <p className="text-xs text-gray-400">{fmt(item.createdAt)}</p>
                </div>
              ))}
            </div>
          </>
        )}
      </div>

      {items.length > 0 && (
        <p className="text-xs text-gray-400 text-right">{items.length} registro(s)</p>
      )}
    </div>
  )
}
