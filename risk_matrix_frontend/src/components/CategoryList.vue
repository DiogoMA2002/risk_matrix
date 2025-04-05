<template>
  <div class="h-screen overflow-y-auto bg-gradient-to-br from-blue-600 to-indigo-100 font-sans">
    <div class="container mx-auto px-4 py-6">
      <!-- Header -->
      <header class="flex justify-between items-center mb-6">
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
        <div class="flex items-center space-x-2 text-white">
          <img src="@/assets/logoCCC.png" alt="Logo" class="h-16">
        </div>
      </header>

      <!-- Main Content -->
      <main class="flex flex-col md:flex-row gap-6">
        <!-- Sidebar: Questionnaires List (Moved to left for better information hierarchy) -->
        <aside class="w-full md:w-1/4 order-2 md:order-1">
          <div class="bg-white bg-opacity-90 backdrop-blur-sm rounded-xl shadow-md p-4 h-full">
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
        </aside>

        <!-- Categories Grid -->
        <section class="w-full md:w-3/4 order-1 md:order-2">
          <div class="max-w-4xl mx-auto">
            <div v-if="!selectedQuestionnaire" class="text-lg text-blue-800 p-4 bg-white bg-opacity-70 rounded-lg">
              <SkeletonLoader />
              <p>Carregando questionário...</p>
            </div>
            <div v-else>
              <div v-if="categories.length > 0" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                <CategoryCard v-for="cat in categories" :key="cat" :category="cat" :answered-count="answeredCount(cat)"
                  :total-count="totalCount(cat)" @click="goToCategory(cat)" />
              </div>
              <div v-else class="text-center p-8 bg-white bg-opacity-70 rounded-lg text-blue-800">
                <p>Nenhuma categoria encontrada neste questionário.</p>
              </div>
            </div>
          </div>

          <!-- Action Buttons -->
          <div class="mt-8 flex flex-wrap justify-center gap-4">
            <ActionButton icon="send" text="Enviar Todas as Respostas" color="blue" @click="submitAllAnswers" />
            <ActionButton icon="export" text="Exportar Progresso" color="green" @click="exportToJSON" />
            <ActionButton icon="import" text="Importar Progresso" color="red" @click="triggerImport" />
            <input type="file" ref="importFile" accept="application/json" class="hidden" @change="importFromJSON" />
          </div>
        </section>
      </main>

      <!-- Progress indicator -->
      <footer class="max-w-3xl mx-auto mt-10 px-4">
        <div class="flex items-center justify-between">
          <ProgressStep number="1" text="Informações" />
          <div class="w-16 h-1 bg-blue-400"></div>
          <ProgressStep number="2" text="Requisitos" />
          <div class="w-16 h-1 bg-blue-400"></div>
          <ProgressStep number="3" text="Questionário" />
        </div>
      </footer>

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
    <ConfirmDialog v-if="showConfirm" :message="confirmMessage" @confirm="handleConfirm" @cancel="handleCancel" />
  </div>
</template>
<script>
import axios from "axios";
import { mapState, mapActions } from "vuex";
import SkeletonLoader from "./SkeletonLoader.vue";
import CategoryCard from "./CategoryCard.vue";
import ActionButton from "./ActionButton.vue";
import ProgressStep from "./Progress.vue";
import ConfirmDialog from "./ConfirmDialogue.vue";
import { v4 as uuidv4 } from 'uuid';

export default {
  name: "CategoryList",
  components: { 
    SkeletonLoader, 
    CategoryCard,
    ActionButton,
    ProgressStep,
    ConfirmDialog
  },
  data() {
    return {
      showConfirm: false,
      confirmMessage: "",
      confirmResolve: null,
      confirmReject: null
    };
  },
  computed: {
    ...mapState(['questionnaires', 'selectedQuestionnaire', 'allAnswers']),
    categories() {
      if (this.selectedQuestionnaire && this.selectedQuestionnaire.questions) {
        return [
          ...new Set(
            this.selectedQuestionnaire.questions.map(q => {
              if (typeof q.category === "object" && q.category !== null) {
                return q.category.name;
              }
              return q.category;
            })
          )
        ];
      }
      return [];
    }
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
    // Helper method to display confirmation dialogs
    confirmAction(message) {
      this.confirmMessage = message;
      this.showConfirm = true;
      return new Promise((resolve, reject) => {
        this.confirmResolve = resolve;
        this.confirmReject = reject;
      });
    },
    handleConfirm() {
      if (this.confirmResolve) {
        this.confirmResolve(true);
      }
      this.resetConfirm();
    },
    handleCancel() {
      if (this.confirmReject) {
        this.confirmReject(false);
      }
      this.resetConfirm();
    },
    resetConfirm() {
      this.showConfirm = false;
      this.confirmMessage = "";
      this.confirmResolve = null;
      this.confirmReject = null;
    },
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
      // First, warn if there are answers for questions not belonging to the selected questionnaire.
      if (this.selectedQuestionnaire && this.selectedQuestionnaire.questions) {
        const selectedQuestionIds = new Set(this.selectedQuestionnaire.questions.map(q => q.id));
        let extraCount = 0;
        for (const category in this.allAnswers) {
          const categoryAnswers = this.allAnswers[category];
          for (const questionId in categoryAnswers) {
            if (!selectedQuestionIds.has(parseInt(questionId))) {
              extraCount++;
            }
          }
        }
        if (extraCount > 0) {
          const proceed = confirm("Você possui respostas que pertencem a outros questionários. Somente as respostas do questionário selecionado serão enviadas. Deseja continuar?");
          if (!proceed) {
            return;
          }
        }
      }

      try {
        await this.confirmAction("Tem a certeza que deseja enviar todas as respostas? Após enviar, o progresso guardado será limpo e você será redirecionado à página principal.");
      } catch {
        return; // User cancelled
      }
      try {
        const payload = [];
        const userEmail = localStorage.getItem("userEmail") || "fallback@example.com";
        // Generate a unique submissionId for this batch
        const submissionId = uuidv4();
        // Get list of question IDs from the currently selected questionnaire
        const selectedQuestionIds = this.selectedQuestionnaire && this.selectedQuestionnaire.questions
          ? this.selectedQuestionnaire.questions.map(q => q.id)
          : [];
        // Only include answers for questions that belong to the selected questionnaire
        for (const category in this.allAnswers) {
          const categoryAnswers = this.allAnswers[category];
          for (const questionId in categoryAnswers) {
            if (!selectedQuestionIds.includes(parseInt(questionId))) continue;
            const userResponse = categoryAnswers[questionId];
            if (!userResponse || !userResponse.trim()) {
              continue;
            }
            payload.push({
              questionId: parseInt(questionId),
              userResponse,
              email: userEmail,
              submissionId: submissionId
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
    async exportToJSON() {
      try {
        await this.confirmAction("Deseja exportar o progresso? Isso irá limpar o progresso salvo localmente.");
      } catch {
        return;
      }
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
      this.confirmAction("Ao importar, o progresso atual será substituído. Deseja continuar?")
        .then(() => {
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
        })
        .catch(() => {
          return;
        });
    },
    goToFeedbackForm() {
      this.$router.push("/feedback-form");
    },
    answeredCount(category) {
      const answersForCategory = this.allAnswers[category] || {};
      return Object.values(answersForCategory).filter(ans => ans && ans.trim() !== "").length;
    },
    totalCount(category) {
      if (this.selectedQuestionnaire && this.selectedQuestionnaire.questions) {
        return this.selectedQuestionnaire.questions.filter(q => {
          const catString = (typeof q.category === "object" && q.category !== null)
            ? q.category.name
            : q.category;
          return catString === category;
        }).length;
      }
      return 0;
    },
    selectQuestionnaire(id) {
  // Clear previous answers before switching
  this.$store.commit("clearAllAnswers");
  // Fetch and update the selected questionnaire
  this.fetchQuestionnaireById(id);
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
</style>
