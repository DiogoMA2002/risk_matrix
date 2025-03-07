import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '../components/HomePage.vue'
import AdminDashboard from '../components/AdminDashboard.vue';
import QuestionarioPage from "../components/QuestionarioPage.vue"; // Importa a nova página



const routes = [
  { path: '/', component: HomePage },
  { path: '/admin', component: AdminDashboard },
  { path: "/questionario", component: QuestionarioPage } // Define a nova rota

]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
