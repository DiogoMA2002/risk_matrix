<template>
  <div class="bg-white bg-opacity-90 backdrop-blur-sm rounded-xl shadow-md p-6 mb-6">
    <header class="mb-6">
      <h2 class="text-xl font-semibold text-blue-800 flex items-center">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        Adicionar Nova Questão
      </h2>
      <p class="text-sm text-gray-600 mt-1">
        Pressione <kbd class="px-2 py-1 text-xs font-semibold text-gray-800 bg-gray-100 border border-gray-200 rounded">Ctrl + Q</kbd> para focar no campo de questão
      </p>
    </header>

    <div class="space-y-6">
      <!-- Question Text -->
      <div>
        <label for="question-text" class="block text-sm font-medium text-gray-700 mb-1">
          Texto da Questão
          <span class="text-xs text-gray-500">(Ctrl + Q)</span>
        </label>
        <textarea id="question-text" v-model="newQuestion" rows="3"
          class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all"
          placeholder="Escreva o texto da questão"
          :disabled="isLoading"
          :aria-label="'Texto da questão'"></textarea>
      </div>

      <!-- Category Selection -->
      <div>
        <label for="category-select" class="block text-sm font-medium text-gray-700 mb-1">Categoria</label>
        <select id="category-select" v-model="selectedCategory"
          class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all"
          :disabled="isLoading"
          :aria-label="'Selecionar categoria'">
          <option value="">Selecione uma categoria</option>
          <option v-for="cat in categories" :key="cat.id" :value="cat.name">
            {{ formatCategoryName(cat.name) }}
          </option>
        </select>
      </div>

      <!-- Questionnaire Selection -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Questionários</label>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <label v-for="questionnaire in questionnaires" :key="questionnaire.id"
            class="flex items-center p-3 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors cursor-pointer">
            <input type="checkbox" v-model="selectedQuestionnaires" :value="questionnaire.id"
              class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
              :disabled="isLoading" />
            <span class="ml-2 text-gray-900">{{ questionnaire.title }}</span>
          </label>
        </div>
      </div>

      <!-- Options -->
      <div class="space-y-4">
        <h3 class="text-lg font-medium text-gray-900">Opções de Resposta</h3>
        <div v-for="(option, index) in newOptions" :key="index" class="p-4 bg-gray-50 rounded-lg">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <!-- Option Text -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Texto da Opção</label>
              <input v-model="option.optionText" type="text"
                class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all"
                placeholder="Escreva o texto da opção"
                :disabled="isLoading"
                :aria-label="'Texto da opção'" />
            </div>

            <!-- Option Type -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Tipo</label>
              <select v-model="option.optionType"
                class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all"
                :disabled="isLoading"
                :aria-label="'Tipo da opção'"
                @change="lockOptionType(option.optionType)">
                <option v-for="type in optionTypes" :key="type" :value="type">
                  {{ optionTypeLabels[type] }}
                </option>
              </select>
            </div>

            <!-- Option Level -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Nível</label>
              <select v-model="option.optionLevel"
                class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all"
                :disabled="isLoading"
                :aria-label="'Nível da opção'">
                <option v-for="level in optionLevels" :key="level" :value="level">
                  {{ optionLevelLabels[level] }}
                </option>
              </select>
            </div>

            <!-- Recommendation -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Recomendação</label>
              <input v-model="option.recommendation" type="text"
                class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all"
                placeholder="Texto da recomendação"
                :disabled="isLoading"
                :aria-label="'Recomendação'" />
            </div>
          </div>

          <!-- Remove Option -->
          <div class="mt-2 flex justify-end">
            <button v-if="newOptions.length > 1" @click="removeOption(index)"
              class="text-red-600 hover:text-red-800 transition-colors focus:ring-2 focus:ring-red-400 rounded-lg p-1"
              :disabled="isLoading"
              :aria-label="'Remover opção'">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            </button>
          </div>
        </div>

        <!-- Add Option Button -->
        <button @click="addOption"
          class="w-full px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 focus:ring-2 focus:ring-green-400 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center"
          :disabled="isLoading">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
          </svg>
          Adicionar outra opção
        </button>
      </div>

      <!-- Optional Description -->
      <div>
        <label for="description-text" class="block text-sm font-medium text-gray-700 mb-1">
          Descrição (opcional)
        </label>
        <textarea id="description-text" v-model="description" rows="2"
          class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all"
          placeholder="Descrição adicional sobre a questão (opcional)"
          :disabled="isLoading"
          :aria-label="'Descrição da questão'"></textarea>
      </div>

      <!-- Submit Button -->
      <div class="pt-4">
        <button @click="submitQuestion"
          class="w-full px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 focus:ring-2 focus:ring-blue-400 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center"
          :disabled="isLoading || !isFormValid">
          <svg v-if="isLoading" class="animate-spin -ml-1 mr-2 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          {{ isLoading ? 'A processar...' : 'Adicionar Questão' }}
        </button>
      </div>
    </div>

    <!-- Toast Notification -->
    <transition
      enter-active-class="transform ease-out duration-300 transition"
      enter-from-class="translate-y-2 opacity-0 sm:translate-y-0 sm:translate-x-2"
      enter-to-class="translate-y-0 opacity-100 sm:translate-x-0"
      leave-active-class="transition ease-in duration-100"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div v-if="toast.show"
        class="fixed top-4 right-4 z-50 max-w-sm w-full bg-white shadow-lg rounded-lg pointer-events-auto ring-1 ring-black ring-opacity-5 overflow-hidden"
        :class="{
          'bg-green-50': toast.type === 'success',
          'bg-red-50': toast.type === 'error'
        }"
      >
        <div class="p-4">
          <div class="flex items-start">
            <div class="flex-shrink-0">
              <svg v-if="toast.type === 'success'" class="h-6 w-6 text-green-400" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <svg v-else class="h-6 w-6 text-red-400" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <div class="ml-3 w-0 flex-1 pt-0.5">
              <p class="text-sm font-medium text-gray-900" :class="{
                'text-green-800': toast.type === 'success',
                'text-red-800': toast.type === 'error'
              }">
                {{ toast.message }}
              </p>
            </div>
            <div class="ml-4 flex-shrink-0 flex">
              <button
                @click="toast.show = false"
                class="bg-white rounded-md inline-flex text-gray-400 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
              >
                <span class="sr-only">Fechar</span>
                <svg class="h-5 w-5" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd" />
                </svg>
              </button>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
export default {
  name: "QuestionForm",
  props: {
    categories: {
      type: Array,
      default: () => [],
    },
    questionnaires: {
      type: Array,
      default: () => [],
    },
    optionTypes: {
      type: Array,
      default: () => ["IMPACT", "PROBABILITY"],
    },
    optionLevels: {
      type: Array,
      default: () => ["LOW", "MEDIUM", "HIGH"],
    },
  },
  data() {
    return {
      newQuestion: "",
      description: "", // nova propriedade
      selectedCategory: "",
      selectedQuestionnaires: [],
      newOptions: [this.createDefaultOption()],
      isLoading: false,
      toast: {
        show: false,
        message: '',
        type: 'success',
        timeout: null
      }
    };
  },
  computed: {
    isFormValid() {
      return this.newQuestion.trim() &&
        this.selectedCategory.trim() &&
        this.selectedQuestionnaires.length > 0 &&
        this.newOptions.every(opt => opt.optionText.trim());
    },
    optionTypeLabels() {
      return {
        'IMPACT': 'Impacto',
        'PROBABILITY': 'Probabilidade'
      };
    },
    optionLevelLabels() {
      return {
        'LOW': 'Baixo',
        'MEDIUM': 'Médio',
        'HIGH': 'Alto'
      };
    }
  },
  mounted() {
    window.addEventListener('keydown', this.handleKeyboardShortcut);
  },
  beforeUnmount() {
    window.removeEventListener('keydown', this.handleKeyboardShortcut);
  },
  methods: {
    createDefaultOption() {
      return {
        optionText: "",
        optionType: "IMPACT",
        optionLevel: "LOW",
        recommendation: ""
      };
    },
    showToast(message, type = 'success') {
      if (this.toast.timeout) {
        clearTimeout(this.toast.timeout);
      }
      
      this.toast = {
        show: true,
        message,
        type,
        timeout: setTimeout(() => {
          this.toast.show = false;
        }, 3000)
      };
    },
    handleKeyboardShortcut(event) {
      if (event.ctrlKey && event.key === 'q') {
        event.preventDefault();
        document.getElementById('question-text').focus();
      }
    },
    formatCategoryName(name) {
      if (!name || typeof name !== 'string') return '';
      return name
        .replace(/_/g, " ")
        .split(" ")
        .map(w => w.charAt(0).toUpperCase() + w.slice(1).toLowerCase())
        .join(" ");
    },
    addOption() {
      this.newOptions.push(this.createDefaultOption());
    },
    removeOption(index) {
      if (this.newOptions.length > 1) {
        this.newOptions.splice(index, 1);
      }
    },
    lockOptionType(type) {
      this.newOptions.forEach(opt => {
        opt.optionType = type;
      });
    },
    async submitQuestion() {
      if (!this.isFormValid) {
        this.showToast("Por favor, preencha todos os campos obrigatórios.", "error");
        return;
      }

      this.isLoading = true;
      try {
        const questionData = {
          newQuestion: this.newQuestion.trim(),
          description: this.description.trim() || null, // inclui a descrição, se existir
          selectedCategory: this.selectedCategory.trim(),
          newOptions: this.newOptions.map(opt => ({
            optionText: opt.optionText.trim(),
            optionType: opt.optionType,
            optionLevel: opt.optionLevel,
            recommendation: opt.recommendation?.trim() || ""
          })),
          selectedQuestionnaires: this.selectedQuestionnaires
        };

        await this.$emit("add-question", questionData);
        this.resetForm();
        this.showToast("Questão adicionada com sucesso!");
      } catch (error) {
        this.showToast(error.message || "Erro ao adicionar questão", "error");
      } finally {
        this.isLoading = false;
      }
    },
    resetForm() {
      this.newQuestion = "";
      this.description = "";
      this.selectedCategory = "";
      this.selectedQuestionnaires = [];
      this.newOptions = [this.createDefaultOption()];
    }
  }
};
</script>

<style scoped>
/* Add keyboard shortcut styles */
kbd {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
  background-color: #f3f4f6;
  border: 1px solid #e5e7eb;
  border-radius: 0.25rem;
  box-shadow: 0 1px 1px rgba(0, 0, 0, 0.1);
  padding: 0.125rem 0.375rem;
  font-size: 0.75rem;
  line-height: 1.25rem;
  color: #374151;
}

/* Improve focus styles for better accessibility */
button:focus-visible,
input:focus-visible,
select:focus-visible,
textarea:focus-visible {
  outline: 2px solid #4f46e5;
  outline-offset: 2px;
}

/* Add focus ring for keyboard navigation */
*:focus-visible {
  box-shadow: 0 0 0 2px #6366f1;
}

/* Add hover effect for better interactivity */
button:not(:disabled):hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}

button:not(:disabled):active {
  transform: translateY(0);
}

/* Add smooth transitions */
.transition-all {
  transition-property: all;
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
  transition-duration: 150ms;
}
</style>
