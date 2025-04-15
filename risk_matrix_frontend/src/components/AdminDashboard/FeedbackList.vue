<template>
  <div class="bg-white bg-opacity-90 backdrop-blur-sm rounded-xl shadow-md p-6 mb-8">
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
        <input v-model="filters.email" type="email" placeholder="Digite o email" class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 transition-all duration-300" />
      </div>
      
      <!-- Type Filter -->
      <div class="flex-1">
        <label class="block text-sm font-medium text-gray-700 mb-1">Tipo</label>
        <select v-model="filters.type" class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 transition-all duration-300">
          <option value="">Todos</option>
          <option value="SUGGESTION">Sugestão</option>
          <option value="HELP">Ajuda</option>
        </select>
      </div>

      <!-- Date Range Filter -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Data Inicial</label>
          <input v-model="filters.startDate" type="date" class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 transition-all duration-300" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Data Final</label>
          <input v-model="filters.endDate" type="date" class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 transition-all duration-300" />
        </div>
      </div>

      <!-- Filter Button -->
      <div class="flex justify-end">
        <button @click="applyFilters" class="px-4 py-2 bg-blue-600 text-white rounded-lg shadow-md hover:bg-blue-700 transition-all duration-300">
          Aplicar Filtros
        </button>
      </div>
    </div>

    <!-- Feedback List -->
    <div class="max-h-96 overflow-y-auto">
      <div v-if="feedbacks && feedbacks.length > 0">
        <ul class="divide-y divide-gray-200">
          <li v-for="feedback in feedbacks" :key="feedback.id" class="py-3 px-3 hover:bg-blue-50 transition-colors rounded-lg">
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
      }
    };
  },
  methods: {
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
    }
  },
};
</script>
