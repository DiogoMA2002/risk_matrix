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
            required
            placeholder="email@exemplo.com"
            class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 transition"
          />
          <p v-if="email && !isEmailValid" class="text-red-500 text-xs mt-1">Formato de email invalido.</p>
        </div>
  
        <div>
          <label class="block text-sm font-medium text-gray-700">Senha</label>
          <input
            v-model="password"
            type="password"
            required
            minlength="8"
            placeholder="••••••••"
            class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 transition"
          />
          <div v-if="password" class="mt-2">
            <div class="flex gap-1 mb-1">
              <div v-for="i in 4" :key="i" class="h-1.5 flex-1 rounded-full transition-all duration-300"
                :class="i <= passwordStrength ? strengthColors[passwordStrength] : 'bg-gray-200'"></div>
            </div>
            <p class="text-xs" :class="strengthTextColors[passwordStrength]">{{ strengthLabel }}</p>
            <ul class="text-xs text-gray-500 mt-1 space-y-0.5">
              <li :class="password.length >= 8 ? 'text-green-600' : ''">Min. 8 caracteres</li>
              <li :class="/[A-Z]/.test(password) ? 'text-green-600' : ''">Uma letra maiuscula</li>
              <li :class="/[a-z]/.test(password) ? 'text-green-600' : ''">Uma letra minuscula</li>
              <li :class="/[0-9]/.test(password) ? 'text-green-600' : ''">Um digito</li>
              <li :class="/[^A-Za-z0-9]/.test(password) ? 'text-green-600' : ''">Um caractere especial</li>
            </ul>
          </div>
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
        strengthColors: { 1: 'bg-red-500', 2: 'bg-orange-400', 3: 'bg-yellow-400', 4: 'bg-green-500' },
        strengthTextColors: { 1: 'text-red-600', 2: 'text-orange-500', 3: 'text-yellow-600', 4: 'text-green-600' },
      }
    },
    computed: {
      isEmailValid() {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.email);
      },
      passwordStrength() {
        let score = 0;
        if (this.password.length >= 8) score++;
        if (/[A-Z]/.test(this.password) && /[a-z]/.test(this.password)) score++;
        if (/[0-9]/.test(this.password)) score++;
        if (/[^A-Za-z0-9]/.test(this.password)) score++;
        return score;
      },
      strengthLabel() {
        const labels = { 0: '', 1: 'Fraca', 2: 'Razoavel', 3: 'Boa', 4: 'Forte' };
        return labels[this.passwordStrength];
      },
      isPasswordValid() {
        return this.password.length >= 8 &&
          /[A-Z]/.test(this.password) &&
          /[a-z]/.test(this.password) &&
          /[0-9]/.test(this.password) &&
          /[^A-Za-z0-9]/.test(this.password);
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

            if (!this.isEmailValid) {
              this.message = 'Formato de email invalido.'
              this.isError = true
              return
            }

            if (!this.isPasswordValid) {
              this.message = 'A senha deve ter no minimo 8 caracteres, incluindo maiuscula, minuscula, digito e caractere especial.'
              this.isError = true
              return
            }

            try {
            await axios.post('/api/auth/register', {
                username: this.username,
                email: this.email,
                password: this.password
            })

            this.message = 'Administrador criado com sucesso!'
            this.username = ''
            this.email = ''
            this.password = ''
            } catch (error) {
              if (error.response?.data?.validationErrors) {
                // Vai buscar as mensagens de validação
                const errors = Object.values(error.response.data.validationErrors).flat()
                this.message = errors.join(', ') // Junta todas num só texto
              } else if (error.response?.data?.message) {
                this.message = error.response.data.message
              } else {
                this.message = 'Erro ao registrar administrador.'
              }
              this.isError = true
            }
        }
        }
  }
  </script>
  