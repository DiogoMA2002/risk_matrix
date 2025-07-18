<template>
  <div class="bg-white bg-opacity-90 backdrop-blur-sm rounded-xl shadow-md p-6 mb-6">
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

    <h2 class="text-xl font-semibold mb-4 text-blue-800 flex items-center">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
      Respostas dos Utilizadores
    </h2>
    <p class="text-sm text-gray-600 mt-1">
      Pressione <kbd class="px-2 py-1 text-xs font-semibold text-gray-800 bg-gray-100 border border-gray-200 rounded">Ctrl + F</kbd> para filtrar por email,
      <kbd class="px-2 py-1 text-xs font-semibold text-gray-800 bg-gray-100 border border-gray-200 rounded">Ctrl + R</kbd> para atualizar a lista
    </p>

    <!-- Search and Filter Controls -->
    <div class="space-y-4 mb-6">
      <div>
        <label for="email-filter" class="block text-sm font-medium text-gray-700 mb-1">
          Filtrar por Email
          <span class="text-xs text-gray-500">(Ctrl + F)</span>
        </label>
        <input id="email-filter" v-model="filters.email" type="email" placeholder="Escreva o email"
          class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-300"
          :aria-label="'Filtrar por email'" />
      </div>
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div>
          <label for="start-date" class="block text-sm font-medium text-gray-700 mb-1">Data Inicial</label>
          <input id="start-date" v-model="filters.startDate" type="date"
            class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all"
            :aria-label="'Data inicial'" />
        </div>
        <div>
          <label for="end-date" class="block text-sm font-medium text-gray-700 mb-1">Data Final</label>
          <input id="end-date" v-model="filters.endDate" type="date"
            class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all"
            :aria-label="'Data final'" />
        </div>
        <div class="flex items-end">
          <button @click="applyDateFilter" :disabled="isLoading"
            class="w-full px-4 py-2 bg-indigo-600 text-white rounded-lg shadow hover:bg-indigo-700 focus:ring-2 focus:ring-indigo-400 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center"
            :aria-label="'Aplicar filtro por data'">
            <svg v-if="isLoading" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            {{ isLoading ? 'A processar...' : 'Filtrar por Data' }}
          </button>
        </div>
      </div>
      <div class="flex flex-col sm:flex-row gap-3">
        <button @click="searchByEmail" :disabled="isLoading"
          class="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg shadow hover:bg-blue-700 focus:ring-2 focus:ring-blue-400 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center"
          :aria-label="'Procurar por email'">
          <svg v-if="isLoading" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          {{ isLoading ? 'A processar...' : 'Procurar por Email' }}
        </button>
        <button @click="fetchAllResponses" :disabled="isLoading"
          class="flex-1 px-4 py-2 bg-green-600 text-white rounded-lg shadow hover:bg-green-700 focus:ring-2 focus:ring-green-400 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center"
          :aria-label="'Ver todas as respostas'">
          <svg v-if="isLoading" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          {{ isLoading ? 'A processar...' : 'Ver Todas as Respostas' }}
        </button>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="isLoading" class="py-8 text-center">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-4 border-gray-300 border-t-blue-600"></div>
      <p class="mt-2 text-gray-600">A carregar resultados...</p>
    </div>

    <!-- Result List -->
    <div v-else-if="userAnswers && userAnswers.length > 0" class="border rounded-lg overflow-hidden">
      <ul class="divide-y divide-gray-200">
        <li v-for="response in userAnswers" :key="response.submissionId" class="p-4 hover:bg-gray-50 transition-colors">
          <div class="flex flex-wrap justify-between items-center gap-2 mb-2">
            <div>
              <h3 class="font-medium text-gray-900">
                Submissão: <span class="font-semibold">{{ response.submissionId }}</span>
              </h3>
              <p class="text-sm text-gray-600">{{ response.email }}</p>
              <p class="text-xs text-gray-500">Data: {{ formatDate(response.createdAt) }}</p>
            </div>
            <button @click="$emit('download-report', response.submissionId)"
              class="px-3 py-1.5 bg-blue-600 text-white text-sm rounded hover:bg-blue-700 focus:ring-2 focus:ring-blue-400 transition-all flex items-center">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd"
                  d="M3 17a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zm3.293-7.707a1 1 0 011.414 0L9 10.586V3a1 1 0 112 0v7.586l1.293-1.293a1 1 0 111.414 1.414l-3 3a1 1 0 01-1.414 0l-3-3a1 1 0 010-1.414z"
                  clip-rule="evenodd" />
              </svg>
              Descarregar Relatório
            </button>
          </div>

          <!-- Category Summary -->
          <div class="flex flex-wrap gap-2 mb-3">
            <span v-for="(severity, category) in response.severitiesByCategory" :key="category"
              class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
              :class="getSeverityColorClass(severity)">
              {{ formatCategoryName(category) }}: {{ severity }}
            </span>
          </div>

          <!-- Toggle button -->
          <button @click="toggleAnswers(response.submissionId)"
            class="text-sm text-blue-600 hover:underline mt-1 flex items-center gap-1">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 transition-transform duration-300"
              :class="{ 'rotate-180': showAnswers[response.submissionId] }" fill="none" viewBox="0 0 24 24"
              stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
            </svg>
            {{ showAnswers[response.submissionId] ? 'Ocultar Respostas' : 'Ver Respostas' }}
          </button>

          <!-- Animated Answer Section -->
          <transition name="fade-slide">
            <div v-if="showAnswers[response.submissionId]" class="mt-3 border-t border-gray-100 pt-3">
              <ul class="space-y-2">
                <li v-for="answer in response.answers" :key="answer.id" class="text-sm">
                  <p class="font-medium text-gray-700">{{ answer.questionText }}</p>
                  <p class="ml-4 text-gray-600">{{ answer.userResponse }}</p>
                </li>
              </ul>
            </div>
          </transition>
        </li>
      </ul>
    </div>

    <!-- Empty State -->
    <div v-else class="py-8 text-center bg-gray-50 rounded-lg">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 mx-auto text-gray-400" fill="none" viewBox="0 0 24 24"
        stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
          d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
      <p class="mt-2 text-gray-500">Nenhuma resposta encontrada.</p>
      <p class="text-sm text-gray-400">Tente aplicar filtros diferentes ou verifique se existem dados disponíveis.</p>
    </div>
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex';

export default {
  name: "UserAnswersList",
  data() {
    return {
      filters: {
        email: "",
        startDate: "",
        endDate: ""
      },
      showAnswers: {},
      pollInterval: null,
      toast: {
        show: false,
        message: '',
        type: 'success',
        timeout: null
      }
    };
  },
  computed: {
    ...mapState({
      userAnswers: state => state.userAnswers,
      isLoading: state => state.isLoadingAnswers
    })
  },
  mounted() {
    this.fetchAllResponses();
    this.startPolling();
    // Add keyboard event listeners
    window.addEventListener('keydown', this.handleKeyboardShortcut);
  },
  beforeUnmount() {
    this.stopPolling();
    // Remove keyboard event listeners
    window.removeEventListener('keydown', this.handleKeyboardShortcut);
  },
  methods: {
    ...mapActions([
      'fetchUserAnswersByEmail',
      'fetchAllUserAnswers',
      'filterAnswersByDate'
    ]),
    startPolling() {
      // Clear any existing interval
      this.stopPolling();
      // Set new interval to fetch answers every 30 seconds
      this.pollInterval = setInterval(() => {
        if (!this.filters.email && !this.filters.startDate) {
          // Only auto-refresh if no filters are applied
          this.fetchAllResponses();
        }
      }, 30000); // 30 seconds
    },
    stopPolling() {
      if (this.pollInterval) {
        clearInterval(this.pollInterval);
        this.pollInterval = null;
      }
    },
    showToast(message, type = 'success') {
      // Clear any existing timeout
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
      // Ctrl + F to focus email filter
      if (event.ctrlKey && event.key === 'f') {
        event.preventDefault();
        document.getElementById('email-filter').focus();
      }
      // Ctrl + R to refresh list
      if (event.ctrlKey && event.key === 'r') {
        event.preventDefault();
        this.fetchAllResponses();
      }
    },
    toggleAnswers(id) {
      this.showAnswers = {
        ...this.showAnswers,
        [id]: !this.showAnswers[id]
      };
    },
    formatCategoryName(category) {
      if (!category) return "";
      
      return category
        .replace(/_/g, " ")
        .split(' ')
        .map(word => {
          if (!word) return '';
          return word.charAt(0).toLocaleUpperCase('pt-PT') + 
                 word.slice(1).toLocaleLowerCase('pt-PT');
        })
        .join(' ');
    },
    async searchByEmail() {
      if (!this.filters.email.trim()) {
        this.showToast("Por favor, introduza um email válido", "error");
        return;
      }
      try {
        await this.fetchUserAnswersByEmail(this.filters.email);
        this.stopPolling();
        this.showToast("Resultados atualizados com sucesso");
      } catch (error) {
        this.showToast(error.message || "Erro ao buscar respostas", "error");
      }
    },
    async fetchAllResponses() {
      try {
        await this.fetchAllUserAnswers();
        this.startPolling();
        this.showToast("Lista atualizada com sucesso");
      } catch (error) {
        this.showToast(error.message || "Erro ao carregar respostas", "error");
      }
    },
    async applyDateFilter() {
      if (!this.filters.startDate || !this.filters.endDate) {
        this.showToast("Por favor, selecione as datas inicial e final", "error");
        return;
      }
      try {
        await this.filterAnswersByDate({
          startDate: this.filters.startDate,
          endDate: this.filters.endDate
        });
        this.stopPolling();
        this.showToast("Filtro aplicado com sucesso");
      } catch (error) {
        this.showToast(error.message || "Erro ao aplicar filtro", "error");
      }
    },
    getSeverityColorClass(severity) {
      const s = (severity || "").toLowerCase();
      if (s.includes("alto") || s.includes("high")) return "bg-red-100 text-red-800";
      if (s.includes("medio") || s.includes("medium")) return "bg-yellow-100 text-yellow-800";
      if (s.includes("baixo") || s.includes("low")) return "bg-green-100 text-green-800";
      if (s.includes("critico") || s.includes("critical")) return "bg-red-100 text-red-800";
      return "bg-blue-100 text-blue-800";
    },
    formatDate(date) {
      if (!date) return "N/A";
      const options = { year: 'numeric', month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit' };
      return new Date(date).toLocaleDateString('pt-PT', options);
    }
  }
};
</script>

<style scoped>
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* Add smooth transitions for loading states */
button {
  transition: all 0.2s ease-in-out;
}

button:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

/* Add hover effect for better interactivity */
button:not(:disabled):hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}

button:not(:disabled):active {
  transform: translateY(0);
}

/* Add toast animation styles */
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

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
select:focus-visible {
  outline: 2px solid #4f46e5;
  outline-offset: 2px;
}

*:focus-visible {
  box-shadow: 0 0 0 2px #6366f1;
}
</style>
