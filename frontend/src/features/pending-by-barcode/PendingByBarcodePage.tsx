import { useEffect, useRef, useState } from 'react'
import { Html5Qrcode } from 'html5-qrcode'
import { toast } from 'sonner'
import { Loader2, ScanLine, Package } from 'lucide-react'
import { fetchPendingByBarcode, fetchPendingByCode } from './api'
import { SeparationProduct } from '@/types'
import { PageHeader } from '@/components/ui/page-header'
import { Button } from '@/components/ui/button'

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
      <PageHeader title="Buscar por código" description="Escaneie ou digite o código do produto." />

      {!scanning && (
        <Button className="w-full" onClick={() => { setResults([]); setBarcode(null); setScanning(true) }}>
          <ScanLine className="w-4 h-4" />
          Escanear código
        </Button>
      )}

      {!scanning && (
        <div className="flex gap-2">
          <input
            type="text"
            className="field flex-1"
            placeholder="Código interno ou de barras"
            value={manualCode}
            onChange={(e) => setManualCode(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleManualSearch()}
          />
          <Button variant="secondary" onClick={handleManualSearch}>
            Buscar
          </Button>
        </div>
      )}

      {scanning && (
        <div className="card p-3 space-y-3">
          <div id="qr-reader" />
          <Button variant="secondary" className="w-full" onClick={() => setScanning(false)}>
            Cancelar
          </Button>
        </div>
      )}

      {barcode && !scanning && (
        <p className="text-xs text-muted-foreground mono">Código: {barcode}</p>
      )}

      {loading && (
        <div className="loading-state py-10">
          <Loader2 className="w-4 h-4 animate-spin" /> Buscando...
        </div>
      )}

      {!loading && barcode && results.length === 0 && (
        <div className="card p-5 text-sm text-muted-foreground text-center">
          Nenhuma pendência encontrada para esse produto.
        </div>
      )}

      {!loading && results.length > 0 && (
        <div className="space-y-2">
          {results[0].currentStock != null && (
            <div className="card px-4 py-3 flex items-center justify-between">
              <span className="text-sm text-muted-foreground">Estoque atual</span>
              <span className="text-sm font-bold">{results[0].currentStock}</span>
            </div>
          )}
          <div className="card divide-y">
            {results.map((r) => (
              <div key={r.id} className="flex items-center justify-between px-4 py-3">
                <div className="flex items-center gap-2 min-w-0">
                  <Package className="w-4 h-4 text-muted-foreground flex-shrink-0" />
                  <div className="min-w-0">
                    <p className="text-sm font-medium truncate">{r.productDescription}</p>
                    <p className="text-xs text-muted-foreground">Loja {r.storeCode}</p>
                  </div>
                </div>
                <span className="text-sm font-bold flex-shrink-0 ml-2">{r.quantity}</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
