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

  // Check if refresh token is expired
  isRefreshTokenExpired() {
    const adminRefreshExpiresAt = localStorage.getItem('adminRefreshExpiresAt')
    const publicRefreshExpiresAt = localStorage.getItem('publicRefreshExpiresAt')
    
    if (adminRefreshExpiresAt && Date.now() < parseInt(adminRefreshExpiresAt)) {
      return false
    }
    
    if (publicRefreshExpiresAt && Date.now() < parseInt(publicRefreshExpiresAt)) {
      return false
    }
    
    return true
  },

  // Refresh token function
  async refreshToken() {
    try {
      const adminRefreshToken = localStorage.getItem('adminRefreshToken')
      const publicRefreshToken = localStorage.getItem('publicRefreshToken')
      
      if (adminRefreshToken && !this.isRefreshTokenExpired()) {
        // Refresh admin token
        const response = await fetch('/api/auth/refresh', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ refreshToken: adminRefreshToken })
        })

        if (response.ok) {
          const data = await response.json()
          localStorage.setItem('jwt', data.token)
          localStorage.setItem('adminRefreshToken', data.refreshToken)
          localStorage.setItem('adminRefreshExpiresAt', (Date.now() + data.expiresIn).toString())
          return data.token
        }
      } else if (publicRefreshToken && !this.isRefreshTokenExpired()) {
        // Refresh public token
        const response = await fetch('/api/auth/refresh', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ refreshToken: publicRefreshToken })
        })

        if (response.ok) {
          const data = await response.json()
          localStorage.setItem('publicToken', data.token)
          localStorage.setItem('publicRefreshToken', data.refreshToken)
          localStorage.setItem('tokenExpiresAt', data.expiresAt.toString())
          localStorage.setItem('publicRefreshExpiresAt', data.refreshExpiresAt.toString())
          return data.token
        }
      }
      
      return null
    } catch (error) {
      console.error('Error refreshing token:', error)
      return null
    }
  },

  // Clear all tokens
  clearTokens() {
    localStorage.removeItem('jwt')
    localStorage.removeItem('adminRefreshToken')
    localStorage.removeItem('adminRefreshExpiresAt')
    localStorage.removeItem('publicToken')
    localStorage.removeItem('publicRefreshToken')
    localStorage.removeItem('tokenExpiresAt')
    localStorage.removeItem('publicRefreshExpiresAt')
  },

  // Clear only public tokens
  clearPublicTokens() {
    localStorage.removeItem('publicToken')
    localStorage.removeItem('publicRefreshToken')
    localStorage.removeItem('tokenExpiresAt')
    localStorage.removeItem('publicRefreshExpiresAt')
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
    async (error) => {
      const originalRequest = error.config

      if (error.response?.status === 401 && !originalRequest._retry) {
        originalRequest._retry = true

        // Try to refresh the token
        const newToken = await TokenManager.refreshToken()
        
        if (newToken) {
          // Retry the original request with the new token
          originalRequest.headers.Authorization = `Bearer ${newToken}`
          return axios(originalRequest)
        } else {
          // Refresh failed, clear tokens and redirect
          TokenManager.clearTokens()
          if (window.location.pathname !== '/') {
            window.location.href = '/'
          }
        }
      }
      
      return Promise.reject(error)
    }
  )
} 