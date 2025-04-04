<template>
  <div class="h-screen overflow-y-auto bg-gradient-to-br from-blue-600 to-indigo-100 font-sans">
    <div class="container mx-auto px-4 py-6 max-w-7xl">
      <!-- Header -->
      <HeaderComponent
        title="Painel Administrativo"
        subtitle="Gerencie questões, questionários e feedbacks"
        back-to="/"
        :show-user-button="true"
      />

      <!-- Questionnaire Manager -->
      <QuestionnaireManager
        :questionnaires="questionnaires"
        @add-questionnaire="addQuestionnaire"
        @delete-questionnaire="deleteQuestionnaire"
        @import-questionnaire="importQuestionnaire"
        @export-questionnaire="exportQuestionnaire"
        @select-questionnaire="selectQuestionnaire"
      />

      <!-- Add Question Form -->
      <QuestionForm
        :categories="categories"
        :questionnaires="questionnaires"
        :optionTypes="optionTypes"
        :optionLevels="optionLevels"
        @add-question="addQuestion"
      />

      <!-- Questions List -->
      <QuestionsList
        :questions="questions"
        :categories="categories"
        @delete-question="deleteQuestion"
        @filter-questions="filterQuestionsByCategory"
      />

      <!-- Feedback List -->
      <FeedbackList
        :feedbacks="feedbacks"
        @fetch-feedback="fetchFeedback"
      />

      <!-- User Answers List -->
      <UserAnswersList
        :user-answers="userAnswers"
        @fetch-by-email="fetchUserAnswersByEmail"
        @fetch-all="fetchAllUserAnswers"
        @filter-by-date="filterAnswersByDate"
      />
    </div>
  </div>
</template>

<script>
 /* eslint-disable */
 import axios from '@/plugins/axios'
import HeaderComponent from "@/components/HeaderComponent.vue";
import QuestionnaireManager from "@/components/AdminDashboard/QuestionnaireManager.vue";
import QuestionForm from "@/components/AdminDashboard/QuestionForm.vue";
import QuestionsList from "@/components/AdminDashboard/QuestionsList.vue";
import FeedbackList from "@/components/AdminDashboard/FeedbackList.vue";
import UserAnswersList from "@/components/AdminDashboard/UserAnswersList.vue";


export default {
  name: "AdminDashboard",
  components: {
    HeaderComponent,
    QuestionnaireManager,
    QuestionForm,
    QuestionsList,
    FeedbackList,
    UserAnswersList,
  },
  data() {
    return {
      questions: [],
      feedbacks: [],
      questionnaires: [],
      userAnswers: [],
      categories: [
      ],
      optionTypes: ["IMPACT", "PROBABILITY"],
      optionLevels: ["LOW", "MEDIUM", "HIGH"],
      isLoading: false,
    };
  },
  created() {
    this.fetchQuestions();
    this.fetchFeedback();
    this.fetchQuestionnaires();
  },
  methods: {
    async fetchQuestions() {
      this.isLoading = true;
      try {
        const response = await axios.get("/api/questions/all");
        this.questions = response.data;
      } catch (error) {
        console.error("Error fetching questions:", error);
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
      if (!questionData.newQuestion || !questionData.selectedCategory || !questionData.selectedQuestionnaire) {
        alert("Por favor, insira a questão, selecione uma categoria e escolha um questionário.");
        return;
      }
      this.isLoading = true;
      try {
        const payload = {
          questionText: questionData.newQuestion,
          category: questionData.selectedCategory,
          options: questionData.newOptions,
        };
        await axios.post(`/api/questions/add/${questionData.selectedQuestionnaire}`, payload);
        this.fetchQuestions();
      } catch (error) {
        console.error("Error adding question:", error);
      } finally {
        this.isLoading = false;
      }
    },
    async deleteQuestion(id) {
      if (!confirm("Tem certeza que deseja excluir esta questão?")) return;
      try {
        await axios.delete(`/api/questions/delete/${id}`);
        this.fetchQuestions();
      } catch (error) {
        console.error("Error deleting question:", error);
      }
    },
    async fetchQuestionnaires() {
      try {
        const response = await axios.get("/api/questionnaires/all");
        this.questionnaires = response.data;
      } catch (error) {
        console.error("Error fetching questionnaires:", error);
        this.questionnaires = [];
      }
    },
    async addQuestionnaire(newTitle) {
      if (!newTitle) {
        alert("Por favor, insira um título para o questionário.");
        return;
      }
      this.isLoading = true;
      try {
        await axios.post("/api/questionnaires/create", { title: newTitle });
        this.fetchQuestionnaires();
      } catch (error) {
        console.error("Error adding questionnaire:", error);
      } finally {
        this.isLoading = false;
      }
    },
    async deleteQuestionnaire(id) {
      if (!confirm("Tem certeza que deseja excluir este questionário?")) return;
      try {
        const response = await axios.delete(`/api/questionnaires/delete/${id}`, {
          validateStatus: status => status < 500,
        });
        if (response.status === 204 || response.status === 200) {
          await this.fetchQuestionnaires(); // atualiza a lista de questionários
          await this.fetchQuestions(); // atualiza a lista de perguntas
        } else {
          console.error("Error deleting questionnaire, status:", response.status);
          alert("Falha ao excluir questionário. Tente novamente.");
        }
      } catch (error) {
        console.error("Error deleting questionnaire:", error);
      }
    },
    async exportQuestionnaire(questionnaireId) {
      try {
        const response = await axios.get(`/api/questionnaires/${questionnaireId}/export`, {
          responseType: "json",
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
        alert("Export concluído!");
      } catch (error) {
        console.error("Error exporting questionnaire:", error);
        alert("Falha ao exportar questionário.");
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
        await axios.post("/api/questionnaires/import", jsonData, {
          headers: { "Content-Type": "application/json" },
        });
        alert("Questionário importado com sucesso!");
        await this.fetchQuestionnaires(); // Atualiza os questionários
        await this.fetchQuestions(); // Atualiza as perguntas associadas
      } catch (error) {
        console.error("Error importing questionnaire:", error);
        alert("Falha ao importar questionário.");
      }
    },
    async fetchFeedback() {
  this.isLoading = true;
  try {
    const token = localStorage.getItem("jwt");
    const response = await axios.get("/api/feedback", {
      headers: {
        Authorization: `Bearer ${token}`
      }
    });
    // Assign the retrieved feedback data to the component property
    this.feedbacks = response.data;
  } catch (error) {
    console.error("Error fetching feedback:", error);
    this.feedbacks = [];
  } finally {
    this.isLoading = false;
  }
},
    async fetchUserAnswersByEmail(email) {
      if (!email) {
        alert("Por favor, insira um email para filtrar.");
        return;
      }
      this.isLoading = true;
      try {
        const response = await axios.get(`/api/answers/by-email-with-severity/${email}`, {
          validateStatus: _status => true
        });
        if (response.status === 401) {
          alert("Você não está autorizado.");
        } else {
          this.userAnswers = [response.data];
        }
      } catch (error) {
        console.error("Error fetching user answers by email:", error);
      } finally {
        this.isLoading = false;
      }
    },
    async fetchAllUserAnswers() {
      this.isLoading = true;
      try {
        const response = await axios.get("/api/answers/get-all-submissions", {
          validateStatus: _status => true,
        });
        if (response.status === 401) {
          alert("Você não está autorizado.");
        } else {
          this.userAnswers = response.data;
        }
      } catch (error) {
        console.error("Error fetching all user answers:", error);
      } finally {
        this.isLoading = false;
      }
    },
    async filterAnswersByDate({ startDate, endDate }) {
      if (!startDate || !endDate) {
        alert("Por favor, selecione ambas as datas.");
        return;
      }
      try {
        const response = await axios.get("/api/answers/by-date-range", {
          params: { startDate, endDate },
        });
        this.userAnswers = response.data;
      } catch (error) {
        console.error("Error filtering answers by date:", error);
      }
    },
  },
};

</script>
