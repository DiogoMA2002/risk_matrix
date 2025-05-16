<template>
  <div class="bg-white bg-opacity-90 backdrop-blur-sm rounded-xl shadow-md p-6 mb-6">
    <header class="mb-6">
      <h2 class="text-xl font-semibold text-blue-800 flex items-center">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
        </svg>
        {{ labels.title }}
      </h2>
      <div class="flex justify-between items-center mt-4">
        <div class="flex-1 max-w-md">
          <div class="relative">
            <input type="text" v-model="searchQuery"
              class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all"
              :placeholder="labels.search"
              :disabled="loading" />
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400 absolute left-3 top-2.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </div>
        </div>
        <button @click="showNewQuestionnaireInput = !showNewQuestionnaireInput"
          class="ml-4 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 focus:ring-2 focus:ring-blue-400 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center"
          :disabled="loading">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
          </svg>
          {{ labels.addNew }}
        </button>
      </div>
    </header>

    <!-- New Questionnaire Input -->
    <div v-if="showNewQuestionnaireInput" class="mb-6 p-4 bg-gray-50 rounded-lg">
      <div class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">{{ labels.titleLabel }}</label>
          <input v-model="newQuestionnaire.title" type="text"
            class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all"
            :placeholder="labels.titleLabel"
            :disabled="loading" />
        </div>
        <div class="flex justify-end">
          <button @click="createQuestionnaire"
            class="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 focus:ring-2 focus:ring-green-400 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center"
            :disabled="loading || !newQuestionnaire.title.trim()">
            <svg v-if="loading" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            {{ loading ? labels.processing : labels.save }}
          </button>
        </div>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="py-8 text-center">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-4 border-gray-300 border-t-blue-600"></div>
      <p class="mt-2 text-gray-600">{{ labels.loading }}</p>
    </div>

    <!-- No Results -->
    <div v-else-if="filteredQuestionnaires.length === 0" class="text-center py-8 text-gray-500">
      {{ labels.noResults }}
    </div>

    <!-- Questionnaires List -->
    <div v-else class="space-y-4">
      <div v-for="questionnaire in paginatedQuestionnaires" :key="questionnaire.id"
        class="p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors">
        <div class="flex items-center justify-between">
          <p class="font-medium text-gray-900">{{ questionnaire.title }}</p>
          <div class="flex items-center space-x-2">
            <button @click="editQuestionnaire(questionnaire.id)"
              class="text-blue-600 hover:text-blue-800 focus:ring-2 focus:ring-blue-400 rounded-lg p-1"
              :disabled="loading">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
            </button>
            <button @click="deleteQuestionnaire(questionnaire.id)"
              class="text-red-600 hover:text-red-800 focus:ring-2 focus:ring-red-400 rounded-lg p-1"
              :disabled="loading">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div v-if="totalPages > 1" class="mt-6 flex justify-center space-x-2">
      <button v-for="page in totalPages" :key="page"
        @click="changePage(page)"
        class="px-4 py-2 rounded-lg transition-colors"
        :class="{
          'bg-blue-600 text-white': currentPage === page,
          'bg-gray-200 text-gray-700 hover:bg-gray-300': currentPage !== page
        }"
        :disabled="loading">
        {{ page }}
      </button>
    </div>
  </div>
</template>

<script>
export default {
  name: "QuestionnaireManager",
  props: {
    questionnaires: {
      type: Array,
      default: () => [],
    },
    loading: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      searchQuery: "",
      currentPage: 1,
      itemsPerPage: 10,
      showNewQuestionnaireInput: false,
      newQuestionnaire: {
        title: "",
      },
    };
  },
  computed: {
    filteredQuestionnaires() {
      if (!this.searchQuery.trim()) return this.questionnaires;
      
      const query = this.searchQuery.toLowerCase().trim();
      return this.questionnaires.filter(q => {
        if (!q || typeof q !== 'object') return false;
        
        const title = (q.title || '').toLowerCase();
        return title.includes(query);
      });
    },
    totalPages() {
      return Math.ceil(this.filteredQuestionnaires.length / this.itemsPerPage);
    },
    paginatedQuestionnaires() {
      const start = (this.currentPage - 1) * this.itemsPerPage;
      const end = start + this.itemsPerPage;
      return this.filteredQuestionnaires.slice(start, end);
    },
    labels() {
      return {
        title: "Gerenciar Questionários",
        search: "Pesquisar questionários...",
        noResults: "Nenhum questionário encontrado",
        loading: "A carregar questionários...",
        addNew: "Adicionar Novo Questionário",
        edit: "Editar Questionário",
        delete: "Excluir Questionário",
        confirmDelete: "Tem certeza que deseja excluir este questionário?",
        confirmDeleteWarning: "Esta ação não pode ser desfeita.",
        cancel: "Cancelar",
        save: "Salvar",
        titleLabel: "Título",
        emptyTitle: "Título não pode ser vazio",
        success: "Questionário salvo com sucesso!",
        error: "Erro ao salvar questionário",
        processing: "A processar..."
      };
    }
  },
  methods: {
    editQuestionnaire(id) {
      this.$router.push({ path: `/admin/edit-questionnaire/${id}` });
    },
    async deleteQuestionnaire(id) {
      const proceed = await this.showConfirmDialog(
        this.labels.confirmDelete,
        this.labels.confirmDeleteWarning
      );
      
      if (!proceed) return;

      try {
        await this.$emit("delete-questionnaire", id);
        this.showToast(this.labels.deleteSuccess);
      } catch (error) {
        console.error("Error deleting questionnaire:", error);
        this.showToast(this.labels.deleteError, "error");
      }
    },
    async createQuestionnaire() {
      if (!this.newQuestionnaire.title.trim()) {
        this.showToast(this.labels.emptyTitle, "error");
        return;
      }

      try {
        await this.$emit("add-questionnaire", this.newQuestionnaire.title.trim());
        this.showToast(this.labels.success);
        this.newQuestionnaire.title = "";
        this.showNewQuestionnaireInput = false;
      } catch (error) {
        console.error("Error creating questionnaire:", error);
        this.showToast(this.labels.error, "error");
      }
    },
    showToast(message, type = 'success') {
      // Implementation depends on your toast system
      console.log(`${type}: ${message}`);
    },
    async showConfirmDialog(title, message) {
      // Implementation depends on your dialog system
      return window.confirm(`${title}\n${message}`);
    },
    changePage(page) {
      this.currentPage = page;
    }
  }
};
</script>

<style scoped>
/* Add smooth transitions */
.transition-all {
  transition-property: all;
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
  transition-duration: 150ms;
}

/* Improve focus styles for better accessibility */
button:focus-visible,
input:focus-visible,
textarea:focus-visible,
select:focus-visible {
  outline: 2px solid #4f46e5;
  outline-offset: 2px;
}

/* Add hover effect for better interactivity */
button:not(:disabled):hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}

button:not(:disabled):active {
  transform: translateY(0);
}
</style>


