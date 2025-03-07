<template>
  <div class="p-6">
    <h1 class="text-2xl font-bold mb-4">Questionário de Risco</h1>

    <!-- Exibir perguntas -->
    <div v-if="questions.length > 0">
      <form @submit.prevent="submitAnswers">
        <div v-for="question in questions" :key="question.id" class="mb-4 p-4 border rounded">
          <p class="text-lg font-semibold">{{ question.questionText }}</p>
          <label class="block mt-2">
            <input type="radio" :name="'question_' + question.id" :value="'Sim'" v-model="answers[question.id]" /> Sim
          </label>
          <label class="block mt-1">
            <input type="radio" :name="'question_' + question.id" :value="'Não'" v-model="answers[question.id]" /> Não
          </label>
          <label class="block mt-1">
            <input type="radio" :name="'question_' + question.id" :value="'Parcialmente'" v-model="answers[question.id]" /> Parcialmente
          </label>
        </div>
        <button type="submit" class="px-4 py-2 bg-blue-500 text-white rounded">Enviar Respostas</button>
      </form>
    </div>

    <p v-else class="text-lg">Carregando perguntas...</p>
  </div>
</template>

<script>
import axios from "axios";

export default {
  data() {
    return {
      questions: [],
      answers: {} // Armazena respostas dos usuários
    };
  },
  created() {
    this.fetchQuestions();
  },
  methods: {
    async fetchQuestions() {
      try {
        const response = await axios.get("/api/questions/all");
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

<style scoped>
input {
  margin-right: 8px;
}
</style>
