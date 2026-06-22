import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Loader2, Printer } from 'lucide-react'
import { fetchBrands, fetchByBrand, fetchByProduct, fetchBrandByProduct, getPdfUrl, fetchDepartments, fetchByDepartment } from './api'
import { CountingItem } from '@/types'

export default function CountingPage() {
  const [searchParams] = useSearchParams()
  const productCodeParam = searchParams.get('productCode')

  const [selectedBrand, setSelectedBrand] = useState<string | null>(null)
  const [selectedDepartment, setSelectedDepartment] = useState<string | null>(null)
  const [manualBrand, setManualBrand] = useState('')
  const [activeBrand, setActiveBrand] = useState<string | null>(null)
  const [resolvedBrand, setResolvedBrand] = useState<string | null>(null)
  const [items, setItems] = useState<CountingItem[]>([])
  const [loading, setLoading] = useState(false)

  const { data: brands = [] } = useQuery({ queryKey: ['counting-brands'], queryFn: fetchBrands })
  const { data: departments = [] } = useQuery({ queryKey: ['counting-departments'], queryFn: fetchDepartments })

  useEffect(() => {
    if (!productCodeParam) return
    setLoading(true)
    fetchBrandByProduct(Number(productCodeParam)).then(setResolvedBrand)
    fetchByProduct(Number(productCodeParam))
      .then((data) => { setItems(data); setActiveBrand(null) })
      .finally(() => setLoading(false))
  }, [productCodeParam])

  async function handleBrandSearch(brand: string) {
    if (!brand.trim()) return
    setLoading(true)
    setActiveBrand(brand)
    setSelectedDepartment(null)
    fetchByBrand(brand)
      .then(setItems)
      .finally(() => setLoading(false))
  }

  async function handleDepartmentSearch(department: string) {
    if (!department.trim()) return
    setLoading(true)
    setActiveBrand(department)
    setSelectedBrand(null)
    fetchByDepartment(department)
      .then(setItems)
      .finally(() => setLoading(false))
  }

  return (
    <div className="max-w-4xl mx-auto space-y-5 fade-in">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-bold text-gray-900">Contagem</h1>
        {items.length > 0 && (activeBrand || resolvedBrand) && (
          <a
            href={getPdfUrl((activeBrand || resolvedBrand)!)}
            target="_blank"
            rel="noreferrer"
            className="flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-semibold"
            style={{ background: 'var(--accent-subtle)', color: 'var(--accent)' }}
          >
            <Printer className="w-3.5 h-3.5" /> Imprimir
          </a>
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
          <button className="btn-secondary" onClick={() => handleBrandSearch(manualBrand)}>
            Buscar
          </button>
        </div>
      </div>

      {activeBrand && <p className="text-xs text-gray-400">Marca: {activeBrand}</p>}
      {productCodeParam && !activeBrand && <p className="text-xs text-gray-400">Similares do produto {productCodeParam}</p>}

      {loading && (
        <div className="flex items-center justify-center py-16 text-gray-400 gap-2 text-sm">
          <Loader2 className="w-4 h-4 animate-spin" /> Carregando...
        </div>
      )}

      {!loading && items.length > 0 && (
        <div className="card overflow-hidden">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-gray-100">
                {['Código', 'Barcode', 'Descrição', 'Complemento', 'Estoque'].map((h) => (
                  <th key={h} className="px-5 py-3 text-left text-xs font-bold text-gray-400 uppercase tracking-wider">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {items.map((item) => (
                <tr key={item.productCode} className="hover:bg-gray-50 transition-colors">
                  <td className="px-5 py-3 mono text-xs font-medium text-gray-800">{item.productCode}</td>
                  <td className="px-5 py-3 mono text-xs text-gray-500">{item.barcode ?? '—'}</td>
                  <td className="px-5 py-3 text-xs text-gray-600 max-w-[200px] truncate">{item.description ?? '—'}</td>
                  <td className="px-5 py-3 text-xs text-gray-500 max-w-[160px] truncate">{item.complement ?? '—'}</td>
                  <td className="px-5 py-3 text-xs font-semibold text-gray-700">{item.currentStock}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {!loading && (productCodeParam || activeBrand) && items.length === 0 && (
        <div className="card p-5 text-sm text-gray-400 text-center">Nenhum produto encontrado.</div>
      )}
      {!loading && items.length > 0 && (
        <p className="text-xs text-gray-400 text-right">{items.length} produto(s)</p>
      )}
    </div>
  )
}
