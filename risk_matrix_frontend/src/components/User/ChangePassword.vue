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
        <div v-if="newPassword" class="mt-2">
          <div class="flex gap-1 mb-1">
            <div v-for="i in 4" :key="i" class="h-1.5 flex-1 rounded-full transition-all duration-300"
              :class="i <= passwordStrength ? strengthColors[passwordStrength] : 'bg-gray-200'"></div>
          </div>
          <p class="text-xs" :class="strengthTextColors[passwordStrength]">{{ strengthLabel }}</p>
          <ul class="text-xs text-gray-500 mt-1 space-y-0.5">
            <li :class="newPassword.length >= 8 ? 'text-green-600' : ''">Min. 8 caracteres</li>
            <li :class="/[A-Z]/.test(newPassword) ? 'text-green-600' : ''">Uma letra maiúscula</li>
            <li :class="/[a-z]/.test(newPassword) ? 'text-green-600' : ''">Uma letra minúscula</li>
            <li :class="/[0-9]/.test(newPassword) ? 'text-green-600' : ''">Um dígito</li>
            <li :class="/[^A-Za-z0-9]/.test(newPassword) ? 'text-green-600' : ''">Um caractere especial</li>
          </ul>
        </div>
      </div>

      <div>
        <label class="block text-sm font-medium text-gray-700">Confirmar Nova Senha</label>
        <input
          v-model="confirmPassword"
          type="password"
          placeholder="••••••••"
          class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 transition"
        />
        <p v-if="confirmPassword && newPassword !== confirmPassword" class="text-red-500 text-xs mt-1">
          As senhas não coincidem.
        </p>
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
import axios from 'axios'

export default {
  name: "ChangePassword",
  data() {
    return {
      oldPassword: '',
      newPassword: '',
      confirmPassword: '',
      message: '',
      isError: false,
      strengthColors: { 1: 'bg-red-500', 2: 'bg-orange-400', 3: 'bg-yellow-400', 4: 'bg-green-500' },
      strengthTextColors: { 1: 'text-red-600', 2: 'text-orange-500', 3: 'text-yellow-600', 4: 'text-green-600' },
    }
  },
  computed: {
    passwordStrength() {
      let score = 0;
      if (this.newPassword.length >= 8) score++;
      if (/[A-Z]/.test(this.newPassword) && /[a-z]/.test(this.newPassword)) score++;
      if (/[0-9]/.test(this.newPassword)) score++;
      if (/[^A-Za-z0-9]/.test(this.newPassword)) score++;
      return score;
    },
    strengthLabel() {
      const labels = { 0: '', 1: 'Fraca', 2: 'Razoável', 3: 'Boa', 4: 'Forte' };
      return labels[this.passwordStrength];
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
        await axios.post('/api/auth/change-password', {
          oldPassword: this.oldPassword,
          newPassword: this.newPassword,
          confirmNewPassword: this.confirmPassword
        })

        this.message = 'Senha alterada com sucesso!'
        this.oldPassword = ''
        this.newPassword = ''
        this.confirmPassword = ''
      } catch (error) {
        const data = error.response?.data

        if (data) {
          if (typeof data === 'string') {
            this.message = data
          } else if (data.validationErrors) {
            const errors = Object.values(data.validationErrors).flat()
            this.message = errors.join(', ')
          } else if (data.message) {
            this.message = data.message
          } else {
            this.message = "Erro ao alterar senha."
          }
        } else {
          this.message = "Erro ao alterar senha."
        }

        this.isError = true
      }
    }
  }
}
</script>
