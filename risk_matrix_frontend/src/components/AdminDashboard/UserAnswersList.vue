<template>
  <div class="bg-white bg-opacity-90 backdrop-blur-sm rounded-xl shadow-md p-6">
    <h2 class="text-xl font-semibold mb-4 text-blue-800 flex items-center">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2" fill="none" viewBox="0 0 24 24"
        stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
      </svg>
      Respostas dos Utilizadores
    </h2>
    <!-- Filter by Email -->
    <div class="mb-4">
      <label class="block text-sm font-medium text-gray-700 mb-1">Filtrar por Email</label>
      <input v-model="userAnswersEmail" type="email" placeholder="Digite o email"
        class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 transition-all duration-300" />
    </div>
    <!-- Filter by Dates -->
    <div class="flex flex-col md:flex-row md:space-x-4 mb-4">
      <div class="flex-1">
        <label class="block text-sm font-medium text-gray-700 mb-1">Data Inicial</label>
        <input v-model="filterStartDate" type="date"
          class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 transition" />
      </div>
      <div class="flex-1">
        <label class="block text-sm font-medium text-gray-700 mb-1">Data Final</label>
        <input v-model="filterEndDate" type="date"
          class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 transition" />
      </div>
      <div class="flex items-end">
        <button @click="onFilterByDate"
          class="px-4 py-2 bg-indigo-600 text-white rounded-lg shadow-md hover:bg-indigo-700 transition">
          Filtrar por Data
        </button>
      </div>
    </div>
    <!-- Buttons for searching -->
    <div class="flex space-x-4 mb-4">
      <button @click="onFetchByEmail"
        class="px-4 py-2 bg-blue-600 text-white rounded-lg shadow-md hover:bg-blue-700 transition-all duration-300">
        Procurar por Email
      </button>
      <button @click="onFetchAll"
        class="px-4 py-2 bg-green-600 text-white rounded-lg shadow-md hover:bg-green-700 transition-all duration-300">
        Ver Todas as Respostas
      </button>
    </div>
    <!-- Display Results -->
    <div v-if="userAnswers && userAnswers.length > 0">
      <ul class="divide-y divide-gray-200">
        <!-- Use submissionId as key -->
        <li v-for="ua in userAnswers" :key="ua.submissionId" class="py-3">
          <p class="font-medium">
            <!-- Optionally display the submissionId -->
            Submission: {{ ua.submissionId }} - Email: {{ ua.email }}
          </p>
          <button @click="$emit('download-report', ua.submissionId)"
            class="mt-2 px-3 py-1 bg-purple-600 text-white text-sm rounded hover:bg-purple-700 transition">
            Descarregar Relatório DOCX
          </button>

          <div v-for="(severity, category) in ua.severitiesByCategory" :key="category" class="ml-4">
            <span class="text-sm text-blue-600">{{ formatCategoryName(category) }}: {{ severity }}</span>
          </div>
          <ul class="mt-2 ml-4 border-l border-gray-300 pl-4">
            <!-- Consider using answer.id if available -->
            <li v-for="answer in ua.answers" :key="answer.id" class="py-1">
              {{ answer.questionText }} - {{ answer.userResponse }}
            </li>
          </ul>
        </li>
      </ul>
    </div>
    <div v-else class="py-4 text-center text-gray-500">
      Nenhuma resposta encontrada.
    </div>
  </div>
</template>
<script>
export default {
  name: "UserAnswersList",
  props: {
    userAnswers: {
      type: Array,
      default: () => [],
    },
  },
  data() {
    return {
      userAnswersEmail: "",
      filterStartDate: "",
      filterEndDate: "",
    };
  },
  mounted() {
    // Automatically fetch all answers when the component is mounted
    this.onFetchAll();
  },
  methods: {
    formatCategoryName(category) {
      if (!category) return "";
      return category.replace(/_/g, " ").toLowerCase().replace(/\b\w/g, l => l.toUpperCase());
    },
    onFetchByEmail() {
      this.$emit("fetch-by-email", this.userAnswersEmail);
    },
    onFetchAll() {
      this.$emit("fetch-all");
    },
    onFilterByDate() {
      this.$emit("filter-by-date", { startDate: this.filterStartDate, endDate: this.filterEndDate });
    },
  },
};
</script>
