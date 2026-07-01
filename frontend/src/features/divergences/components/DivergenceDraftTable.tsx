import { DivergenceDraftItem } from '@/types'
import { formatNum, tipoBadgeClass } from '@/lib/utils'
import { Trash2, Pencil } from 'lucide-react'

interface Props {
  items: DivergenceDraftItem[]
  onRemove: (draftId: string) => void
  onEdit: (draftId: string) => void
}

export default function DivergenceDraftTable({ items, onRemove, onEdit }: Props) {
  if (items.length === 0) return (
    <div className="empty-state py-10 text-sm">Nenhum item adicionado ainda.</div>
  )

  return (
    <>
      <div className="hidden md:block overflow-x-auto">
        <table className="data-table">
          <thead>
            <tr>
              <th>Produto</th>
              <th>Loja</th>
              <th>Tipo</th>
              <th>Qtde</th>
              <th>Estoque</th>
              <th>Separador</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <tr key={item.draftId}>
                <td>
                  <div className="font-medium">{item.productCode}</div>
                  {item.productDescription && <div className="text-xs text-muted-foreground truncate max-w-[200px]">{item.productDescription}</div>}
                </td>
                <td>{item.storeCode}</td>
                <td>
                  <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${tipoBadgeClass(item.tipo)}`}>{item.tipo}</span>
                </td>
                <td>{formatNum(item.quantity)}</td>
                <td className="text-muted-foreground">{formatNum(item.currentStock)}</td>
                <td className="truncate max-w-[140px]">{item.separatorName || '—'}</td>
                <td>
                  <div className="flex gap-1">
                    <button onClick={() => onEdit(item.draftId)} className="btn-icon"><Pencil className="w-3.5 h-3.5" /></button>
                    <button onClick={() => onRemove(item.draftId)} className="btn-icon-danger"><Trash2 className="w-3.5 h-3.5" /></button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="md:hidden space-y-2">
        {items.map((item) => (
          <div key={item.draftId} className="rounded-lg border bg-muted/30 p-3 text-sm">
            <div className="flex items-start justify-between gap-2">
              <div className="min-w-0">
                <div className="font-medium">{item.productCode} - Loja {item.storeCode}</div>
                {item.productDescription && <div className="text-xs text-muted-foreground truncate">{item.productDescription}</div>}
                <div className="flex gap-2 mt-1 flex-wrap">
                  <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${tipoBadgeClass(item.tipo)}`}>{item.tipo}</span>
                  <span>Qtde: {formatNum(item.quantity)}</span>
                  <span className="text-muted-foreground">Est: {formatNum(item.currentStock)}</span>
                </div>
                {item.separatorName && <div className="text-xs text-muted-foreground mt-1">{item.separatorName}</div>}
              </div>
              <div className="flex gap-1 flex-shrink-0">
                <button onClick={() => onEdit(item.draftId)} className="btn-icon"><Pencil className="w-4 h-4" /></button>
                <button onClick={() => onRemove(item.draftId)} className="btn-icon-danger"><Trash2 className="w-4 h-4" /></button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </>
  )
}
