import api from '@/lib/axios'
import { SeparationProduct } from '@/types'

export async function fetchOldPending(): Promise<SeparationProduct[]> {
  const res = await api.get<SeparationProduct[]>('/old-pending')
  return res.data
}

export async function fetchOldPendingWithStock(): Promise<SeparationProduct[]> {
  const res = await api.get<SeparationProduct[]>('/old-pending-with-stock')
  return res.data
}
