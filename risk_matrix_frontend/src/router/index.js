import { createRouter, createWebHistory } from 'vue-router'
import { TokenManager } from '@/utils/tokenManager'

const routes = [
  { path: '/', component: () => import('@/components/views/HomePage.vue') },
  { path: '/risk-info', component: () => import('@/components/views/RiskMatrixInfo.vue') },
  { path: '/requirements', component: () => import('@/components/views/RequirementsPage.vue') },
  {
    path: '/questions/:questionnaireId/:category',
    name: 'Questionary',
    component: () => import('@/components/views/QuestionarioPage.vue')
  },
  { path: '/login', component: () => import('@/components/views/AdminLogin.vue') },
  { path: '/admin', component: () => import('@/components/views/AdminDashboard.vue'), meta: { requiresAdmin: true } },
  { path: '/user', component: () => import('@/components/views/UserManagement.vue'), meta: { requiresAdmin: true } },
  { path: '/category', name: 'CategoryList', component: () => import('@/components/views/CategoryList.vue') },
  { path: '/feedback-form', component: () => import('@/components/views/FeedbackForm.vue') },
  {
    path: '/admin/edit-question/:questionId',
    name: 'EditQuestion',
    component: () => import('@/components/AdminDashboard/EditQuestionPage.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/edit-questionnaire/:questionnaireId',
    name: 'EditQuestionnaire',
    component: () => import('@/components/AdminDashboard/EditQuestionnairePage.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const completedRiskInfo = localStorage.getItem('completedRiskInfo') === 'true';
  const completedRequirements = localStorage.getItem('completedRequirements') === 'true';

  const isAdminRoute = to.meta.requiresAdmin === true;

  const email = localStorage.getItem('userEmail');

  const requiresEmail = ['/risk-info', '/requirements', '/category'].includes(to.path) ||
    to.name === 'Questionary';
  if (requiresEmail && (!email || email.trim() === '')) {
    return next('/');
  }

  if (isAdminRoute && !TokenManager.hasAdminToken()) {
    TokenManager.clearTokens();
    console.log('Admin token invalid, redirecting to login.');
    return next('/login');
  }

  if (to.path === '/requirements' && !completedRiskInfo) {
    return next('/risk-info');
  }

  if ((to.path === '/category' || to.name === 'Questionary') && (!completedRiskInfo || !completedRequirements)) {
    if (!completedRiskInfo) return next('/risk-info');
    return next('/requirements');
  }

  next();
})

export default router;
