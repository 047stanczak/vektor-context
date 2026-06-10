import api from '@/lib/axios'

export interface RankingItem {
  name: string
  total: number
}

export async function fetchRankBySeparator(from: string, to: string): Promise<RankingItem[]> {
  const res = await api.get<RankingItem[]>('/divergence/ranking/separator', { params: { from, to } })
  return res.data
}

export async function fetchRankByProduct(from: string, to: string): Promise<RankingItem[]> {
  const res = await api.get<RankingItem[]>('/divergence/ranking/product', { params: { from, to } })
  return res.data
}