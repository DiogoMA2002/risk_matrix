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

export default router
