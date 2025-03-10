<template>
    <div class="p-6">
      <!-- Header com botão de voltar -->
      <div class="flex items-center mb-4">
        <button
          @click="$router.go(-1)"
          class="p-2 rounded-full bg-blue-600 text-white hover:bg-blue-700 transition-all duration-300 mr-4"
        >
          <!-- Ícone de seta para a esquerda -->
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none"
               viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round"
                  stroke-width="2" d="M15 19l-7-7 7-7" />
          </svg>
        </button>
        <h1 class="text-2xl font-bold">Categorias de Questões</h1>
      </div>
  
      <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
        <div
          v-for="cat in categories"
          :key="cat"
          @click="goToCategory(cat)"
          class="cursor-pointer bg-white rounded-lg shadow-md p-6 flex flex-col items-center justify-center transition-all duration-300 hover:shadow-xl hover:bg-blue-50"
        >
          <h2 class="text-xl font-semibold text-blue-600">{{ cat }}</h2>
          <p class="mt-2 text-gray-500">Clique para ver as perguntas</p>
        </div>
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
        const uniqueCategories = [...new Set(allQuestions.map(q => q.category))];
        this.categories = uniqueCategories;
      } catch (error) {
        console.error("Erro ao buscar perguntas:", error);
      }
    },
    methods: {
      goToCategory(category) {
        // Navega para a tela de questionário
        this.$router.push({
          name: 'Questionary',
          params: { category }
        });
      }
    }
  };
  </script>
  