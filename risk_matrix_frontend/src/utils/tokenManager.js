// Token management utilities
export const TokenManager = {
  // Get the appropriate token (admin or public)
  getToken() {
    const adminToken = localStorage.getItem('jwt')
    const publicToken = localStorage.getItem('publicToken')
    
    // Check if admin token exists and is valid
    if (adminToken && !this.isTokenExpired(adminToken)) {
      return adminToken
    }
    
    // Check if public token exists and is valid
    if (publicToken && !this.isPublicTokenExpired()) {
      return publicToken
    }
    
    return null
  },

  // Check if admin token is expired
  isTokenExpired(token) {
    if (!token) return true
    try {
      const parts = token.split('.')
      if (parts.length !== 3) return true

      const decoded = JSON.parse(atob(parts[1].replace(/-/g, '+').replace(/_/g, '/')))
      const exp = decoded.exp
      if (typeof exp !== 'number') return true

      return Math.floor(Date.now() / 1000) >= exp
    } catch (err) {
      console.error('Token error:', err)
      return true
    }
  },

  // Check if public token is expired
  isPublicTokenExpired() {
    const expiresAt = localStorage.getItem('tokenExpiresAt')
    if (!expiresAt) return true
    
    return Date.now() >= parseInt(expiresAt)
  },

  // Clear all tokens
  clearTokens() {
    localStorage.removeItem('jwt')
    localStorage.removeItem('publicToken')
    localStorage.removeItem('tokenExpiresAt')
  },

  // Clear only public tokens
  clearPublicTokens() {
    localStorage.removeItem('publicToken')
    localStorage.removeItem('tokenExpiresAt')
  },

  // Check if user has valid admin token
  hasAdminToken() {
    const adminToken = localStorage.getItem('jwt')
    return adminToken && !this.isTokenExpired(adminToken)
  },

  // Check if user has valid public token
  hasPublicToken() {
    const publicToken = localStorage.getItem('publicToken')
    return publicToken && !this.isPublicTokenExpired()
  }
}

// Axios interceptor to automatically add tokens to requests
export function setupAxiosInterceptors(axios) {
  axios.interceptors.request.use(
    (config) => {
      const token = TokenManager.getToken()
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
      return config
    },
    (error) => {
      return Promise.reject(error)
    }
  )

  axios.interceptors.response.use(
    (response) => response,
    (error) => {
      if (error.response?.status === 401) {
        // Token expired or invalid, clear tokens and redirect
        TokenManager.clearTokens()
        if (window.location.pathname !== '/') {
          window.location.href = '/'
        }
      }
      return Promise.reject(error)
    }
  )
} 