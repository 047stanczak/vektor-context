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
    <div className="text-center py-10 text-gray-400 text-sm">Nenhum item adicionado ainda.</div>
  )

  return (
    <>
      {/* Desktop table */}
      <div className="hidden md:block overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-gray-200 text-left">
              <th className="pb-2 font-medium text-gray-500 pr-3">Produto</th>
              <th className="pb-2 font-medium text-gray-500 pr-3">Loja</th>
              <th className="pb-2 font-medium text-gray-500 pr-3">Tipo</th>
              <th className="pb-2 font-medium text-gray-500 pr-3">Qtde</th>
              <th className="pb-2 font-medium text-gray-500 pr-3">Estoque</th>
              <th className="pb-2 font-medium text-gray-500 pr-3">Separador</th>
              <th className="pb-2 font-medium text-gray-500"></th>
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <tr key={item.draftId} className="border-b border-gray-100 hover:bg-gray-50">
                <td className="py-2.5 pr-3">
                  <div className="font-medium text-gray-900">{item.productCode}</div>
                  {item.productDescription && <div className="text-xs text-gray-500 truncate max-w-[200px]">{item.productDescription}</div>}
                </td>
                <td className="py-2.5 pr-3 text-gray-700">{item.storeCode}</td>
                <td className="py-2.5 pr-3">
                  <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${tipoBadgeClass(item.tipo)}`}>{item.tipo}</span>
                </td>
                <td className="py-2.5 pr-3 text-gray-700">{formatNum(item.quantity)}</td>
                <td className="py-2.5 pr-3 text-gray-500">{formatNum(item.currentStock)}</td>
                <td className="py-2.5 pr-3 text-gray-700 truncate max-w-[140px]">{item.separatorName || '—'}</td>
                <td className="py-2.5">
                  <div className="flex gap-1">
                    <button onClick={() => onEdit(item.draftId)} className="p-1 text-gray-400 hover:text-blue-600 rounded transition-colors"><Pencil className="w-3.5 h-3.5" /></button>
                    <button onClick={() => onRemove(item.draftId)} className="p-1 text-gray-400 hover:text-red-500 rounded transition-colors"><Trash2 className="w-3.5 h-3.5" /></button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Mobile cards */}
      <div className="md:hidden space-y-2">
        {items.map((item) => (
          <div key={item.draftId} className="bg-gray-50 rounded-lg p-3 text-sm">
            <div className="flex items-start justify-between gap-2">
              <div className="min-w-0">
                <div className="font-medium text-gray-900">{item.productCode} — Loja {item.storeCode}</div>
                {item.productDescription && <div className="text-xs text-gray-500 truncate">{item.productDescription}</div>}
                <div className="flex gap-2 mt-1 flex-wrap">
                  <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${tipoBadgeClass(item.tipo)}`}>{item.tipo}</span>
                  <span className="text-gray-600">Qtde: {formatNum(item.quantity)}</span>
                  <span className="text-gray-500">Est: {formatNum(item.currentStock)}</span>
                </div>
                {item.separatorName && <div className="text-xs text-gray-500 mt-1">{item.separatorName}</div>}
              </div>
              <div className="flex gap-1 flex-shrink-0">
                <button onClick={() => onEdit(item.draftId)} className="p-1 text-gray-400 hover:text-blue-600"><Pencil className="w-4 h-4" /></button>
                <button onClick={() => onRemove(item.draftId)} className="p-1 text-gray-400 hover:text-red-500"><Trash2 className="w-4 h-4" /></button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </>
  )
}
