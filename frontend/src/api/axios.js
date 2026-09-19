import axios from 'axios'

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
})

// Request Interceptor: sisipkan JWT jika ada
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('elms_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Response Interceptor: tangani format ApiResponse dari backend
apiClient.interceptors.response.use(
  (response) => {
    // Kembalikan response.data langsung (yang berisi { success, data, message })
    return response.data
  },
  (error) => {
    if (error.response) {
      if (error.response.status === 401) {
        localStorage.removeItem('elms_token')
        localStorage.removeItem('elms_user')
        if (window.location.pathname !== '/login') {
          window.location.href = '/login'
        }
      }
      // Kembalikan payload error dari backend jika ada
      const backendError = error.response.data?.error || {
        code: `HTTP_${error.response.status}`,
        message: error.response.data?.message || 'Terjadi kesalahan pada server',
      }
      return Promise.reject(backendError)
    }
    return Promise.reject({
      code: 'NETWORK_ERROR',
      message: 'Tidak dapat terhubung ke server backend',
    })
  }
)

export default apiClient
