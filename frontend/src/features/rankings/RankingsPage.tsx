import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { fetchRankBySeparator, fetchRankByProduct, RankingItem } from './api'
import { Loader2, Users, Package, Search } from 'lucide-react'

function today() {
  return new Date().toISOString().slice(0, 10)
}

function firstOfMonth() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-01`
}

interface RankTableProps {
  title: string
  icon: React.ReactNode
  items: RankingItem[]
  isLoading: boolean
  labelHeader: string
}

function RankTable({ title, icon, items, isLoading, labelHeader }: RankTableProps) {
  const max = items[0]?.total ?? 1

  return (
    <div className="card p-5 space-y-4">
      <div className="flex items-center gap-2">
        <div className="w-7 h-7 rounded-lg flex items-center justify-center flex-shrink-0"
          style={{ background: 'var(--accent-subtle)', color: 'var(--accent)' }}>
          {icon}
        </div>
        <h2 className="font-semibold text-gray-900 text-sm">{title}</h2>
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center py-10 text-gray-400 gap-2 text-sm">
          <Loader2 className="w-4 h-4 animate-spin" /> Carregando...
        </div>
      ) : items.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-10 text-gray-400 gap-2">
          <p className="text-sm">Nenhum dado no período.</p>
        </div>
      ) : (
        <div className="space-y-2.5">
          {items.map((item, i) => (
            <div key={item.name} className="space-y-1">
              <div className="flex items-center justify-between text-xs">
                <div className="flex items-center gap-2 min-w-0">
                  <span className="font-bold w-5 text-gray-400 flex-shrink-0">#{i + 1}</span>
                  <span className="font-medium text-gray-800 truncate">{item.name}</span>
                </div>
                <span className="font-bold text-gray-700 flex-shrink-0 ml-2">{item.total}</span>
              </div>
              <div className="h-1.5 rounded-full bg-gray-100 overflow-hidden">
                <div
                  className="h-full rounded-full transition-all duration-500"
                  style={{
                    width: `${(item.total / max) * 100}%`,
                    background: i === 0 ? 'var(--accent)' : `rgba(79,126,248,${0.6 - i * 0.08})`
                  }}
                />
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

export default function RankingsPage() {
  const [from, setFrom] = useState(firstOfMonth())
  const [to, setTo] = useState(today())
  const [query, setQuery] = useState({ from: firstOfMonth(), to: today() })

  const separatorQuery = useQuery({
    queryKey: ['ranking-separator', query.from, query.to],
    queryFn: () => fetchRankBySeparator(query.from, query.to),
  })

  const productQuery = useQuery({
    queryKey: ['ranking-product', query.from, query.to],
    queryFn: () => fetchRankByProduct(query.from, query.to),
  })

  function handleSearch() {
    setQuery({ from, to })
  }

  return (
    <div className="max-w-4xl mx-auto space-y-5 fade-in">
      <div>
        <h1 className="text-xl font-bold text-gray-900">Rankings</h1>
        <p className="text-sm text-gray-400 mt-1">Separadores e produtos com mais divergências no período.</p>
      </div>

      {/* Filtro */}
      <div className="card p-4 flex flex-wrap items-end gap-3">
        <div>
          <label className="label">De</label>
          <input type="date" className="field" value={from} onChange={(e) => setFrom(e.target.value)} />
        </div>
        <div>
          <label className="label">Até</label>
          <input type="date" className="field" value={to} onChange={(e) => setTo(e.target.value)} />
        </div>
        <button onClick={handleSearch} className="btn-primary">
          <Search className="w-4 h-4" /> Buscar
        </button>
      </div>

      {/* Rankings */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <RankTable
          title="Por separador"
          icon={<Users className="w-3.5 h-3.5" />}
          items={separatorQuery.data ?? []}
          isLoading={separatorQuery.isLoading}
          labelHeader="Separador"
        />
        <RankTable
          title="Por produto"
          icon={<Package className="w-3.5 h-3.5" />}
          items={productQuery.data ?? []}
          isLoading={productQuery.isLoading}
          labelHeader="Produto"
        />
      </div>
    </div>
  )
}