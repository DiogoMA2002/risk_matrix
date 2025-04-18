<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-600 to-indigo-100 font-sans">
    <!-- Admin access button -->
    <div class="absolute top-4 right-4">
      <button
        @click="goToAdmin"
        class="px-4 py-2 bg-white bg-opacity-20 backdrop-blur-sm text-white rounded-md font-medium transition-bg duration-300 hover:bg-opacity-30 shadow-sm flex items-center space-x-1"
      >
        <span>Painel de Admin</span>
        <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
        </svg>
      </button>
    </div>

    <!-- Logo and branding -->
    <div class="w-full flex justify-center pt-10">
      <div class="flex items-center space-x-3">
        <img src="@/assets/logoCCC.webp" alt="Logo" class="h-16 object-contain" loading="lazy" />
      </div>
    </div>

    <!-- Main content card -->
    <div class="flex flex-col items-center justify-center px-4 py-12">
      <div
        class="bg-white bg-opacity-90 backdrop-blur-md rounded-xl shadow-xl p-8 w-full max-w-md transition-shadow duration-500 hover:shadow-2xl"
      >
        <h1 class="text-3xl font-bold text-blue-600 mb-2">Matriz de Risco</h1>
        <p class="text-gray-500 mb-8">Avalie e gerencie os seus riscos de forma eficiente</p>

        <div class="space-y-6">
          <div class="space-y-1">
            <label for="email" class="block text-sm font-medium text-gray-700">Email</label>
            <div class="relative">
              <input
                id="email"
                ref="emailInput"
                type="email"
                placeholder="email@email.com"
                class="w-full px-4 py-3 pl-10 border rounded-lg transition-all duration-300 focus:outline-none
                 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 border-gray-200"
              />
              <svg class="h-5 w-5 text-gray-400 absolute left-3 top-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
              </svg>
            </div>
            <p v-if="emailError" class="text-red-600 text-sm mt-1">{{ emailError }}</p>
          </div>

          <button
            @click="proceed"
            class="w-full px-4 py-3 bg-blue-600 text-white rounded-lg font-medium transition-colors duration-300 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 shadow-md hover:shadow-lg"
          >
            Prosseguir
          </button>
        </div>

        <div class="mt-8 pt-6 border-t border-gray-100 flex justify-center space-x-4 text-gray-400 text-xs">
          <div class="flex items-center">
            <svg class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
            </svg>
            Dados seguros
          </div>
        </div>
      </div>
    </div>

    <!-- Help Button -->
    <div class="fixed bottom-6 right-6">
      <button @click="goToFeedbackForm" class="p-4 bg-white rounded-full shadow-lg hover:shadow-xl transition-shadow duration-300 text-blue-600">
        <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M8.228 9c.549-1.165 2.03-2 3.772-2
            2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006
            2.907-.542.104-.994.54-.994 1.093m0 3h.01M21
            12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      </button>
    </div>

    <!-- Logos -->
    <div class="fixed bottom-6 left-6">
      <img src="@/assets/LogoFinanciamento.webp" alt="Logo C-Network" class="h-12 object-contain" loading="lazy" />
    </div>
    <div class="fixed bottom-6 left-1/2 transform -translate-x-1/2">
      <img src="@/assets/Barra-PRR-RP-EU-1024x148.webp" alt="Financiamento Logo" class="h-12 object-contain" loading="lazy" />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const emailInput = ref(null)
const emailError = ref('')

function isValidEmail(email) {
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return re.test(email.toLowerCase())
}

function proceed() {
  const email = emailInput.value?.value || ''
  if (!email || !isValidEmail(email)) {
    emailError.value = 'Informe um email válido'
    return
  }

  emailError.value = ''
  localStorage.setItem('userEmail', email)
  if (route.path !== '/risk-info') {
    router.push('/risk-info')
  }
}

function goToAdmin() {
  const token = localStorage.getItem('jwt')
  const target = token ? '/admin' : '/login'
  if (route.path !== target) {
    router.push(target)
  }
}

function goToFeedbackForm() {
  if (route.path !== '/feedback-form') {
    router.push('/feedback-form')
  }
}
</script>
