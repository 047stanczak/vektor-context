import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { DivergenceRecord } from '@/types'
import { fetchByDate, deleteDivergence } from './api'
import { today, formatDate, formatNum, tipoBadgeClass } from '@/lib/utils'
import EditDivergenceDialog from './components/EditDivergenceDialog'
import { Search, Pencil, Trash2 } from 'lucide-react'
import { PageHeader } from '@/components/ui/page-header'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'

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
    <div className="space-y-5 fade-in">
      <PageHeader title="Histórico" description="Consulte divergências registradas por data." />

      <div className="card p-4">
        <div className="flex flex-col gap-3 sm:flex-row">
          <input type="date" className="field flex-1 sm:max-w-xs" value={date} onChange={(e) => setDate(e.target.value)} />
          <Button onClick={() => setSearched(date)}>
            <Search className="w-4 h-4" /> Buscar
          </Button>
        </div>
      </div>

      <div className="card p-5">
        <div className="mb-4">
          <h2 className="font-medium">
            {isLoading || isFetching ? 'Carregando...' : `${records.length} registro(s) em ${formatDate(searched)}`}
          </h2>
        </div>

        <div className="hidden md:block overflow-x-auto">
          {records.length === 0 && !isLoading ? (
            <div className="empty-state py-10 text-sm">Nenhuma divergência encontrada para essa data.</div>
          ) : (
            <table className="data-table">
              <thead>
                <tr>
                  {['Produto', 'Loja', 'Tipo', 'Qtde', 'Estoque', 'Separador', 'Criado em', ''].map((h) => (
                    <th key={h}>{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {records.map((r) => (
                  <tr key={r.id}>
                    <td>
                      <div className="font-medium">{r.productCode}</div>
                      {r.productDescription && <div className="text-xs text-muted-foreground truncate max-w-[180px]">{r.productDescription}</div>}
                    </td>
                    <td>{r.storeCode}</td>
                    <td>
                      <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${tipoBadgeClass(r.tipo)}`}>{r.tipo}</span>
                    </td>
                    <td>{formatNum(r.quantity)}</td>
                    <td className="text-muted-foreground">{formatNum(r.currentStock)}</td>
                    <td className="truncate max-w-[140px]">{r.separatorName ?? '—'}</td>
                    <td className="text-xs text-muted-foreground whitespace-nowrap">
                      {new Date(r.createdAt).toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' })}
                    </td>
                    <td>
                      <div className="flex gap-1">
                        <button onClick={() => setEditRecord(r)} className="btn-icon"><Pencil className="w-3.5 h-3.5" /></button>
                        <button onClick={() => setConfirmDelete(r.id)} className="btn-icon-danger"><Trash2 className="w-3.5 h-3.5" /></button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        <div className="md:hidden space-y-2">
          {records.length === 0 && !isLoading && (
            <div className="empty-state py-10 text-sm">Nenhuma divergência encontrada.</div>
          )}
          {records.map((r) => (
            <div key={r.id} className="rounded-lg border bg-muted/30 p-3 text-sm">
              <div className="flex justify-between items-start">
                <div className="min-w-0">
                  <div className="font-medium">{r.productCode} - Loja {r.storeCode}</div>
                  {r.productDescription && <div className="text-xs text-muted-foreground truncate">{r.productDescription}</div>}
                  <div className="flex gap-2 mt-1 flex-wrap">
                    <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${tipoBadgeClass(r.tipo)}`}>{r.tipo}</span>
                    <span>Qtde: {formatNum(r.quantity)}</span>
                    <span className="text-muted-foreground">Est: {formatNum(r.currentStock)}</span>
                  </div>
                  {r.separatorName && <div className="text-xs text-muted-foreground mt-1">{r.separatorName}</div>}
                </div>
                <div className="flex gap-1 flex-shrink-0">
                  <button onClick={() => setEditRecord(r)} className="btn-icon"><Pencil className="w-4 h-4" /></button>
                  <button onClick={() => setConfirmDelete(r.id)} className="btn-icon-danger"><Trash2 className="w-4 h-4" /></button>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      <EditDivergenceDialog record={editRecord} onClose={() => setEditRecord(null)} queryKey={qKey} />

      <Dialog open={confirmDelete !== null} onOpenChange={(open) => !open && setConfirmDelete(null)}>
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle>Confirmar exclusão</DialogTitle>
            <DialogDescription>
              Deseja excluir a divergência #{confirmDelete}? Essa ação não pode ser desfeita.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="secondary" onClick={() => setConfirmDelete(null)}>Cancelar</Button>
            <Button variant="destructive" onClick={() => delMut.mutate(confirmDelete!)} disabled={delMut.isPending}>
              {delMut.isPending ? 'Excluindo...' : 'Excluir'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
