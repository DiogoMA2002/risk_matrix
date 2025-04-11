<template>
  <div class="h-screen overflow-y-auto bg-gradient-to-br from-blue-600 to-indigo-100 font-sans">
    <div class="container mx-auto px-4 py-6 max-w-7xl">
      <!-- Header -->
      <HeaderComponent title="Painel Administrativo" subtitle="Gerencie questões, questionários e feedbacks" back-to="/"
        :show-user-button="true" />

      <!-- Questionnaire Manager -->
      <QuestionnaireManager :questionnaires="questionnaires" @add-questionnaire="addQuestionnaire"
        @delete-questionnaire="deleteQuestionnaire" @import-questionnaire="importQuestionnaire"
        @export-questionnaire="exportQuestionnaire" @select-questionnaire="selectQuestionnaire" />

      <!-- Add Question Form -->
      <QuestionForm :categories="categories" :questionnaires="questionnaires" :optionTypes="optionTypes"
        :optionLevels="optionLevels" @add-question="addQuestion" @create-category="createCategory"
        @update-categories="updateCategories" />

      <!-- Questions List -->
      <QuestionsList :questions="questions" :categories="categories" @delete-question="deleteQuestion"
        @filter-questions="filterQuestionsByCategory" />

      <!-- Feedback List -->
      <FeedbackList :feedbacks="feedbacks" @fetch-feedback="fetchFeedback" />

      <!-- User Answers List -->
      <UserAnswersList :user-answers="userAnswers" @fetch-by-email="fetchUserAnswersByEmail"
        @fetch-all="fetchAllUserAnswers" @filter-by-date="filterAnswersByDate"
        @download-report="downloadSubmissionDocx" />


      <!-- Alert Dialog -->
      <AlertDialog :show="showAlert" :title="alertTitle" :message="alertMessage" :type="alertType"
        @confirm="handleAlertConfirm" @cancel="handleAlertCancel" />
    </div>
  </div>
</template>

<script>
/* eslint-disable */
import axios from 'axios'
import HeaderComponent from "@/components/Static/HeaderComponent.vue";
import QuestionnaireManager from "@/components/AdminDashboard/QuestionnaireManager.vue";
import QuestionForm from "@/components/AdminDashboard/QuestionForm.vue";
import QuestionsList from "@/components/AdminDashboard/QuestionsList.vue";
import FeedbackList from "@/components/AdminDashboard/FeedbackList.vue";
import UserAnswersList from "@/components/AdminDashboard/UserAnswersList.vue";
import AlertDialog from "@/components/Static/AlertDialog.vue";

export default {
  name: "AdminDashboard",
  components: {
    HeaderComponent,
    QuestionnaireManager,
    QuestionForm,
    QuestionsList,
    FeedbackList,
    UserAnswersList,
    AlertDialog
  },
  data() {
    return {
      questions: [],
      feedbacks: [],
      questionnaires: [],
      userAnswers: [],
      // Categories are fetched from the backend and stored as an array of strings (names)
      categories: [],
      optionTypes: ["IMPACT", "PROBABILITY"],
      optionLevels: ["LOW", "MEDIUM", "HIGH"],
      isLoading: false,
      // We'll track the currently selected questionnaire
      selectedQuestionnaire: null,
      showAlert: false,
      alertTitle: "",
      alertMessage: "",
      alertType: "info",
      alertResolve: null
    };
  },
  created() {
    this.fetchCategories();
    this.fetchQuestions();
    this.fetchFeedback();
    this.fetchQuestionnaires();
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
    async fetchCategories() {
      try {
        const response = await axios.get("/api/categories");
        // Ensure each category has a valid name
        this.categories = response.data
          .map(category => category.name)
          .filter(name => !!name); // Filter out falsy values
      } catch (error) {
        console.error("Error fetching categories:", error);
        this.categories = [];
        await this.showAlertDialog("Erro", "Erro ao carregar categorias.", "error");
      }
    },
    async fetchQuestions() {
      this.isLoading = true;
      try {
        const response = await axios.get("/api/questions/all");
        this.questions = response.data;
      } catch (error) {
        console.error("Error fetching questions:", error);
        await this.showAlertDialog("Erro", "Erro ao carregar questões.", "error");
      } finally {
        this.isLoading = false;
      }
    },
    async filterQuestionsByCategory(category) {
      this.isLoading = true;
      try {
        if (category === "") {
          await this.fetchQuestions();
        } else {
          const response = await axios.get(`/api/questions/category/${category}`);
          this.questions = response.data;
        }
      } catch (error) {
        console.error("Error filtering questions:", error);
      } finally {
        this.isLoading = false;
      }
    },
    async addQuestion(questionData) {
      console.log("Adding question with data:", questionData);
      if (!questionData.newQuestion || !questionData.selectedCategory || questionData.selectedQuestionnaires.length === 0) {
        await this.showAlertDialog("Aviso", "Por favor, insira a questão, selecione uma categoria e escolha pelo menos um questionário.", "error");
        return;
      }
      this.isLoading = true;
      try {
        const token = localStorage.getItem("jwt");
        const payload = {
          questionText: questionData.newQuestion,
          category: questionData.selectedCategory,
          options: questionData.newOptions,
          questionnaireIds: questionData.selectedQuestionnaires, // new field with multiple IDs
        };

        await axios.post("/api/questions/add", payload, 
        {
          headers: {
             Authorization: `Bearer ${token}`
           },
        },
        );
        

        // Then fetch the updated questionnaire
        await this.$store.dispatch('fetchQuestionnaireById', questionData.selectedQuestionnaires[0]);

        // Optionally fetch all questions as well
        this.fetchQuestions();

        await this.showAlertDialog("Sucesso", "Questão adicionada com sucesso!", "success");
      } catch (error) {
        console.error("Error adding question:", error);
        await this.showAlertDialog("Erro", "Erro ao adicionar questão.", "error");
      } finally {
        this.isLoading = false;
      }
    },
    async deleteQuestion(id) {
      const proceed = await this.showAlertDialog(
        "Confirmar Exclusão",
        "Tem certeza que deseja excluir esta questão?",
        "confirm"
      );
      if (!proceed) return;

      try {
        const token = localStorage.getItem("jwt");
        await axios.delete(`/api/questions/delete/${id}`, {
           headers: {
             Authorization: `Bearer ${token}`
           }}
        );
        this.fetchQuestions();
        await this.showAlertDialog("Sucesso", "Questão excluída com sucesso!", "success");
      } catch (error) {
        console.error("Error deleting question:", error);
        await this.showAlertDialog("Erro", "Erro ao excluir questão.", "error");
      }
    },
    async fetchQuestionnaires() {
      try {
        const response = await axios.get("/api/questionnaires/all");
        this.questionnaires = response.data;
        // If there's no selected questionnaire, set the first one as selected.
        if (response.data.length > 0 && !this.selectedQuestionnaire) {
          this.selectedQuestionnaire = response.data[0];
        }
      } catch (error) {
        console.error("Error fetching questionnaires:", error);
        this.questionnaires = [];
        await this.showAlertDialog("Erro", "Erro ao carregar questionários.", "error");
      }
    },
    async fetchQuestionnaireById(id) {
      try {
        const response = await axios.get(`/api/questionnaires/${id}`);
        this.selectedQuestionnaire = response.data;
      } catch (error) {
        console.error("Error fetching questionnaire by id:", error);
        await this.showAlertDialog("Erro", "Erro ao carregar questionário.", "error");
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
        await axios.post("/api/questionnaires/create", { title: newTitle, 
          headers: {
             Authorization: `Bearer ${token}`
           },
          },
         );
        this.fetchQuestionnaires();
        await this.showAlertDialog("Sucesso", "Questionário criado com sucesso!", "success");
      } catch (error) {
        console.error("Error adding questionnaire:", error);
        await this.showAlertDialog("Erro", "Erro ao criar questionário.", "error");
      } finally {
        this.isLoading = false;
      }
    },
    async deleteQuestionnaire(id) {
      const proceed = await this.showAlertDialog(
        "Confirmar Exclusão",
        "Tem certeza que deseja excluir este questionário?",
        "confirm"
      );
      if (!proceed) return;

      try {
        const token = localStorage.getItem("jwt");
        const response = await axios.delete(`/api/questionnaires/delete/${id}`, {
          validateStatus: status => status < 500,
          headers: {
             Authorization: `Bearer ${token}`
           },
        });
        if (response.status === 204 || response.status === 200) {
          await this.fetchQuestionnaires();
          await this.fetchQuestions();
          await this.showAlertDialog("Sucesso", "Questionário excluído com sucesso!", "success");
        } else {
          console.error("Error deleting questionnaire, status:", response.status);
          await this.showAlertDialog("Erro", "Falha ao excluir questionário. Tente novamente mais tarde.", "error");
        }
      } catch (error) {
        console.error("Error deleting questionnaire:", error);
        await this.showAlertDialog("Erro", "Erro ao excluir questionário.", "error");
      }
    },
    async exportQuestionnaire(questionnaireId) {
      try {
        const token = localStorage.getItem("jwt");
        const response = await axios.get(`/api/questionnaires/${questionnaireId}/export`, {
          responseType: "json",
          headers: {
             Authorization: `Bearer ${token}`
           },
        });
        const jsonData = response.data;
        const jsonString = JSON.stringify(jsonData, null, 2);
        const blob = new Blob([jsonString], { type: "application/json" });
        const url = URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = `questionnaire_${questionnaireId}.json`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
        await this.showAlertDialog("Sucesso", "Export concluído!");
      } catch (error) {
        console.error("Error exporting questionnaire:", error);
        await this.showAlertDialog("Erro", "Falha ao exportar questionário.", "error");
      }
    },
    async selectQuestionnaire(id) {
      try {
        await axios.get(`/api/questionnaires/${id}`);
      } catch (error) {
        console.error("Error selecting questionnaire:", error);
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
        await this.showAlertDialog("Sucesso", "Questionário importado com sucesso!");
        await this.fetchQuestionnaires();
        await this.fetchQuestions();
      } catch (error) {
        console.error("Error importing questionnaire:", error);
        await this.showAlertDialog("Erro", "Falha ao importar questionário.", "error");
      }
    },
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
      } catch (error) {
        console.error("Erro ao descarregar relatório:", error);
        await this.showAlertDialog("Erro", "Falha ao descarregar relatório.", "error");
      }
    }
    ,
    async fetchFeedback() {
      this.isLoading = true;
      try {
        const token = localStorage.getItem("jwt");
        const response = await axios.get("/api/feedback", {
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        this.feedbacks = response.data;
      } catch (error) {
        console.error("Error fetching feedback:", error);
        this.feedbacks = [];
        await this.showAlertDialog("Erro", "Erro ao carregar feedbacks.", "error");
      } finally {
        this.isLoading = false;
      }
    },
    async fetchUserAnswersByEmail(email) {
      if (!email) {
        await this.showAlertDialog("Aviso", "Por favor, insira um email para filtrar.", "error");
        return;
      }
      this.isLoading = true;
      try {
        const token = localStorage.getItem("jwt");
        const response = await axios.get(`/api/answers/by-email-with-severity/${email}`, {
          validateStatus: _status => true, 
          headers: {
             Authorization: `Bearer ${token}`
           },
        });
        if (response.status === 401) {
          await this.showAlertDialog("Erro", "Você não está autorizado.", "error");
        } else {
          this.userAnswers = [response.data];
        }
      } catch (error) {
        console.error("Error fetching user answers by email:", error);
        await this.showAlertDialog("Erro", "Erro ao buscar respostas por email.", "error");
      } finally {
        this.isLoading = false;
      }
    },
    async fetchAllUserAnswers() {
      this.isLoading = true;
      try {
        const token = localStorage.getItem("jwt");
        const response = await axios.get("/api/answers/get-all-submissions", {
          validateStatus: _status => true,
          headers: {
             Authorization: `Bearer ${token}`
           },
        });
        if (response.status === 401) {
          await this.showAlertDialog("Erro", "Você não está autorizado.", "error");
        } else {
          this.userAnswers = response.data;
        }
      } catch (error) {
        console.error("Error fetching all user answers:", error);
        await this.showAlertDialog("Erro", "Erro ao buscar todas as respostas.", "error");
      } finally {
        this.isLoading = false;
      }
    },
    async filterAnswersByDate({ startDate, endDate }) {
      if (!startDate || !endDate) {
        await this.showAlertDialog("Aviso", "Por favor, selecione ambas as datas.", "error");
        return;
      }
      try {
        const token = localStorage.getItem("jwt");
        const response = await axios.get("/api/answers/by-date-range", {
          params: { startDate, endDate },
          headers: {
             Authorization: `Bearer ${token}`
           },
        });
        this.userAnswers = response.data;
      } catch (error) {
        console.error("Error filtering answers by date:", error);
        await this.showAlertDialog("Erro", "Erro ao filtrar respostas por data.", "error");
      }
    },
    // NEW METHODS TO HANDLE CATEGORY EVENTS FROM QuestionForm
    async createCategory(newCategory) {
      try {
        const response = await axios.post("/api/categories", { name: newCategory });
        const createdCategory = response.data;
        if (!this.categories.includes(createdCategory.name)) {
          this.categories.push(createdCategory.name);
        }
        await this.showAlertDialog("Sucesso", "Categoria criada com sucesso!");
      } catch (error) {
        console.error("Error creating category:", error);
        await this.showAlertDialog("Erro", "Erro ao criar categoria.", "error");
      }
    },
    updateCategories(newCategory) {
      if (!this.categories.includes(newCategory)) {
        this.categories.push(newCategory);
      }
    },
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
