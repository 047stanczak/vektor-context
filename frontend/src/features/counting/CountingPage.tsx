import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { Loader2, Printer, X, Trash2, Plus, ClipboardCheck } from 'lucide-react'
import { fetchBrands, fetchByBrand, fetchByProduct, fetchBrandByProduct, fetchDepartments, fetchByDepartment, searchCountingProduct, generatePdf, fetchAudits, saveAudit, AuditRecord } from './api'
import { CountingItem } from '@/types'

export default function CountingPage() {
  const [searchParams] = useSearchParams()
  const productCodeParam = searchParams.get('productCode')
  const queryClient = useQueryClient()

  const [selectedBrand, setSelectedBrand] = useState<string | null>(null)
  const [selectedDepartment, setSelectedDepartment] = useState<string | null>(null)
  const [manualBrand, setManualBrand] = useState('')
  const [activeBrand, setActiveBrand] = useState<string | null>(null)
  const [resolvedBrand, setResolvedBrand] = useState<string | null>(null)
  const [items, setItems] = useState<CountingItem[]>([])
  const [filteredItems, setFilteredItems] = useState<CountingItem[]>([])
  const [loading, setLoading] = useState(false)

  const [searchQ, setSearchQ] = useState('')
  const [searchResult, setSearchResult] = useState<CountingItem | null>(null)
  const [searching, setSearching] = useState(false)
  const [printing, setPrinting] = useState(false)

  const [showAudit, setShowAudit] = useState(false)
  const [auditLabel, setAuditLabel] = useState('')
  const [auditType, setAuditType] = useState('COMPLETO')
  const [auditedAt, setAuditedAt] = useState('')
  const [saving, setSaving] = useState(false)

  const { data: brands = [] } = useQuery({ queryKey: ['counting-brands'], queryFn: fetchBrands })
  const { data: departments = [] } = useQuery({ queryKey: ['counting-departments'], queryFn: fetchDepartments })
  const { data: audits = [] } = useQuery({ queryKey: ['audits'], queryFn: fetchAudits, enabled: showAudit })

  useEffect(() => {
    if (!productCodeParam) return
    setLoading(true)
    fetchBrandByProduct(Number(productCodeParam)).then(setResolvedBrand)
    fetchByProduct(Number(productCodeParam))
      .then((data) => { setItems(data); setFilteredItems(data); setActiveBrand(null) })
      .finally(() => setLoading(false))
  }, [productCodeParam])

  async function handleBrandSearch(brand: string) {
    if (!brand.trim()) return
    setLoading(true)
    setActiveBrand(brand)
    setSelectedDepartment(null)
    fetchByBrand(brand)
      .then((data) => { setItems(data); setFilteredItems(data) })
      .finally(() => setLoading(false))
  }

  async function handleDepartmentSearch(department: string) {
    if (!department.trim()) return
    setLoading(true)
    setActiveBrand(department)
    setSelectedBrand(null)
    fetchByDepartment(department)
      .then((data) => { setItems(data); setFilteredItems(data) })
      .finally(() => setLoading(false))
  }

  function handleRemove(productCode: number) {
    setFilteredItems((prev) => prev.filter((i) => i.productCode !== productCode))
  }

  async function handleSearch() {
    if (!searchQ.trim()) return
    setSearching(true)
    setSearchResult(null)
    searchCountingProduct(searchQ.trim())
      .then(setSearchResult)
      .finally(() => setSearching(false))
  }

  function handleAddProduct() {
    if (!searchResult) return
    if (filteredItems.find((i) => i.productCode === searchResult.productCode)) return
    setFilteredItems((prev) => [...prev, searchResult])
    setSearchResult(null)
    setSearchQ('')
  }

  async function handlePrint() {
    setPrinting(true)
    try {
      await generatePdf({
        auditedLabel: activeBrand || resolvedBrand || '',
        auditType: '',
        auditedAt: '',
        items: filteredItems,
      })
    } finally {
      setPrinting(false)
    }
  }

  async function handleSaveAudit() {
    if (!auditLabel.trim() || !auditedAt) return
    setSaving(true)
    try {
      await saveAudit({ auditedLabel: auditLabel, auditType, auditedAt })
      queryClient.invalidateQueries({ queryKey: ['audits'] })
      setAuditLabel('')
      setAuditedAt('')
      setAuditType('COMPLETO')
    } finally {
      setSaving(false)
    }
  }

  const label = activeBrand || resolvedBrand

  return (
    <div className="max-w-4xl mx-auto space-y-5 fade-in">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-bold text-gray-900">Contagem</h1>
        {filteredItems.length > 0 && label && (
          <button
            onClick={handlePrint}
            disabled={printing}
            className="flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-semibold"
            style={{ background: 'var(--accent-subtle)', color: 'var(--accent)' }}
          >
            {printing ? <Loader2 className="w-3.5 h-3.5 animate-spin" /> : <Printer className="w-3.5 h-3.5" />}
            Imprimir
          </button>
        )}
      </div>

      <div className="flex flex-wrap gap-3">
        <select
          className="border border-gray-200 rounded-lg px-3 py-2 text-sm"
          value={selectedBrand ?? ''}
          onChange={(e) => { setSelectedBrand(e.target.value); handleBrandSearch(e.target.value) }}
        >
          <option value="">Selecionar marca...</option>
          {brands.map((b) => <option key={b} value={b}>{b}</option>)}
        </select>

        <select
          className="border border-gray-200 rounded-lg px-3 py-2 text-sm"
          value={selectedDepartment ?? ''}
          onChange={(e) => { setSelectedDepartment(e.target.value); handleDepartmentSearch(e.target.value) }}
        >
          <option value="">Selecionar departamento...</option>
          {departments.map((d) => <option key={d} value={d}>{d}</option>)}
        </select>

        <div className="flex gap-2">
          <input
            type="text"
            className="border border-gray-200 rounded-lg px-3 py-2 text-sm"
            placeholder="Buscar marca manualmente"
            value={manualBrand}
            onChange={(e) => setManualBrand(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleBrandSearch(manualBrand)}
          />
          <button className="btn-secondary" onClick={() => handleBrandSearch(manualBrand)}>Buscar</button>
        </div>
      </div>

      {activeBrand && <p className="text-xs text-gray-400">Marca/Departamento: {activeBrand}</p>}
      {productCodeParam && !activeBrand && <p className="text-xs text-gray-400">Similares do produto {productCodeParam}</p>}

      {loading && (
        <div className="flex items-center justify-center py-16 text-gray-400 gap-2 text-sm">
          <Loader2 className="w-4 h-4 animate-spin" /> Carregando...
        </div>
      )}

      {!loading && filteredItems.length > 0 && (
        <>
          <div className="flex gap-2 items-center">
            <input
              type="text"
              className="border border-gray-200 rounded-lg px-3 py-2 text-sm flex-1"
              placeholder="Adicionar produto (código ou barcode)"
              value={searchQ}
              onChange={(e) => setSearchQ(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
            />
            <button className="btn-secondary" onClick={handleSearch} disabled={searching}>
              {searching ? <Loader2 className="w-3.5 h-3.5 animate-spin" /> : 'Buscar'}
            </button>
            {searchResult && (
              <button className="btn-primary flex items-center gap-1" onClick={handleAddProduct}>
                <Plus className="w-3.5 h-3.5" /> Adicionar
              </button>
            )}
          </div>
          {searchResult && (
            <p className="text-xs text-gray-500">{searchResult.productCode} — {searchResult.description}</p>
          )}

          <div className="card overflow-hidden">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-gray-100">
                  {['Código', 'Barcode', 'Descrição', 'Complemento', 'Estoque', ''].map((h) => (
                    <th key={h} className="px-5 py-3 text-left text-xs font-bold text-gray-400 uppercase tracking-wider">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {filteredItems.map((item) => (
                  <tr key={item.productCode} className="hover:bg-gray-50 transition-colors">
                    <td className="px-5 py-3 mono text-xs font-medium text-gray-800">{item.productCode}</td>
                    <td className="px-5 py-3 mono text-xs text-gray-500">{item.barcode ?? '—'}</td>
                    <td className="px-5 py-3 text-xs text-gray-600 max-w-[200px] truncate">{item.description ?? '—'}</td>
                    <td className="px-5 py-3 text-xs text-gray-500 max-w-[160px] truncate">{item.complement ?? '—'}</td>
                    <td className="px-5 py-3 text-xs font-semibold text-gray-700">{item.currentStock}</td>
                    <td className="px-5 py-3">
                      <button onClick={() => handleRemove(item.productCode)}>
                        <Trash2 className="w-3.5 h-3.5 text-gray-300 hover:text-red-400" />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <p className="text-xs text-gray-400 text-right">{filteredItems.length} produto(s)</p>
        </>
      )}

      {!loading && (productCodeParam || activeBrand) && filteredItems.length === 0 && (
        <div className="card p-5 text-sm text-gray-400 text-center">Nenhum produto encontrado.</div>
      )}

      {/* FAB Auditorias */}
      <button
        onClick={() => setShowAudit(true)}
        className="fixed bottom-6 right-6 w-12 h-12 rounded-full shadow-lg flex items-center justify-center z-40"
        style={{ background: 'var(--accent)', color: 'white' }}
        title="Auditorias"
      >
        <ClipboardCheck className="w-5 h-5" />
      </button>

      {/* Modal Auditorias */}
      {showAudit && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <div className="card p-6 w-full max-w-md space-y-4 max-h-[80vh] flex flex-col">
            <div className="flex items-center justify-between">
              <h2 className="font-bold text-gray-900">Auditorias</h2>
              <button onClick={() => setShowAudit(false)}><X className="w-4 h-4 text-gray-400" /></button>
            </div>

            <div className="space-y-3">
              <input
                type="text"
                className="border border-gray-200 rounded-lg px-3 py-2 text-sm w-full"
                placeholder="O que foi auditado"
                value={auditLabel}
                onChange={(e) => setAuditLabel(e.target.value)}
              />
              <div className="flex gap-2">
                <select
                  className="border border-gray-200 rounded-lg px-3 py-2 text-sm flex-1"
                  value={auditType}
                  onChange={(e) => setAuditType(e.target.value)}
                >
                  <option value="COMPLETO">Completo</option>
                  <option value="PARCIAL">Parcial</option>
                </select>
                <input
                  type="date"
                  className="field flex-1"
                  value={auditedAt}
                  onChange={(e) => setAuditedAt(e.target.value)}
                />
              </div>
              <button
                className="btn-primary w-full flex items-center justify-center gap-2"
                onClick={handleSaveAudit}
                disabled={saving || !auditLabel.trim() || !auditedAt}
              >
                {saving ? <Loader2 className="w-3.5 h-3.5 animate-spin" /> : 'Registrar'}
              </button>
            </div>

            <div className="overflow-y-auto flex-1 divide-y divide-gray-50">
              {audits.length === 0 && (
                <p className="text-xs text-gray-400 text-center py-4">Nenhuma auditoria registrada.</p>
              )}
              {audits.map((a: AuditRecord) => (
                <div key={a.id} className="py-3 space-y-0.5">
                  <p className="text-sm font-medium text-gray-800">{a.auditedLabel}</p>
                  <p className="text-xs text-gray-400">{a.auditType} — {a.auditedAt}</p>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}