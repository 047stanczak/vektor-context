import api from '@/lib/axios'
import { ApiResponse, ImportJob } from '@/types'

export async function uploadProducts(file: File) {
  const fd = new FormData()
  fd.append('file', file)
  const res = await api.post<ImportJob>('/import/products', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
  return res.data
}

export async function uploadSeparatedProducts(file: File) {
  const fd = new FormData()
  fd.append('file', file)
  const res = await api.post<ImportJob>('/import/separated-products', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
  return res.data
}

export async function uploadSeparationOperations(file: File) {
  const fd = new FormData()
  fd.append('file', file)
  const res = await api.post<ImportJob>('/import/separation-operations', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
  return res.data
}

export async function uploadSeparationProducts(file: File) {
  const fd = new FormData()
  fd.append('file', file)
  const res = await api.post<ImportJob>('/import/separation-products', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
  return res.data
}
