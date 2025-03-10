<template>
    <div class="min-h-screen bg-gradient-to-br from-blue-600 to-indigo-100 font-sans">
      <div class="container mx-auto px-4 py-6">
        <!-- Header with back button -->
        <div class="flex items-center mb-6">
          <button
            @click="$router.go(-1)"
            class="p-2 rounded-full bg-white bg-opacity-20 backdrop-blur-sm text-white hover:bg-opacity-30 transition-all duration-300 mr-4"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none"
                 viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round"
                    stroke-width="2" d="M15 19l-7-7 7-7" />
            </svg>
          </button>
          <div class="text-white">
            <h1 class="text-2xl font-bold">Categorias de Questões</h1>
            <p class="text-sm opacity-80">Selecione uma categoria para responder às perguntas</p>
          </div>
        </div>
  
        <!-- Categories Grid -->
        <div class="max-w-4xl mx-auto">
          <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
  
            <!-- Skeleton de carregamento se não houver categorias -->
            <template v-if="categories.length === 0">
              <div
                v-for="i in 3"
                :key="'skeleton-'+i"
                class="bg-white bg-opacity-70 rounded-xl shadow-md p-6 flex flex-col items-center justify-center"
              >
                <div class="w-16 h-16 rounded-full bg-gray-200 animate-pulse mb-4"></div>
                <div class="h-6 w-32 bg-gray-200 animate-pulse rounded mb-2"></div>
                <div class="h-4 w-24 bg-gray-200 animate-pulse rounded"></div>
              </div>
            </template>
  
            <!-- Se há categorias, exibe os cards -->
            <template v-else>
              <div
                v-for="cat in categories"
                :key="cat"
                @click="goToCategory(cat)"
                class="cursor-pointer bg-white bg-opacity-90 backdrop-blur-sm rounded-xl shadow-md p-6 flex flex-col items-center justify-center transition-all duration-300 hover:shadow-xl hover:bg-blue-50 hover:-translate-y-1"
              >
                <div class="w-16 h-16 rounded-full bg-blue-100 flex items-center justify-center mb-4">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M8.228 9c.549-1.165 2.03-2 3.772-2
                             2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994
                             1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <h2 class="text-xl font-semibold text-blue-600 text-center">{{ cat }}</h2>
                <p class="mt-2 text-gray-500 text-center text-sm">Clique para ver as perguntas</p>
                <div class="mt-4 flex justify-center">
                  <span class="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                    Iniciar
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3 ml-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round"
                            stroke-width="2" d="M9 5l7 7-7 7" />
                    </svg>
                  </span>
                </div>
              </div>
            </template>
          </div>
        </div>
  
        <!-- Botão "Enviar Todas as Respostas" -->
        <div class="mt-8 flex justify-center">
          <button
            @click="submitAllAnswers"
            class="px-6 py-3 bg-blue-600 text-white rounded-lg shadow-md hover:bg-blue-700 transition-all duration-300 flex items-center"
          >
            <span>Enviar Todas as Respostas</span>
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round"
                    stroke-width="2" d="M13 5l7 7-7 7M5 5l7 7-7 7" />
            </svg>
          </button>
        </div>
      </div>
  
      <!-- Progress indicator -->
      <div class="max-w-3xl mx-auto mt-10 px-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center">
            <div class="w-8 h-8 rounded-full bg-blue-600 flex items-center justify-center text-white font-medium">1</div>
            <div class="ml-2 text-white">Informações</div>
          </div>
          <div class="w-16 h-1 bg-blue-400"></div>
          <div class="flex items-center">
            <div class="w-8 h-8 rounded-full bg-blue-600 flex items-center justify-center text-white font-medium">2</div>
            <div class="ml-2 text-white">Requisitos</div>
          </div>
          <div class="w-16 h-1 bg-blue-400"></div>
          <div class="flex items-center">
            <div class="w-8 h-8 rounded-full bg-blue-600 flex items-center justify-center text-white font-medium">3</div>
            <div class="ml-2 text-white">Questionário</div>
          </div>
        </div>
      </div>
  
      <!-- Floating help button -->
      <div class="fixed bottom-6 right-6">
        <button class="p-4 bg-white rounded-full shadow-lg hover:shadow-xl transition-all duration-300 text-blue-600">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none"
               viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round"
                  stroke-width="2"
                  d="M8.228 9c.549-1.165 2.03-2 3.772-2
                     2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994
                     1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </button>
      </div>
    </div>
  </template>
  
  <script>
  import axios from "axios";
  
  export default {
    name: 'CategoryList',
    data() {
      return {
        categories: []
      };
    },
    async created() {
      try {
        const response = await axios.get("/api/questions/all");
        const allQuestions = response.data;
        // Extraímos as categorias únicas
        const uniqueCategories = [...new Set(allQuestions.map(q => q.category))];
        this.categories = uniqueCategories;
      } catch (error) {
        console.error("Erro ao buscar perguntas:", error);
        // Fallback manual caso a API falhe
        this.categories = [
          "Segurança de Rede",
          "Proteção de Dados",
          "Gestão de Acesso",
        ];
      }
    },
    methods: {
      goToCategory(category) {
        // Navega para a tela de questionário
        this.$router.push({
          name: 'Questionary',
          params: { category }
        });
      },
      async submitAllAnswers() {
        try {
          // Carrega TODAS as respostas do localStorage
          const allAnswers = JSON.parse(localStorage.getItem("allAnswers")) || {};
  
          // Loop em cada categoria e cada pergunta
          for (const category in allAnswers) {
            const categoryAnswers = allAnswers[category]; // Ex: { "1": "Sim", "2": "Não" }
            for (const questionId in categoryAnswers) {
              const userResponse = categoryAnswers[questionId];
              // Faz o POST para cada pergunta respondida
              await axios.post(`/api/answers/submit/${questionId}`, {
                userResponse
              });
            }
          }
          alert("Todas as respostas foram enviadas com sucesso!");
  
          // Se quiser limpar as respostas do localStorage depois de enviar:
          // localStorage.removeItem("allAnswers");
        } catch (error) {
          console.error("Erro ao enviar respostas:", error);
          alert("Ocorreu um erro ao enviar as respostas.");
        }
      }
    }
  };
  </script>
  