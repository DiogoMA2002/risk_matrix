<template>
    <div class="bg-white bg-opacity-90 backdrop-blur-sm rounded-xl shadow-md p-6 mb-8">
      <h2 class="text-2xl font-bold mb-6 text-blue-800 flex items-center space-x-3">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 11c0 1.104.896 2 2 2s2-.896 2-2m-4 4h4m-4 0a4 4 0 01-4-4m0-4a4 4 0 014-4m0 0v.01M6 8v.01M6 20h12" />
        </svg>
        <span>Alterar Senha</span>
      </h2>
  
      <div class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700">Senha Atual</label>
          <input
            v-model="oldPassword"
            type="password"
            placeholder="••••••••"
            class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 transition"
          />
        </div>
  
        <div>
          <label class="block text-sm font-medium text-gray-700">Nova Senha</label>
          <input
            v-model="newPassword"
            type="password"
            placeholder="••••••••"
            class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 transition"
          />
        </div>
  
        <div>
          <label class="block text-sm font-medium text-gray-700">Confirmar Nova Senha</label>
          <input
            v-model="confirmPassword"
            type="password"
            placeholder="••••••••"
            class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 transition"
          />
        </div>
  
        <button
          @click="changePassword"
          class="px-6 py-3 bg-blue-600 text-white rounded-lg shadow-md hover:bg-blue-700 transition-all duration-300 flex items-center"
        >
          <span>Alterar Senha</span>
        </button>
  
        <p v-if="message" :class="['text-sm mt-2', isError ? 'text-red-600' : 'text-green-600']">{{ message }}</p>
      </div>
    </div>
  </template>
  
  <script>
 /* eslint-disable */
import axios from '@/plugins/axios'

// Apenas limpa o prefixo da mensagem de erro do Spring
function formatValidationMessage(message) {
  if (message.startsWith("Validation failed:")) {
    const cleaned = message.replace("Validation failed:", "").trim();
    return cleaned
      .split(";")
      .map(m => {
        const trimmed = m.trim();
        const spaceIndex = trimmed.indexOf(" ");
        return spaceIndex !== -1 ? trimmed.slice(spaceIndex + 1) : trimmed;
      })
      .filter(Boolean)
      .join(" | ");
  }

  return message;
}

export default {
  name: "ChangePassword",
  data() {
    return {
      oldPassword: '',
      newPassword: '',
      confirmPassword: '',
      message: '',
      isError: false
    }
  },
  methods: {
    async changePassword() {
      this.message = ''
      this.isError = false

      if (!this.oldPassword || !this.newPassword || !this.confirmPassword) {
        this.message = 'Por favor, preencha todos os campos.'
        this.isError = true
        return
      }

      if (this.newPassword !== this.confirmPassword) {
        this.message = 'A nova senha e a confirmação não coincidem.'
        this.isError = true
        return
      }

      try {
        const token = localStorage.getItem('jwt')
        await axios.post('/api/auth/change-password', {
          oldPassword: this.oldPassword,
          newPassword: this.newPassword,
          confirmNewPassword: this.confirmPassword
        }, {
          headers: {
            Authorization: `Bearer ${token}`
          }
        })

        this.message = 'Senha alterada com sucesso!'
        this.oldPassword = ''
        this.newPassword = ''
        this.confirmPassword = ''
      } catch (error) {
        const data = error.response?.data
        if (data && data.message) {
          this.message = formatValidationMessage(data.message)
        } else {
          this.message = "Erro ao alterar senha."
        }
        this.isError = true
      }
    }
  }
}

  </script>
  