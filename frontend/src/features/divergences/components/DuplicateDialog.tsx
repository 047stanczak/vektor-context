interface Props {
  open: boolean
  productCode: number
  tipo: string
  onSum: () => void
  onNew: () => void
  onCancel: () => void
}

export default function DuplicateDialog({ open, productCode, tipo, onSum, onNew, onCancel }: Props) {
  if (!open) return null
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="fixed inset-0 bg-black/40" onClick={onCancel} />
      <div className="relative bg-white rounded-xl shadow-xl p-6 w-full max-w-sm mx-4">
        <h3 className="font-semibold text-gray-900 mb-2">Item duplicado</h3>
        <p className="text-sm text-gray-600 mb-5">
          O produto <strong>{productCode}</strong> ({tipo}) já está na lista. O que deseja fazer?
        </p>
        <div className="space-y-2">
          <button onClick={onSum} className="w-full bg-blue-600 text-white rounded-lg py-2 text-sm font-medium hover:bg-blue-700 transition-colors">
            Somar quantidade
          </button>
          <button onClick={onNew} className="w-full bg-gray-100 text-gray-800 rounded-lg py-2 text-sm font-medium hover:bg-gray-200 transition-colors">
            Criar linha separada
          </button>
          <button onClick={onCancel} className="w-full text-gray-500 text-sm py-2 hover:text-gray-700">
            Cancelar
          </button>
        </div>
      </div>
    </div>
  )
}
