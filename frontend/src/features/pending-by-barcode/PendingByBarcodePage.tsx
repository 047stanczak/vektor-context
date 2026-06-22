import { useEffect, useRef, useState } from 'react'
import { Html5Qrcode } from 'html5-qrcode'
import { toast } from 'sonner'
import { Loader2, ScanLine, Package } from 'lucide-react'
import { fetchPendingByBarcode, fetchPendingByCode } from './api'
import { SeparationProduct } from '@/types'

export default function PendingByBarcodePage() {
  const [scanning, setScanning] = useState(false)
  const [loading, setLoading] = useState(false)
  const [barcode, setBarcode] = useState<string | null>(null)
  const [results, setResults] = useState<SeparationProduct[]>([])
  const [manualCode, setManualCode] = useState('')
  const scannerRef = useRef<Html5Qrcode | null>(null)

  useEffect(() => {
    if (!scanning) return

    const scanner = new Html5Qrcode('qr-reader')
    scannerRef.current = scanner

    scanner
      .start(
        { facingMode: 'environment' },
        { fps: 10, qrbox: 250 },
        async (decodedText) => {
          await scanner.stop()
          setScanning(false)
          setBarcode(decodedText)
          setLoading(true)
          try {
            const data = await fetchPendingByBarcode(decodedText)
            setResults(data)
          } catch {
            toast.error('Erro ao buscar pendências.')
          } finally {
            setLoading(false)
          }
        },
        undefined,
      )
      .catch(() => {
        toast.error('Não foi possível acessar a câmera.')
        setScanning(false)
      })

    return () => {
      if (scanner.isScanning) scanner.stop().catch(() => {})
    }
  }, [scanning])

  async function handleManualSearch() {
    if (!manualCode.trim()) return
    setBarcode(manualCode.trim())
    setLoading(true)
    try {
      const data = await fetchPendingByCode(manualCode.trim())
      setResults(data)
    } catch {
      toast.error('Erro ao buscar pendências.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="space-y-4 max-w-md fade-in">
      <h1 className="font-bold text-gray-900 text-lg">Buscar por código de barras</h1>

      {!scanning && (
        <button className="btn-primary w-full" onClick={() => { setResults([]); setBarcode(null); setScanning(true) }}>
          <ScanLine className="w-4 h-4" />
          Escanear código
        </button>
      )}

      {!scanning && (
        <div className="flex gap-2">
          <input
            type="text"
            className="flex-1 border border-gray-200 rounded-lg px-3 py-2 text-sm"
            placeholder="Código interno ou de barras"
            value={manualCode}
            onChange={(e) => setManualCode(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleManualSearch()}
          />
          <button className="btn-secondary" onClick={handleManualSearch}>
            Buscar
          </button>
        </div>
      )}

      {scanning && (
        <div className="card p-3 space-y-3">
          <div id="qr-reader" />
          <button className="btn-secondary w-full" onClick={() => setScanning(false)}>
            Cancelar
          </button>
        </div>
      )}

      {barcode && !scanning && (
        <p className="text-xs text-gray-400 mono">Código: {barcode}</p>
      )}

      {loading && (
        <div className="flex items-center justify-center py-10 text-gray-400 gap-2 text-sm">
          <Loader2 className="w-4 h-4 animate-spin" /> Buscando...
        </div>
      )}

      {!loading && barcode && results.length === 0 && (
        <div className="card p-5 text-sm text-gray-400 text-center">
          Nenhuma pendência encontrada para esse produto.
        </div>
      )}

      {!loading && results.length > 0 && (
        <div className="card divide-y divide-gray-100">
          {results.map((r) => (
            <div key={r.id} className="flex items-center justify-between px-4 py-3">
              <div className="flex items-center gap-2 min-w-0">
                <Package className="w-4 h-4 text-gray-400 flex-shrink-0" />
                <div className="min-w-0">
                  <p className="text-sm font-medium text-gray-800 truncate">{r.productDescription}</p>
                  <p className="text-xs text-gray-400">Loja {r.storeCode}</p>
                </div>
              </div>
              <span className="text-sm font-bold text-gray-700 flex-shrink-0 ml-2">{r.quantity}</span>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}