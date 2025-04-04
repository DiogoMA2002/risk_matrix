<template>
    <div class="bg-white bg-opacity-90 backdrop-blur-sm rounded-xl shadow-md p-6 mb-8">
        <h2 class="text-2xl font-bold mb-6 text-blue-800 flex items-center space-x-3">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-7 w-7" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-4-3a4 4 0 100-8 4 4 0 000 8zm0 2c-4 0-6 2-6 4v1h12v-1c0-2-2-4-6-4z" />
            </svg>
            <span class="leading-tight">Registar Novo Administrador</span>
        </h2>
  
      <div class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700">Username</label>
          <input
            v-model="username"
            type="text"
            placeholder="Digite o username"
            class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 transition"
          />
        </div>
  
        <div>
          <label class="block text-sm font-medium text-gray-700">Email</label>
          <input
            v-model="email"
            type="email"
            placeholder="email@exemplo.com"
            class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 transition"
          />
        </div>
  
        <div>
          <label class="block text-sm font-medium text-gray-700">Senha</label>
          <input
            v-model="password"
            type="password"
            placeholder="••••••••"
            class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 transition"
          />
        </div>
  
        <button
          @click="register"
          class="px-6 py-3 bg-blue-600 text-white rounded-lg shadow-md hover:bg-blue-700 transition-all duration-300 flex items-center"
        >
          <span>Registrar</span>
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
          </svg>
        </button>
  
        <p v-if="message" :class="['text-sm mt-2', isError ? 'text-red-600' : 'text-green-600']">{{ message }}</p>
      </div>
    </div>
  </template>
  
  <script>
  /* eslint-disable */
  import axios from 'axios'
  
  export default {
    name: "RegisterAdmin",
    data() {
      return {
        username: '',
        email: '',
        password: '',
        message: '',
        isError: false,
      }
    },
    methods: {
        async register() {
            this.message = ''
            this.isError = false

            if (!this.username || !this.email || !this.password) {
            this.message = 'Preencha todos os campos.'
            this.isError = true
            return
            }

            const token = localStorage.getItem('jwt')
            if (!token) {
            this.message = 'Você não está autenticado.'
            this.isError = true
            return
            }

            try {
            const response = await axios.post('/api/auth/register', {
                username: this.username,
                email: this.email,
                password: this.password
            }, {
                headers: {
                Authorization: `Bearer ${token}`
                }
            })

            this.message = 'Administrador criado com sucesso!'
            this.username = ''
            this.email = ''
            this.password = ''
            } catch (error) {
            this.message = error.response?.data || 'Erro ao registrar administrador.'
            this.isError = true
            }
        }
        }
  }
  </script>
  