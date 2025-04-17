<template>
  <div class="h-screen bg-gradient-to-br from-blue-600 to-indigo-100 font-sans overflow-y-scroll">
    <!-- Header -->
    <div class="container mx-auto px-4 py-6">
      <div class="flex justify-between items-center">
        <div class="flex items-center">
          <button @click="$router.push('/admin')" class="p-2 rounded-full bg-white bg-opacity-20 text-white hover:bg-opacity-30 mr-4">
            <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
            </svg>
          </button>
          <div class="text-white">
            <div class="text-lg font-bold">Editar Questionário</div>
            <div class="text-sm font-light">Gerir os questionários e respetivas questões</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Main -->
    <div class="container mx-auto px-4 py-6">
      <div class="bg-white bg-opacity-90 backdrop-blur-md rounded-xl shadow-xl p-8 max-w-3xl mx-auto transition hover:shadow-2xl">
        <h1 class="text-3xl font-bold text-blue-600 mb-6">Editar Questionários</h1>

        <div v-if="loading" class="text-center py-10">
          <p>A carregar os detalhes do questionário...</p>
          <div class="animate-spin rounded-full h-8 w-8 border-4 border-gray-300 border-t-blue-600 inline-block"></div>
        </div>

        <div v-else-if="error" class="text-center py-10 text-red-600">
          <p>Erro: {{ error }}</p>
          <button @click="fetchQuestionnaireDetails" class="mt-2 px-3 py-1 text-sm border border-blue-500 text-blue-500 rounded hover:bg-blue-50">
            Tentar Novamente
          </button>
        </div>

        <div v-else>
          <!-- Title edit form -->
          <form @submit.prevent="updateQuestionnaire" class="space-y-6 text-gray-700 mb-8">
            <div>
              <label for="questionnaireTitle" class="block text-xl font-semibold text-blue-600 mb-3">Nome do Questionário</label>
              <input id="questionnaireTitle" v-model="questionnaire.title" type="text" required
                class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 transition" />
            </div>

            <div class="mt-4">
              <button type="submit" :disabled="isSaving"
                class="px-6 py-3 bg-blue-600 text-white rounded-lg transition hover:bg-blue-700 disabled:opacity-50 flex items-center">
                <svg v-if="isSaving" class="animate-spin h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke-width="4" />
                  <path class="opacity-75" fill="currentColor"
                    d="M4 12a8 8 0 018-8v8z" />
                </svg>
                <span>{{ isSaving ? 'A guardar...' : 'Alterar Nome' }}</span>
              </button>
            </div>
          </form>

          <!-- Question Filtering -->
          <div>
            <h2 class="text-xl font-semibold text-blue-600 mb-4">Questões</h2>
            <label class="block text-sm font-medium text-gray-700 mb-1">Filtrar por Categoria</label>
            <select v-model="selectedFilterCategory" @change="filterQuestions"
              class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 transition">
              <option value="">Todas as Categorias</option>
              <option v-for="cat in categories" :key="cat.id" :value="cat.name">{{ formatCategoryName(cat.name) }}</option>
            </select>

            <!-- Question List -->
            <ul class="divide-y divide-gray-200 mt-4">
              <li v-for="question in paginatedQuestions" :key="question.id" class="py-3 flex justify-between items-center px-3 hover:bg-blue-50 rounded-lg">
                <div>
                  <p class="font-medium">{{ question.questionText }}</p>
                  <span class="text-sm text-blue-600 bg-blue-100 px-2 py-1 rounded-full mt-1 inline-block">
                    {{ formatCategoryName(question.category?.name || question.category) }}
                  </span>
                </div>
                <div class="flex space-x-2">
                  <button @click="goToEditQuestion(question.id)" class="p-2 text-gray-500 hover:text-blue-600" title="Editar">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M11 5H6a2 2 0 0 0-2 2v11a2 2 0 0 0 2 2h11a2 2 0 0 0 2-2v-5m-1.414-9.414a2 2 0 1 1 2.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
                  </button>
                  <button @click="deleteQuestion(question.id)" class="p-2 text-gray-500 hover:text-red-600" title="Remover">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M19 7l-.867 12.142A2 2 0 0 1 16.138 21H7.862a2 2 0 0 1-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 0 0-1-1h-4a1 1 0 0 0-1 1v3M4 7h16" />
              </svg>
                  </button>
                </div>
              </li>
            </ul>

            <!-- No results -->
            <div v-if="!filteredQuestions.length" class="py-8 text-center text-gray-500">
              Nenhuma questão encontrada para este questionário.
            </div>

            <!-- Pagination -->
            <div v-if="totalPages > 1" class="flex justify-center mt-6 space-x-2">
              <button @click="prevPage" :disabled="currentPage === 1" class="px-3 py-1 border rounded">Voltar</button>
              <span>Página {{ currentPage }} de {{ totalPages }}</span>
              <button @click="nextPage" :disabled="currentPage === totalPages" class="px-3 py-1 border rounded">Seguinte</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <AlertDialog
  v-if="showSuccessDialog"
  :show="showSuccessDialog"
  type="success"
  title="Sucesso"
  message="O nome do questionário foi atualizado com sucesso!"
  @confirm="showSuccessDialog = false"
/>

</template>

<script>
import { mapState } from 'vuex';
import axios from 'axios';
import AlertDialog from '@/components/Static/AlertDialog.vue';

export default {
  name: 'EditQuestionnairePage',
  data() {
    return {
      questionnaire: { id: null, title: '', questions: [] },
      error: null,
      loading: true,
      isSaving: false,
      selectedFilterCategory: '',
      currentPage: 1,
      itemsPerPage: 5,
      filteredQuestions: [],
      showSuccessDialog: false,

    };
  },
  computed: {
    ...mapState(['categories']),
    questionnaireId() {
      return this.$route.params.questionnaireId;
    },
    paginatedQuestions() {
      const start = (this.currentPage - 1) * this.itemsPerPage;
      return this.filteredQuestions.slice(start, start + this.itemsPerPage);
    },
    totalPages() {
      return Math.ceil(this.filteredQuestions.length / this.itemsPerPage);
    }
  },
  async created() {
    await Promise.all([
      this.$store.dispatch('fetchCategories'),
      this.fetchQuestionnaireDetails()
    ]);
  },
  components: {
  AlertDialog
}
,
  methods: {
    async fetchQuestionnaireDetails() {
      this.loading = true;
      try {
        const response = await axios.get(`/api/questionnaires/${this.questionnaireId}`);
        this.questionnaire = response.data;
        this.filteredQuestions = [...response.data.questions];
      } catch (err) {
        this.error = err.response?.data?.message || 'Erro ao carregar questionário.';
      } finally {
        this.loading = false;
      }
    },

    async updateQuestionnaire() {
  if (!this.questionnaire.title.trim()) return;
  this.isSaving = true;

  try {
    const token = localStorage.getItem('jwt');
    await axios.put(`/api/questionnaires/${this.questionnaireId}`, {
      title: this.questionnaire.title.trim()
    }, {
      headers: { Authorization: `Bearer ${token}` }
    });

    this.showSuccessDialog = true;
  } catch (err) {
    this.error = err.response?.data?.message || 'Erro ao atualizar título.';
  } finally {
    this.isSaving = false;
  }
}

,
    filterQuestions() {
      if (!this.selectedFilterCategory) {
        this.filteredQuestions = [...this.questionnaire.questions];
      } else {
        this.filteredQuestions = this.questionnaire.questions.filter(q => {
          const cat = typeof q.category === 'object' ? q.category?.name : q.category;
          return cat === this.selectedFilterCategory;
        });
      }
      this.currentPage = 1;
    },

    formatCategoryName(categoryName) {
      if (!categoryName || typeof categoryName !== "string") return "";
      
      return categoryName
        .replace(/_/g, " ")
        .split(' ')
        .map(word => {
          if (!word) return '';
          return word.charAt(0).toLocaleUpperCase('pt-PT') + 
                 word.slice(1).toLocaleLowerCase('pt-PT');
        })
        .join(' ');
    },

    goToEditQuestion(id) {
      this.$router.push(`/admin/edit-question/${id}`);
    },

    async deleteQuestion(questionId) {
      if (!confirm('Tem a certeza que deseja remover esta questão do questionário?')) return;

      try {
        const token = localStorage.getItem('jwt');
        const question = await axios.get(`/api/questions/${questionId}`);
        const updatedIds = question.data.questionnaireIds.filter(id => id != this.questionnaireId);
        await axios.put(`/api/questions/${questionId}`, {
          ...question.data,
          questionnaireIds: updatedIds
        }, {
          headers: { Authorization: `Bearer ${token}` }
        });

        await this.fetchQuestionnaireDetails();
      } catch (err) {
        alert('Erro ao remover questão.');
        console.error(err);
      }
    },

    nextPage() {
      if (this.currentPage < this.totalPages) this.currentPage++;
    },

    prevPage() {
      if (this.currentPage > 1) this.currentPage--;
    }
  }
};
</script>

<style>
html, body {
  height: 100%;
  margin: 0;
  padding: 0;
}
</style>
