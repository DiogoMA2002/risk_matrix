import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '../components/home.vue'
import QuestionsPage from '../components/questions.vue'
import ReportPage from '../components/report.vue'

const routes = [
  { path: '/', name: 'HomePage', component: HomePage },
  { path: '/questions', name: 'QuestionsPage', component: QuestionsPage },
  { path: '/report', name: 'ReportPage', component: ReportPage }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
