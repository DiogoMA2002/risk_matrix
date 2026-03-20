/**
 * Auth state helper.
 *
 * JWTs now live in HttpOnly cookies (set by the server) and are never
 * accessible to JavaScript.  This module only tracks a non-sensitive
 * "isAdmin" / "isPublic" flag in localStorage so the router can make
 * synchronous navigation decisions.  All actual authentication is
 * enforced by the backend on every request.
 */
// Module-level promise used to deduplicate concurrent refresh attempts.
// If multiple requests fail with 401 simultaneously, only one refresh call
// is made; all callers wait for the same result.
let _refreshPromise = null

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
  // All concurrent callers share the same in-flight request so only one
  // refresh call is made regardless of how many 401s arrive together.
  async refreshToken() {
    if (_refreshPromise) {
      return _refreshPromise
    }

    _refreshPromise = (async () => {
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
      } finally {
        _refreshPromise = null
      }
    })()

    return _refreshPromise
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
      const status = error.response?.status

      // 401: server explicitly says unauthenticated — try refresh.
      // 403 + hasAdminToken: the localStorage flag says we're admin but the
      // server disagrees, which means the access token silently expired and
      // a stale public cookie produced a 403 instead of a 401. Try refresh.
      const shouldRefresh =
        !originalRequest._retry &&
        (status === 401 || (status === 403 && TokenManager.hasAdminToken()))

      if (shouldRefresh) {
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
