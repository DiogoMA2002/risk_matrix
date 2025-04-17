<template>
  <div class="h-screen bg-gradient-to-br from-blue-600 to-indigo-100 font-sans overflow-y-scroll">
    <div class="container mx-auto px-4 py-6">
      <div class="flex justify-between items-center">
        <div class="flex items-center">
          <button @click="cancelEdit" class="p-2 rounded-full bg-white bg-opacity-20 text-white hover:bg-opacity-30 mr-4">
            <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
            </svg>
          </button>
          <div class="text-white">
            <div class="text-lg font-bold">Editar Pergunta</div>
            <div class="text-sm font-light">Gerir os detalhes da pergunta</div>
          </div>
        </div>
      </div>
    </div>

    <div class="container mx-auto px-4 py-6">
      <div class="bg-white bg-opacity-90 backdrop-blur-md rounded-xl shadow-xl p-8 max-w-3xl mx-auto">
        <h1 class="text-3xl font-bold text-blue-600 mb-6">Editar Pergunta</h1>

        <div v-if="loading" class="text-center py-10">
          <p>A carregar os detalhes da pergunta...</p>
          <SkeletonLoader />
        </div>

        <div v-else-if="error" class="text-center py-10 text-red-600">
          <p>Erro: {{ error }}</p>
          <button @click="fetchQuestionDetails"
            class="mt-2 px-3 py-1 text-sm border border-blue-500 text-blue-500 rounded hover:bg-blue-50">
            Tentar novamente
          </button>
        </div>

        <form v-else @submit.prevent="saveQuestion" class="space-y-6 text-gray-700">
          <!-- Question text -->
          <div>
            <label class="block text-xl font-semibold text-blue-600 mb-3">Texto da Pergunta</label>
            <input v-model="question.questionText" type="text" required
              placeholder="Introduza o texto da pergunta"
              class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500" />
          </div>

          <!-- Category select -->
          <div>
            <label class="block text-sm font-medium text-gray-700">Categoria</label>
            <div v-if="loadingCategories" class="mt-1 text-sm text-gray-500">A carregar categorias...</div>
            <div v-else-if="categoryError" class="mt-1 text-sm text-red-500">Erro: {{ categoryError }}</div>
            <select v-else v-model="question.categoryName" required
              class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
              :disabled="loadingCategories || !!categoryError">
              <option disabled value="">-- Selecione uma categoria --</option>
              <option v-for="cat in availableCategories" :key="cat.name" :value="cat.name">{{ cat.name }}</option>
              <option v-if="!availableCategories.length" disabled value="">Nenhuma categoria disponível</option>
            </select>
          </div>

          <!-- Options -->
          <div>
            <h2 class="text-xl font-semibold text-blue-600 mb-3">Opções da Pergunta</h2>
            <div class="space-y-4">
              <div v-for="(option, index) in question.options" :key="index" class="p-4 bg-indigo-50 rounded-lg">
                <div class="flex flex-col md:flex-row md:space-x-4">
                  <div class="flex-1">
                    <label class="block text-sm font-medium text-gray-700">Texto da Opção</label>
                    <input v-model="option.optionText" type="text" required
                      class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500" />
                  </div>
                  <input type="hidden" v-model="option.optionType" />
                  <div class="flex-1">
                    <label class="block text-sm font-medium text-gray-700">Nível</label>
                    <select v-model="option.optionLevel" required
                      class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500">
                      <option value="LOW">Baixo</option>
                      <option value="MEDIUM">Médio</option>
                      <option value="HIGH">Alto</option>
                    </select>
                  </div>
                  <div>
                    <button @click="removeOption(index)" type="button"
                      class="p-2 rounded-full bg-white text-red-500 hover:bg-red-100 transition" title="Remover Opção">
                      <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24"
                        stroke="currentColor" stroke-width="2">
                        <path stroke-linecap="round" stroke-linejoin="round"
                          d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4H9v3M4 7h16" />
                      </svg>
                    </button>
                  </div>
                </div>
              </div>
              <button @click="addOption" type="button"
                class="mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition">
                + Adicionar Opção
              </button>
            </div>
          </div>

          <!-- Questionnaire checkboxes -->
          <div>
            <h2 class="text-xl font-semibold text-blue-600 mb-3">Questionários Associados</h2>
            <div v-if="questionnaireError" class="text-red-500 mb-2">Erro ao carregar questionários: {{ questionnaireError }}</div>
            <div v-else-if="!allQuestionnaires.length" class="text-gray-500">A carregar questionários...</div>
            <div v-else class="space-y-2 bg-white p-4 rounded-lg shadow">
              <div v-for="q in allQuestionnaires" :key="q.id" class="flex items-center space-x-2">
                <input type="checkbox" :value="q.id" v-model="associatedQuestionnaireIds"
                  class="form-checkbox h-4 w-4 text-blue-600" />
                <span>{{ q.name }}</span>
              </div>
            </div>
          </div>

          <!-- Save buttons -->
          <div class="mt-6 flex justify-between items-center">
            <button type="button" @click="cancelEdit"
              class="px-6 py-3 bg-white border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition">
              Cancelar
            </button>
            <button type="submit" :disabled="isSaving || loading"
              class="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition disabled:opacity-50 disabled:cursor-not-allowed">
              <span v-if="isSaving">A guardar...</span>
              <span v-else>Guardar Alterações</span>
            </button>
          </div>

          <div v-if="saveError" class="mt-4 text-center text-red-600 text-sm">{{ saveError }}</div>
        </form>
      </div>
    </div>

    <!-- Alert Dialog -->
    <AlertDialog
      v-if="showSuccessDialog"
      :show="showSuccessDialog"
      type="success"
      title="Sucesso"
      message="A pergunta foi guardada com sucesso!"
      @confirm="showSuccessDialog = false"
    />
  </div>
</template>

<script>
import { mapState } from 'vuex';
import axios from 'axios';
import SkeletonLoader from '@/components/Static/SkeletonLoader.vue';
import AlertDialog from '@/components/Static/AlertDialog.vue';

const OptionLevel = {
  LOW: 'LOW',
  MEDIUM: 'MEDIUM',
  HIGH: 'HIGH',
};
const OptionLevelType = {
  IMPACT: 'IMPACT',
  PROBABILITY: 'PROBABILITY',
};

export default {
  name: 'EditQuestionPage',
  components: { SkeletonLoader, AlertDialog },
  data() {
    return {
      question: {
        id: null,
        questionText: '',
        categoryName: '',
        options: [],
      },
      associatedQuestionnaireIds: [],
      loading: true,
      error: null,
      loadingCategories: false,
      categoryError: null,
      questionnaireError: null,
      isSaving: false,
      saveError: null,
      showSuccessDialog: false,
    };
  },
  computed: {
    ...mapState(['questions', 'questionnaires', 'categories']),
    questionId() {
      return parseInt(this.$route.params.questionId, 10);
    },
    allQuestionnaires() {
      return this.questionnaires.map(q => ({ id: q.id, name: q.title || q.name }));
    },
    availableCategories() {
      return this.categories.filter(c => c.name);
    }
  },
  methods: {
    async fetchQuestionDetails() {
      this.loading = true;
      try {
        const { data } = await axios.get(`/api/questions/${this.questionId}`);
        this.question = {
          id: data.id,
          questionText: data.questionText,
          categoryName: data.category?.name || '',
          options: data.options.map(opt => ({
            optionText: opt.optionText,
            optionLevel: opt.optionLevel,
            optionType: opt.optionType
          }))
        };
        this.associatedQuestionnaireIds = data.questionnaires?.map(q => q.id) || [];
      } catch (err) {
        this.error = err.response?.data?.message || 'Erro ao carregar os detalhes da pergunta.';
      } finally {
        this.loading = false;
      }
    },

    async saveQuestion() {
      this.isSaving = true;
      this.saveError = null;

      const token = localStorage.getItem("jwt");
      if (!token) {
        this.saveError = "Erro de autenticação. Faça login novamente.";
        this.isSaving = false;
        return;
      }

      const finalCategory = this.question.categoryName?.trim();
      if (!finalCategory) {
        this.saveError = "Por favor selecione uma categoria válida.";
        this.isSaving = false;
        return;
      }

      const isDuplicate = this.questions.some(q =>
        q.questionText?.trim().toLowerCase() === this.question.questionText.trim().toLowerCase() &&
        q.id !== this.question.id
      );

      if (isDuplicate) {
        this.saveError = "Já existe uma pergunta com este texto.";
        this.isSaving = false;
        return;
      }

      try {
        await axios.put(`/api/questions/${this.question.id}`, {
          id: this.question.id,
          questionText: this.question.questionText,
          categoryName: finalCategory,
          options: this.question.options.map(opt => ({
            optionText: opt.optionText,
            optionLevel: opt.optionLevel,
            optionType: opt.optionType || OptionLevelType.IMPACT
          })),
          questionnaireIds: this.associatedQuestionnaireIds
        }, {
          headers: { Authorization: `Bearer ${token}` }
        });

        this.showSuccessDialog = true;
      } catch (err) {
        this.saveError = err.response?.data?.message || 'Erro ao guardar a pergunta.';
      } finally {
        this.isSaving = false;
      }
    },

    addOption() {
      this.question.options.push({
        optionText: '',
        optionLevel: OptionLevel.LOW,
        optionType: OptionLevelType.IMPACT
      });
    },

    removeOption(index) {
      this.question.options.splice(index, 1);
    },

    cancelEdit() {
      this.$router.go(-1);
    }
  },
  async mounted() {
    await Promise.all([
      this.$store.dispatch('fetchQuestionnaires'),
      this.$store.dispatch('fetchQuestions'),
      this.$store.dispatch('fetchCategories'),
      this.fetchQuestionDetails()
    ]);
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
