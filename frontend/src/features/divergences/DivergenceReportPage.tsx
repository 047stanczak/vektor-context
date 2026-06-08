import { useState } from 'react'
import { today } from '@/lib/utils'
import { getPdfUrl } from './api'
import { FileText, Download, ExternalLink, Info } from 'lucide-react'

export default function DivergenceReportPage() {
  const [date, setDate] = useState(today())
  const url = getPdfUrl(date)

  return (
    <div className="max-w-md mx-auto space-y-5 fade-in">
      <div>
        <h1 className="text-xl font-bold text-gray-900">Relatório PDF</h1>
        <p className="text-sm text-gray-400 mt-1">Gere o relatório de divergências por data.</p>
      </div>

      <div className="card p-5 space-y-4">
        <div>
          <label className="label">Data do relatório</label>
          <input type="date" className="field" value={date} onChange={(e) => setDate(e.target.value)} />
        </div>

        <div className="flex flex-col gap-2 pt-1">
          <a href={url} target="_blank" rel="noreferrer"
            className="btn-primary w-full">
            <ExternalLink className="w-4 h-4" /> Abrir PDF
          </a>
          <a href={url} download={`divergencias-${date}.pdf`}
            className="btn-secondary w-full">
            <Download className="w-4 h-4" /> Baixar PDF
          </a>
        </div>
      </div>

      <div className="flex items-start gap-3 p-4 rounded-2xl bg-blue-50 border border-blue-100">
        <Info className="w-4 h-4 text-blue-400 flex-shrink-0 mt-0.5" />
        <p className="text-xs text-blue-600">
          O relatório contém todas as divergências registradas na data selecionada, de todas as lojas.
        </p>
      </div>
    </div>
  )
}
