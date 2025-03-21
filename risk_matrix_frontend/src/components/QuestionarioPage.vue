<template>
  <!-- Força a altura da tela e habilita scroll vertical -->
  <div class="h-screen overflow-y-auto bg-gradient-to-br from-blue-600 to-indigo-100 font-sans">
    <div class="container mx-auto px-4 py-6">
      <!-- Header with back button -->
      <div class="flex items-center mb-6">
        <button @click="$router.go(-1)"
          class="p-2 rounded-full bg-white bg-opacity-20 backdrop-blur-sm text-white hover:bg-opacity-30 transition-all duration-300 mr-4">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
          </svg>
        </button>
        <div class="text-white">
          <h1 class="text-2xl font-bold">Questionário de Risco</h1>
          <p class="text-sm opacity-80">Responda às perguntas para avaliar os riscos</p>
        </div>
      </div>

      <!-- Questions -->
      <div class="max-w-4xl mx-auto">
        <!-- Se há perguntas -->
        <div v-if="questions.length > 0">
          <div v-for="(question, index) in questions" :key="question.id"
            class="mb-6 bg-white bg-opacity-90 backdrop-blur-sm rounded-xl shadow-md p-6 transition-all duration-300">
            <div class="flex items-center mb-4">
              <div
                class="w-8 h-8 rounded-full bg-blue-600 flex items-center justify-center text-white font-medium mr-3">
                {{ index + 1 }}
              </div>
              <p class="text-lg font-semibold text-blue-800">
                {{ question.questionText }}
              </p>
            </div>

            <!-- Loop over options provided by backend -->
            <div class="pl-11 space-y-3">
              <label v-for="option in question.options" :key="option.optionText"
                class="block p-3 rounded-lg hover:bg-blue-50 transition-colors cursor-pointer">
                <div class="flex items-center">
                  <input type="radio" :name="'question_' + question.id" :value="option.optionText"
                    v-model="answers[question.id]" class="mr-3 h-4 w-4 text-blue-600" @change="saveAnswers" />
                  <span>{{ option.optionText }}</span>
                </div>
              </label>
            </div>
          </div>
        </div>

        <!-- Loading state -->
        <div v-else
          class="bg-white bg-opacity-90 backdrop-blur-sm rounded-xl shadow-md p-6 flex flex-col items-center justify-center">
          <div class="w-16 h-16 rounded-full bg-blue-100 flex items-center justify-center mb-4 animate-pulse">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-blue-600" fill="none" viewBox="0 0 24 24"
              stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.228 9c.549-1.165 2.03-2 3.772-2
                   2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994
                   1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <p class="text-lg text-blue-800">Carregando perguntas...</p>
        </div>
      </div>

      <!-- Progress indicator -->
      <div class="max-w-3xl mx-auto mt-10 px-4">
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
          <div class="w-16 h-1 bg-blue-400"></div>
          <div class="flex items-center">
            <div
              class="w-8 h-8 rounded-full bg-white text-blue-600 flex items-center justify-center font-medium border-2 border-blue-600">
              3</div>
            <div class="ml-2 text-white font-bold">Questionário</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Floating help button -->
    <div class="fixed bottom-6 right-6">
      <button @click="goToFeedbackForm"
        class="p-4 bg-white rounded-full shadow-lg hover:shadow-xl transition-all duration-300 text-blue-600">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.228 9c.549-1.165 2.03-2 3.772-2
               2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21
               12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      </button>
    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  name: 'QuestionarioPage',
  data() {
    return {
      questions: [],
      answers: {}
    };
  },
  async created() {
  const { questionnaireId, category } = this.$route.params;
  if (questionnaireId && category) {
    await this.fetchQuestionsByCategory(questionnaireId, category);
    this.loadAnswersFromLocalStorage(category);
  }
},
  
  methods: {
    async fetchQuestionsByCategory(questionnaireId, category) {
  try {
    console.log("Questionnaire ID:", questionnaireId);
    console.log("Category:", category);
    const response = await axios.get(
      `/api/questionnaires/${questionnaireId}/category/${category}`
    );
    this.questions = response.data;
  } catch (error) {
    console.error("Erro ao buscar perguntas:", error);
    this.questions = [];
  }
}
,
    loadAnswersFromLocalStorage(category) {
      const allAnswers = JSON.parse(localStorage.getItem("allAnswers")) || {};
      if (allAnswers[category]) {
        this.answers = allAnswers[category];
      }
    },
    saveAnswers() {
      const category = this.$route.params.category;
      const allAnswers = JSON.parse(localStorage.getItem("allAnswers")) || {};
      allAnswers[category] = this.answers;
      localStorage.setItem("allAnswers", JSON.stringify(allAnswers));
    },
    goToFeedbackForm() {
      this.$router.push('/feedback-form');
    }
  }
};
</script>

<style>
/* Garante que html e body tenham 100% de altura */
html,
body {
  height: 100%;
  margin: 0;
  padding: 0;
}
</style>
