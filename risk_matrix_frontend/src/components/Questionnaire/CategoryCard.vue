<template>
    <div
      class="cursor-pointer bg-white bg-opacity-90 backdrop-blur-sm rounded-xl shadow-md p-6 flex flex-col items-center justify-center transition-all duration-300 hover:shadow-xl hover:bg-blue-50 hover:-translate-y-1">
      <div class="w-16 h-16 rounded-full bg-blue-100 flex items-center justify-center mb-4">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-blue-600" fill="none" viewBox="0 0 24 24"
          stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      </div>
  
      <h2 class="text-xl font-semibold text-blue-600 text-center">{{ formattedCategory }}</h2>
      <p class="mt-2 text-gray-500 text-center text-sm">Clique para ver as perguntas</p>
  
      <!-- Progress Indicator -->
      <div class="mt-2 flex items-center space-x-2">
        <span class="text-sm text-gray-700">
          {{ answeredCount }}/{{ totalCount }}
        </span>
        <svg v-if="answeredCount === totalCount && totalCount > 0" xmlns="http://www.w3.org/2000/svg"
          class="h-5 w-5 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
        </svg>
      </div>
  
      <div class="mt-4 flex justify-center">
        <span class="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
          Iniciar
          <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3 ml-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
          </svg>
        </span>
      </div>
    </div>
  </template>
  
  <script>
  export default {
    name: "CategoryCard",
    props: {
      category: {
        type: String,
        required: true
      },
      answeredCount: {
        type: Number,
        required: true
      },
      totalCount: {
        type: Number,
        required: true
      }
    },
    computed: {
      formattedCategory() {
        if (!this.category) return "";
        
        // Title case with proper locale handling for Portuguese
        return this.category
          .split(' ')
          .map(word => {
            if (!word) return '';
            return word.charAt(0).toLocaleUpperCase('pt-PT') + 
                   word.slice(1).toLocaleLowerCase('pt-PT');
          })
          .join(' ');
      }
    }
  }
  </script>