/* eslint-disable */
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store/index' // <- import your store
import './assets/styles.css'
import axios from 'axios'
import { setupAxiosInterceptors } from './utils/tokenManager'

// Setup axios interceptors for automatic token handling
setupAxiosInterceptors(axios)

createApp(App)
  .use(router)
  .use(store)  // <- register it
  .mount('#app')
