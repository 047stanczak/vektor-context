import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { fetchOldPending, fetchOldPendingWithStock, fetchOldPendingNoStock } from './api'
import { SeparationProduct } from '@/types'
import { Package, PackageX, Loader2, RefreshCw, LayoutList, Layers, Copy, Check, ClipboardList } from 'lucide-react'

type Mode = 'all' | 'with-stock' | 'no-stock'

const DAY_OPTIONS = [5, 7, 10, 15, 20, 30]

function buildMessage(items: SeparationProduct[]): string {
  const byStore: Record<string, SeparationProduct[]> = {}
  for (const item of items) {
    const key = String(item.storeCode ?? 'SEM LOJA')
    if (!byStore[key]) byStore[key] = []
    byStore[key].push(item)
  }
  const lines = ['📦 Produtos com pedido de separação mas sem estoque no sistema\n']
  for (const [store, products] of Object.entries(byStore).sort()) {
    lines.push(`Loja ${store}`)
    for (const p of products) {
      const desc = p.productDescription ? ` - ${p.productDescription}` : ''
      lines.push(`  ${p.productCode}${desc} (Qtde: ${p.quantity})`)
    }
    lines.push('')
  }
  return lines.join('\n').trim()
}

function queryFnFor(mode: Mode, days: number) {
  if (mode === 'with-stock') return () => fetchOldPendingWithStock(days)
  if (mode === 'no-stock')   return () => fetchOldPendingNoStock(days)
  return () => fetchOldPending(days)
}

export default function OldPendingPage() {
  const navigate = useNavigate()
  const [mode, setMode] = useState<Mode>('all')
  const [days, setDays] = useState(15)
  const [copied, setCopied] = useState(false)

  const { data: items = [], isLoading, refetch, isFetching } = useQuery({
    queryKey: ['old-pending', mode, days],
    queryFn: queryFnFor(mode, days),
  })

  function handleCopy() {
    navigator.clipboard.writeText(buildMessage(items))
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  return (
    <div className="max-w-4xl mx-auto space-y-5 fade-in">
      <div className="flex items-start justify-between flex-wrap gap-3">
        <div>
          <h1 className="text-xl font-bold text-gray-900">Pendências antigas</h1>
          <p className="text-sm text-gray-400 mt-1">Produtos de separação com status pendente.</p>
        </div>
        <div className="flex gap-2">
          {items.length > 0 && (
            <button
              onClick={handleCopy}
              className="flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-semibold transition-all duration-150 active:scale-[0.98]"
              style={{ background: copied ? 'rgba(34,197,94,0.1)' : 'rgba(0,0,0,0.05)', color: copied ? '#16a34a' : '#374151' }}
            >
              {copied ? <Check className="w-3.5 h-3.5" /> : <Copy className="w-3.5 h-3.5" />}
              {copied ? 'Copiado!' : 'Copiar mensagem'}
            </button>
          )}
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
      </div>

      {/* Filtros */}
      <div className="flex flex-wrap items-center gap-3">
        {/* Mode toggle */}
        <div className="flex gap-2 flex-wrap">
          <button
            onClick={() => setMode('all')}
            className={`flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-semibold border transition-all duration-150 ${
              mode === 'all' ? 'text-white border-transparent' : 'bg-white text-gray-500 border-gray-200 hover:border-gray-300'
            }`}
            style={mode === 'all' ? { background: 'var(--accent)' } : {}}
          >
            <LayoutList className="w-3.5 h-3.5" /> Todos pendentes
          </button>
          <button
            onClick={() => setMode('with-stock')}
            className={`flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-semibold border transition-all duration-150 ${
              mode === 'with-stock' ? 'text-white border-transparent' : 'bg-white text-gray-500 border-gray-200 hover:border-gray-300'
            }`}
            style={mode === 'with-stock' ? { background: 'var(--accent)' } : {}}
          >
            <Layers className="w-3.5 h-3.5" /> Com estoque
          </button>
          <button
            onClick={() => setMode('no-stock')}
            className={`flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-semibold border transition-all duration-150 ${
              mode === 'no-stock' ? 'text-white border-transparent' : 'bg-white text-gray-500 border-gray-200 hover:border-gray-300'
            }`}
            style={mode === 'no-stock' ? { background: 'var(--accent)' } : {}}
          >
            <PackageX className="w-3.5 h-3.5" /> Sem estoque
          </button>
        </div>

        {/* Days selector */}
        <div className="flex items-center gap-2 ml-auto">
          <span className="text-xs text-gray-400 font-medium">Pendente há mais de</span>
          <div className="flex gap-1">
            {DAY_OPTIONS.map((d) => (
              <button
                key={d}
                onClick={() => setDays(d)}
                className="px-2.5 py-1.5 rounded-lg text-xs font-semibold border transition-all duration-150"
                style={days === d
                  ? { background: 'var(--accent)', color: 'white', borderColor: 'transparent' }
                  : { background: 'white', color: '#6b7280', borderColor: '#e5e7eb' }
                }
              >
                {d}d
              </button>
            ))}
          </div>
        </div>
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
            <div className="hidden md:block overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-gray-100">
                    {['Produto', 'Descrição', 'Complemento', 'Loja', 'Qtde', ''].map((h) => (
                      <th key={h} className="px-5 py-3 text-left text-xs font-bold text-gray-400 uppercase tracking-wider">{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-50">
                  {items.map((item) => (
                    <tr key={item.id} className="hover:bg-gray-50 transition-colors">
                      <td className="px-5 py-3 mono text-xs font-medium text-gray-800">{item.productCode}</td>
                        <td className="px-5 py-3 text-gray-600 max-w-[260px] truncate text-xs">{item.productDescription ?? '—'}</td>
                        <td className="px-5 py-3 text-gray-500 text-xs max-w-[160px] truncate">{item.productComplement ?? '—'}</td>
                        <td className="px-5 py-3 text-gray-600 text-xs">{item.storeCode ?? '—'}</td>
                      <td className="px-5 py-3 text-gray-700 text-xs">{item.quantity}</td>
                      <td className="px-5 py-3">
                        <button onClick={() => navigate(`/vektor/counting?productCode=${item.productCode}`)} title="Ver contagem">
                          <ClipboardList className="w-3.5 h-3.5 text-gray-400 hover:text-gray-700" />
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <div className="md:hidden divide-y divide-gray-50">
              {items.map((item) => (
                <div key={item.id} className="p-4 space-y-1">
                  <div className="flex items-center justify-between gap-2">
                    <span className="mono text-sm font-semibold text-gray-800">{item.productCode}</span>
                    <span className="text-xs text-gray-400">Loja {item.storeCode ?? '—'}</span>
                  </div>
                  {item.productDescription && <p className="text-xs text-gray-500 truncate">{item.productDescription}</p>}
                  {item.productComplement && <p className="text-xs text-gray-400 truncate">{item.productComplement}</p>}
                  <div className="flex items-center justify-between">
                    <p className="text-xs text-gray-400">Qtde: {item.quantity}</p>
                    <button onClick={() => navigate(`/vektor/counting?productCode=${item.productCode}`)}>
                      <ClipboardList className="w-3.5 h-3.5 text-gray-400" />
                    </button>
                  </div>
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