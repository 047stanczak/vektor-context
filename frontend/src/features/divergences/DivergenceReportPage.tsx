import { useState } from 'react'
import { today } from '@/lib/utils'
import { getPdfUrl } from './api'
import { FileText, Download, ExternalLink } from 'lucide-react'

export default function DivergenceReportPage() {
  const [date, setDate] = useState(today())

  const url = getPdfUrl(date)

  return (
    <div className="max-w-md mx-auto space-y-5">
      <h1 className="text-xl font-semibold text-gray-900">Relatório PDF</h1>

      <div className="bg-white rounded-xl border border-gray-200 p-5 space-y-4">
        <div>
          <label className="label">Data do relatório</label>
          <input type="date" className="field" value={date} onChange={(e) => setDate(e.target.value)} />
        </div>

        <div className="flex flex-col gap-2">
          <a href={url} target="_blank" rel="noreferrer"
            className="flex items-center justify-center gap-2 bg-blue-600 text-white rounded-lg py-2.5 text-sm font-medium hover:bg-blue-700 transition-colors">
            <ExternalLink className="w-4 h-4" /> Abrir PDF
          </a>
          <a href={url} download={`divergencias-${date}.pdf`}
            className="flex items-center justify-center gap-2 bg-gray-100 text-gray-800 rounded-lg py-2.5 text-sm font-medium hover:bg-gray-200 transition-colors">
            <Download className="w-4 h-4" /> Baixar PDF
          </a>
        </div>

        <div className="flex items-start gap-2 text-xs text-gray-500 bg-gray-50 rounded-lg p-3">
          <FileText className="w-4 h-4 flex-shrink-0 mt-0.5" />
          O relatório contém todas as divergências registradas na data selecionada, de todas as lojas.
        </div>
      </div>
    </div>
  )
}
