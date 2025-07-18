<template>
  <div class="bg-white bg-opacity-90 backdrop-blur-sm rounded-xl shadow-md p-6 mb-6">
    <header class="mb-6">
      <h2 class="text-xl font-semibold text-blue-800 flex items-center">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
        </svg>
        {{ labels.title }}
      </h2>
      <!-- Subtitle if needed -->
      <!-- <p class="text-sm text-gray-600">Gerencie os questionários disponíveis.</p> -->
      <div class="flex flex-wrap gap-2 mt-4">
        <!-- Search -->
        <div class="flex-1 min-w-[250px] relative">
          <input
            type="text"
            v-model="searchQuery"
            class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all"
            :placeholder="labels.search"
            :disabled="loading"
          />
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="h-5 w-5 text-gray-400 absolute left-3 top-2.5"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
        </div>
        <!-- Import button -->
        <button
          @click="showImportDialog = true"
          class="px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 focus:ring-2 focus:ring-indigo-400 transition-all flex items-center"
          :disabled="loading"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v12m0 0l-3-3m3 3l3-3m-7 5h8a2 2 0 002-2V6a2 2 0 00-2-2h-3" />
          </svg>
          {{ labels.import }}
        </button>
        <!-- Add new button -->
        <button
          @click="showNewQuestionnaireInput = !showNewQuestionnaireInput"
          class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 focus:ring-2 focus:ring-blue-400 transition-all flex items-center"
          :disabled="loading"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
          </svg>
          {{ labels.addNew }}
        </button>
      </div>
    </header>

    <div v-if="showNewQuestionnaireInput" class="mb-4 flex gap-2 items-center">
      <input
        v-model="newQuestionnaire.title"
        type="text"
        :placeholder="labels.titleLabel"
        class="border border-gray-300 rounded px-3 py-2 flex-1"
      />
      <button
        @click="createQuestionnaire"
        class="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700"
      >
        {{ labels.save }}
      </button>
      <button
        @click="showNewQuestionnaireInput = false"
        class="px-4 py-2 bg-gray-300 rounded hover:bg-gray-400"
      >
        {{ labels.cancel }}
      </button>
    </div>

    <div v-if="filteredQuestionnaires.length" class="space-y-4">
      <div
        v-for="q in paginatedQuestionnaires"
        :key="q.id"
        class="p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors flex items-center justify-between"
      >
        <span class="font-medium text-gray-900">{{ q.title }}</span>
        <div class="flex space-x-2">
          <button
            @click="exportSingle(q)"
            class="px-3 py-1 text-sm bg-green-100 text-green-800 rounded hover:bg-green-200 flex items-center"
          >
            {{ labels.export }}
          </button>
          <button
            @click="$emit('edit-questionnaire', q.id)"
            class="px-3 py-1 text-sm bg-blue-100 text-blue-800 rounded hover:bg-blue-200 flex items-center"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.232 5.232l3.536 3.536M9 13h3l8-8a2.828 2.828 0 00-4-4l-8 8v3z" />
            </svg>
            {{ labels.edit }}
          </button>
          <button
            @click="$emit('delete-questionnaire', q.id)"
            class="px-3 py-1 text-sm bg-red-100 text-red-800 rounded hover:bg-red-200 flex items-center"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
            {{ labels.delete }}
          </button>
        </div>
      </div>
    </div>

    <div v-else class="text-center text-gray-500 py-8">
      {{ labels.noResults }}
    </div>

    <!-- Add this after your main content, inside the <template> -->
    <div v-if="showImportDialog" class="fixed inset-0 flex items-center justify-center bg-black bg-opacity-30 z-50">
      <div class="bg-white p-6 rounded-lg shadow-lg w-full max-w-md">
        <h3 class="text-lg font-semibold mb-4">{{ labels.importTitle }}</h3>
        <input type="file" accept=".json" @change="handleFileImport" class="mb-4" />
        <div class="flex justify-end gap-2">
          <button @click="showImportDialog = false" class="px-4 py-2 bg-gray-300 rounded hover:bg-gray-400">{{ labels.cancel }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'QuestionnaireManager',
  props: {
    questionnaires: Array,
    loading: Boolean
  },
  data() {
    return {
      searchQuery: '',
      currentPage: 1,
      itemsPerPage: 10,
      showNewQuestionnaireInput: false,
      showImportDialog: false,
      newQuestionnaire: { title: '' }
    };
  },
  computed: {
    filteredQuestionnaires() {
      if (!this.searchQuery.trim()) return this.questionnaires;
      const query = this.searchQuery.toLowerCase().trim();
      return this.questionnaires.filter(q => q?.title?.toLowerCase().includes(query));
    },
    totalPages() {
      return Math.ceil(this.filteredQuestionnaires.length / this.itemsPerPage);
    },
    paginatedQuestionnaires() {
      const start = (this.currentPage - 1) * this.itemsPerPage;
      return this.filteredQuestionnaires.slice(start, start + this.itemsPerPage);
    },
    labels() {
      return {
        title: 'Gerir Questionários',
        search: 'Pesquisar questionários...',
        noResults: 'Nenhum questionário encontrado',
        loading: 'A carregar questionários...',
        addNew: 'Adicionar Novo Questionário',
        import: 'Importar',
        export: 'Exportar',
        importTitle: 'Importar Questionário',
        cancel: 'Cancelar',
        save: 'Guardar',
        titleLabel: 'Título',
        emptyTitle: 'Título não pode ser vazio',
        success: 'Operação realizada com sucesso!',
        error: 'Erro ao processar',
        processing: 'A processar...',
        importError: 'Erro ao importar o arquivo',
        importInvalidStructure: "Estrutura do arquivo JSON inválida. Deve conter 'title' e 'questions'.",
        importNoFile: 'Nenhum arquivo selecionado.',
        importNotJson: 'Por favor, selecione um arquivo JSON.',
        edit: 'Editar',
        delete: 'Eliminar'
      };
    }
  },
  methods: {
    exportSingle(q) {
      const data = JSON.stringify(q, null, 2);
      const blob = new Blob([data], { type: 'application/json' });
      const link = document.createElement('a');
      link.href = URL.createObjectURL(blob);
      link.download = `${q.title.replace(/\s+/g, '_').toLowerCase()}.json`;
      link.click();
      URL.revokeObjectURL(link.href);
    },
    handleFileImport(event) {
      const file = event.target.files[0];
      if (!file) return this.showToast(this.labels.importNoFile, 'error');
      if (!file.name.endsWith('.json')) return this.showToast(this.labels.importNotJson, 'error');

      const reader = new FileReader();
      reader.onload = e => {
        try {
          const json = JSON.parse(e.target.result);
          if (!json.title || !json.questions) throw new Error(this.labels.importInvalidStructure);
          this.$emit('import-questionnaire', json);
          this.showImportDialog = false;
          this.showToast(this.labels.success);
        } catch (err) {
          this.showToast(`${this.labels.importError}: ${err.message}`, 'error');
        }
      };
      reader.onerror = () => this.showToast(this.labels.importError, 'error');
      reader.readAsText(file);
    },
    createQuestionnaire() {
      if (!this.newQuestionnaire.title.trim()) return this.showToast(this.labels.emptyTitle, 'error');
      this.$emit('add-questionnaire', this.newQuestionnaire.title.trim());
      this.newQuestionnaire.title = '';
      this.showNewQuestionnaireInput = false;
      this.showToast(this.labels.success);
    },
    changePage(page) {
      this.currentPage = page;
    },
    showToast(msg, type = 'success') {
      console.log(`${type}: ${msg}`);
    }
  }
};
</script>

<style scoped>
.transition-all {
  transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
}
button:focus-visible,
input:focus-visible,
textarea:focus-visible,
select:focus-visible {
  outline: 2px solid #4f46e5;
  outline-offset: 2px;
}
button:not(:disabled):hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1), 0 2px 4px -1px rgba(0,0,0,0.06);
}
button:not(:disabled):active {
  transform: translateY(0);
}
</style>
