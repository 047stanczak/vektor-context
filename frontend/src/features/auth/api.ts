import api from '@/lib/axios'
import { ApiResponse } from '@/types'

export async function loginApi(codeUser: string, password: string) {
  const res = await api.post<ApiResponse<null>>('/login', { codeUser, password })
  return res.data
}

export async function logoutApi() {
  const res = await api.post<ApiResponse<null>>('/logout')
  return res.data
}
