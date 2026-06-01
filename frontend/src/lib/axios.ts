import axios from 'axios'
import { toast } from 'sonner'

const api = axios.create({
  baseURL: '/vektor/api',
  withCredentials: true,
})

api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      window.location.href = '/vektor/login'
    }
    if (err.code === 'ERR_NETWORK') {
      toast.error('Sem conexão com o servidor. Verifique se o backend está rodando.')
    }
    return Promise.reject(err)
  }
)

export default api
