import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './assets/styles.css'  // Ensure this path is correct

createApp(App).use(router).mount('#app')
