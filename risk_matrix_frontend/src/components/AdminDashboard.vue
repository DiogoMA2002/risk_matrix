<template>
    <div class="p-6">
      <h1 class="text-2xl font-bold mb-4">Admin Dashboard</h1>
      
      <!-- Add New Question Form -->
      <div class="mb-6 p-4 bg-gray-100 rounded">
        <h2 class="text-xl font-semibold mb-2">Add New Question</h2>
        <input v-model="newQuestion" type="text" placeholder="Enter question" class="w-full p-2 border rounded mb-2" />
        <select v-model="selectedCategory" class="w-full p-2 border rounded mb-2">
          <option disabled value="">Select Category</option>
          <option v-for="category in categories" :key="category" :value="category">
            {{ category }}
          </option>
        </select>
        <button @click="addQuestion" class="px-4 py-2 bg-blue-500 text-white rounded">Add Question</button>
      </div>
      
      <!-- List of Questions -->
      <div>
        <h2 class="text-xl font-semibold mb-2">Existing Questions</h2>
        <ul>
          <li v-for="question in questions" :key="question.id" class="border p-2 my-2 rounded">
            {{ question.questionText }} ({{ question.category }})
          </li>
        </ul>
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
      };
    },
    created() {
      this.fetchQuestions();
    },
    methods: {
      async fetchQuestions() {
        try {
          const response = await axios.get("/api/questions/all");
          this.questions = response.data;
        } catch (error) {
          console.error("Error fetching questions:", error);
        }
      },
      async addQuestion() {
        if (!this.newQuestion || !this.selectedCategory) {
          alert("Please enter a question and select a category.");
          return;
        }
        
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
        }
      },
    },
  };
  </script>
  
  <style scoped>
  input, select {
    display: block;
  }
  </style>
  