<template>
  <div class="bg-white bg-opacity-90 backdrop-blur-sm rounded-xl shadow-md p-6 mb-8">
    <h2 class="text-xl font-semibold mb-4 text-blue-800 flex items-center">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
      </svg>
      Gerenciar Questionários
    </h2>
    <!-- Create and Import Row -->
    <div class="flex flex-col md:flex-row md:items-end md:space-x-4 space-y-4 md:space-y-0 mb-4">
      <div class="flex-1">
        <label class="block text-sm font-medium text-gray-700 mb-1">Título do Questionário</label>
        <input
          v-model="newQuestionnaireTitle"
          type="text"
          placeholder="Digite o título do questionário"
          class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 transition-all duration-300"
        />
      </div>
      <div class="flex items-end space-x-4">
        <button
          @click="onAddQuestionnaire"
          class="px-6 py-3 bg-blue-600 text-white rounded-lg shadow-md hover:bg-blue-700 transition-all duration-300 flex items-center"
        >
          <span>Criar</span>
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
          </svg>
        </button>
        <!-- Import Questionnaire -->
        <input type="file" ref="importFile" accept=".json" class="hidden" @change="onImportFileSelected" />
        <button
          @click="triggerImportFilePicker"
          class="px-6 py-3 bg-green-600 text-white rounded-lg shadow-md hover:bg-green-700 transition-all duration-300 flex items-center"
        >
          <span>Importar</span>
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v16c0 1.105.895 2 2 2h8m2-2h2c1.105 0 2-.895 2-2V4c0-1.105-.895-2-2-2H6c-1.105 0-2 .895-2 2z" />
          </svg>
        </button>
      </div>
    </div>

    <!-- List of Questionnaires -->
    <div v-if="questionnaires.length > 0">
      <ul class="divide-y divide-gray-200">
        <li v-for="questionnaire in questionnaires" :key="questionnaire.id" class="py-3 flex justify-between items-center">
          <p class="font-medium">{{ questionnaire.title }}</p>
          <div class="flex space-x-2">
            <button @click="$emit('export-questionnaire', questionnaire.id)" class="p-2 text-gray-500 hover:text-blue-600" title="Exportar">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
              </svg>
            </button>
            <button @click="$emit('delete-questionnaire', questionnaire.id)" class="p-2 text-gray-500 hover:text-red-600" title="Excluir">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            </button>
          </div>
        </li>
      </ul>
    </div>
    <div v-else class="py-8 text-center">
      <p class="text-gray-500">Nenhum questionário encontrado.</p>
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
  },
  data() {
    return {
      newQuestionnaireTitle: "",
      selectedImportFile: null,
    };
  },
  methods: {
    onAddQuestionnaire() {
      if (!this.newQuestionnaireTitle) {
        alert("Por favor, insira um título para o questionário.");
        return;
      }
      this.$emit("add-questionnaire", this.newQuestionnaireTitle);
      this.newQuestionnaireTitle = "";
    },
    triggerImportFilePicker() {
      this.$refs.importFile.click();
    },
    onImportFileSelected(event) {
      this.selectedImportFile = event.target.files[0];
      this.importQuestionnaire();
    },
    async importQuestionnaire() {
      if (!this.selectedImportFile) return;
      try {
        const fileContent = await this.selectedImportFile.text();
        const jsonData = JSON.parse(fileContent);
        this.$emit("import-questionnaire", jsonData);
      } catch (error) {
        console.error("Erro ao importar questionário:", error);
        alert("Falha ao importar questionário.");
      } finally {
        this.selectedImportFile = null;
        this.$refs.importFile.value = null;
      }
    },
  },
};
</script>
