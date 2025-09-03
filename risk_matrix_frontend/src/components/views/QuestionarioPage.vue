<template>
  <div class="h-screen overflow-y-auto bg-gradient-to-br from-blue-600 to-indigo-100 font-sans">
    <div class="container mx-auto px-4 py-6">
      <!-- Header with back button and logo -->
      <div class="flex justify-between items-center mb-6">
        <div class="flex items-center">
          <button @click="goBackToCategories"
            class="p-2 rounded-full bg-white bg-opacity-20 backdrop-blur-sm text-white hover:bg-opacity-30 transition-all duration-300 mr-4">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24"
              stroke="currentColor" v-once>
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
            </svg>
          </button>
          <div class="text-white">
            <h1 class="text-2xl font-bold">Questionário de Risco | {{ formattedCategory }}</h1>
            <p class="text-sm opacity-80" v-once>Responda às perguntas para avaliar os riscos</p>
          </div>
          
        </div>
        <div class="flex items-center space-x-2 text-white">
          <img src="@/assets/logoCCC.webp" alt="Logo" class="h-10" v-once>
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

              <!-- Question Text + Description -->
              <div class="flex items-start mb-4">
                <div
                  class="w-8 h-8 rounded-full bg-blue-600 flex items-center justify-center text-white font-medium mr-3 flex-shrink-0 min-w-[2rem] min-h-[2rem]">
                  {{ index + 1 }}
                </div>
                <div>
                  <p class="text-lg font-semibold text-blue-800">{{ question.questionText }}</p>
                  <p v-if="question.description" class="text-sm text-gray-600 mt-1">{{ question.description }}</p>
                </div>
              </div>

              <!-- Options -->
              <div class="pl-11 space-y-3">
                <label v-for="option in question.options" :key="option.optionText"
                  class="block p-3 rounded-lg hover:bg-blue-50 transition-colors cursor-pointer">
                  <div class="flex items-center">
                    <input type="radio" :name="'question_' + question.id" :value="option.optionText"
                      :checked="getAnswer(question.id) === option.optionText"
                      @click="handleRadioClick(question.id, option.optionText)" class="mr-3 h-4 w-4 text-blue-600" />
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
                <div class="w-8 h-8 rounded-full bg-blue-100 animate-pulse mr-3 flex-shrink-0 min-w-[2rem] min-h-[2rem]"></div>
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
        <!-- Botão Confirmar Respostas no fim da página -->
        <div class="max-w-4xl mx-auto mt-10 flex justify-end px-4">
          <button
            @click="goBackToCategories"
            class="bg-blue-600 hover:bg-blue-700 text-white font-semibold px-6 py-3 rounded-lg shadow transition duration-300"
          >
            Confirmar Respostas
          </button>
        </div>
      </div>

      <!-- Progress indicator -->
      <div class="max-w-3xl mx-auto mt-10 px-4" v-once>
        <div class="flex items-center justify-between">
          <div class="flex items-center">
            <div class="w-8 h-8 rounded-full bg-blue-600 flex items-center justify-center text-white font-medium flex-shrink-0 min-w-[2rem] min-h-[2rem]">1
            </div>
            <div class="ml-2 text-white">Informações</div>
          </div>
          <div class="w-16 h-1 bg-blue-400"></div>
          <div class="flex items-center">
            <div class="w-8 h-8 rounded-full bg-blue-600 flex items-center justify-center text-white font-medium flex-shrink-0 min-w-[2rem] min-h-[2rem]">2
            </div>
            <div class="ml-2 text-white">Requisitos</div>
          </div>
          <div class="w-16 h-1 bg-blue-400"></div>
          <div class="flex items-center">
            <div class="w-8 h-8 rounded-full bg-blue-600 flex items-center justify-center text-white font-medium flex-shrink-0 min-w-[2rem] min-h-[2rem]">3
            </div>
            <div class="ml-2 text-white font-bold">Questionário</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Categoria Não Aplicável Button - Bottom Left -->
    <div class="fixed bottom-6 left-6 z-50">
      <button
        @click="aplicarNaoAplicavelATodas"
        class="p-4 bg-gradient-to-r from-orange-500 to-red-600 text-white rounded-full shadow-lg hover:from-orange-600 hover:to-red-700 transition font-semibold text-base border-4 border-white focus:outline-none focus:ring-4 focus:ring-orange-300"
        aria-label="Marcar categoria como não aplicável"
      >
        Não Aplicável
      </button>
    </div>

    <!-- Help Button and Glossário Button -->
    <div class="fixed bottom-6 right-6 flex flex-col items-end gap-3 z-50">
      <button @click="goToFeedbackForm"
        class="p-4 bg-white rounded-full shadow-lg hover:shadow-xl transition-all duration-300 text-blue-600"
        aria-label="Ir para o formulário de feedback">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      </button>
      <button
        @click="showGlossary = true"
        class="p-4 bg-gradient-to-r from-blue-500 to-blue-700 text-white rounded-full shadow-lg hover:from-blue-600 hover:to-blue-800 transition font-semibold text-base border-4 border-white focus:outline-none focus:ring-4 focus:ring-blue-300 flex items-center gap-2"
        aria-label="Abrir Glossário"
      >
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" class="w-6 h-6">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 20V6m8 6H4m16 0a8 8 0 11-16 0 8 8 0 0116 0z" />
        </svg>
        Glossário
      </button>
    </div>
    <GlossaryDrawer :open="showGlossary" @close="showGlossary = false" title="Glossário" />

      <!-- Alert Dialog -->
      <AlertDialog
        :show="showAlert"
        :title="alertTitle"
        :message="alertMessage"
        :type="alertType"
        @confirm="handleAlertConfirm"
        @cancel="handleAlertCancel"
      />
  </div>
</template>

<script>
import axios from "axios";
import AlertDialog from "@/components/Static/AlertDialog.vue";
import GlossaryDrawer from '@/components/Static/GlossaryDrawer.vue';

export default {
  name: "QuestionarioPage",
  components: { 
    AlertDialog,
    GlossaryDrawer
  },
  data() {
    return {
      questions: [],
      isLoading: true,
      showAlert: false,
      alertTitle: "",
      alertMessage: "",
      alertType: "alert",
      alertResolve: null,
      loading: true,
      showGlossary: false
    };
  },
  computed: {
    formattedCategory() {
      const raw = this.$route.params.category || "";
      return raw.replace(/_/g, ' ')
        .split(' ')
        .map(word => {
          if (!word) return '';
          return word.charAt(0).toLocaleUpperCase('pt-PT') +
            word.slice(1).toLocaleLowerCase('pt-PT');
        })
        .join(' ');
    },
    answers() {
      const category = this.$route.params.category;
      return this.$store.state.allAnswers[category] || {};
    }
  },
  async created() {
    const { questionnaireId, category } = this.$route.params;
    if (questionnaireId && category) {
      await this.fetchQuestionsByCategory(questionnaireId, category);
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
    showAlertDialog(title, message, type = "alert") {
      this.alertTitle = title;
      this.alertMessage = message;
      this.alertType = type;
      this.showAlert = true;
      return new Promise((resolve) => {
        this.alertResolve = resolve;
      });
    },
    handleAlertConfirm() {
      this.showAlert = false;
      if (this.alertResolve) {
        this.alertResolve(true);
        this.alertResolve = null;
      }
    },
    handleAlertCancel() {
      this.showAlert = false;
      if (this.alertResolve) {
        this.alertResolve(false);
        this.alertResolve = null;
      }
    },
    updateAnswer(questionId, value) {
      const category = this.$route.params.category;
      const current = this.getAnswer(questionId);
      const newValue = current === value ? "" : value;
      this.$store.commit("updateAnswer", { category, questionId, answer: newValue });
    },
    getAnswer(questionId) {
      const category = this.$route.params.category;
      const answersForCategory = this.$store.state.allAnswers[category] || {};
      return answersForCategory[questionId] || "";
    },
    allQuestionsAnswered() {
      return this.questions.every(q => {
        const answer = this.getAnswer(q.id);
        return typeof answer === "string" && answer.trim() !== "";
      });
    },
    handleRadioClick(questionId, optionText) {
      const current = this.getAnswer(questionId);
      const newValue = current === optionText ? "" : optionText;
      this.updateAnswer(questionId, newValue);
    }
    ,
   async goBackToCategories() {
      if (!this.allQuestionsAnswered()) {
        const proceed = await this.showAlertDialog(
            "Aviso",
            "Nem todas as perguntas foram respondidas.\nIsto pode afetar a precisão da avaliação de risco.\nDeseja continuar mesmo assim?",
            "confirm"
          );
          if (!proceed) {
            return;
          }
      }

      const { questionnaireId } = this.$route.params;
      this.$router.push({
        name: 'CategoryList',
        query: { selected: questionnaireId }
      });
    },
    aplicarNaoAplicavelATodas() {
      const naoAplicavelText = "Não Aplicável";
      const category = this.$route.params.category;

      this.questions.forEach((question) => {
        const naoAplicavelOption = question.options.find(opt => opt.optionText === naoAplicavelText);
        if (naoAplicavelOption) {
          this.$store.commit("updateAnswer", {
            category,
            questionId: question.id,
            answer: naoAplicavelOption.optionText
          });
        }
      });
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
