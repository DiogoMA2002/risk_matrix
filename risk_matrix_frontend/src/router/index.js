import { createRouter, createWebHistory } from 'vue-router'
import RiskMatrixInfo from '../components/RiskMatrixInfo.vue'
import RequirementsPage from '../components/RequirementsPage.vue'
import QuestionarioPage from '../components/QuestionarioPage.vue'
import AdminDashboard from '../components/AdminDashboard.vue'
import HomePage from '@/components/HomePage.vue'
import CategoryList from '../components/CategoryList.vue'

const routes = [
  { path: '/', component: HomePage },
  { path: '/risk-info', component: RiskMatrixInfo },
  { path: '/requirements', component: RequirementsPage },
  {
    path: '/questions/:category',
    name: 'Questionary',
    component: QuestionarioPage
  },
    { path: '/admin', component: AdminDashboard },
  { path: '/category', component: CategoryList }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const completedRiskInfo = localStorage.getItem('completedRiskInfo') === 'true';
  const completedRequirements = localStorage.getItem('completedRequirements') === 'true';

  // If they're going to /requirements
  if (to.path === '/requirements') {
    // Check if they finished risk info
    if (!completedRiskInfo) {
      return next('/risk-info');
    }
  }

  // If they're going to the questionary
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


export default router
