<template>
    <div class="h-screen bg-gradient-to-br from-blue-600 to-indigo-100 font-sans overflow-y-scroll">
      <!-- Header -->
      <div class="container mx-auto px-4 py-6">
        <div class="flex justify-between items-center">
          <div class="flex items-center">
            <button @click="goBack"
              class="p-2 rounded-full bg-white bg-opacity-20 backdrop-blur-sm text-white hover:bg-opacity-30 transition-all duration-300 mr-4 focus:outline-none focus:ring-2 focus:ring-white focus:ring-offset-2 focus:ring-offset-blue-600"
              :aria-label="'Voltar à página inicial'">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
              </svg>
            </button>
            <div class="text-white">
              <div class="text-lg font-bold">Matriz de Risco</div>
              <div class="text-sm font-light">Informações</div>
            </div>
          </div>
          <div class="flex items-center space-x-2 text-white">
            <img src="@/assets/logoCCC.webp" alt="Logo CCC" class="h-16" />
          </div>
        </div>
      </div>
  
      <!-- Main content -->
      <div class="container mx-auto px-4 py-6">
        <div
          class="bg-white bg-opacity-90 backdrop-blur-md rounded-xl shadow-xl p-8 max-w-3xl mx-auto transition-all duration-500 hover:shadow-2xl">
          <h1 class="text-3xl font-bold text-blue-600 mb-6">Informações da Matriz de Risco</h1>
  
          <div class="space-y-6 text-gray-700">
            <div class="p-4 bg-blue-50 rounded-lg border-l-4 border-blue-500 transform transition-all duration-300 hover:scale-[1.02]">
              <p class="font-medium">
                A matriz de risco é uma ferramenta essencial para identificar, avaliar e priorizar riscos num ambiente corporativo.
              </p>
            </div>
  
            <div class="transform transition-all duration-300 hover:scale-[1.01]">
              <h2 class="text-xl font-semibold text-blue-600 mb-3">O que é uma Matriz de Risco?</h2>
              <p>
                Uma matriz de risco é uma ferramenta visual que ajuda a classificar e priorizar riscos com base na sua probabilidade de ocorrência e potencial impacto.
              </p>
            </div>
  
            <div class="transform transition-all duration-300 hover:scale-[1.01]">
              <h2 class="text-xl font-semibold text-blue-600 mb-3">Benefícios</h2>
              <ul class="list-disc pl-5 space-y-2">
                <li class="flex items-center">
                  <svg class="h-5 w-5 text-green-500 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                  </svg>
                  Identificação clara de riscos prioritários
                </li>
                <li class="flex items-center">
                  <svg class="h-5 w-5 text-green-500 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                  </svg>
                  Melhor alocação de recursos para mitigação
                </li>
                <li class="flex items-center">
                  <svg class="h-5 w-5 text-green-500 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                  </svg>
                  Comunicação eficaz sobre riscos entre as equipas
                </li>
                <li class="flex items-center">
                  <svg class="h-5 w-5 text-green-500 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                  </svg>
                  Base para desenvolvimento de planos de contingência
                </li>
              </ul>
            </div>
  
            <div class="transform transition-all duration-300 hover:scale-[1.01]">
              <h2 class="text-xl font-semibold text-blue-600 mb-3">Como Funciona</h2>
              <p>Este processo consiste em três etapas principais:</p>
              <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mt-4">
                <div class="bg-indigo-50 p-4 rounded-lg text-center transform transition-all duration-300 hover:scale-105 hover:shadow-md">
                  <div class="bg-indigo-100 w-10 h-10 rounded-full flex items-center justify-center mx-auto mb-3">
                    <span class="font-bold text-blue-600">1</span>
                  </div>
                  <h3 class="font-medium">Identificação</h3>
                  <p class="text-sm text-gray-600 mt-2">Levantamento de todos os possíveis riscos</p>
                </div>
                <div class="bg-indigo-50 p-4 rounded-lg text-center transform transition-all duration-300 hover:scale-105 hover:shadow-md">
                  <div class="bg-indigo-100 w-10 h-10 rounded-full flex items-center justify-center mx-auto mb-3">
                    <span class="font-bold text-blue-600">2</span>
                  </div>
                  <h3 class="font-medium">Análise</h3>
                  <p class="text-sm text-gray-600 mt-2">Avaliação de probabilidade e impacto</p>
                </div>
                <div class="bg-indigo-50 p-4 rounded-lg text-center transform transition-all duration-300 hover:scale-105 hover:shadow-md">
                  <div class="bg-indigo-100 w-10 h-10 rounded-full flex items-center justify-center mx-auto mb-3">
                    <span class="font-bold text-blue-600">3</span>
                  </div>
                  <h3 class="font-medium">Tratamento</h3>
                  <p class="text-sm text-gray-600 mt-2">Desenvolvimento de estratégias de mitigação</p>
                </div>
              </div>
            </div>
  
            <div class="transform transition-all duration-300 hover:scale-[1.01]">
              <h2 class="text-xl font-semibold text-blue-600 mb-3">Próximos Passos</h2>
              <p>
                Após esta introdução, será direcionado para um questionário que nos ajudará a compreender melhor os riscos específicos do seu ambiente.
              </p>
            </div>
  
            <div class="transform transition-all duration-300 hover:scale-[1.01]">
              <h2 class="text-xl font-semibold text-blue-600 mb-3">Referenciais e Metodologia</h2>
              <p>
                A elaboração da Matriz de Risco baseia-se em normas internacionais como a ISO 31000 e a ISO/IEC 27005, garantindo uma abordagem padronizada e eficaz para a gestão dos riscos. A organização deve compreender que a atualização contínua e a revisão periódica da matriz são fundamentais para enfrentar novas ameaças e adaptar as estratégias de mitigação.
              </p>
            </div>
          </div>
  
          <div class="mt-8 pt-4 border-t border-gray-200">
            <div class="flex justify-between items-center">
              <span class="text-sm text-gray-500">Tempo estimado: 30-60 minutos</span>
              <button @click="proceed"
                class="px-6 py-3 bg-blue-600 text-white rounded-lg font-medium transition-all duration-300 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 shadow-md hover:shadow-lg flex items-center transform hover:scale-105"
                :disabled="isLoading">
                <span>{{ isLoading ? 'A processar...' : 'Continuar' }}</span>
                <svg v-if="!isLoading" xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3" />
                </svg>
                <svg v-else class="animate-spin h-5 w-5 ml-2" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
              </button>
            </div>
          </div>
        </div>
  
        <!-- Progress indicator -->
        <div class="max-w-3xl mx-auto mt-8 px-4">
          <div class="flex items-center justify-between">
            <div class="flex items-center">
              <div class="w-8 h-8 rounded-full bg-blue-600 flex items-center justify-center text-white font-medium">1</div>
              <div class="ml-2 text-white">Informações</div>
            </div>
            <div class="w-16 h-1 bg-white bg-opacity-30"></div>
            <div class="flex items-center">
              <div class="w-8 h-8 rounded-full bg-white bg-opacity-30 flex items-center justify-center text-white font-medium">2</div>
              <div class="ml-2 text-white">Requisitos</div>
            </div>
            <div class="w-16 h-1 bg-white bg-opacity-30"></div>
            <div class="flex items-center">
              <div class="w-8 h-8 rounded-full bg-white bg-opacity-30 flex items-center justify-center text-white font-medium">3</div>
              <div class="ml-2 text-white">Questionário</div>
            </div>
          </div>
        </div>
      </div>
  
      <!-- Help Button -->
      <div class="fixed bottom-6 right-6">
        <button @click="goToFeedbackForm" 
          class="p-4 bg-white rounded-full shadow-lg hover:shadow-xl transition-all duration-300 text-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
          :aria-label="'Ir para o formulário de feedback'">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </button>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref } from 'vue'
  import { useRouter } from 'vue-router'
  
  const router = useRouter()
  const isLoading = ref(false)
  
  async function proceed() {
    try {
      isLoading.value = true
      localStorage.setItem('completedRiskInfo', 'true')
      await router.push('/requirements')
    } catch (error) {
      console.error('Navigation error:', error)
    } finally {
      isLoading.value = false
    }
  }
  
  function goToFeedbackForm() {
    router.push('/feedback-form')
  }
  
  function goBack() {
    router.push('/')
  }
  </script>
  
  <style scoped>
  /* Add smooth transitions for better interactivity */
  button {
    transition: all 0.2s ease-in-out;
  }
  
  button:hover {
    transform: translateY(-1px);
  }
  
  button:active {
    transform: translateY(0);
  }
  
  /* Improve focus styles for better accessibility */
  button:focus-visible,
  input:focus-visible {
    outline: 2px solid #4f46e5;
    outline-offset: 2px;
  }
  
  /* Add hover animations for cards */
  .hover-scale {
    transition: transform 0.3s ease;
  }
  
  .hover-scale:hover {
    transform: scale(1.02);
  }
  </style>
  