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
  { path: '/feedback-form', component: () => import('@/components/views/FeedbackForm.vue') }, // New route for feedback form
  {
    path: '/admin/edit-question/:questionId',
    name: 'EditQuestion',
    component: () => import('@/components/AdminDashboard/EditQuestionPage.vue'), // Route for editing questions
    meta: { requiresAuth: true, requiresAdmin: true } // Assuming admin routes need authentication
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const completedRiskInfo = localStorage.getItem('completedRiskInfo') === 'true';
  const completedRequirements = localStorage.getItem('completedRequirements') === 'true';
  const token = localStorage.getItem('jwt')
  const isAdminRoute = to.path.startsWith('/admin')

  if (isAdminRoute && !token) {
    return next('/login')
  }

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
