<template>
  <div class="bg-white bg-opacity-90 backdrop-blur-sm rounded-xl shadow-xl p-6 mb-6">
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-2xl font-semibold text-blue-800">Gerenciar Questionários</h2>
      <div class="flex space-x-4">
        <button @click="showImportDialog = true" class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
          Importar
        </button>
        <button @click="showAddDialog = true" class="px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition-colors">
          Adicionar
        </button>
      </div>
    </div>

    <!-- Questionnaires List -->
    <div class="space-y-4">
      <div v-for="questionnaire in questionnaires" :key="questionnaire.id" 
           class="flex items-center justify-between p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors">
        <span class="text-gray-800">{{ questionnaire.title }}</span>
        <div class="flex space-x-2">
          <button @click="editQuestionnaire(questionnaire.id)"
                  class="px-3 py-1 text-sm bg-blue-100 text-blue-800 rounded hover:bg-blue-200 transition-colors">
            Editar
          </button>
          <button @click="exportQuestionnaire(questionnaire.id)" 
                  class="px-3 py-1 text-sm bg-green-100 text-green-800 rounded hover:bg-green-200 transition-colors">
            Exportar
          </button>
          <button @click="deleteQuestionnaire(questionnaire.id)" 
                  class="px-3 py-1 text-sm bg-red-100 text-red-800 rounded hover:bg-red-200 transition-colors">
            Eliminar
          </button>
        </div>
      </div>
    </div>

    <!-- Add Questionnaire Dialog -->
    <div v-if="showAddDialog" class="fixed inset-0 bg-white bg-opacity-20 backdrop-blur-sm flex items-center justify-center z-50">
      <div class="bg-white bg-opacity-90 backdrop-blur-sm rounded-xl shadow-xl p-6 w-full max-w-md">
        <h3 class="text-xl font-semibold text-blue-800 mb-4">Adicionar Questionário</h3>
        <input v-model="newQuestionnaireTitle" type="text" placeholder="Título do questionário" 
               class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500">
        <div class="mt-4 flex justify-end space-x-3">
          <button @click="showAddDialog = false" 
                  class="px-4 py-2 text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors">
            Cancelar
          </button>
          <button @click="handleAddQuestionnaire" 
                  class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
            Adicionar
          </button>
        </div>
      </div>
    </div>

    <!-- Import Dialog -->
    <div v-if="showImportDialog" class="fixed inset-0 bg-white bg-opacity-20 backdrop-blur-sm flex items-center justify-center z-50">
      <div class="bg-white bg-opacity-90 backdrop-blur-sm rounded-xl shadow-xl p-6 w-full max-w-md">
        <h3 class="text-xl font-semibold text-blue-800 mb-4">Importar Questionário</h3>
        <input type="file" @change="handleFileImport" accept=".json" 
               class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500">
        <div class="mt-4 flex justify-end space-x-3">
          <button @click="showImportDialog = false" 
                  class="px-4 py-2 text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors">
            Cancelar
          </button>
        </div>
      </div>
    </div>

    <!-- Alert Dialog -->
    <AlertDialog
      :show="showAlert"
      :title="alertTitle"
      :message="alertMessage"
      :type="alertType"
      @confirm="handleAlertConfirm"
      @cancel="handleAlertCancel"
    />
  </div>
</template>

<script>
import AlertDialog from "@/components/Static/AlertDialog.vue";

export default {
  name: "QuestionnaireManager",
  components: {
    AlertDialog
  },
  props: {
    questionnaires: {
      type: Array,
      required: true
    }
  },
  data() {
    return {
      showAddDialog: false,
      showImportDialog: false,
      newQuestionnaireTitle: "",
      showAlert: false,
      alertTitle: "",
      alertMessage: "",
      alertType: "info",
      alertResolve: null
    };
  },
  methods: {
    async showAlertDialog(title, message, type = "info") {
      this.alertTitle = title;
      this.alertMessage = message;
      this.alertType = type;
      this.showAlert = true;
      return new Promise((resolve) => {
        this.alertResolve = resolve;
      });
    },
    handleAlertConfirm() {
      this.showAlert = false;
      if (this.alertResolve) {
        this.alertResolve(true);
        this.alertResolve = null;
      }
    },
    handleAlertCancel() {
      this.showAlert = false;
      if (this.alertResolve) {
        this.alertResolve(false);
        this.alertResolve = null;
      }
    },
    async handleAddQuestionnaire() {
      if (!this.newQuestionnaireTitle) {
        await this.showAlertDialog("Aviso", "Por favor, insira um título para o questionário.", "error");
        return;
      }
      this.$emit("add-questionnaire", this.newQuestionnaireTitle);
      this.newQuestionnaireTitle = "";
      this.showAddDialog = false;
    },
    async deleteQuestionnaire(id) {
      const proceed = await this.showAlertDialog(
        "Confirmar Exclusão",
        "Tem certeza que deseja excluir este questionário?",
        "confirm"
      );
      if (!proceed) return;
      this.$emit("delete-questionnaire", id);
    },
    selectQuestionnaire(questionnaire) {
      this.$emit("select-questionnaire", questionnaire);
    },
    async handleFileImport(event) {
      const file = event.target.files[0];
      if (!file) {
        await this.showAlertDialog("Erro", "Nenhum arquivo selecionado.", "error");
        return;
      }

      if (!file.name.endsWith('.json')) {
        await this.showAlertDialog("Erro", "Por favor, selecione um arquivo JSON.", "error");
        return;
      }

      try {
        const reader = new FileReader();
        reader.onload = async (e) => {
          try {
            const jsonData = JSON.parse(e.target.result);
            console.log("Parsed JSON data:", jsonData);
            
            // Validate the JSON structure
            if (!jsonData.title || !jsonData.questions) {
              throw new Error("Estrutura do arquivo JSON inválida. O arquivo deve conter 'title' e 'questions'.");
            }
            
            this.$emit("import-questionnaire", jsonData);
            this.showImportDialog = false;
          } catch (error) {
            console.error("Error parsing JSON:", error);
            await this.showAlertDialog("Erro", `Erro ao processar o arquivo: ${error.message}`, "error");
          }
        };
        reader.onerror = async (error) => {
          console.error("Error reading file:", error);
          await this.showAlertDialog("Erro", "Erro ao ler o arquivo. Por favor, tente novamente.", "error");
        };
        reader.readAsText(file);
      } catch (error) {
        console.error("Error in file import:", error);
        await this.showAlertDialog("Erro", "Erro ao processar o arquivo. Por favor, tente novamente.", "error");
      }
    },
    exportQuestionnaire(id) {
      // Find the questionnaire to export
      const questionnaire = this.questionnaires.find(q => q.id === id);
      if (!questionnaire) {
        this.showAlertDialog("Erro", "Questionário não encontrado.", "error");
        return;
      }

      // Format the data for export
      const exportData = {
        title: questionnaire.title,
        questions: questionnaire.questions.map(question => ({
          questionText: question.questionText,
          categoryName: question.category || "Sem Categoria",
          options: question.options.map(option => ({
            optionText: option.optionText,
            optionType: option.optionType,
            optionLevel: option.optionLevel,
            recommendation: option.recommendation
          }))
        }))
      };

      // Create and download the file
      const dataStr = JSON.stringify(exportData, null, 2);
      const dataUri = 'data:application/json;charset=utf-8,'+ encodeURIComponent(dataStr);
      
      const exportFileDefaultName = `questionario_${questionnaire.title.replace(/\s+/g, '_')}.json`;
      
      const linkElement = document.createElement('a');
      linkElement.setAttribute('href', dataUri);
      linkElement.setAttribute('download', exportFileDefaultName);
      linkElement.click();
    },
    editQuestionnaire(id) {
      this.$router.push({ path: `/admin/edit-questionnaire/${id}` });
    }
  }
};
</script>
