export interface ApiResponse<T> {
  success: boolean
  status: number
  message: string
  data: T
}

export type Tipo = 'FALTA' | 'SOBRA' | 'SEM_NF'

export interface DivergenceQueryResponse {
  productCode: number
  productDescription: string
  productComplement: string
  barcode: string
  currentStock: number | null
  separatorName: string | null
}

export interface DivergenceRecordRequest {
  date: string
  storeCode: number
  productCode: number
  tipo: Tipo
  quantity: number
  currentStock: number | null
  separatorName: string
  observation: string | null
}

export interface DivergenceRecord {
  id: number
  date: string
  storeCode: number
  productCode: number
  productDescription: string | null
  productComplement: string | null
  barcode: string | null
  tipo: Tipo
  quantity: number
  currentStock: number | null
  separatorName: string | null
  observation: string | null
  createdAt: string
}

export interface DivergenceDraftItem {
  draftId: string
  date: string
  storeCode: number
  productCode: number
  productDescription: string | null
  productComplement: string | null
  tipo: Tipo
  quantity: number
  currentStock: number | null
  separatorName: string
  observation: string | null
}

export interface ImportJob {
  id: number
  fileName: string
  type: string
  status: 'PROCESSING' | 'SUCCESS' | 'ERROR'
  errorMessage: string | null
  startedAt: string
  finishedAt: string | null
}

export interface SeparationProduct {
  id: number
  separationOperationId: number
  productCode: number
  productDescription: string | null
  quantity: number
  quantityEmb: number | null
  storeCode: number | null
  status: string | null
  createdAt: string
}
