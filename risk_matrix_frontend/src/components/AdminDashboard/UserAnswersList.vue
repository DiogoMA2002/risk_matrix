<template>
  <div class="bg-white bg-opacity-90 backdrop-blur-sm rounded-xl shadow-md p-6">
    <header class="mb-6">
      <h2 class="text-xl font-semibold text-blue-800 flex items-center">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2" fill="none" viewBox="0 0 24 24"
          stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
        </svg>
        Respostas dos Utilizadores
      </h2>
    </header>

    <!-- Search and Filter Controls -->
    <div class="space-y-4 mb-6">
      <div>
        <label for="email-filter" class="block text-sm font-medium text-gray-700 mb-1">Filtrar por Email</label>
        <input id="email-filter" v-model="filters.email" type="email" placeholder="Digite o email"
          class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-300" />
      </div>
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div>
          <label for="start-date" class="block text-sm font-medium text-gray-700 mb-1">Data Inicial</label>
          <input id="start-date" v-model="filters.startDate" type="date"
            class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all" />
        </div>
        <div>
          <label for="end-date" class="block text-sm font-medium text-gray-700 mb-1">Data Final</label>
          <input id="end-date" v-model="filters.endDate" type="date"
            class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all" />
        </div>
        <div class="flex items-end">
          <button @click="applyDateFilter"
            class="w-full px-4 py-2 bg-indigo-600 text-white rounded-lg shadow hover:bg-indigo-700 focus:ring-2 focus:ring-indigo-400 transition-all">
            Filtrar por Data
          </button>
        </div>
      </div>
      <div class="flex flex-col sm:flex-row gap-3">
        <button @click="searchByEmail"
          class="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg shadow hover:bg-blue-700 focus:ring-2 focus:ring-blue-400 transition-all">
          Procurar por Email
        </button>
        <button @click="fetchAllResponses"
          class="flex-1 px-4 py-2 bg-green-600 text-white rounded-lg shadow hover:bg-green-700 focus:ring-2 focus:ring-green-400 transition-all">
          Ver Todas as Respostas
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
export default {
  name: "UserAnswersList",
  props: {
    userAnswers: { type: Array, default: () => [] },
    isLoading: { type: Boolean, default: false }
  },
  data() {
    return {
      filters: {
        email: "",
        startDate: "",
        endDate: ""
      },
      showAnswers: {}
    };
  },
  mounted() {
    this.fetchAllResponses();
  },
  methods: {
    toggleAnswers(id) {
      this.showAnswers = {
        ...this.showAnswers,
        [id]: !this.showAnswers[id]
      };
    }
    ,
    formatCategoryName(category) {
      return category ? category.replace(/_/g, " ").toLowerCase().replace(/\b\w/g, l => l.toUpperCase()) : "";
    },
    searchByEmail() {
      if (!this.filters.email.trim()) {
        alert("Por favor, introduza um email válido");
        return;
      }
      this.$emit("fetch-by-email", this.filters.email);
    },
    fetchAllResponses() {
      this.$emit("fetch-all");
    },
    applyDateFilter() {
      if (!this.filters.startDate || !this.filters.endDate) {
        alert("Por favor, selecione as datas inicial e final");
        return;
      }
      this.$emit("filter-by-date", {
        startDate: this.filters.startDate,
        endDate: this.filters.endDate
      });
    },
    getSeverityColorClass(severity) {
      const s = (severity || "").toLowerCase();
      if (s.includes("alto") || s.includes("high")) return "bg-red-100 text-red-800";
      if (s.includes("medio") || s.includes("medium")) return "bg-yellow-100 text-yellow-800";
      if (s.includes("baixo") || s.includes("low")) return "bg-green-100 text-green-800";
      return "bg-blue-100 text-blue-800";
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
</style>
