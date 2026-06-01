import { useState, useRef, ChangeEvent } from 'react'
import { useMutation } from '@tanstack/react-query'
import { toast } from 'sonner'
import { ImportJob } from '@/types'
import { uploadProducts, uploadSeparatedProducts, uploadSeparationOperations } from './api'
import { Upload, CheckCircle, XCircle, Loader2 } from 'lucide-react'

interface CardProps {
  title: string
  description: string
  filename: string
  onUpload: (file: File) => Promise<ImportJob>
}

function UploadCard({ title, description, filename, onUpload }: CardProps) {
  const inputRef = useRef<HTMLInputElement>(null)
  const [job, setJob] = useState<ImportJob | null>(null)

  const mut = useMutation({
    mutationFn: onUpload,
    onSuccess: (data) => {
      setJob(data)
      if (data.status === 'SUCCESS') toast.success(`${title}: importação concluída`)
      else if (data.status === 'PROCESSING') toast.info(`${title}: processando em background`)
      else toast.error(`${title}: ${data.errorMessage ?? 'Erro desconhecido'}`)
    },
    onError: (err: any) => toast.error(`${title}: ${err.response?.data?.message ?? 'Erro ao importar'}`),
  })

  function handleChange(e: ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0]
    if (!file) return
    setJob(null)
    mut.mutate(file)
    e.target.value = ''
  }

  return (
    <div className="bg-white rounded-xl border border-gray-200 p-5">
      <div className="flex items-start justify-between gap-3 mb-3">
        <div>
          <h3 className="font-medium text-gray-900">{title}</h3>
          <p className="text-xs text-gray-500 mt-0.5">{description}</p>
          <p className="text-xs text-gray-400 mt-0.5 font-mono">{filename}</p>
        </div>
        <div className="w-9 h-9 rounded-lg bg-blue-50 flex items-center justify-center flex-shrink-0">
          <Upload className="w-4 h-4 text-blue-600" />
        </div>
      </div>

      {job && (
        <div className={`mb-3 p-2.5 rounded-lg text-xs flex items-center gap-2 ${
          job.status === 'SUCCESS' ? 'bg-green-50 text-green-700' :
          job.status === 'ERROR' ? 'bg-red-50 text-red-700' :
          'bg-blue-50 text-blue-700'}`}>
          {job.status === 'SUCCESS' && <CheckCircle className="w-3.5 h-3.5 flex-shrink-0" />}
          {job.status === 'ERROR' && <XCircle className="w-3.5 h-3.5 flex-shrink-0" />}
          {job.status === 'PROCESSING' && <Loader2 className="w-3.5 h-3.5 flex-shrink-0 animate-spin" />}
          {job.status === 'SUCCESS' ? 'Importação concluída' :
           job.status === 'ERROR' ? (job.errorMessage ?? 'Erro na importação') :
           `Processando... (Job #${job.id})`}
        </div>
      )}

      <input ref={inputRef} type="file" accept=".txt,.csv" className="hidden" onChange={handleChange} />
      <button onClick={() => inputRef.current?.click()} disabled={mut.isPending}
        className="w-full flex items-center justify-center gap-2 bg-gray-900 text-white rounded-lg py-2 text-sm font-medium hover:bg-gray-800 disabled:opacity-50 transition-colors">
        {mut.isPending ? <><Loader2 className="w-4 h-4 animate-spin" /> Enviando...</> : <><Upload className="w-4 h-4" /> Selecionar arquivo</>}
      </button>
    </div>
  )
}

export default function UploadsPage() {
  return (
    <div className="max-w-2xl mx-auto space-y-5">
      <div>
        <h1 className="text-xl font-semibold text-gray-900">Importar arquivos</h1>
        <p className="text-sm text-gray-500 mt-1">Faça o upload diário dos arquivos exportados do ERP.</p>
      </div>

      <div className="grid gap-4 sm:grid-cols-1">
        <UploadCard
          title="Produtos e estoque"
          description="Importa cadastro de produtos e snapshot de estoque do CD."
          filename="relacao_produtos.txt"
          onUpload={uploadProducts}
        />
        <UploadCard
          title="Produtos separados"
          description="Importa as separações realizadas e cria operações automáticas."
          filename="relacao_produtos_separados.txt"
          onUpload={uploadSeparatedProducts}
        />
        <UploadCard
          title="Operações de separação"
          description="Importa os nomes dos separadores vinculados às transações."
          filename="relacao_documentos_separados.txt"
          onUpload={uploadSeparationOperations}
        />
      </div>

      <div className="bg-amber-50 border border-amber-200 rounded-xl p-4 text-sm text-amber-800">
        <strong>Ordem recomendada:</strong> importe sempre nessa sequência — Operações → Produtos separados → Produtos e estoque. Isso garante que os separadores sejam vinculados corretamente antes das transações.
      </div>
    </div>
  )
}
