<template>
  <div>
    <!-- Filter Section -->
    <div class="mb-4">
      <label class="block text-sm font-medium text-gray-700 mb-1">Filtrar por Categoria</label>
      <select v-model="selectedFilterCategory" @change="onFilterChange" class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 transition-all duration-300">
        <option value="">Todos</option>
        <option v-for="cat in categories" :key="cat" :value="cat">
          {{ formatCategoryName(cat) }}
        </option>
      </select>
    </div>

    <!-- Questions List -->
    <div class="bg-white bg-opacity-90 backdrop-blur-sm rounded-xl shadow-md p-6 mb-8">
      <h2 class="text-xl font-semibold mb-4 text-blue-800 flex items-center">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
        </svg>
        Questões Existentes
      </h2>
      <div v-if="questions && questions.length > 0">
        <ul class="divide-y divide-gray-200">
          <li v-for="question in paginatedQuestions" :key="question.id" class="py-3 flex justify-between items-center hover:bg-blue-50 transition-colors rounded-lg px-3">
            <div>
              <p class="font-medium">{{ question.questionText }}</p>
              <span class="text-sm text-blue-600 bg-blue-100 px-2 py-1 rounded-full inline-block mt-1">
                {{ formatCategoryName(question.category) }}
              </span>
            </div>
            <div class="flex space-x-2">
              <!-- Edit Button (not implemented) -->
              <button class="p-2 text-gray-500 hover:text-blue-600 transition-colors" title="Editar">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                </svg>
              </button>
              <!-- Delete Button -->
              <button @click="$emit('delete-question', question.id)" class="p-2 text-gray-500 hover:text-red-600 transition-colors" title="Excluir">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                </svg>
              </button>
            </div>
          </li>
        </ul>
      </div>
      <div v-else class="py-8 text-center">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 text-gray-400 mx-auto mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
        </svg>
        <p class="text-gray-500">Nenhuma questão adicionada ainda</p>
      </div>

      <!-- Pagination Controls -->
      <div v-if="totalPages > 1" class="flex justify-center items-center space-x-2 mt-4">
        <button @click="prevPage" :disabled="currentPage === 1" class="px-3 py-1 border rounded">
          Previous
        </button>
        <span>Page {{ currentPage }} of {{ totalPages }}</span>
        <button @click="nextPage" :disabled="currentPage === totalPages" class="px-3 py-1 border rounded">
          Next
        </button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "QuestionsList",
  props: {
    questions: {
      type: Array,
      default: () => [],
    },
    categories: {
      type: Array,
      default: () => [],
    },
  },
  data() {
    return {
      selectedFilterCategory: "",
      currentPage: 1,
      itemsPerPage: 5, // Adjust this number as needed
    };
  },
  computed: {
    paginatedQuestions() {
      const start = (this.currentPage - 1) * this.itemsPerPage;
      return this.questions.slice(start, start + this.itemsPerPage);
    },
    totalPages() {
      return Math.ceil(this.questions.length / this.itemsPerPage);
    },
  },
  methods: {
    formatCategoryName(category) {
  // Check if category is an object and has a 'name' property.
  const categoryName = typeof category === 'object' && category !== null ? category.name : category;
  return categoryName.replace("_", " ");
}
,
    onFilterChange() {
      this.$emit("filter-questions", this.selectedFilterCategory);
      this.currentPage = 1; // Reset pagination when the filter changes
    },
    nextPage() {
      if (this.currentPage < this.totalPages) {
        this.currentPage++;
      }
    },
    prevPage() {
      if (this.currentPage > 1) {
        this.currentPage--;
      }
    },
  },
  watch: {
    questions() {
      // Reset current page if questions change and the current page becomes invalid
      if (this.currentPage > this.totalPages) {
        this.currentPage = 1;
      }
    },
  },
};
</script>
