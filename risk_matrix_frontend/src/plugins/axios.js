import axios from 'axios'

// Adiciona o token JWT automaticamente em todas as requisições
axios.interceptors.request.use(
  config => {
    const token = localStorage.getItem('jwt')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

export default axios
