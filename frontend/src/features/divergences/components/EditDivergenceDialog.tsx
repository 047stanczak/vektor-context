import { useState, useEffect } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { DivergenceRecord, Tipo } from '@/types'
import { updateDivergence } from '../api'
import { today } from '@/lib/utils'
import SeparatorCombobox from './SeparatorCombobox'

interface Props {
  record: DivergenceRecord | null
  onClose: () => void
  queryKey: unknown[]
}

const TIPOS: Tipo[] = ['FALTA', 'SOBRA', 'SEM_NF']

export default function EditDivergenceDialog({ record, onClose, queryKey }: Props) {
  const qc = useQueryClient()
  const [form, setForm] = useState({ date: today(), storeCode: '', tipo: 'FALTA' as Tipo, quantity: '', currentStock: '', separatorName: '', observation: '' })

  useEffect(() => {
    if (record) setForm({
      date: record.date,
      storeCode: String(record.storeCode),
      tipo: record.tipo,
      quantity: String(record.quantity),
      currentStock: record.currentStock != null ? String(record.currentStock) : '',
      separatorName: record.separatorName ?? '',
      observation: record.observation ?? '',
    })
  }, [record])

  const mut = useMutation({
    mutationFn: () => updateDivergence(record!.id, {
      date: form.date,
      storeCode: Number(form.storeCode),
      productCode: record!.productCode,
      tipo: form.tipo,
      quantity: Number(form.quantity),
      currentStock: form.currentStock ? Number(form.currentStock) : null,
      separatorName: form.separatorName,
      observation: form.observation || null,
    }),
    onSuccess: () => {
      toast.success('Divergência atualizada')
      qc.invalidateQueries({ queryKey })
      onClose()
    },
    onError: (err: any) => toast.error(err.response?.data?.message ?? 'Erro ao atualizar'),
  })

  if (!record) return null

  const set = (k: string) => (v: string) => setForm((f) => ({ ...f, [k]: v }))
  const setE = (k: string) => (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => setForm((f) => ({ ...f, [k]: e.target.value }))

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="fixed inset-0 bg-black/40" onClick={onClose} />
      <div className="relative bg-white rounded-xl shadow-xl p-6 w-full max-w-md mx-4 max-h-screen overflow-auto">
        <h3 className="font-semibold text-gray-900 mb-4">Editar divergência #{record.id}</h3>

        <div className="space-y-3">
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="label">Data</label>
              <input type="date" className="field" value={form.date} onChange={setE('date')} />
            </div>
            <div>
              <label className="label">Loja</label>
              <input className="field" value={form.storeCode} onChange={setE('storeCode')} />
            </div>
          </div>
          <div>
            <label className="label">Produto</label>
            <input className="field bg-gray-50" value={record.productCode} readOnly />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="label">Tipo</label>
              <select className="field" value={form.tipo} onChange={setE('tipo')}>
                {TIPOS.map((t) => <option key={t}>{t}</option>)}
              </select>
            </div>
            <div>
              <label className="label">Quantidade</label>
              <input type="number" step="0.01" className="field" value={form.quantity} onChange={setE('quantity')} />
            </div>
          </div>
          <div>
            <label className="label">Separador</label>
            <SeparatorCombobox value={form.separatorName} onChange={set('separatorName')} />
          </div>
          <div>
            <label className="label">Estoque atual</label>
            <input type="number" step="0.01" className="field" value={form.currentStock} onChange={setE('currentStock')} placeholder="opcional" />
          </div>
          <div>
            <label className="label">Observação</label>
            <input className="field" value={form.observation} onChange={(e) => setForm((f) => ({ ...f, observation: e.target.value.split('\n').join('') }))} maxLength={200} />
          </div>
        </div>

        <div className="flex gap-2 mt-5">
          <button onClick={onClose} className="flex-1 bg-gray-100 text-gray-800 rounded-lg py-2 text-sm font-medium hover:bg-gray-200 transition-colors">Cancelar</button>
          <button onClick={() => mut.mutate()} disabled={mut.isPending} className="flex-1 bg-blue-600 text-white rounded-lg py-2 text-sm font-medium hover:bg-blue-700 disabled:opacity-50 transition-colors">
            {mut.isPending ? 'Salvando...' : 'Salvar'}
          </button>
        </div>
      </div>
    </div>
  )
}
