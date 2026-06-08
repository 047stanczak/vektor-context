import api from '@/lib/axios'
import { ImportJob } from '@/types'

export async function fetchJobs(): Promise<ImportJob[]> {
  const res = await api.get<ImportJob[]>('/status')
  return res.data
}

export async function fetchJob(jobId: number): Promise<ImportJob> {
  const res = await api.get<ImportJob>(`/status/${jobId}`)
  return res.data
}
