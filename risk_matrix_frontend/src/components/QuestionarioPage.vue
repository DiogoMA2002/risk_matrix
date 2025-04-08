<template>
  <div class="h-screen overflow-y-auto bg-gradient-to-br from-blue-600 to-indigo-100 font-sans">
    <div class="container mx-auto px-4 py-6">
      <!-- Header with back button and logo -->
      <div class="flex justify-between items-center mb-6">
        <div class="flex items-center">
          <button @click="goBackToCategories"
                      class="p-2 rounded-full bg-white bg-opacity-20 backdrop-blur-sm text-white hover:bg-opacity-30 transition-all duration-300 mr-4">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none"
              viewBox="0 0 24 24" stroke="currentColor" v-once>
              <path stroke-linecap="round" stroke-linejoin="round"
                stroke-width="2" d="M15 19l-7-7 7-7" />
            </svg>
          </button>
          <div class="text-white">
            <h1 class="text-2xl font-bold">Questionário de Risco | {{ formattedCategory  }}</h1>
            <p class="text-sm opacity-80" v-once>Responda às perguntas para avaliar os riscos</p>
          </div>
        </div>
        <div class="flex items-center space-x-2 text-white">
          <img src="@/assets/logo.png" alt="Logo" class="h-10" v-once>
          <span class="text-2xl font-bold" v-once>C-Network</span>
        </div>
      </div>
      <!-- Questions -->
      <div class="max-w-4xl mx-auto">
        <transition name="fade" mode="out-in">
          <div v-if="!isLoading && questions.length > 0" key="questions">
            <div v-for="(question, index) in questions" :key="question.id"
              class="mb-6 bg-white bg-opacity-90 backdrop-blur-sm rounded-xl shadow-md p-6 transition-all duration-300"
              v-memo="[question.id, getAnswer(question.id)]">
              <div class="flex items-center mb-4">
                <div class="w-8 h-8 rounded-full bg-blue-600 flex items-center justify-center text-white font-medium mr-3">
                  {{ index + 1 }}
                </div>
                <p class="text-lg font-semibold text-blue-800">{{ question.questionText }}</p>
              </div>
              <!-- Options -->
              <div class="pl-11 space-y-3">
                <label v-for="option in question.options" :key="option.optionText"
                  class="block p-3 rounded-lg hover:bg-blue-50 transition-colors cursor-pointer">
                  <div class="flex items-center">
                    <input type="radio"
                      :name="'question_' + question.id"
                      :value="option.optionText"
                      :checked="getAnswer(question.id) === option.optionText"
                      @change="updateAnswer(question.id, option.optionText)"
                      class="mr-3 h-4 w-4 text-blue-600" />
                    <span>{{ option.optionText }}</span>
                  </div>
                </label>
              </div>
            </div>
          </div>
          <div v-else key="loading" class="space-y-6">
            <!-- Skeleton for 3 questions -->
            <div v-for="i in 3" :key="i" class="mb-6 bg-white bg-opacity-90 backdrop-blur-sm rounded-xl shadow-md p-6">
              <div class="flex items-center mb-4">
                <div class="w-8 h-8 rounded-full bg-blue-100 animate-pulse mr-3"></div>
                <div class="h-6 bg-blue-100 rounded w-3/4 animate-pulse"></div>
              </div>
              <div class="pl-11 space-y-3">
                <div v-for="j in 4" :key="j" class="flex items-center p-3">
                  <div class="w-4 h-4 rounded-full bg-blue-100 animate-pulse mr-3"></div>
                  <div class="h-4 bg-blue-100 rounded w-1/2 animate-pulse"></div>
                </div>
              </div>
            </div>
          </div>
        </transition>
      </div>
      <!-- Progress indicator (footer) -->
      <div class="max-w-3xl mx-auto mt-10 px-4" v-once>
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
            <div class="ml-2 text-white font-bold">Questionário</div>
          </div>
        </div>
      </div>
    </div>
    <!-- Floating Help Button -->
    <div class="fixed bottom-6 right-6">
      <button @click="goToFeedbackForm"
        class="p-4 bg-white rounded-full shadow-lg hover:shadow-xl transition-all duration-300 text-blue-600">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none"
          viewBox="0 0 24 24" stroke="currentColor" v-once>
          <path stroke-linecap="round" stroke-linejoin="round"
            stroke-width="2" d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      </button>
    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  name: "QuestionarioPage",
  data() {
    return {
      questions: [],
      isLoading: true
    };
  },
  computed: {
    formattedCategory() {
    const raw = this.$route.params.category || "";
    return raw
      .replace(/_/g, ' ')
      .replace(/\b\w/g, c => c.toUpperCase());
  },
    // Get answers for the current category from Vuex store
    answers() {
      const category = this.$route.params.category;
      return this.$store.state.allAnswers[category] || {};
    }
  },
  async created() {
    const { questionnaireId, category } = this.$route.params;
    if (questionnaireId && category) {
      await this.fetchQuestionsByCategory(questionnaireId, category);
      // Preinitialize answers for each question if not already set
      this.questions.forEach((question) => {
        const current = this.$store.state.allAnswers[this.$route.params.category] || {};
        if (current[question.id] === undefined) {
          this.$store.commit("updateAnswer", { category: this.$route.params.category, questionId: question.id, answer: "" });
        }
      });
    }
  },
  methods: {
    async fetchQuestionsByCategory(questionnaireId, category) {
      try {
        this.isLoading = true;
        const response = await axios.get(`/api/questionnaires/${questionnaireId}/category/${category}`);
        this.questions = response.data;
      } catch (error) {
        console.error("Erro ao buscar perguntas:", error);
        this.questions = [];
      } finally {
        this.isLoading = false;
      }
    },
    updateAnswer(questionId, value) {
    const category = this.$route.params.category;
    // Only store an answer if the user actually selected something
    // If you need to handle "no selection," store null or remove the key
    this.$store.commit("updateAnswer", { category, questionId, answer: value });
  },
  goBackToCategories() {
  const { questionnaireId } = this.$route.params;
  this.$router.push({
    name: 'CategoryList', // or whatever route shows the categories
    query: { selected: questionnaireId }
  });
}
,
  getAnswer(questionId) {
    const category = this.$route.params.category;
    const answersForCategory = this.$store.state.allAnswers[category] || {};
    return answersForCategory[questionId] || null;
  },
    goToFeedbackForm() {
      this.$router.push("/feedback-form");
    }
  }
};
</script>

<style>
html,
body {
  height: 100%;
  margin: 0;
  padding: 0;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
