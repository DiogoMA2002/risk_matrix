<template>
  <div class="h-screen overflow-y-auto bg-gradient-to-br from-blue-600 to-indigo-100 font-sans">
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

      <!-- Main layout: Categories grid + Sidebar with Questionnaires -->
      <div class="flex flex-col md:flex-row">
        <!-- Main Content: Categories Grid -->
        <div class="w-full md:w-3/4">
          <div class="max-w-4xl mx-auto">
            <div v-if="!selectedQuestionnaire">
              <p class="text-lg text-blue-800">Carregando questionário...</p>
            </div>
            <div v-else>
              <!-- Categories extracted from the selected questionnaire -->
              <div v-if="categories.length > 0" class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
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
                  <h2 class="text-xl font-semibold text-blue-600 text-center">{{ formatCategoryName(cat) }}</h2>
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
              </div>
              <div v-else class="text-center text-blue-800">
                <p>Nenhuma categoria encontrada neste questionário.</p>
              </div>
            </div>
          </div>
          
          <!-- Submit All Answers Button -->
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
        
        <!-- Sidebar: List of Questionnaires -->
        <div class="w-full md:w-1/4 md:pl-4 mt-6 md:mt-0">
          <div class="bg-white bg-opacity-90 backdrop-blur-sm rounded-xl shadow-md p-4">
            <h3 class="text-lg font-semibold text-blue-800 mb-4">Questionários</h3>
            <ul class="divide-y divide-gray-200">
              <li
                v-for="qnr in questionnaires"
                :key="qnr.id"
                class="py-2 cursor-pointer hover:bg-blue-50 transition-colors rounded px-2"
                :class="{ 'bg-blue-100': selectedQuestionnaire && qnr.id === selectedQuestionnaire.id }"
                @click="selectQuestionnaire(qnr.id)"
              >
                {{ qnr.title }}
              </li>
            </ul>
          </div>
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

      <!-- Floating Help Button -->
      <div class="fixed bottom-6 right-6">
        <button @click="goToFeedbackForm" class="p-4 bg-white rounded-full shadow-lg hover:shadow-xl transition-all duration-300 text-blue-600">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" 
               viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M8.228 9c.549-1.165 2.03-2 3.772-2
                 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006
                 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21
                 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </button>
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
      questionnaires: [],
      selectedQuestionnaire: null
    };
  },
  computed: {
    categories() {
  if (this.selectedQuestionnaire && this.selectedQuestionnaire.questions) {
    const cats = this.selectedQuestionnaire.questions.map(q => q.category);
    return [...new Set(cats)];
  }
  return [];
}

  },
  async created() {
    await this.fetchQuestionnaires();
    if (this.questionnaires.length > 0) {
      // Auto-select the first questionnaire available.
      this.selectQuestionnaire(this.questionnaires[0].id);
    }
  },
  methods: {
    async fetchQuestionnaires() {
      try {
        const response = await axios.get("/api/questionnaires/all");
        this.questionnaires = response.data;
      } catch (error) {
        console.error("Erro ao buscar questionários:", error);
        // fallback example
        this.questionnaires = [
          { id: 1, title: "Questionário 1" },
          { id: 2, title: "Questionário 2" },
          { id: 3, title: "Questionário 3" }
        ];
      }
    },
    formatCategoryName(rawEnum) {
      // Example: "Risco_de_Autenticacao" -> "Risco de Autenticacao"
      return rawEnum.replace(/_/g, ' ');
    },
    
    async selectQuestionnaire(id) {
      try {
        const response = await axios.get(`/api/questionnaires/${id}`);
        this.selectedQuestionnaire = response.data;
      } catch (error) {
        console.error("Erro ao buscar questionário:", error);
        this.selectedQuestionnaire = null;
      }
    },
    goToCategory(category) {
  this.$router.push({
    name: 'Questionary',
    params: { category, questionnaireId: this.selectedQuestionnaire.id }
  });
},

    // Updated to do a single multi-answer request
    async submitAllAnswers() {
      try {
        // 1. Load allAnswers from localStorage
        const allAnswers = JSON.parse(localStorage.getItem("allAnswers")) || {};
        const payload = [];

        // 2. Build an array of AnswerDTO objects
        //    Here we don't have user emails, so you might add an "email" field from your store or a form
        const userEmail = localStorage.getItem("userEmail") || "fallback@example.com";
        console.log("User email:", userEmail);
        for (const category in allAnswers) {
          const categoryAnswers = allAnswers[category]; // e.g. { "10": "aaaa", "11": "bbb" }
          for (const questionId in categoryAnswers) {
            const userResponse = categoryAnswers[questionId];
            // push one object per answered question
            payload.push({
              questionId: parseInt(questionId),
              userResponse,
              email: userEmail
            });
          }
        }

        if (!payload.length) {
          alert("Nenhuma resposta para enviar!");
          return;
        }

        // 3. POST them in a single request to your new /submit-multiple endpoint
        await axios.post("/api/answers/submit-multiple", payload);

        alert("Todas as respostas foram enviadas com sucesso!");

        // Optionally clear localStorage:
        // localStorage.removeItem("allAnswers");

      } catch (error) {
        console.error("Erro ao enviar respostas:", error);
        alert("Ocorreu um erro ao enviar as respostas.");
      }
    },
    goToFeedbackForm() {
      this.$router.push('/feedback-form');
    }
  }
};
</script>

<style>
html, body {
  height: 100%;
  margin: 0;
  padding: 0;
}
</style>
