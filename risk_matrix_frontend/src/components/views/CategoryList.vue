<template>
  <div class="h-screen overflow-y-auto bg-gradient-to-br from-blue-600 to-indigo-100 font-sans">
    <div class="container mx-auto px-4 py-6">
      <!-- Header -->
      <header class="flex justify-between items-center mb-6">
        <div class="flex items-center">
          <button @click="$router.push('/requirements')"
            class="p-2 rounded-full bg-white bg-opacity-20 backdrop-blur-sm text-white hover:bg-opacity-30 transition-all duration-300 mr-4 focus:outline-none focus:ring-2 focus:ring-white focus:ring-offset-2 focus:ring-offset-blue-600"
            :aria-label="'Voltar à página de requisitos'">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24"
              stroke="currentColor" aria-hidden="true">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
            </svg>
          </button>
          <div class="text-white">
            <h1 class="text-2xl font-bold">
              Categorias de Questões
              <span v-if="selectedQuestionnaire" class="text-blue-200">| {{ selectedQuestionnaire.title }}</span>
            </h1>
            <p class="text-sm opacity-80">Selecione uma categoria para responder às perguntas</p>
          </div>
        </div>
        <div class="flex items-center space-x-2 text-white">
          <img src="@/assets/logoCCC.webp" alt="Logo CCC" class="h-16" />
        </div>
      </header>

      <!-- Main Content -->
      <main class="flex flex-col gap-6">
        <!-- Questionnaires Dropdown -->
        <div class="w-full max-w-md mx-auto">
          <div class="bg-white bg-opacity-90 backdrop-blur-sm rounded-xl shadow-md p-4 transform transition-all duration-300 hover:shadow-lg">
            <h3 class="text-lg font-semibold text-blue-800 mb-3">Questionários</h3>
            <div class="relative">
              <select 
                v-if="!loading" 
                class="w-full bg-white border border-blue-200 rounded-lg py-2 px-3 appearance-none cursor-pointer focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all duration-300"
                @change="selectQuestionnaire($event.target.value)"
                :value="selectedQuestionnaire ? selectedQuestionnaire.id : ''"
                :aria-label="'Selecionar questionário'">
                <option v-for="qnr in questionnaires" :key="qnr.id" :value="qnr.id">
                  {{ qnr.title }}
                </option>
              </select>
              <div v-else class="h-10 bg-blue-100 rounded animate-pulse"></div>
              <div class="pointer-events-none absolute inset-y-0 right-0 flex items-center px-2 text-blue-800">
                <svg class="h-5 w-5" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                  <path fill-rule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clip-rule="evenodd" />
                </svg>
              </div>
            </div>
          </div>
        </div>

        <!-- Categories Grid -->
        <section class="w-full">
          <div class="max-w-4xl mx-auto">
            <transition name="fade" mode="out-in">
              <div v-if="!loading && categories.length > 0" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6 justify-items-center">
                <CategoryCard
                  v-for="cat in categories"
                  :key="cat"
                  :category="cat"
                  :answered-count="answeredCount(cat)"
                  :total-count="totalCount(cat)"
                  @click="goToCategory(cat)"
                  class="w-full transform transition-all duration-300 hover:scale-105"
                />
              </div>
              <div v-else-if="!loading" class="text-center p-8 bg-white bg-opacity-70 rounded-lg text-blue-800">
                <p>Nenhuma categoria encontrada neste questionário.</p>
              </div>
              <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6 justify-items-center">
                <div v-for="i in 6" :key="i" class="bg-white bg-opacity-90 backdrop-blur-sm rounded-xl shadow-md p-6 w-full">
                  <div class="h-6 bg-blue-100 rounded w-3/4 animate-pulse mb-4"></div>
                  <div class="flex justify-between items-center">
                    <div class="h-4 bg-blue-100 rounded w-1/4 animate-pulse"></div>
                    <div class="h-4 bg-blue-100 rounded w-1/4 animate-pulse"></div>
                  </div>
                </div>
              </div>
            </transition>
          </div>

          <!-- Action Buttons -->
          <div class="mt-8 flex flex-wrap justify-center gap-4">
            <button
              @click="submitAllAnswers"
              :disabled="isSubmitting"
              class="action-btn bg-blue-600 hover:bg-blue-700 focus:ring-blue-400"
              aria-label="Enviar Todas as Respostas"
            >
              <svg class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              <span v-if="!isSubmitting">Submeter</span>
              <span v-else class="loader"></span>
            </button>
            <button
              @click="exportToJSON()"
              class="action-btn bg-green-600 hover:bg-green-700 focus:ring-green-400"
              aria-label="Exportar Progresso"
            >
              <svg class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
              </svg>
              Exportar
            </button>
            <button
              @click="triggerImport"
              class="action-btn bg-purple-600 hover:bg-purple-700 focus:ring-purple-400"
              aria-label="Importar Progresso"
            >
              <svg class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v2a2 2 0 002 2h12a2 2 0 002-2v-2M7 10l5 5 5-5M12 15V3" />
              </svg>
              Importar
            </button>
            <input 
              type="file" 
              ref="importFile" 
              accept="application/json" 
              class="hidden" 
              @change="importFromJSON" />
          </div>
        </section>
      </main>

      <!-- Progress -->
      <footer class="max-w-3xl mx-auto mt-10 px-4">
        <div class="flex items-center justify-between">
          <ProgressStep number="1" text="Informações" />
          <div class="w-16 h-1 bg-blue-400"></div>
          <ProgressStep number="2" text="Requisitos" />
          <div class="w-16 h-1 bg-blue-400"></div>
          <ProgressStep number="3" text="Questionário" />
        </div>
      </footer>

      <!-- Help Button -->
      <div class="fixed bottom-6 right-6">
        <button @click="goToFeedbackForm"
          class="p-4 bg-white rounded-full shadow-lg hover:shadow-xl transition-all duration-300 text-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
          :aria-label="'Ir para o formulário de feedback'">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </button>
      </div>

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
  </div>
</template>

<script>
import axios from "axios";
import { mapState, mapActions } from "vuex";
import CategoryCard from "@/components/Questionnaire/CategoryCard.vue";
import ProgressStep from "@/components/Static/Progress.vue";
import AlertDialog from "@/components/Static/AlertDialog.vue";
import { v4 as uuidv4 } from 'uuid';

export default {
  name: "CategoryList",
  components: { 
    CategoryCard,
    ProgressStep,
    AlertDialog
  },
  data() {
    return {
      showAlert: false,
      alertTitle: "",
      alertMessage: "",
      alertType: "alert",
      alertResolve: null,
      loading: true,
      isSubmitting: false
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
  async created() {
    const selectedId = this.$route.query.selected;
    await this.fetchQuestionnaires();
    
    if (selectedId) {
      await this.fetchQuestionnaireById(selectedId);
    } else if (this.questionnaires.length > 0) {
      await this.fetchQuestionnaireById(this.questionnaires[0].id);
    }

    this.loading = false;
  },
  methods: {
    ...mapActions(["fetchQuestionnaires", "fetchQuestionnaireById"]),
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
    formatCategoryName(rawEnum) {
      if (!rawEnum) return "";
      
      return rawEnum.replace(/_/g, " ")
                   .split(' ')
                   .map(word => {
                     if (!word) return '';
                     return word.charAt(0).toLocaleUpperCase('pt-PT') + 
                            word.slice(1).toLocaleLowerCase('pt-PT');
                   })
                   .join(' ');
    },
    goToCategory(category) {
      this.$router.push({
        name: "Questionary",
        params: { category, questionnaireId: this.selectedQuestionnaire.id },
      });
    },
    async submitAllAnswers() {
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
          const proceed = await this.showAlertDialog(
            "Aviso",
            "Você possui respostas que pertencem a outros questionários. Somente as respostas do questionário selecionado serão enviadas. Deseja continuar?",
            "confirm"
          );
          if (!proceed) {
            return;
          }
        }
      }

      try {
        this.isSubmitting = true;
        // First confirmation - ask if they want to export
        const wantToExport = await this.showAlertDialog(
          "Exportar Respostas",
          "Deseja exportar as respostas antes de enviar?",
          "confirm"
        );

        if (wantToExport) {
          await this.exportToJSON(false); // Don't clear after export when part of submission
        }

        // Second confirmation - proceed with submission
        const proceed = await this.showAlertDialog(
          "Confirmar Envio",
          "Tem a certeza que deseja enviar todas as respostas? Após enviar, o progresso guardado será limpo e você será redirecionado à página principal.",
          "confirm"
        );
        if (!proceed) {
          return;
        }
        
        const payload = [];
        const userEmail = localStorage.getItem("userEmail");
        const submissionId = uuidv4();
        const selectedQuestionIds = this.selectedQuestionnaire && this.selectedQuestionnaire.questions
          ? this.selectedQuestionnaire.questions.map(q => q.id)
          : [];

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
          await this.showAlertDialog("Aviso", "Nenhuma resposta para enviar!");
          return;
        }

        await axios.post("/api/answers/submit-multiple", payload);
        await this.showAlertDialog("Sucesso", "Todas as respostas foram enviadas com sucesso!", "success");
        this.$store.commit("clearAllAnswers");
        this.$router.push("/");
      } catch (error) {
        console.error("Erro ao enviar respostas:", error);
        await this.showAlertDialog("Erro", "Ocorreu um erro ao enviar as respostas.", "error");
      } finally {
        this.isSubmitting = false;
      }
    },
    async exportToJSON(shouldClearAfterExport = true) {
      try {
        const proceed = await this.showAlertDialog(
          "Exportar Progresso",
          "Deseja exportar o progresso? Isso irá limpar o progresso salvo localmente.",
          "confirm"
        );
        if (!proceed) return;

        const allAnswersArray = [];

        for (const category in this.allAnswers) {
          for (const questionId in this.allAnswers[category]) {
            allAnswersArray.push({
              category,
              questionId: parseInt(questionId),
              selectedOption: this.allAnswers[category][questionId]
            });
          }
        }

        if (!allAnswersArray.length) {
          await this.showAlertDialog("Aviso", "Nenhuma resposta para exportar!");
          return;
        }

        const blob = new Blob([JSON.stringify({ answers: allAnswersArray }, null, 2)], {
          type: "application/json"
        });

        const url = URL.createObjectURL(blob);
        const link = document.createElement("a");
        link.href = url;
        link.download = "progress.json";
        link.click();
        URL.revokeObjectURL(url);

        if (shouldClearAfterExport) {
          this.$store.commit("clearAllAnswers");
        }

      } catch (error) {
        await this.showAlertDialog("Erro", "Ocorreu um erro ao exportar o progresso.");
      }
    },
    triggerImport() {
      this.$refs.importFile.click();
    },
    importFromJSON(event) {
      const file = event.target.files[0];
      if (!file) {
        return;
      }

      const reader = new FileReader();
      reader.onload = (e) => {
        try {
          const importedData = JSON.parse(e.target.result);

          // Validate imported data structure
          if (!importedData || typeof importedData !== 'object' || !Array.isArray(importedData.answers)) {
            throw new Error('Formato de arquivo inválido.');
          }

          // Merge answers
          let mergedCount = 0;
          let skippedCount = 0;
          const currentAnswers = JSON.parse(JSON.stringify(this.allAnswers));

          importedData.answers.forEach(answerSet => {
            const { category, questionId, selectedOption } = answerSet;
            if (category && questionId && selectedOption) {
              if (!currentAnswers[category]) {
                currentAnswers[category] = {};
              }
              if (!currentAnswers[category][questionId]) {
                currentAnswers[category][questionId] = selectedOption;
                mergedCount++;
              } else {
                skippedCount++;
              }
            }
          });

          // Update the store
          this.$store.commit('setAllAnswers', currentAnswers);

          let message = `Progresso importado com sucesso. ${mergedCount} respostas foram adicionadas.`;
          if (skippedCount > 0) {
            message += ` ${skippedCount} respostas já existentes foram ignoradas.`;
          }

          this.showAlertDialog("Importação Concluída", message, "success");
          // Reset file input to allow importing the same file again
          this.$refs.importFile.value = null;

        } catch (error) {
          console.error("Erro ao importar o arquivo:", error);
          this.showAlertDialog("Erro de Importação", `Falha ao importar o arquivo: ${error.message}`, "error");
          // Reset file input in case of error
          this.$refs.importFile.value = null;
        }
      };
      reader.readAsText(file);
    },
    goToFeedbackForm() {
      this.$router.push("/feedback-form");
    },
    answeredCount(category) {
      const answersForCategory = this.allAnswers[category] || {};
      return Object.values(answersForCategory).filter(ans => typeof ans === 'string' && ans.trim() !== "").length;
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
      this.$store.commit("clearAllAnswers");
      this.$store.commit("setSelectedQuestionnaireId", id);
      this.fetchQuestionnaireById(id);
    }
  }
};
</script>

<style scoped>
/* Add smooth transitions for better interactivity */
button {
  transition: all 0.2s ease-in-out;
}

button:hover {
  transform: translateY(-1px);
}

button:active {
  transform: translateY(0);
}

/* Improve focus styles for better accessibility */
button:focus-visible,
input:focus-visible,
select:focus-visible {
  outline: 2px solid #4f46e5;
  outline-offset: 2px;
}

/* Add hover animations for cards */
.hover-scale {
  transition: transform 0.3s ease;
}

.hover-scale:hover {
  transform: scale(1.02);
}

/* Fade transition */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* Loading animation */
@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.animate-pulse {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1.25rem;
  border-radius: 0.5rem;
  font-weight: 600;
  color: #fff;
  box-shadow: 0 2px 8px 0 rgba(0,0,0,0.08);
  transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
  outline: none;
}
.action-btn:focus-visible {
  outline: 2px solid #4f46e5;
  outline-offset: 2px;
}
.action-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.loader {
  border: 2px solid #fff;
  border-top: 2px solid #3498db;
  border-radius: 50%;
  width: 1rem;
  height: 1rem;
  animation: spin 1s linear infinite;
  display: inline-block;
}
@keyframes spin {
  0% { transform: rotate(0deg);}
  100% { transform: rotate(360deg);}
}
</style>