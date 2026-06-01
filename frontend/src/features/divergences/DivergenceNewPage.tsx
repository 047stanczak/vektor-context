import { useState, useEffect, useRef } from 'react'
import { useMutation } from '@tanstack/react-query'
import { useBlocker } from 'react-router-dom'
import { toast } from 'sonner'
import { DivergenceDraftItem, DivergenceQueryResponse, Tipo } from '@/types'
import { queryProduct, saveDivergences } from './api'
import { today, detectInputMode, formatNum, addRecentSeparator } from '@/lib/utils'
import DivergenceDraftTable from './components/DivergenceDraftTable'
import DuplicateDialog from './components/DuplicateDialog'
import SeparatorCombobox from './components/SeparatorCombobox'
import { Search, PlusCircle, Save, AlertCircle } from 'lucide-react'

const TIPOS: Tipo[] = ['FALTA', 'SOBRA', 'SEM_NF']

let idCounter = 0
function newId() { return `draft-${++idCounter}-${Date.now()}` }

export default function DivergenceNewPage() {
  const [storeCode, setStoreCode]         = useState('')
  const [date, setDate]                   = useState(today())
  const [productInput, setProductInput]   = useState('')
  const [inputMode, setInputMode]         = useState<'productCode' | 'barcode'>('productCode')
  const [tipo, setTipo]                   = useState<Tipo>('FALTA')
  const [quantity, setQuantity]           = useState('')
  const [separatorName, setSeparatorName] = useState('')
  const [observation, setObservation]     = useState('')

  const [queryResult, setQueryResult] = useState<DivergenceQueryResponse | null>(null)
  const [queryError, setQueryError]   = useState('')
  const [queryLoading, setQueryLoading] = useState(false)
  const [confirmed, setConfirmed]     = useState(false)

  const [items, setItems] = useState<DivergenceDraftItem[]>([])
  const [dupState, setDupState] = useState<{ open: boolean; pending: DivergenceDraftItem | null; existingId: string }>
    ({ open: false, pending: null, existingId: '' })
  const [editingId, setEditingId] = useState<string | null>(null)

  const productRef = useRef<HTMLInputElement>(null)

  // Block in-app navigation when there are unsaved items
  const blocker = useBlocker(items.length > 0)
  useEffect(() => {
    if (blocker.state === 'blocked') {
      const ok = window.confirm('Há itens não salvos. Deseja sair mesmo assim?')
      if (ok) blocker.proceed()
      else blocker.reset()
    }
  }, [blocker])

  // Block browser close / refresh
  useEffect(() => {
    const handler = (e: BeforeUnloadEvent) => {
      if (items.length > 0) { e.preventDefault(); e.returnValue = '' }
    }
    window.addEventListener('beforeunload', handler)
    return () => window.removeEventListener('beforeunload', handler)
  }, [items.length])

  // Auto-detect barcode vs productCode
  useEffect(() => {
    if (productInput) setInputMode(detectInputMode(productInput))
  }, [productInput])

  // Load draft item back into form for editing
  useEffect(() => {
    if (!editingId) return
    const item = items.find((i) => i.draftId === editingId)
    if (!item) return
    setProductInput(String(item.productCode))
    setInputMode('productCode')
    setTipo(item.tipo)
    setQuantity(String(item.quantity))
    setSeparatorName(item.separatorName)
    setObservation(item.observation ?? '')
    setQueryResult(item.productDescription ? {
      productCode: item.productCode,
      productDescription: item.productDescription ?? '',
      productComplement: item.productComplement ?? '',
      barcode: '',
      currentStock: item.currentStock,
      separatorName: item.separatorName,
    } : null)
    setItems((prev) => prev.filter((i) => i.draftId !== editingId))
    setEditingId(null)
    productRef.current?.focus()
  }, [editingId])

  async function handleProductBlur() {
    if (!productInput || !storeCode) return
    setQueryLoading(true)
    setQueryResult(null)
    setQueryError('')
    setConfirmed(false)
    try {
      const params: Record<string, unknown> = { storeCode: Number(storeCode) }
      if (inputMode === 'barcode') params.barcode = productInput
      else params.productCode = Number(productInput)
      const res = await queryProduct(params as Parameters<typeof queryProduct>[0])
      if (res.success) {
        setQueryResult(res.data)
        setSeparatorName(res.data.separatorName ?? '')
      } else {
        setQueryError(res.message)
      }
    } catch (err: any) {
      setQueryError(err.response?.data?.message ?? 'Produto não encontrado')
    } finally {
      setQueryLoading(false)
    }
  }

  function getProductCode(): number | null {
    if (queryResult) return queryResult.productCode
    if (inputMode === 'productCode' && productInput && !isNaN(Number(productInput))) return Number(productInput)
    return null
  }

  function handleAdd() {
    const productCode = getProductCode()
    if (!storeCode)  { toast.error('Informe a loja'); return }
    if (!productCode) { toast.error('Produto inválido ou não encontrado'); return }
    if (!quantity || isNaN(Number(quantity)) || Number(quantity) <= 0) { toast.error('Quantidade deve ser maior que zero'); return }
    if (!separatorName.trim()) { toast.error('Informe o separador'); return }

    const pending: DivergenceDraftItem = {
      draftId: newId(),
      date,
      storeCode: Number(storeCode),
      productCode,
      productDescription: queryResult?.productDescription ?? null,
      productComplement:  queryResult?.productComplement ?? null,
      tipo,
      quantity: Number(quantity),
      currentStock: queryResult?.currentStock ?? null,
      separatorName: separatorName.trim(),
      observation: observation.trim() || null,
    }

    const dup = items.findIndex((i) =>
      i.storeCode === pending.storeCode &&
      i.productCode === pending.productCode &&
      i.tipo === pending.tipo
    )
    if (dup >= 0) {
      setDupState({ open: true, pending, existingId: items[dup].draftId })
      return
    }

    commitAdd(pending)
  }

  function commitAdd(item: DivergenceDraftItem) {
    setItems((prev) => [...prev, item])
    addRecentSeparator(item.separatorName)
    clearForm()
    setTimeout(() => productRef.current?.focus(), 50)
  }

  function handleDupSum() {
    const { pending, existingId } = dupState
    if (!pending) return
    setItems((prev) => prev.map((i) =>
      i.draftId === existingId ? { ...i, quantity: i.quantity + pending.quantity } : i
    ))
    setDupState({ open: false, pending: null, existingId: '' })
    clearForm()
    setTimeout(() => productRef.current?.focus(), 50)
  }

  function handleDupNew() {
    if (dupState.pending) commitAdd(dupState.pending)
    setDupState({ open: false, pending: null, existingId: '' })
  }

  function clearForm() {
    setProductInput('')
    setQuantity('')
    setObservation('')
    setQueryResult(null)
    setQueryError('')
    setConfirmed(false)
    setSeparatorName('')
    setTipo('FALTA')
  }

  const saveMut = useMutation({
    mutationFn: () => saveDivergences(items.map((i) => ({
      date: i.date,
      storeCode: i.storeCode,
      productCode: i.productCode,
      tipo: i.tipo,
      quantity: i.quantity,
      currentStock: i.currentStock,
      separatorName: i.separatorName,
      observation: i.observation,
    }))),
    onSuccess: (res) => {
      if (res.success) {
        toast.success(`${items.length} divergência(s) salva(s) com sucesso`)
        setItems([])
      } else {
        toast.error(res.message)
      }
    },
    onError: (err: any) => toast.error(err.response?.data?.message ?? 'Erro ao salvar'),
  })

  return (
    <div className="max-w-3xl mx-auto space-y-5">
      <h1 className="text-xl font-semibold text-gray-900">Registrar divergências</h1>

      {/* Form card */}
      <div className="bg-white rounded-xl border border-gray-200 p-5 space-y-4">

        {/* Loja + Data */}
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="label">Loja <span className="text-red-500">*</span></label>
            <input className="field" value={storeCode} onChange={(e) => setStoreCode(e.target.value)} placeholder="ex: 1" type="number" />
          </div>
          <div>
            <label className="label">Data</label>
            <input type="date" className="field" value={date} onChange={(e) => setDate(e.target.value)} />
          </div>
        </div>

        {/* Produto */}
        <div>
          <div className="flex items-center justify-between mb-1">
            <label className="label mb-0">Produto <span className="text-red-500">*</span></label>
            <div className="flex gap-1 text-xs">
              {(['productCode', 'barcode'] as const).map((m) => (
                <button key={m} onClick={() => setInputMode(m)}
                  className={`px-2 py-0.5 rounded-full border transition-colors ${inputMode === m
                    ? 'bg-blue-600 text-white border-blue-600'
                    : 'bg-white text-gray-500 border-gray-300 hover:border-gray-400'}`}>
                  {m === 'productCode' ? 'Código' : 'Barcode'}
                </button>
              ))}
            </div>
          </div>
          <div className="relative">
            <input
              ref={productRef}
              className="field pr-9"
              value={productInput}
              onChange={(e) => setProductInput(e.target.value)}
              onBlur={handleProductBlur}
              onKeyDown={(e) => e.key === 'Enter' && handleProductBlur()}
              placeholder={inputMode === 'barcode' ? 'Código de barras...' : 'Código do produto...'}
            />
            {queryLoading
              ? <div className="absolute right-2.5 top-2.5 w-4 h-4 border-2 border-blue-500 border-t-transparent rounded-full animate-spin" />
              : <Search className="absolute right-2.5 top-2.5 w-4 h-4 text-gray-400" />}
          </div>

          {queryError && (
            <div className="mt-2 p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-700 flex gap-2">
              <AlertCircle className="w-4 h-4 flex-shrink-0 mt-0.5" />
              <div>
                <div>{queryError}</div>
                {inputMode === 'barcode' && <div className="text-xs mt-1 text-red-600">Tente o código interno.</div>}
                {!confirmed && (
                  <button onClick={() => setConfirmed(true)} className="mt-1 text-xs underline">
                    Registrar mesmo assim
                  </button>
                )}
                {confirmed && <div className="text-xs mt-1 font-medium text-amber-700">Confirmar habilitado — informe o código numérico manualmente.</div>}
              </div>
            </div>
          )}
        </div>

        {/* Product info */}
        {queryResult && (
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-3 text-sm">
            <div className="font-medium text-blue-900">{queryResult.productDescription}</div>
            {queryResult.productComplement && <div className="text-blue-700 text-xs">{queryResult.productComplement}</div>}
            <div className="text-blue-600 text-xs mt-1">
              Código: {queryResult.productCode} · Estoque CD: {formatNum(queryResult.currentStock)}
            </div>
          </div>
        )}

        {/* Tipo + Quantidade */}
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="label">Tipo <span className="text-red-500">*</span></label>
            <div className="flex gap-1">
              {TIPOS.map((t) => (
                <button key={t} onClick={() => setTipo(t)}
                  className={`flex-1 py-2 rounded-lg text-xs font-medium border transition-colors ${tipo === t
                    ? t === 'FALTA'  ? 'bg-red-600   text-white border-red-600'
                    : t === 'SOBRA'  ? 'bg-green-600 text-white border-green-600'
                    :                  'bg-amber-500 text-white border-amber-500'
                    : 'bg-white text-gray-600 border-gray-300 hover:border-gray-400'}`}>
                  {t}
                </button>
              ))}
            </div>
          </div>
          <div>
            <label className="label">Quantidade <span className="text-red-500">*</span></label>
            <input type="number" step="0.01" className="field" value={quantity}
              onChange={(e) => setQuantity(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleAdd()}
              placeholder="0" />
          </div>
        </div>

        {/* Separador + Estoque */}
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="label">Separador <span className="text-red-500">*</span></label>
            <SeparatorCombobox value={separatorName} onChange={setSeparatorName} />
          </div>
          <div>
            <label className="label">Estoque CD</label>
            <input className="field bg-gray-50 text-gray-500"
              value={queryResult?.currentStock != null ? String(queryResult.currentStock) : ''}
              readOnly placeholder="—" />
          </div>
        </div>

        {/* Observação */}
        <div>
          <label className="label">Observação <span className="text-gray-400 text-xs">(opcional)</span></label>
          <input className="field" value={observation}
            onChange={(e) => setObservation(e.target.value.split('\n').join(''))}
            maxLength={200} placeholder="Observação opcional..." />
        </div>

        <button onClick={handleAdd} disabled={!storeCode || !productInput || !quantity}
          className="w-full flex items-center justify-center gap-2 bg-blue-600 text-white rounded-lg py-2.5 text-sm font-medium hover:bg-blue-700 disabled:opacity-40 transition-colors">
          <PlusCircle className="w-4 h-4" /> Adicionar à lista
        </button>
      </div>

      {/* Draft list */}
      <div className="bg-white rounded-xl border border-gray-200 p-5">
        <div className="flex items-center justify-between mb-4">
          <h2 className="font-medium text-gray-900">
            Lista ({items.length} item{items.length !== 1 ? 's' : ''})
          </h2>
          {items.length > 0 && (
            <button onClick={() => saveMut.mutate()} disabled={saveMut.isPending}
              className="flex items-center gap-2 bg-green-600 text-white rounded-lg px-4 py-2 text-sm font-medium hover:bg-green-700 disabled:opacity-50 transition-colors">
              <Save className="w-4 h-4" />
              {saveMut.isPending ? 'Salvando...' : 'Salvar tudo'}
            </button>
          )}
        </div>
        <DivergenceDraftTable items={items} onRemove={(id) => setItems((p) => p.filter((i) => i.draftId !== id))} onEdit={setEditingId} />
      </div>

      <DuplicateDialog
        open={dupState.open}
        productCode={dupState.pending?.productCode ?? 0}
        tipo={dupState.pending?.tipo ?? ''}
        onSum={handleDupSum}
        onNew={handleDupNew}
        onCancel={() => setDupState({ open: false, pending: null, existingId: '' })}
      />
    </div>
  )
}
