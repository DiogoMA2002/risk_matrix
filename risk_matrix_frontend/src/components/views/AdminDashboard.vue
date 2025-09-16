<template>
  <div class="h-screen overflow-y-auto bg-gradient-to-br from-blue-600 to-indigo-100 font-sans">
    <div class="container mx-auto px-4 py-6 max-w-7xl">
      <HeaderComponent title="Painel Administrativo" subtitle="Gestao de questões, questionários e feedbacks"
        back-to="/" :show-user-button="true" />

      <QuestionnaireManager :questionnaires="questionnaires" @add-questionnaire="addQuestionnaire"
        @delete-questionnaire="deleteQuestionnaire" @import-questionnaire="importQuestionnaire"
        @export-questionnaire="exportQuestionnaire" @edit-questionnaire="editQuestionnaire" />

      <CategoryManager :categories="categories" :loading="isLoadingCategories" @create-category="handleCreateCategory"
        @edit-category="handleEditCategory" @delete-category="handleDeleteCategory" 
        @show-alert="({ title, message, type }) => showAlertDialog(title, message, type || 'error')" />

      <QuestionForm :categories="categories" :questionnaires="questionnaires" :optionTypes="optionTypes"
        :optionLevels="optionLevels" @add-question="addQuestion" />

      <QuestionsList :questions="questions" :categories="categories" @delete-question="deleteQuestion"
        @filter-questions="filterQuestionsByCategory" />

      <FeedbackList :feedbacks="feedbacks" @fetch-feedback="fetchFeedback" />

      <GlossaryManager />

      <UserAnswersList @download-report="downloadSubmissionDocx" />

      <AlertDialog :show="showAlert" :title="alertTitle" :message="alertMessage" :type="alertType"
        @confirm="handleAlertConfirm" @cancel="handleAlertCancel" />
    </div>
  </div>
</template>
<script>
/* eslint-disable */
import { mapState } from "vuex";
import axios from "axios";

import HeaderComponent from "@/components/Static/HeaderComponent.vue";
import QuestionnaireManager from "@/components/AdminDashboard/QuestionnaireManager.vue";
import CategoryManager from "@/components/AdminDashboard/CategoryManager.vue";
import QuestionForm from "@/components/AdminDashboard/QuestionForm.vue";
import QuestionsList from "@/components/AdminDashboard/QuestionsList.vue";
import FeedbackList from "@/components/AdminDashboard/FeedbackList.vue";
import UserAnswersList from "@/components/AdminDashboard/UserAnswersList.vue";
import AlertDialog from "@/components/Static/AlertDialog.vue";
import GlossaryManager from "@/components/AdminDashboard/GlossaryManager.vue";

export default {
  name: "AdminDashboard",
  components: {
    HeaderComponent,
    QuestionnaireManager,
    CategoryManager,
    QuestionForm,
    QuestionsList,
    FeedbackList,
    UserAnswersList,
    AlertDialog,
    GlossaryManager
  },
  computed: {
    ...mapState(["questions", "questionnaires", "categories"]),
  },
  data() {
    return {
      feedbacks: [],
      isLoading: false,
      isLoadingCategories: false,
      optionTypes: ["IMPACT", "PROBABILITY"],
      optionLevels: ["LOW", "MEDIUM", "HIGH"],
      showAlert: false,
      alertTitle: "",
      alertMessage: "",
      alertType: "info",
      alertResolve: null,
    };
  },
  async created() {
    await Promise.all([
      this.$store.dispatch("fetchQuestions"),
      this.$store.dispatch("fetchQuestionnairesForAdmin"),
      this.$store.dispatch("fetchCategories"),
      this.fetchFeedback()
    ]);
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
    async filterQuestionsByCategory(category) {
      this.isLoading = true;
      try {
        if (category === "") {
          await this.$store.dispatch('fetchQuestions');
        } else {
          const response = await axios.get(`/api/questions/category/${category}`);
          this.$store.commit('setQuestions', response.data);
        }
      } catch (error) {
        console.error("Erro ao filtrar questões:", error);
        await this.showAlertDialog("Erro", "Erro ao filtrar questões por categoria.", "error");
      } finally {
        this.isLoading = false;
      }
    },
    async handleCreateCategory(newCategoryName) {
      if (!newCategoryName || !newCategoryName.trim()) {
        await this.showAlertDialog("Erro", "Nome da categoria não pode ser vazio.", "error");
        return;
      }

      // Basic validation (similar to QuestionForm, maybe centralize later)
      const forbidden = /[^a-zA-Z0-9\sáàâãéèêíïóôõöúçÁÀÂÃÉÈÍÏÓÔÕÖÚÇ]/;
      if (forbidden.test(newCategoryName.trim())) {
        await this.showAlertDialog("Erro", "Nome da categoria contém caracteres inválidos.", "error");
        return;
      }

      // Prevent duplicates (case-insensitive check)
      if (this.categories.some(cat => cat.name.toLowerCase() === newCategoryName.trim().toLowerCase())) {
        await this.showAlertDialog("Erro", `A categoria '${newCategoryName.trim()}' já existe.`, "error");
        return;
      }

      this.isLoadingCategories = true;
      try {
        const token = localStorage.getItem("jwt");
        const response = await axios.post("/api/categories",
          { name: newCategoryName.trim() },
          {
            headers: {
              Authorization: `Bearer ${token}`
            }
          }
        );

        // Refresh categories list
        await this.$store.dispatch("fetchCategories");

        // Return the created category data
        return response.data;
      } catch (error) {
        console.error("Error creating category:", error);
        const errorMsg = error.response?.data?.message || "Erro ao criar categoria.";
        await this.showAlertDialog("Erro", errorMsg, "error");
        throw error; // Re-throw the error to be handled by the child component
      } finally {
        this.isLoadingCategories = false;
      }
    },
    async handleEditCategory({ id, name }) {
      if (!name || !name.trim()) {
        await this.showAlertDialog("Erro", "Nome da categoria não pode ser vazio.", "error");
        return;
      }

      const trimmedName = name.trim();

      // Prevent duplicates (ding the one being edited)
      if (this.categories.some(cat => cat.id !== id && cat.name.toLowerCase() === trimmedName.toLowerCase())) {
        await this.showAlertDialog("Erro", `Já existe uma categoria com o nome '${trimmedName}'.`, "error");
        return;
      }

      try {
        const token = localStorage.getItem("jwt");
        await axios.put(`/api/categories/${id}`,
          { name: trimmedName },
          {
            headers: {
              Authorization: `Bearer ${token}`
            }
          }
        );

        // ✅ Refresh categories from Vuex store
        await this.$store.dispatch("fetchCategories");

        await this.showAlertDialog("Sucesso", "Categoria atualizada com sucesso!", "success");
      } catch (error) {
        console.error("Erro ao atualizar categoria:", error);
        const errorMsg = error.response?.data?.message || "Erro ao atualizar categoria.";
        await this.showAlertDialog("Erro", errorMsg, "error");
      }
    }
    ,
    async downloadSubmissionDocx(submissionId) {
      try {
        const token = localStorage.getItem("jwt");
        if (!token) {
          await this.showAlertDialog("Erro", "Token JWT não encontrado.", "error");
          return;
        }
        const response = await axios.get(`/api/answers/export-submission/${submissionId}`, {
          responseType: 'blob',
          headers: {
            Authorization: `Bearer ${token}`
          }
        });

        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement("a");
        link.href = url;
        link.setAttribute("download", `relatorio_${submissionId}.docx`);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);

        // Refresh the answers list after download
        await this.$store.dispatch("fetchAllUserAnswers");
      } catch (error) {
        console.error("Erro ao descarregar relatório:", error);
        await this.showAlertDialog("Erro", "Falha ao descarregar relatório.", "error");
      }
    }
    ,
    async handleDeleteCategory(id) {
      const proceed = await this.showAlertDialog(
        "Confirmar Eliminação",
        "Tem certeza que deseja eliminar esta categoria? Esta ação não pode ser desfeita.",
        "confirm"
      );
      if (!proceed) return;

      try {
        const token = localStorage.getItem("jwt");
        await axios.delete(`/api/categories/${id}`, {
          headers: {
            Authorization: `Bearer ${token}`
          }
        });

        await this.showAlertDialog("Sucesso", "Categoria eliminada com sucesso!");
        await this.$store.dispatch("fetchCategories"); // Refresh categories via Vuex
        await this.$store.dispatch("fetchQuestions");   // Refresh questions in case any used the category
      } catch (error) {
        console.error("Erro ao eliminar categoria:", error);
        let errorMsg = "Erro ao eliminar categoria.";
        if (error.response?.status === 409) {
          errorMsg = "Não é possível eliminar a categoria pois ela está associada a questões existentes.";
        } else if (error.response?.data?.message) {
          errorMsg = error.response.data.message;
        }
        await this.showAlertDialog("Erro", errorMsg, "error");
      }
    }
    ,
    async addQuestion(questionData) {
      if (!questionData.newQuestion || !questionData.selectedCategory || questionData.selectedQuestionnaires.length === 0) {
        await this.showAlertDialog("Aviso", "Por favor, preencha todos os campos obrigatórios.", "error");
        return;
      }

      this.isLoading = true;
      try {
        const token = localStorage.getItem("jwt");
        const payload = {
          questionText: questionData.newQuestion,
          description: questionData.description || null,
          categoryName: questionData.selectedCategory,
          options: questionData.newOptions,
          questionnaireIds: questionData.selectedQuestionnaires,
        };

        await axios.post("/api/questions", payload, {
          headers: { Authorization: `Bearer ${token}` },
        });

        await Promise.all([
          this.$store.dispatch("fetchQuestions"),
          this.$store.dispatch("fetchQuestionnaireByIdForAdmin", questionData.selectedQuestionnaires[0])
        ]);

        await this.showAlertDialog("Sucesso", "Questão adicionada com sucesso!", "success");
      } catch (error) {
        console.error("Erro ao adicionar questão:", error);
        await this.showAlertDialog("Erro", "Erro ao adicionar questão.", "error");
      } finally {
        this.isLoading = false;
      }
    },
    async deleteQuestion(id) {
      const proceed = await this.showAlertDialog(
        "Confirmar Eliminação",
        "Tem certeza que deseja eliminar esta questão?",
        "confirm"
      );
      if (!proceed) return;

      try {
        const token = localStorage.getItem("jwt");
        await axios.delete(`/api/questions/${id}`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        await this.$store.dispatch("fetchQuestions");
        await this.showAlertDialog("Sucesso", "Questão eliminada com sucesso!", "success");
      } catch (error) {
        console.error("Erro ao eliminar questão:", error);
        await this.showAlertDialog("Erro", "Erro ao eliminar questão.", "error");
      }
    },
    async addQuestionnaire(newTitle) {
      if (!newTitle) {
        await this.showAlertDialog("Aviso", "Por favor, insira um título para o questionário.", "error");
        return;
      }

      this.isLoading = true;
      try {
        const token = localStorage.getItem("jwt");
        await axios.post("/api/questionnaires/create", { title: newTitle }, {
          headers: { Authorization: `Bearer ${token}` },
        });
        await this.$store.dispatch("fetchQuestionnairesForAdmin");
        await this.showAlertDialog("Sucesso", "Questionário criado com sucesso!", "success");
      } catch (error) {
        console.error("Erro ao criar questionário:", error);
        await this.showAlertDialog("Erro", "Erro ao criar questionário.", "error");
      } finally {
        this.isLoading = false;
      }
    },
    async deleteQuestionnaire(id) {
      const proceed = await this.showAlertDialog(
        "Confirmar Eliminação",
        "Tem certeza que deseja eliminar este questionário?",
        "confirm"
      );
      if (!proceed) return;

      try {
        const token = localStorage.getItem("jwt");
        const response = await axios.delete(`/api/questionnaires/delete/${id}`, {
          headers: { Authorization: `Bearer ${token}` },
        });

        if ([200, 204].includes(response.status)) {
          await Promise.all([
            this.$store.dispatch("fetchQuestionnairesForAdmin"),
            this.$store.dispatch("fetchQuestions")
          ]);
          await this.showAlertDialog("Sucesso", "Questionário eliminado com sucesso!", "success");
        } else {
          throw new Error(`HTTP ${response.status}`);
        }
      } catch (error) {
        console.error("Erro ao eliminar questionário:", error);
        await this.showAlertDialog("Erro", "Erro ao eliminar questionário.", "error");
      }
    },
    async importQuestionnaire(jsonData) {
      if (!jsonData) return;

      try {
        const token = localStorage.getItem("jwt");
        await axios.post("/api/questionnaires/import", jsonData, {
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`
          },
        });

        await Promise.all([
          this.$store.dispatch("fetchQuestionnairesForAdmin"),
          this.$store.dispatch("fetchQuestions")
        ]);

        await this.showAlertDialog("Sucesso", "Questionário importado com sucesso!", "success");
      } catch (error) {
        console.error("Erro ao importar questionário:", error);
        await this.showAlertDialog("Erro", "Falha ao importar questionário.", "error");
      }
    },
    async exportQuestionnaire(id) {
      try {
        const token = localStorage.getItem("jwt");
        const response = await axios.get(`/api/questionnaires/${id}/export`, {
          headers: { Authorization: `Bearer ${token}` },
          responseType: "json",
        });

        const blob = new Blob([JSON.stringify(response.data, null, 2)], { type: "application/json" });
        const url = URL.createObjectURL(blob);
        const link = document.createElement("a");
        link.href = url;
        link.download = `questionnaire_${id}.json`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        URL.revokeObjectURL(url);

        await this.showAlertDialog("Sucesso", "Exportação concluída!", "success");
      } catch (error) {
        console.error("Erro ao exportar questionário:", error);
        await this.showAlertDialog("Erro", "Falha ao exportar questionário.", "error");
      }
    },
    async fetchFeedback(filters = {}) {
      this.isLoading = true;
      try {
        const token = localStorage.getItem("jwt");
        let url = "/api/feedback";

        const query = [];

        if (filters.email?.trim()) {
          query.push(`email=${encodeURIComponent(filters.email.trim())}`);
        }
        if (filters.type?.trim()) {
          query.push(`type=${encodeURIComponent(filters.type.trim())}`);
        }
        if (filters.startDate && filters.endDate) {
          query.push(`startDate=${filters.startDate}T00:00:00`);
          query.push(`endDate=${filters.endDate}T23:59:59`);
        }

        if (query.length > 0) {
          url = `/api/feedback/filter?${query.join("&")}`;
        }

        const response = await axios.get(url, {
          headers: { Authorization: `Bearer ${token}` }
        });

        this.feedbacks = response.data;
      } catch (error) {
        console.error("Erro ao buscar feedbacks:", error);
        this.feedbacks = [];
        await this.showAlertDialog("Erro", "Erro ao carregar feedbacks.", "error");
      } finally {
        this.isLoading = false;
      }
    },
    editQuestionnaire(id) {
      this.$router.push({ name: 'EditQuestionnaire', params: { questionnaireId: id } });
    }
  },
};
</script>


<style>
html,
body {
  height: 100%;
  margin: 0;
  padding: 0;
}
</style>