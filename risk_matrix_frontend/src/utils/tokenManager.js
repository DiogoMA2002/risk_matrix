/**
 * Auth state helper.
 *
 * JWTs now live in HttpOnly cookies (set by the server) and are never
 * accessible to JavaScript.  This module only tracks a non-sensitive
 * "isAdmin" / "isPublic" flag in localStorage so the router can make
 * synchronous navigation decisions.  All actual authentication is
 * enforced by the backend on every request.
 */
export const TokenManager = {
  setAdmin() {
    localStorage.setItem('authRole', 'admin')
  },

  setPublic() {
    localStorage.setItem('authRole', 'public')
  },

  clearAuth() {
    localStorage.removeItem('authRole')
    // Clear public-user flow state so the next visitor starts fresh
    sessionStorage.removeItem('userEmail')
    sessionStorage.removeItem('completedRiskInfo')
    sessionStorage.removeItem('completedRequirements')
  },

  hasAdminToken() {
    return localStorage.getItem('authRole') === 'admin'
  },

  hasPublicToken() {
    return localStorage.getItem('authRole') === 'public'
  },

  // Refresh by calling the backend; cookies are sent automatically.
  async refreshToken() {
    try {
      const response = await fetch('/api/auth/refresh', {
        method: 'POST',
        credentials: 'include', // send HttpOnly cookies
        headers: { 'Content-Type': 'application/json' }
      })

      if (response.ok) {
        return true // new cookies have been set by the server
      }

      this.clearAuth()
      return false
    } catch {
      this.clearAuth()
      return false
    }
  },

  async logout() {
    try {
      await fetch('/api/auth/logout', {
        method: 'POST',
        credentials: 'include'
      })
    } finally {
      this.clearAuth()
    }
  }
}

// Axios interceptor — cookies are sent automatically via withCredentials;
// no Authorization header manipulation needed.
// `navigate` is a callback (e.g. `(path) => router.push(path)`) injected from main.js
// to avoid a circular import between this module and the router.
export function setupAxiosInterceptors(axios, navigate) {
  axios.defaults.withCredentials = true

  axios.interceptors.response.use(
    (response) => response,
    async (error) => {
      const originalRequest = error.config

      if (error.response?.status === 401 && !originalRequest._retry) {
        originalRequest._retry = true

        const refreshed = await TokenManager.refreshToken()

        if (refreshed) {
          return axios(originalRequest)
        } else {
          TokenManager.clearAuth()
          const path = window.location.pathname
          if (path !== '/login' && path !== '/') {
            navigate('/login')
          }
        }
      }

      return Promise.reject(error)
    }
  )
}
