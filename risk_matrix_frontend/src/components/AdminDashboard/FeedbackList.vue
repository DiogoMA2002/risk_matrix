<template>
  <div class="bg-white bg-opacity-90 backdrop-blur-sm rounded-xl shadow-md p-6 mb-8">
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
        class="fixed top-4 right-4 z-[100] max-w-sm w-full bg-white shadow-lg rounded-lg pointer-events-auto ring-1 ring-black ring-opacity-5 overflow-hidden"
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
      <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
      Feedback dos Utilizadores
    </h2>
    <!-- Feedback Filter -->
    <div class="mb-4 space-y-4">
      <!-- Email Filter -->
      <div class="flex-1">
        <label class="block text-sm font-medium text-gray-700 mb-1">Filtrar por Email</label>
        <input v-model="filters.email" type="email" placeholder="Digite o email" 
          class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 transition-all duration-300" />
      </div>
      
      <!-- Type Filter -->
      <div class="flex-1">
        <label class="block text-sm font-medium text-gray-700 mb-1">Tipo</label>
        <select v-model="filters.type" 
          class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 transition-all duration-300">
          <option value="">Todos</option>
          <option value="SUGGESTION">Sugestão</option>
          <option value="HELP">Ajuda</option>
        </select>
      </div>

      <!-- Date Range Filter -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Data Inicial</label>
          <input v-model="filters.startDate" type="date" 
            class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 transition-all duration-300" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Data Final</label>
          <input v-model="filters.endDate" type="date" 
            class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 transition-all duration-300" />
        </div>
      </div>

      <!-- Filter Button -->
      <div class="flex justify-end">
        <button @click="applyFilters" 
          class="px-4 py-2 bg-blue-600 text-white rounded-lg shadow-md hover:bg-blue-700 transition-all duration-300">
          Aplicar Filtros
        </button>
      </div>
    </div>

    <!-- Feedback List -->
    <div class="max-h-96 overflow-y-auto">
      <div v-if="feedbacks && feedbacks.length > 0">
        <ul class="divide-y divide-gray-200">
          <li v-for="feedback in feedbacks" :key="feedback.id" 
            class="py-3 px-3 hover:bg-blue-50 transition-colors rounded-lg">
            <p class="font-medium">{{ feedback.userFeedback }}</p>
            <div class="mt-1 text-sm text-gray-600">
              <span>Email: {{ feedback.email }}</span>
              <span class="ml-4">Tipo: {{ formatFeedbackType(feedback.feedbackType) }}</span>
              <span class="ml-4">Data: {{ formatDate(feedback.createdAt) }}</span>
            </div>
          </li>
        </ul>
      </div>
      <div v-else class="py-4 text-center text-gray-500">
        Nenhum feedback encontrado.
      </div>
    </div>
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex';

export default {
  name: "FeedbackList",
  props: {
    feedbacks: {
      type: Array,
      default: () => [],
    },
  },
  data() {
    return {
      filters: {
        email: "",
        type: "",
        startDate: "",
        endDate: ""
      },
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
      isLoading: state => state.isLoadingFeedback
    })
  },
  methods: {
    ...mapActions([
      'fetchFeedbackByEmail',
      'fetchAllFeedback',
      'filterFeedbackByDate',
      'markFeedbackAsRead',
      'deleteFeedback'
    ]),
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
    formatFeedbackType(type) {
      if (type === "SUGGESTION") return "Sugestão";
      if (type === "HELP") return "Ajuda";
      return type;
    },
    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleDateString('pt-PT', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    },
    applyFilters() {
      this.$emit("fetch-feedback", this.filters);
    },
    async fetchAllFeedback() {
      try {
        await this.fetchAllFeedback();
        this.showToast("Lista atualizada com sucesso");
      } catch (error) {
        this.showToast(error.message || "Erro ao carregar feedback", "error");
      }
    },
    async applyDateFilter() {
      if (!this.filters.startDate || !this.filters.endDate) {
        this.showToast("Por favor, selecione as datas inicial e final", "error");
        return;
      }
      try {
        await this.filterFeedbackByDate({
          startDate: this.filters.startDate,
          endDate: this.filters.endDate
        });
        this.showToast("Filtro aplicado com sucesso");
      } catch (error) {
        this.showToast(error.message || "Erro ao aplicar filtro", "error");
      }
    },
    async markAsRead(feedbackId) {
      try {
        await this.markFeedbackAsRead(feedbackId);
        this.showToast("Feedback marcado como lido");
      } catch (error) {
        this.showToast(error.message || "Erro ao marcar feedback como lido", "error");
      }
    },
    async deleteFeedback(feedbackId) {
      if (!confirm("Tem certeza que deseja eliminar este feedback?")) {
        return;
      }
      try {
        await this.deleteFeedback(feedbackId);
        this.showToast("Feedback eliminado com sucesso");
      } catch (error) {
        this.showToast(error.message || "Erro ao eliminar feedback", "error");
      }
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
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
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

/* Add focus ring for keyboard navigation */
*:focus-visible {
  box-shadow: 0 0 0 2px #6366f1;
}
</style>
