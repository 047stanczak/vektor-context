import { useState } from 'react'
import { today } from '@/lib/utils'
import { getPdfUrl } from './api'
import { Download, ExternalLink, Info } from 'lucide-react'
import { PageHeader } from '@/components/ui/page-header'

export default function DivergenceReportPage() {
  const [date, setDate] = useState(today())
  const url = getPdfUrl(date)

  return (
    <div className="space-y-5 fade-in max-w-md">
      <PageHeader
        title="Relatório PDF"
        description="Gere o relatório de divergências por data."
      />

      <div className="card p-5 space-y-4">
        <div>
          <label className="label">Data do relatório</label>
          <input type="date" className="field" value={date} onChange={(e) => setDate(e.target.value)} />
        </div>

        <div className="flex flex-col gap-2 pt-1">
          <a href={url} target="_blank" rel="noreferrer" className="btn-primary w-full">
            <ExternalLink className="w-4 h-4" /> Abrir PDF
          </a>
          <a href={url} download={`divergencias-${date}.pdf`} className="btn-secondary w-full">
            <Download className="w-4 h-4" /> Baixar PDF
          </a>
        </div>
      </div>

      <div className="flex items-start gap-3 rounded-lg border border-blue-200 bg-blue-50 p-4">
        <Info className="w-4 h-4 text-blue-500 flex-shrink-0 mt-0.5" />
        <p className="text-xs text-blue-700">
          O relatório contém todas as divergências registradas na data selecionada, de todas as lojas.
        </p>
      </div>
    </div>
  )
}
