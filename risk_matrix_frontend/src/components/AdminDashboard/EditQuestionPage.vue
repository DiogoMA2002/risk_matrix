<template>
  <div class="h-screen bg-gradient-to-br from-blue-600 to-indigo-100 font-sans overflow-y-scroll">
    <!-- Header with back button and logo -->
    <div class="container mx-auto px-4 py-6">
      <div class="flex justify-between items-center">
        <!-- Left side: back button and title -->
        <div class="flex items-center">
          <button @click="$router.go(-1)"
            class="p-2 rounded-full bg-white bg-opacity-20 backdrop-blur-sm text-white hover:bg-opacity-30 transition-all duration-300 mr-4">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
            </svg>
          </button>
          <div class="text-white">
            <div class="text-lg font-bold">Edit Question</div>
            <div class="text-sm font-light">Manage question details</div>
          </div>
        </div>

        <!-- Right side: Logo placeholder -->
        <div class="flex items-center space-x-2 text-white">
          <!-- Logo image would go here -->
        </div>
      </div>
    </div>

    <!-- Main content -->
    <div class="container mx-auto px-4 py-6">
      <div class="bg-white bg-opacity-90 backdrop-blur-md rounded-xl shadow-xl p-8 max-w-3xl mx-auto transition-all duration-500 hover:shadow-2xl">
        <h1 class="text-3xl font-bold text-blue-600 mb-6">Edit Question</h1>

        <div v-if="loading" class="text-center py-10">
          <p>Loading question details...</p>
          <SkeletonLoader />
        </div>

        <div v-else-if="error" class="text-center py-10 text-red-600">
          <p>Error loading question: {{ error }}</p>
          <button @click="fetchQuestionDetails" class="mt-2 px-3 py-1 text-sm border border-blue-500 text-blue-500 rounded hover:bg-blue-50">
            Retry
          </button>
        </div>

        <form v-else @submit.prevent="saveQuestion" class="space-y-6 text-gray-700">
          <!-- Question Text -->
          <div>
            <label for="questionText" class="block text-xl font-semibold text-blue-600 mb-3">Question Text</label>
            <input 
              id="questionText" 
              v-model="question.questionText" 
              type="text" 
              required
              placeholder="Enter question text"
              class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 transition-all duration-300"
            />
          </div>

          <!-- Category -->
          <div>
            <label for="category" class="block text-sm font-medium text-gray-700">Category</label>
            <div v-if="loadingCategories" class="mt-1 text-sm text-gray-500">Loading categories...</div>
            <div v-else-if="categoryError" class="mt-1 text-sm text-red-500">Error: {{ categoryError }}</div>
            <select v-else id="category" v-model="question.categoryName" required
                    class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 disabled:bg-gray-100"
                    :disabled="loadingCategories || !!categoryError">
              <option disabled value="">-- Please select a category --</option>
              <option v-for="cat in availableCategories" :key="cat.name" :value="cat.name">{{ cat.name }}</option>
              <option v-if="!loadingCategories && !categoryError && !availableCategories.length" disabled value="">No categories found</option>
            </select>
          </div>

          <!-- Options -->
          <div>
            <h2 class="text-xl font-semibold text-blue-600 mb-3">Question Options</h2>
            <div class="p-4 bg-blue-50 rounded-lg border-l-4 border-blue-500 mb-4">
              <p class="font-medium">
                Define the possible answers and their corresponding risk level.
              </p>
            </div>
            
            <div class="space-y-4">
              <div
                v-for="(option, index) in question.options"
                :key="index"
                class="p-4 bg-indigo-50 rounded-lg"
              >
                <div class="flex flex-col md:flex-row md:items-center md:space-x-4 space-y-2 md:space-y-0">
                  <!-- Option Text -->
                  <div class="flex-1">
                    <label :for="'optionText-' + index" class="block text-sm font-medium text-gray-700">Option Text</label>
                    <input
                      :id="'optionText-' + index"
                      type="text"
                      v-model="option.optionText"
                      placeholder="Option text"
                      required
                      class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 transition-all duration-300"
                    />
                  </div>
                  
                  <!-- Option Type (Hidden input) -->
                  <input type="hidden" v-model="option.optionType">
                  
                  <!-- Option Level -->
                  <div class="flex-1">
                    <label :for="'optionLevel-' + index" class="block text-sm font-medium text-gray-700">Level</label>
                    <select
                      :id="'optionLevel-' + index"
                      v-model="option.optionLevel"
                      required
                      class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 transition-all duration-300"
                    >
                      <option value="LOW">Low</option>
                      <option value="MEDIUM">Medium</option>
                      <option value="HIGH">High</option>
                    </select>
                  </div>
                  
                  <!-- Remove Option Button -->
                  <div>
                    <button
                      type="button"
                      @click="removeOption(index)"
                      class="p-2 rounded-full bg-white text-red-500 hover:bg-red-100 hover:text-red-700 transition-all duration-300"
                      title="Remove Option"
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                      </svg>
                    </button>
                  </div>
                </div>
              </div>
            </div>
            
            <button
              type="button"
              @click="addOption"
              class="mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg font-medium transition-all duration-300 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 shadow-md hover:shadow-lg flex items-center"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
              </svg>
              Add Option
            </button>
          </div>

          <!-- Questionnaire Associations -->
          <div>
            <h2 class="text-xl font-semibold text-blue-600 mb-3">Associated Questionnaires</h2>
            <div class="p-4 bg-blue-50 rounded-lg border-l-4 border-blue-500 mb-4">
              <p class="font-medium">
                Select the questionnaires this question belongs to.
              </p>
            </div>
            
            <div v-if="loadingQuestionnaires" class="text-center py-4">
              <p>Loading questionnaires...</p>
            </div>
            <div v-else-if="questionnaireError" class="p-4 bg-red-50 rounded-lg border-l-4 border-red-500 mb-4">
              <p class="text-red-700">Error loading questionnaires: {{ questionnaireError }}</p>
            </div>
            <div v-else class="bg-indigo-50 p-4 rounded-lg">
              <div class="max-h-60 overflow-y-auto p-3 rounded-md bg-white shadow-sm">
                <div v-for="q in allQuestionnaires" :key="q.id" class="flex items-center hover:bg-indigo-50 p-2 rounded">
                  <label class="inline-flex items-center cursor-pointer w-full">
                    <input 
                      type="checkbox" 
                      :id="'q-' + q.id" 
                      :value="q.id" 
                      v-model="associatedQuestionnaireIds"
                      class="form-checkbox h-4 w-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500 cursor-pointer"
                    >
                    <span class="ml-2 block text-sm text-gray-900">{{ q.name }}</span>
                  </label>
                </div>
                <p v-if="!allQuestionnaires.length" class="text-center py-3 text-gray-500">No questionnaires found.</p>
              </div>
            </div>
          </div>

          <!-- Action Buttons -->
          <div class="mt-8 pt-4 border-t border-gray-200">
            <div class="flex justify-between items-center">
              <button 
                type="button" 
                @click="cancelEdit"
                class="px-6 py-3 bg-white border border-gray-300 text-gray-700 rounded-lg font-medium transition-all duration-300 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 shadow-md flex items-center"
              >
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
                Cancel
              </button>
              <button 
                type="submit" 
                :disabled="isSaving || loading"
                class="px-6 py-3 bg-blue-600 text-white rounded-lg font-medium transition-all duration-300 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 shadow-md hover:shadow-lg flex items-center disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <svg v-if="isSaving" class="animate-spin h-5 w-5 mr-2" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                <span>{{ isSaving ? 'Saving...' : 'Save Changes' }}</span>
                <svg v-if="!isSaving" xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3" />
                </svg>
              </button>
            </div>
          </div>
          
          <div class="text-center mt-4">
            <p v-if="saveError" class="text-red-600 text-sm">{{ saveError }}</p>
            <p v-if="saveSuccess" class="text-green-600 text-sm">Question saved successfully!</p>
          </div>
        </form>
      </div>
    </div>

    <!-- Help Button -->
    <div class="fixed bottom-6 right-6">
      <button
        class="p-4 bg-white rounded-full shadow-lg hover:shadow-xl transition-all duration-300 text-blue-600"
      >
        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.228 9c.549-1.165 2.03-2 3.772-2
           2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006
           2.907-.542.104-.994.54-.994 1.093m0 3h.01M21
           12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      </button>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import SkeletonLoader from '@/components/Static/SkeletonLoader.vue';

// Define OptionLevel enum/constants for clarity
const OptionLevel = {
  LOW: 'LOW',
  MEDIUM: 'MEDIUM',
  HIGH: 'HIGH',
};

// Define OptionLevelType enum/constants
const OptionLevelType = {
  IMPACT: 'IMPACT',
  PROBABILITY: 'PROBABILITY'
};

export default {
  name: 'EditQuestionPage',
  components: { SkeletonLoader },
  data() {
    return {
      question: {
        id: null,
        questionText: '',
        categoryName: '',
        options: [],
      },
      allQuestionnaires: [],
      associatedQuestionnaireIds: [],
      availableCategories: [],
      loading: true,
      error: null,
      loadingQuestionnaires: true,
      questionnaireError: null,
      loadingCategories: true,
      categoryError: null,
      isSaving: false,
      saveError: null,
      saveSuccess: false,
    };
  },
  computed: {
    questionId() {
      return parseInt(this.$route.params.questionId, 10);
    }
  },
  methods: {
    async fetchQuestionDetails() {
      this.loading = true;
      this.error = null;
      try {
        const response = await axios.get(`/api/questions/${this.questionId}`);
        const backendQuestion = response.data;

        this.question = {
          id: backendQuestion.id,
          questionText: backendQuestion.questionText,
          categoryName: backendQuestion.category?.name || '',
          options: backendQuestion.options.map(opt => ({
            optionText: opt.optionText,
            optionLevel: opt.optionLevel,
            optionType: opt.optionType
          })) || []
        };

        this.associatedQuestionnaireIds = backendQuestion.questionnaires?.map(q => q.id) || [];

      } catch (err) {
        console.error("Error fetching question details:", err);
        this.error = err.response?.data?.message || err.message || 'Failed to load question details.';
      } finally {
        this.loading = false;
        // Log category name after fetching question details
        console.log('Category Name after fetchQuestionDetails:', this.question.categoryName);
      }
    },

    async fetchAllQuestionnaires() {
      this.loadingQuestionnaires = true;
      this.questionnaireError = null;
      try {
        const response = await axios.get('/api/questionnaires/all');
        this.allQuestionnaires = response.data.map(dto => ({
          id: dto.id,
          name: dto.title
        }));
      } catch (err) {
        console.error("Error fetching questionnaires:", err);
        this.questionnaireError = err.response?.data?.message || err.message || 'Failed to load questionnaires.';
      } finally {
        this.loadingQuestionnaires = false;
      }
    },

    async fetchCategories() {
      this.loadingCategories = true;
      this.categoryError = null;
      try {
        const response = await axios.get('/api/categories');
        this.availableCategories = response.data.filter(cat => cat.name);
      } catch (err) {
        console.error("Error fetching categories:", err);
        this.categoryError = err.response?.data?.message || err.message || 'Failed to load categories.';
        this.availableCategories = [];
      } finally {
        this.loadingCategories = false;
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

    async saveQuestion() {
      this.isSaving = true;
      this.saveError = null;
      this.saveSuccess = false;

      const token = localStorage.getItem("jwt");
      if (!token) {
        this.saveError = "Authentication error: No token found. Please log in.";
        this.isSaving = false;
        return;
      }

      // Log the category name right before creating the payload
      console.log('Category Name before validation:', this.question.categoryName);

      // Trim and re-validate categoryName before creating payload
      const finalCategoryName = this.question.categoryName ? this.question.categoryName.trim() : '';
      console.log('Category Name after trim for validation:', finalCategoryName);
      if (!finalCategoryName) {
          this.saveError = "Please select a valid category for the question.";
          this.isSaving = false;
          return;
      }

      const payload = {
        id: this.question.id,
        questionText: this.question.questionText,
        categoryName: finalCategoryName, // Use the validated name
        options: this.question.options.map(opt => ({
          optionText: opt.optionText,
          optionLevel: opt.optionLevel,
          optionType: opt.optionType || OptionLevelType.IMPACT
        })),
        questionnaireIds: this.associatedQuestionnaireIds
      };

      console.log('Saving question with payload:', JSON.stringify(payload, null, 2));

      try {
        console.log("Option validation check:", this.question.options);

        await axios.put(`/api/questions/${this.question.id}`, payload, {
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        this.saveSuccess = true;
        this.saveError = null;

        setTimeout(() => {
          this.saveSuccess = false;
        }, 2500);

      } catch (err) {
        console.error("Error saving question:", err);
        this.saveError = err.response?.data?.message || err.message || 'Failed to save question.';
        this.saveSuccess = false;
      } finally {
        this.isSaving = false;
      }
    },

    cancelEdit() {
      this.$router.go(-1);
    }
  },
  async mounted() {
  try {
    await Promise.all([
      this.fetchAllQuestionnaires(),
      this.fetchCategories(),
      this.fetchQuestionDetails()
    ]);
  } catch (err) {
    console.error("Error during initial load:", err);
  }
}

};
</script>

<style scoped>
.max-h-60 {
  max-height: 15rem;
}
</style>