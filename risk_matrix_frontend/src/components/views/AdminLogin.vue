<template>
    <div class="min-h-screen bg-gradient-to-br from-indigo-500 to-blue-200 font-sans">
      
      <!-- 🔙 Back to home header -->
      <div class="flex justify-between items-center p-4">
        <div class="flex items-center">
          <button
            @click="$router.push('/')"
            class="p-2 rounded-full bg-white bg-opacity-20 backdrop-blur-sm text-white hover:bg-opacity-30 transition-all duration-300 mr-4"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
            </svg>
          </button>
          <div>
            <h1 class="text-xl font-bold text-white">Login de Administrador</h1>
            <p class="text-sm text-white text-opacity-70">Acesso à Página Inicial</p>
          </div>
        </div>
  
        <div>
          <img src="@/assets/logoCCC.png" alt="Logo" class="h-10 object-contain" />
        </div>
      </div>
  
      <!-- 🔐 Login Card -->
      <div class="flex items-center justify-center px-4 py-8">
        <form
          @submit.prevent="login"
          class="bg-white bg-opacity-90 backdrop-blur-md p-8 rounded-xl shadow-2xl max-w-md w-full"
        >
          <h2 class="text-2xl font-bold text-center text-indigo-700 mb-4">Entrar no Sistema</h2>
  
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700">Usuário ou Email</label>
              <input
                v-model="username"
                type="text"
                placeholder="admin"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring focus:ring-indigo-300"
              />
            </div>
  
            <div>
              <label class="block text-sm font-medium text-gray-700">Senha</label>
              <input
                v-model="password"
                type="password"
                placeholder="••••••••"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring focus:ring-indigo-300"
              />
            </div>
  
            <button
              type="submit"
              class="w-full py-2 bg-indigo-600 text-white font-semibold rounded-lg hover:bg-indigo-700 transition"
            >
              Entrar
            </button>
  
            <p v-if="error" class="text-red-600 text-sm mt-2">{{ error }}</p>
          </div>
        </form>
      </div>
    </div>
  </template>
  
  <script>
  import axios from 'axios'
  
  export default {
    name: 'AdminLogin',
    data() {
      return {
        username: '',
        password: '',
        error: ''
      }
    },
    methods: {
      async login() {
        try {
          const res = await axios.post('/api/auth/login', {
            username: this.username,
            password: this.password
          })
  
          localStorage.setItem('jwt', res.data.token)
          this.$router.push('/admin') // redirect on success
        } catch (err) {
          this.error = 'Credenciais inválidas'
        }
      }
    }
  }
  </script>
  