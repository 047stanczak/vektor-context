import { useEffect, useRef, useState } from 'react'
import { Html5Qrcode } from 'html5-qrcode'
import { toast } from 'sonner'
import { ScanLine, Plus, Trash2, Copy, Check } from 'lucide-react'
import { formatDdMmYyyyInput, isValidDdMmYyyy } from '@/lib/utils'
import { PageHeader } from '@/components/ui/page-header'
import { Button } from '@/components/ui/button'

interface Item {
  barcode: string
  quantity: number
  expiry: string
}

export default function RequestPendingPage() {
  const [scanning, setScanning] = useState(false)
  const [barcode, setBarcode] = useState('')
  const [quantity, setQuantity] = useState('')
  const [expiry, setExpiry] = useState('')
  const [items, setItems] = useState<Item[]>([])
  const [copied, setCopied] = useState(false)
  const scannerRef = useRef<Html5Qrcode | null>(null)
  const quantityRef = useRef<HTMLInputElement>(null)

  useEffect(() => {
    if (!scanning) return
    const scanner = new Html5Qrcode('qr-reader-pending')
    scannerRef.current = scanner
    scanner
      .start(
        { facingMode: 'environment' },
        { fps: 10, qrbox: 250 },
        (decoded) => {
          scanner.stop()
          setScanning(false)
          setBarcode(decoded)
          setTimeout(() => quantityRef.current?.focus(), 100)
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

  function handleAdd() {
    if (!barcode.trim()) { toast.error('Informe o código de barras.'); return }
    const qty = Number(quantity)
    if (!qty || qty <= 0) { toast.error('Informe a quantidade.'); return }

    const expiryValue = expiry.trim()
    if (expiryValue && !isValidDdMmYyyy(expiryValue)) {
      toast.error('Validade inválida. Use o formato dd/mm/aaaa.')
      return
    }

    setItems(prev => [...prev, { barcode: barcode.trim(), quantity: qty, expiry: expiryValue }])
    setBarcode('')
    setQuantity('')
    setExpiry('')
  }

  function handleRemove(index: number) {
    setItems(prev => prev.filter((_, i) => i !== index))
  }

  function buildMessage() {
    return items.map(item => {
      const base = `${item.barcode} - ${item.quantity}`
      return item.expiry ? `${base} (vencimento ${item.expiry})` : base
    }).join('\n')
  }

  async function handleCopy() {
    if (items.length === 0) { toast.error('Nenhum item na lista.'); return }
    await navigator.clipboard.writeText(buildMessage())
    setCopied(true)
    toast.success('Mensagem copiada!')
    setTimeout(() => setCopied(false), 2000)
  }

  return (
    <div className="space-y-4 max-w-md fade-in">
      <PageHeader title="Pedir pendência" description="Monte a lista de produtos para solicitar pendência." />

      {!scanning ? (
        <Button className="w-full" onClick={() => setScanning(true)}>
          <ScanLine className="w-4 h-4" />
          Escanear código
        </Button>
      ) : (
        <div className="card p-3 space-y-3">
          <div id="qr-reader-pending" />
          <Button variant="secondary" className="w-full" onClick={() => setScanning(false)}>
            Cancelar
          </Button>
        </div>
      )}

      {!scanning && (
        <div className="card p-4 space-y-3">
          <div>
            <label className="label">Código de barras</label>
            <input
              className="field"
              placeholder="Digite ou escaneie"
              value={barcode}
              onChange={e => setBarcode(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && quantityRef.current?.focus()}
            />
          </div>
          <div className="flex gap-2">
            <div className="flex-1">
              <label className="label">Quantidade</label>
              <input
                ref={quantityRef}
                className="field"
                type="number"
                min="1"
                placeholder="0"
                value={quantity}
                onChange={e => setQuantity(e.target.value)}
                onKeyDown={e => e.key === 'Enter' && handleAdd()}
              />
            </div>
            <div className="flex-1">
              <label className="label">Validade (opcional)</label>
              <input
                className="field"
                placeholder="dd/mm/aaaa"
                inputMode="numeric"
                maxLength={10}
                value={expiry}
                onChange={e => setExpiry(formatDdMmYyyyInput(e.target.value))}
                onKeyDown={e => e.key === 'Enter' && handleAdd()}
              />
            </div>
          </div>
          <Button className="w-full" onClick={handleAdd}>
            <Plus className="w-4 h-4" />
            Adicionar
          </Button>
        </div>
      )}

      {items.length > 0 && (
        <>
          <div className="card divide-y">
            {items.map((item, i) => (
              <div key={i} className="flex items-center justify-between px-4 py-3 gap-2">
                <div className="min-w-0">
                  <p className="text-sm font-medium truncate">{item.barcode}</p>
                  <p className="text-xs mt-0.5 text-muted-foreground">
                    Qtd: {item.quantity}
                    {item.expiry && ` · Venc: ${item.expiry}`}
                  </p>
                </div>
                <button onClick={() => handleRemove(i)} className="btn-icon-danger flex-shrink-0">
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            ))}
          </div>

          <Button className="w-full" onClick={handleCopy}>
            {copied ? <Check className="w-4 h-4" /> : <Copy className="w-4 h-4" />}
            {copied ? 'Copiado!' : 'Copiar mensagem'}
          </Button>
        </>
      )}
    </div>
  )
}
