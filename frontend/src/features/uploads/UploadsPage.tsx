import { useState, useRef, ChangeEvent } from 'react'
import { useMutation } from '@tanstack/react-query'
import { toast } from 'sonner'
import { ImportJob } from '@/types'
import { uploadProducts, uploadSeparatedProducts, uploadSeparationOperations, uploadSeparationProducts } from './api'
import { Upload, CheckCircle, XCircle, Loader2, AlertCircle } from 'lucide-react'
import { PageHeader } from '@/components/ui/page-header'
import { Button } from '@/components/ui/button'

interface CardProps {
  title: string
  description: string
  filename: string
  step: number
  onUpload: (file: File) => Promise<ImportJob>
}

function UploadCard({ title, description, filename, step, onUpload }: CardProps) {
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
    <div className="card p-5 transition-all duration-200 hover:shadow-md">
      <div className="flex items-start gap-4">
        <div className="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-md bg-primary text-xs font-bold text-primary-foreground">
          {step}
        </div>
        <div className="flex-1 min-w-0">
          <div className="flex items-start justify-between gap-2">
            <div>
              <h3 className="font-semibold text-gray-900 text-sm">{title}</h3>
              <p className="text-xs text-gray-400 mt-0.5">{description}</p>
              <p className="text-xs mt-1 mono text-gray-300">{filename}</p>
            </div>
          </div>

          {job && (
            <div className={`mt-3 p-2.5 rounded-xl text-xs flex items-center gap-2 ${
              job.status === 'SUCCESS' ? 'bg-green-50 text-green-700 border border-green-100' :
              job.status === 'ERROR'   ? 'bg-red-50 text-red-700 border border-red-100' :
              'bg-blue-50 text-blue-700 border border-blue-100'}`}>
              {job.status === 'SUCCESS'    && <CheckCircle className="w-3.5 h-3.5 flex-shrink-0" />}
              {job.status === 'ERROR'      && <XCircle className="w-3.5 h-3.5 flex-shrink-0" />}
              {job.status === 'PROCESSING' && <Loader2 className="w-3.5 h-3.5 flex-shrink-0 animate-spin" />}
              {job.status === 'SUCCESS'   ? 'Importação concluída' :
               job.status === 'ERROR'     ? (job.errorMessage ?? 'Erro na importação') :
               `Processando... (Job #${job.id})`}
            </div>
          )}

          <input ref={inputRef} type="file" accept=".txt,.csv" className="hidden" onChange={handleChange} />
          <Button
            variant="accent"
            size="sm"
            onClick={() => inputRef.current?.click()}
            disabled={mut.isPending}
            className="mt-3"
          >
            {mut.isPending
              ? <><Loader2 className="w-3.5 h-3.5 animate-spin" /> Enviando...</>
              : <><Upload className="w-3.5 h-3.5" /> Selecionar arquivo</>}
          </Button>
        </div>
      </div>
    </div>
  )
}

export default function UploadsPage() {
  return (
    <div className="space-y-5 fade-in">
      <PageHeader
        title="Importar arquivos"
        description="Faça o upload dos arquivos exportados do ERP."
      />

      <div className="space-y-3">
        <UploadCard
          step={1}
          title="Operações de separação"
          description="Importa os nomes dos separadores vinculados às transações."
          filename="relacao_documentos_separados.txt"
          onUpload={uploadSeparationOperations}
        />
        <UploadCard
          step={2}
          title="Produtos separados"
          description="Importa as separações realizadas e cria operações automáticas."
          filename="relacao_produtos_separados.txt"
          onUpload={uploadSeparatedProducts}
        />
        <UploadCard
          step={3}
          title="Produtos e estoque"
          description="Importa cadastro de produtos e snapshot de estoque do CD."
          filename="relacao_produtos.txt"
          onUpload={uploadProducts}
        />
        <UploadCard
          step={4}
          title="Produtos de separação"
          description="Importa os produtos vinculados às operações de separação."
          filename="relacao_separation_products.txt"
          onUpload={uploadSeparationProducts}
        />
      </div>

      <div className="flex items-start gap-3 rounded-lg border border-amber-200 bg-amber-50 p-4">
        <AlertCircle className="w-4 h-4 text-amber-500 flex-shrink-0 mt-0.5" />
        <p className="text-xs text-amber-700">
          <strong>Ordem recomendada:</strong> siga sempre a sequência numérica acima para garantir que os separadores sejam vinculados corretamente antes das transações.
        </p>
      </div>
    </div>
  )
}
