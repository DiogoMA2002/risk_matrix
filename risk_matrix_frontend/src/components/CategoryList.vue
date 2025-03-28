<template>
  <div class="h-screen overflow-y-auto bg-gradient-to-br from-blue-600 to-indigo-100 font-sans">
    <div class="container mx-auto px-4 py-6">
      <!-- Header with back button and logo -->
      <div class="flex justify-between items-center mb-6">
        <!-- Left side: back button and title -->
        <div class="flex items-center">
          <button @click="$router.go(-1)"
            class="p-2 rounded-full bg-white bg-opacity-20 backdrop-blur-sm text-white hover:bg-opacity-30 transition-all duration-300 mr-4">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24"
              stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
            </svg>
          </button>
          <div class="text-white">
            <h1 class="text-2xl font-bold">Categorias de Questões</h1>
            <p class="text-sm opacity-80">Selecione uma categoria para responder às perguntas</p>
          </div>
        </div>
        <!-- Right side: Logo + C-Network text -->
        <div class="flex items-center space-x-2 text-white">
          <img src="@/assets/logo.png" alt="Logo" class="h-10">
          <span class="text-2xl font-bold">C-Network</span>
        </div>
      </div>

      <!-- Main layout: Categories grid + Sidebar with Questionnaires -->
      <div class="flex flex-col md:flex-row">
        <!-- Categories Grid -->
        <div class="w-full md:w-3/4">
          <div class="max-w-4xl mx-auto">
            <div v-if="!selectedQuestionnaire">
              <p class="text-lg text-blue-800">Carregando questionário...</p>
            </div>
            <div v-else>
              <div v-if="categories.length > 0" class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
                <div v-for="cat in categories" :key="cat" @click="goToCategory(cat)"
                  class="cursor-pointer bg-white bg-opacity-90 backdrop-blur-sm rounded-xl shadow-md p-6 flex flex-col items-center justify-center transition-all duration-300 hover:shadow-xl hover:bg-blue-50 hover:-translate-y-1">
                  <div class="w-16 h-16 rounded-full bg-blue-100 flex items-center justify-center mb-4">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-blue-600" fill="none"
                      viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                  </div>
                  <h2 class="text-xl font-semibold text-blue-600 text-center">{{ formatCategoryName(cat) }}</h2>
                  <p class="mt-2 text-gray-500 text-center text-sm">Clique para ver as perguntas</p>
                  <!-- Progress Indicator -->
                  <div class="mt-2 flex items-center space-x-2">
                    <span class="text-sm text-gray-700">
                      {{ answeredCount(cat) }}/{{ totalCount(cat) }}
                    </span>
                    <svg v-if="answeredCount(cat) === totalCount(cat) && totalCount(cat) > 0"
                      xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-green-500" fill="none" viewBox="0 0 24 24"
                      stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                    </svg>
                  </div>
                  <div class="mt-4 flex justify-center">
                    <span
                      class="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                      Iniciar
                      <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3 ml-1" fill="none" viewBox="0 0 24 24"
                        stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
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
          <!-- Buttons: Submit, Export, and Import -->
          <div class="mt-8 flex justify-center space-x-4">
            <button @click="submitAllAnswers"
              class="px-6 py-3 bg-blue-600 text-white rounded-lg shadow-md hover:bg-blue-700 transition-all duration-300 flex items-center">
              <span>Enviar Todas as Respostas</span>
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-2" fill="none" viewBox="0 0 24 24"
                stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 5l7 7-7 7M5 5l7 7-7 7" />
              </svg>
            </button>
            <button @click="exportToJSON"
              class="px-6 py-3 bg-green-600 text-white rounded-lg shadow-md hover:bg-green-700 transition-all duration-300 flex items-center">
              <span>Exportar Progresso</span>
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-2" fill="none" viewBox="0 0 24 24"
                stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M4 16v1a2 2 0 002 2h12a2 2 0 002-2v-1M8 12l4 4 4-4M12 4v12" />
              </svg>
            </button>
            <button @click="triggerImport"
              class="px-6 py-3 bg-orange-600 text-white rounded-lg shadow-md hover:bg-orange-700 transition-all duration-300 flex items-center">
              <span>Importar Progresso</span>
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-2" fill="none" viewBox="0 0 24 24"
                stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M12 4v12m0 0l-4-4m4 4l4-4M20 12a8 8 0 11-16 0 8 8 0 0116 0z" />
              </svg>
            </button>
            <input type="file" ref="importFile" accept="application/json" style="display: none"
              @change="importFromJSON" />
          </div>
        </div>
        <!-- Sidebar: List of Questionnaires -->
        <div class="w-full md:w-1/4 md:pl-4 mt-6 md:mt-0">
          <div class="bg-white bg-opacity-90 backdrop-blur-sm rounded-xl shadow-md p-4">
            <h3 class="text-lg font-semibold text-blue-800 mb-4">Questionários</h3>
            <ul class="divide-y divide-gray-200">
              <li v-for="qnr in questionnaires" :key="qnr.id"
                class="py-2 cursor-pointer hover:bg-blue-50 transition-colors rounded px-2"
                :class="{ 'bg-blue-100': selectedQuestionnaire && qnr.id === selectedQuestionnaire.id }"
                @click="selectQuestionnaire(qnr.id)">
                {{ qnr.title }}
              </li>
            </ul>
          </div>
        </div>
      </div>
      <!-- Progress indicator (footer) -->
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
            <div class="w-8 h-8 rounded-full bg-blue-600 flex items-center justify-center text-white font-medium">3
            </div>
            <div class="ml-2 text-white">Questionário</div>
          </div>
        </div>
      </div>
      <!-- Floating Help Button -->
      <div class="fixed bottom-6 right-6">
        <button @click="goToFeedbackForm"
          class="p-4 bg-white rounded-full shadow-lg hover:shadow-xl transition-all duration-300 text-blue-600">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from "axios";
import { mapState, mapActions } from "vuex";
/* eslint-disable */
import SkeletonLoader from "./SkeletonLoader.vue";

export default {
  name: "CategoryList",
  components: { SkeletonLoader },
  computed: {
    ...mapState(["questionnaires", "selectedQuestionnaire", "allAnswers"]),
    categories() {
      if (this.selectedQuestionnaire && this.selectedQuestionnaire.questions) {
        const cats = this.selectedQuestionnaire.questions.map(q => q.category);
        return [...new Set(cats)];
      }
      return [];
    },
  },
  created() {
    this.fetchQuestionnaires().then(() => {
      if (!this.selectedQuestionnaire && this.questionnaires.length > 0) {
        this.fetchQuestionnaireById(this.questionnaires[0].id);
      }
    });
  },
  methods: {
    ...mapActions(["fetchQuestionnaires", "fetchQuestionnaireById"]),
    formatCategoryName(rawEnum) {
      return rawEnum.replace(/_/g, " ");
    },
    goToCategory(category) {
      this.$router.push({
        name: "Questionary",
        params: { category, questionnaireId: this.selectedQuestionnaire.id },
      });
    },

    async submitAllAnswers() {
      const confirmed = window.confirm(
        "Tem a certeza que deseja enviar todas as respostas? Após enviar, o progresso guardado será limpo e você será redirecionado à página principal."
      ); if (!confirmed) return;

      try {
        const payload = [];
        const userEmail = localStorage.getItem("userEmail") || "fallback@example.com";

        for (const category in this.allAnswers) {
          const categoryAnswers = this.allAnswers[category];
          for (const questionId in categoryAnswers) {
            const userResponse = categoryAnswers[questionId];
            // Skip empty or null
            if (!userResponse || !userResponse.trim()) {
              continue;
            }
            payload.push({
              questionId: parseInt(questionId),
              userResponse,
              email: userEmail,
            });
          }
        }

        if (!payload.length) {
          alert("Nenhuma resposta para enviar!");
          return;
        }

        await axios.post("/api/answers/submit-multiple", payload);
        alert("Todas as respostas foram enviadas com sucesso!");
        this.$store.commit("clearAllAnswers");
        this.$router.push("/");
      } catch (error) {
        console.error("Erro ao enviar respostas:", error);
        alert("Ocorreu um erro ao enviar as respostas.");
      }
    },

    exportToJSON() {
      const confirmed = window.confirm(
        "Deseja exportar o progresso? Isso irá limpar o progresso salvo localmente."
      );
      if (!confirmed) return;
      const allAnswersStr = JSON.stringify(this.allAnswers);
      if (!allAnswersStr) {
        alert("Nenhuma resposta para exportar!");
        return;
      }
      const blob = new Blob([allAnswersStr], { type: "application/json" });
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = "progress.json";
      link.click();
      URL.revokeObjectURL(url);
      this.$store.commit("clearAllAnswers");
    },
    triggerImport() {
      this.$refs.importFile.click();
    },
    importFromJSON(event) {
      const confirmed = window.confirm(
        "Ao importar, o progresso atual será substituído. Deseja continuar?"
      );
      if (!confirmed) return;
      const file = event.target.files[0];
      if (!file) return;
      const reader = new FileReader();
      reader.onload = (e) => {
        try {
          const importedData = JSON.parse(e.target.result);
          this.$store.commit("setAllAnswers", importedData);
          alert("Progresso importado com sucesso!");
        } catch (error) {
          alert("Falha ao importar o progresso. Certifique-se de que o arquivo é válido.");
        }
      };
      reader.readAsText(file);
    },
    goToFeedbackForm() {
      this.$router.push("/feedback-form");
    },
    // Reactive functions for progress indicator:
    answeredCount(category) {
      const answersForCategory = this.allAnswers[category] || {};
      // Count only non-empty (and non-null) answers
      return Object.values(answersForCategory).filter(ans => ans && ans.trim() !== "").length;
    },

    totalCount(category) {
      if (this.selectedQuestionnaire && this.selectedQuestionnaire.questions) {
        return this.selectedQuestionnaire.questions.filter(q => q.category === category).length;
      }
      return 0;
    },
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
</style>
