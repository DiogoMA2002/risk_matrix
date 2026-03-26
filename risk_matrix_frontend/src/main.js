/* eslint-disable */
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store/index' // <- import your store
import './assets/styles.css'
import axios from 'axios'
import { setupAxiosInterceptors } from './utils/tokenManager'

// Send cookies with every request (tokens are stored in HttpOnly cookies)
axios.defaults.withCredentials = true

// Setup 401 auto-refresh interceptor — pass router.push so tokenManager
// doesn't need to import the router (avoids circular dependency).
setupAxiosInterceptors(axios, (path) => router.push(path))

const app = createApp(App)

app.config.errorHandler = (err, instance, info) => {
  console.error(`[Vue Error] ${info}:`, err);
  store.commit('setError', err.message || 'Ocorreu um erro inesperado.');
};

app
  .use(router)
  .use(store)
  .mount('#app')
