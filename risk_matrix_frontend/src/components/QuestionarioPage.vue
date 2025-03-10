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
      <h1 class="text-2xl font-bold">Questionário de Risco</h1>
    </div>

    <!-- Exibir perguntas -->
    <div v-if="questions.length > 0">
      <form @submit.prevent="submitAnswers">
        <div
          v-for="question in questions"
          :key="question.id"
          class="mb-4 p-4 border rounded"
        >
          <p class="text-lg font-semibold">
            {{ question.questionText }}
          </p>
          <label class="block mt-2">
            <input
              type="radio"
              :name="'question_' + question.id"
              value="Sim"
              v-model="answers[question.id]"
            />
            Sim
          </label>
          <label class="block mt-1">
            <input
              type="radio"
              :name="'question_' + question.id"
              value="Não"
              v-model="answers[question.id]"
            />
            Não
          </label>
          <label class="block mt-1">
            <input
              type="radio"
              :name="'question_' + question.id"
              value="Parcialmente"
              v-model="answers[question.id]"
            />
            Parcialmente
          </label>
        </div>

        <button
          type="submit"
          class="px-4 py-2 bg-blue-500 text-white rounded"
        >
          Enviar Respostas
        </button>
      </form>
    </div>
    <p v-else class="text-lg">Carregando perguntas...</p>
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
    const category = this.$route.params.category; // <-- Pega a categoria via rota
    if (category) {
      await this.fetchQuestionsByCategory(category);
    }
  },
  methods: {
    async fetchQuestionsByCategory(category) {
      try {
        const response = await axios.get(`/api/questions/category/${category}`);
        this.questions = response.data;
      } catch (error) {
        console.error("Erro ao buscar perguntas:", error);
      }
    },
    async submitAnswers() {
      try {
        for (const questionId in this.answers) {
          await axios.post(`/api/answers/submit/${questionId}`, {
            userResponse: this.answers[questionId]
          });
        }
        alert("Respostas enviadas com sucesso!");
      } catch (error) {
        console.error("Erro ao enviar respostas:", error);
      }
    }
  }
};
</script>
