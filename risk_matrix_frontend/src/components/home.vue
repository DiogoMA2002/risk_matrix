<template>
  <div class="flex flex-col items-center justify-center min-h-screen bg-gray-100">
    <h1 class="text-4xl font-bold mb-8">Home Page</h1>
    <button 
      @click="goToQuestions"
      class="bg-blue-500 hover:bg-blue-600 text-white font-semibold py-2 px-4 rounded"
    >
      Go to Questions
    </button>
  </div>
</template>

<script>
export default {
  name: 'HomePage',
  methods: {
    goToQuestions() {
      this.$router.push('/questions')
    }
  }
}
</script>
