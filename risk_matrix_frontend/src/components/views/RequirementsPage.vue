<template>
  <div class="h-screen bg-gradient-to-br from-blue-600 to-indigo-100 font-sans overflow-y-scroll">
    <!-- Header -->
    <div class="container mx-auto px-4 py-6">
      <div class="flex justify-between items-center">
        <!-- Back button and title -->
        <div class="flex items-center">
          <button @click="goBack"
            class="p-2 rounded-full bg-white bg-opacity-20 backdrop-blur-sm text-white hover:bg-opacity-30 transition-all duration-300 mr-4">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24"
              stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
            </svg>
          </button>
          <div class="text-white">
            <div class="text-lg font-bold">Matriz de Risco</div>
            <div class="text-sm font-light">Requisitos</div>
          </div>
        </div>

        <!-- Logo -->
        <div class="flex items-center space-x-2 text-white">
          <img src="@/assets/logoCCC.webp" alt="Logo" class="h-16" />
        </div>
      </div>
    </div>

    <!-- Main content -->
    <div class="container mx-auto px-4 py-6">
      <div
        class="bg-white bg-opacity-90 backdrop-blur-md rounded-xl shadow-xl p-8 max-w-3xl mx-auto transition-shadow duration-500 hover:shadow-xl">
        <h1 class="text-3xl font-bold text-blue-600 mb-6">Termos e Requisitos</h1>

        <div class="space-y-6 text-gray-700">
          <div class="p-4 bg-blue-50 rounded-lg border-l-4 border-blue-500">
            <p class="font-medium">
              Antes de iniciar a construção de uma Matriz de Risco na plataforma, a organização deve realizar algumas
              etapas preparatórias essenciais para garantir uma avaliação eficaz e precisa:
            </p>
          </div>

          <div class="space-y-4">
            <div class="flex items-start">
              <div
                class="flex-shrink-0 h-10 w-10 rounded-full bg-blue-100 flex items-center justify-center text-blue-600">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24"
                  stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                </svg>
              </div>
              <div class="ml-4">
                <h2 class="text-lg font-semibold text-blue-600">Levantamento de ativos</h2>
                <p class="mt-1">Identificação dos ativos organizacionais críticos.</p>
              </div>
            </div>

            <div class="flex items-start">
              <div
                class="flex-shrink-0 h-10 w-10 rounded-full bg-blue-100 flex items-center justify-center text-blue-600">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-blue-600" fill="none" viewBox="0 0 24 24"
                  stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M3 3v18h18M9 17V9m4 8V5m4 12v-4" />
                </svg>

              </div>
              <div class="ml-4">
                <h2 class="text-lg font-semibold text-blue-600">Classificação dos ativos</h2>
                <p class="mt-1">Priorização com base na sua importância e impacto em caso de falha.</p>
              </div>
            </div>

            <div class="flex items-start">
              <div
                class="flex-shrink-0 h-10 w-10 rounded-full bg-blue-100 flex items-center justify-center text-blue-600">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24"
                  stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <div class="ml-4">
                <h2 class="text-lg font-semibold text-blue-600">Checklist de avaliação preliminar</h2>
                <p class="mt-1">Verificação de requisitos essenciais antes do início do processo.</p>
              </div>
            </div>
          </div>

          <p>
            Este levantamento inicial é fundamental para a criação de um perfil de risco detalhado, permitindo uma
            análise mais estruturada e assertiva. Caso a entidade necessite de suporte adicional, poderá entrar em
            contacto com a equipa responsável pela plataforma.
          </p>

          <div class="bg-indigo-50 p-4 rounded-lg">
            <div class="flex items-start">
              <div class="flex-shrink-0">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-blue-600" fill="none" viewBox="0 0 24 24"
                  stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <div class="ml-3">
                <p class="text-sm text-gray-600">
                  Ao prosseguir, declara ter lido e aceite os termos e requisitos acima mencionados, e confirma que a
                  sua organização completou as etapas preparatórias necessárias.
                </p>
              </div>
            </div>
          </div>
        </div>

        <div class="mt-8 pt-4 border-t border-gray-200">
          <div class="flex flex-col md:flex-row md:justify-between md:items-center">
            <button @click="goBack"
              class="px-6 py-2 text-gray-600 rounded-lg font-medium transition-all duration-300 hover:bg-gray-100 mb-4 md:mb-0">
              Voltar
            </button>

            <button @click="accept"
              class="px-6 py-3 bg-blue-600 text-white rounded-lg font-medium transition-all duration-300 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 shadow-md hover:shadow-lg flex items-center justify-center">
              <span>Aceitei, quero prosseguir</span>
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-2" fill="none" viewBox="0 0 24 24"
                stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3" />
              </svg>
            </button>
          </div>
        </div>
      </div>
      <div class="max-w-3xl mx-auto mt-8 px-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center">
            <div class="w-8 h-8 rounded-full bg-blue-600 flex items-center justify-center text-white font-medium">1
            </div>
            <div class="ml-2 text-white">Informações</div>
          </div>
          <div class="w-16 h-1 bg-blue-400"></div>
          <div class="flex items-center">
            <div class="w-8 h-8 rounded-full bg-blue-600 flex items-center justify-center text-white font-medium">2
            </div>
            <div class="ml-2 text-white">Requisitos</div>
          </div>
          <div class="w-16 h-1 bg-white bg-opacity-30"></div>
          <div class="flex items-center">
            <div
              class="w-8 h-8 rounded-full bg-white bg-opacity-30 flex items-center justify-center text-white font-medium">
              3</div>
            <div class="ml-2 text-white">Questionário</div>
          </div>
        </div>
      </div>
      <!-- Help Button -->
      <div class="fixed bottom-6 right-6">
        <button @click="goToFeedbackForm" aria-label="Ajuda"
          class="p-4 bg-white rounded-full shadow-lg hover:shadow-xl transition-all duration-300 text-blue-600">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.228 9c.549-1.165 2.03-2 3.772-2
               2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006
               2.907-.542.104-.994.54-.994 1.093m0 3h.01M21
               12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

function accept() {
  localStorage.setItem('completedRequirements', 'true')
  router.push('/category')
}

function goToFeedbackForm() {
  router.push('/feedback-form')
}

function goBack() {
router.push('/risk-info')
}
</script>
