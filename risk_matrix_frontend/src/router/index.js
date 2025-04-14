import { createRouter, createWebHistory } from 'vue-router'
// Remove static imports
// import RiskMatrixInfo from '../components/RiskMatrixInfo.vue'
// import RequirementsPage from '../components/RequirementsPage.vue'
// import QuestionarioPage from '../components/QuestionarioPage.vue'
// import AdminDashboard from '../components/views/AdminDashboard.vue'
// import HomePage from '@/components/HomePage.vue'
// import CategoryList from '../components/CategoryList.vue'
// import FeedbackForm from '@/components/FeedbackForm.vue' // Import the new component
// import AdminLogin from '../components/views/AdminLogin.vue'
// import UserManagement from '../components/views/UserManagement.vue'

const routes = [
  { path: '/', component: () => import('@/components/views/HomePage.vue') },
  { path: '/risk-info', component: () => import('@/components/views/RiskMatrixInfo.vue') },
  { path: '/requirements', component: () => import('@/components/views/RequirementsPage.vue') },
  {
    path: '/questions/:questionnaireId/:category',
    name: 'Questionary',
    component: () => import('@/components/views/QuestionarioPage.vue')
  },
  {
    path: '/login',
    component: () => import('@/components/views/AdminLogin.vue')
  },
  { path: '/admin', component: () => import('@/components/views/AdminDashboard.vue') },
  { path: '/user', component: () => import('@/components/views/UserManagement.vue') },
  { path: '/category', name: 'CategoryList', component: () => import('@/components/views/CategoryList.vue') },
  { path: '/feedback-form', component: () => import('@/components/views/FeedbackForm.vue') } // New route for feedback form
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('jwt'); // Get token first

  // Helper function to check token expiration
  function isTokenExpired(token) {
    if (!token) return true;
    try {
      // Decode the payload part of the JWT (the second part)
      const payloadBase64 = token.split('.')[1];
      if (!payloadBase64) return true; // Invalid token format

      // Base64 URL decode (replace URL-safe characters)
      const decodedJson = atob(payloadBase64.replace(/-/g, '+').replace(/_/g, '/'));
      const decoded = JSON.parse(decodedJson);

      const exp = decoded.exp; // Expiration timestamp in seconds
      if (typeof exp !== 'number') {
        console.warn("Token 'exp' claim is missing or not a number.");
        return true; // Treat as invalid/expired if 'exp' is missing or wrong type
      }

      const now = Math.floor(Date.now() / 1000); // Current time in seconds
      return now >= exp; // Check if current time is past expiration time
    } catch (error) {
      console.error("Error decoding/checking token expiration:", error);
      return true; // Treat any decoding errors as an invalid/expired token
    }
  }

  const isAdminRoute = to.path.startsWith('/admin');

  // Check admin routes: require a valid, non-expired token
  if (isAdminRoute) {
    if (!token || isTokenExpired(token)) {
      // If no token or token is expired, clear potentially bad token and redirect to login
      localStorage.removeItem('jwt');
      console.log('JWT missing or expired, redirecting to login.');
      return next('/login');
    }
    // If token exists and is valid, allow navigation to admin route (falls through to final next())
  }

  // Existing checks for non-admin routes (keep these as they are)
  const completedRiskInfo = localStorage.getItem('completedRiskInfo') === 'true';
  const completedRequirements = localStorage.getItem('completedRequirements') === 'true';
  // const token = localStorage.getItem('jwt') // Token retrieval moved up
  // const isAdminRoute = to.path.startsWith('/admin') // isAdminRoute check moved up

  // if (isAdminRoute && !token) { // This specific check is now covered by the new logic above
  //   return next('/login')
  // }

  if (to.path === '/requirements') {
    if (!completedRiskInfo) {
      return next('/risk-info');
    }
  }

  if (to.path === '/category' || to.name === 'Questionary') {
    if (!completedRiskInfo) {
      return next('/risk-info');
    }
    if (!completedRequirements) {
      return next('/requirements');
    }
  }

  next();
});

export default router;
