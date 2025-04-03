import { createRouter, createWebHistory } from 'vue-router'
import RiskMatrixInfo from '../components/RiskMatrixInfo.vue'
import RequirementsPage from '../components/RequirementsPage.vue'
import QuestionarioPage from '../components/QuestionarioPage.vue'
import AdminDashboard from '../components/views/AdminDashboard.vue'
import HomePage from '@/components/HomePage.vue'
import CategoryList from '../components/CategoryList.vue'
import FeedbackForm from '@/components/FeedbackForm.vue' // Import the new component
import AdminLogin from '../components/views/AdminLogin.vue'

const routes = [
  { path: '/', component: HomePage },
  { path: '/risk-info', component: RiskMatrixInfo },
  { path: '/requirements', component: RequirementsPage },
  {
    path: '/questions/:questionnaireId/:category',
    name: 'Questionary',
    component: QuestionarioPage
  },
  {
    path: '/login',
    component: AdminLogin
  },
  { path: '/admin', component: AdminDashboard },
  { path: '/category', component: CategoryList },
  { path: '/feedback-form', component: FeedbackForm } // New route for feedback form
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
