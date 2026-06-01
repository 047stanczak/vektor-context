import api from '@/lib/axios'
import { ApiResponse, DivergenceQueryResponse, DivergenceRecord, DivergenceRecordRequest } from '@/types'

export async function queryProduct(params: { productCode?: number; barcode?: string; storeCode: number }) {
  const res = await api.get<ApiResponse<DivergenceQueryResponse>>('/divergence/query', { params })
  return res.data
}

export async function saveDivergences(items: DivergenceRecordRequest[]) {
  const res = await api.post<ApiResponse<DivergenceRecord[]>>('/divergence', items)
  return res.data
}

export async function fetchByDate(date: string) {
  const res = await api.get<ApiResponse<DivergenceRecord[]>>('/divergence', { params: { date } })
  return res.data
}

export async function updateDivergence(id: number, data: DivergenceRecordRequest) {
  const res = await api.put<ApiResponse<DivergenceRecord>>(`/divergence/${id}`, data)
  return res.data
}

export async function deleteDivergence(id: number) {
  await api.delete(`/divergence/${id}`)
}

export async function fetchSeparators() {
  const res = await api.get<ApiResponse<string[]>>('/separation-operations/separators')
  return res.data.data ?? []
}

export function getPdfUrl(date: string) {
  return `/vektor/api/divergence/report/pdf?date=${date}`
}
