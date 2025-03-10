<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-600 to-indigo-100 font-sans">
    <div class="container mx-auto px-4 py-6">
      <!-- Header with back button -->
      <div class="flex items-center mb-6">
        <button
          @click="$router.go(-1)"
          class="p-2 rounded-full bg-white bg-opacity-20 backdrop-blur-sm text-white hover:bg-opacity-30 transition-all duration-300 mr-4"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none"
               viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round"
                  stroke-width="2" d="M15 19l-7-7 7-7" />
          </svg>
        </button>
        <div class="text-white">
          <h1 class="text-2xl font-bold">Admin Dashboard</h1>
          <p class="text-sm opacity-80">Manage questions and categories</p>
        </div>
      </div>

      <!-- Add New Question Form -->
      <div class="max-w-4xl mx-auto mb-8">
        <div class="bg-white bg-opacity-90 backdrop-blur-sm rounded-xl shadow-md p-6">
          <h2 class="text-xl font-semibold mb-4 text-blue-800 flex items-center">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
            </svg>
            Add New Question
          </h2>
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Question Text</label>
              <input 
                v-model="newQuestion" 
                type="text" 
                placeholder="Enter question" 
                class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-300" 
              />
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Category</label>
              <select 
                v-model="selectedCategory" 
                class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-300 bg-white"
              >
                <option disabled value="">Select Category</option>
                <option v-for="category in categories" :key="category" :value="category">
                  {{ formatCategoryName(category) }}
                </option>
              </select>
            </div>
            
            <div class="pt-2">
              <button 
                @click="addQuestion" 
                class="px-6 py-3 bg-blue-600 text-white rounded-lg shadow-md hover:bg-blue-700 transition-all duration-300 flex items-center"
              >
                <span>Add Question</span>
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                </svg>
              </button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- List of Questions -->
      <div class="max-w-4xl mx-auto">
        <div class="bg-white bg-opacity-90 backdrop-blur-sm rounded-xl shadow-md p-6">
          <h2 class="text-xl font-semibold mb-4 text-blue-800 flex items-center">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
            </svg>
            Existing Questions
          </h2>
          
          <div v-if="questions.length > 0">
            <ul class="divide-y divide-gray-200">
              <li v-for="question in questions" :key="question.id" class="py-3 flex justify-between items-center hover:bg-blue-50 transition-colors rounded-lg px-3">
                <div>
                  <p class="font-medium">{{ question.questionText }}</p>
                  <span class="text-sm text-blue-600 bg-blue-100 px-2 py-1 rounded-full inline-block mt-1">
                    {{ formatCategoryName(question.category) }}
                  </span>
                </div>
                <div class="flex">
                  <button class="p-2 text-gray-500 hover:text-blue-600 transition-colors" title="Edit">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                    </svg>
                  </button>
                  <button class="p-2 text-gray-500 hover:text-red-600 transition-colors" title="Delete">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                    </svg>
                  </button>
                </div>
              </li>
            </ul>
          </div>
          
          <div v-else class="py-8 text-center">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 text-gray-400 mx-auto mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
            </svg>
            <p class="text-gray-500">No questions added yet</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Floating help button -->
    <div class="fixed bottom-6 right-6">
      <button class="p-4 bg-white rounded-full shadow-lg hover:shadow-xl transition-all duration-300 text-blue-600">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      </button>
    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  data() {
    return {
      questions: [],
      newQuestion: "",
      selectedCategory: "",
      categories: ["AUTHENTICATION_RISK", "DATA_STORAGE_RISK", "EMAIL_SECURITY", "NETWORK_SECURITY", "INFRASTRUCTURE_SECURITY"],
      isLoading: false
    };
  },
  created() {
    this.fetchQuestions();
  },
  methods: {
    async fetchQuestions() {
      this.isLoading = true;
      try {
        const response = await axios.get("/api/questions/all");
        this.questions = response.data;
      } catch (error) {
        console.error("Error fetching questions:", error);
        // Fallback for testing
        this.questions = [
          { id: 1, questionText: "Is multi-factor authentication enabled?", category: "AUTHENTICATION_RISK" },
          { id: 2, questionText: "Are all data backups encrypted?", category: "DATA_STORAGE_RISK" },
          { id: 3, questionText: "Do you use SPF and DKIM for email?", category: "EMAIL_SECURITY" }
        ];
      } finally {
        this.isLoading = false;
      }
    },
    async addQuestion() {
      if (!this.newQuestion || !this.selectedCategory) {
        alert("Please enter a question and select a category.");
        return;
      }
      
      this.isLoading = true;
      try {
        await axios.post("/api/questions/create", {
          questionText: this.newQuestion,
          category: this.selectedCategory,
        });
        
        this.newQuestion = "";
        this.selectedCategory = "";
        this.fetchQuestions(); // Refresh question list
      } catch (error) {
        console.error("Error adding question:", error);
        alert("Failed to add question. Please try again.");
      } finally {
        this.isLoading = false;
      }
    },
    formatCategoryName(category) {
      if (!category) return "";
      return category
        .replace(/_/g, " ")
        .toLowerCase()
        .replace(/\b\w/g, l => l.toUpperCase());
    }
  }
};
</script>