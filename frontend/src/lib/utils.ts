import { clsx, type ClassValue } from 'clsx'
import { twMerge } from 'tailwind-merge'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export function detectInputMode(value: string): 'productCode' | 'barcode' {
  if (/[a-zA-Z]/.test(value)) return 'barcode'
  if (value.length >= 8) return 'barcode'
  return 'productCode'
}

export function formatDate(iso: string) {
  return new Date(iso + 'T00:00:00').toLocaleDateString('pt-BR')
}

export function today() {
  return new Date().toISOString().slice(0, 10)
}

export function formatNum(n: number | null | undefined) {
  if (n == null) return '—'
  return Number.isInteger(n) ? String(n) : n.toFixed(2).replace('.', ',')
}

export function tipoBadgeClass(tipo: string) {
  if (tipo === 'FALTA') return 'bg-red-100 text-red-700'
  if (tipo === 'SOBRA') return 'bg-green-100 text-green-700'
  if (tipo === 'SEM_NF') return 'bg-amber-100 text-amber-700'
  return 'bg-gray-100 text-gray-700'
}

const RECENT_KEY = 'vektor_recent_separators'

export function getRecentSeparators(): string[] {
  try {
    return JSON.parse(localStorage.getItem(RECENT_KEY) || '[]')
  } catch {
    return []
  }
}

export function addRecentSeparator(name: string) {
  const list = getRecentSeparators().filter((s) => s !== name)
  list.unshift(name)
  localStorage.setItem(RECENT_KEY, JSON.stringify(list.slice(0, 20)))
}
