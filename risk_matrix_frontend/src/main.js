import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store/index' // <- import your store
import './assets/styles.css'

createApp(App)
  .use(router)
  .use(store)  // <- register it
  .mount('#app')
