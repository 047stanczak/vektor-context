import api from '@/lib/axios'
import { CountingItem } from '@/types'

export async function fetchBrandByProduct(productCode: number): Promise<string> {
  const res = await api.get<string>('/counting/brand-by-product', { params: { productCode } })
  return res.data
}

export function getPdfUrl(brand: string) {
  return `/vektor/api/counting/report/pdf?brand=${encodeURIComponent(brand)}`
}

export async function fetchDepartments(): Promise<string[]> {
  const res = await api.get<string[]>('/counting/departments')
  return res.data
}

export async function fetchByDepartment(department: string): Promise<CountingItem[]> {
  const res = await api.get<CountingItem[]>('/counting/by-department', { params: { department } })
  return res.data
}

export async function fetchBrands(): Promise<string[]> {
  const res = await api.get<string[]>('/counting/brands')
  return res.data
}

export async function fetchByBrand(brand: string): Promise<CountingItem[]> {
  const res = await api.get<CountingItem[]>('/counting/by-brand', { params: { brand } })
  return res.data
}

export async function fetchByProduct(productCode: number): Promise<CountingItem[]> {
  const res = await api.get<CountingItem[]>('/counting/by-product', { params: { productCode } })
  return res.data
}
