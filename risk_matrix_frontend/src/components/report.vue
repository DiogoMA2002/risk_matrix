<template>
    <div class="flex flex-col items-center justify-center min-h-screen bg-gray-100">
      <h1 class="text-3xl font-bold mb-4">Report Page</h1>
      <!-- Placeholder for the report content -->
      <p class="mb-4">Here the report will be displayed.</p>
      <button
        @click="goHome"
        class="bg-gray-500 hover:bg-gray-600 text-white font-semibold py-2 px-4 rounded"
      >
        Back to Home
      </button>
    </div>
  </template>
  
  <script>
  export default {
    name: 'ReportPage',
    methods: {
      goHome() {
        this.$router.push('/')
      }
    }
  }
  </script>
  