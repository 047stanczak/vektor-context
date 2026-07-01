import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { fetchOldPending, fetchOldPendingWithStock, fetchOldPendingNoStock } from './api'
import { SeparationProduct } from '@/types'
import { Package, PackageX, Loader2, RefreshCw, LayoutList, Layers, Copy, Check, ClipboardList } from 'lucide-react'
import { PageHeader } from '@/components/ui/page-header'
import { Button } from '@/components/ui/button'
import { cn } from '@/lib/utils'

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
    <div className="space-y-5 fade-in">
      <PageHeader
        title="Pendências antigas"
        description="Produtos de separação com status pendente."
        actions={
          <>
            {items.length > 0 && (
              <Button variant={copied ? 'success' : 'secondary'} onClick={handleCopy}>
                {copied ? <Check className="w-3.5 h-3.5" /> : <Copy className="w-3.5 h-3.5" />}
                {copied ? 'Copiado!' : 'Copiar mensagem'}
              </Button>
            )}
            <Button variant="accent" onClick={() => refetch()} disabled={isFetching}>
              <RefreshCw className={cn('w-3.5 h-3.5', isFetching && 'animate-spin')} />
              Atualizar
            </Button>
          </>
        }
      />

      <div className="flex flex-wrap items-center gap-3">
        <div className="flex gap-2 flex-wrap">
          <button
            onClick={() => setMode('all')}
            className={mode === 'all' ? 'toggle-btn-active' : 'toggle-btn'}
          >
            <LayoutList className="w-3.5 h-3.5" /> Todos pendentes
          </button>
          <button
            onClick={() => setMode('with-stock')}
            className={mode === 'with-stock' ? 'toggle-btn-active' : 'toggle-btn'}
          >
            <Layers className="w-3.5 h-3.5" /> Com estoque
          </button>
          <button
            onClick={() => setMode('no-stock')}
            className={mode === 'no-stock' ? 'toggle-btn-active' : 'toggle-btn'}
          >
            <PackageX className="w-3.5 h-3.5" /> Sem estoque
          </button>
        </div>

        <div className="flex items-center gap-2 ml-auto flex-wrap">
          <span className="text-xs text-muted-foreground font-medium">Pendente há mais de</span>
          <div className="flex gap-1 flex-wrap">
            {DAY_OPTIONS.map((d) => (
              <button
                key={d}
                onClick={() => setDays(d)}
                className={days === d ? 'toggle-pill-active' : 'toggle-pill'}
              >
                {d}d
              </button>
            ))}
          </div>
        </div>
      </div>

      <div className="card overflow-hidden">
        {isLoading ? (
          <div className="loading-state">
            <Loader2 className="w-4 h-4 animate-spin" /> Carregando...
          </div>
        ) : items.length === 0 ? (
          <div className="empty-state">
            <Package className="w-8 h-8 text-muted-foreground/30" />
            <p className="text-sm">Nenhuma pendência encontrada.</p>
          </div>
        ) : (
          <>
            <div className="hidden md:block overflow-x-auto">
              <table className="data-table">
                <thead>
                  <tr>
                    {['Produto', 'Descrição', 'Complemento', 'Loja', 'Qtde', ''].map((h) => (
                      <th key={h}>{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {items.map((item) => (
                    <tr key={item.id}>
                      <td className="mono text-xs font-medium">{item.productCode}</td>
                      <td className="max-w-[260px] truncate text-xs text-muted-foreground">{item.productDescription ?? '—'}</td>
                      <td className="max-w-[160px] truncate text-xs text-muted-foreground">{item.productComplement ?? '—'}</td>
                      <td className="text-xs">{item.storeCode ?? '—'}</td>
                      <td className="text-xs">{item.quantity}</td>
                      <td>
                        <button
                          onClick={() => navigate(`/vektor/counting?productCode=${item.productCode}`)}
                          title="Ver contagem"
                          className="btn-icon"
                        >
                          <ClipboardList className="w-3.5 h-3.5" />
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <div className="md:hidden divide-y">
              {items.map((item) => (
                <div key={item.id} className="p-4 space-y-1">
                  <div className="flex items-center justify-between gap-2">
                    <span className="mono text-sm font-semibold">{item.productCode}</span>
                    <span className="text-xs text-muted-foreground">Loja {item.storeCode ?? '—'}</span>
                  </div>
                  {item.productDescription && <p className="text-xs text-muted-foreground truncate">{item.productDescription}</p>}
                  {item.productComplement && <p className="text-xs text-muted-foreground truncate">{item.productComplement}</p>}
                  <div className="flex items-center justify-between">
                    <p className="text-xs text-muted-foreground">Qtde: {item.quantity}</p>
                    <button
                      onClick={() => navigate(`/vektor/counting?productCode=${item.productCode}`)}
                      className="btn-icon"
                    >
                      <ClipboardList className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </>
        )}
      </div>

      {items.length > 0 && (
        <p className="text-xs text-muted-foreground text-right">{items.length} registro(s)</p>
      )}
    </div>
  )
}
