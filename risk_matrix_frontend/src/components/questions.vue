<template>
  <div class="flex flex-col items-center justify-center min-h-screen bg-gray-100">
    <h1 class="text-3xl font-bold mb-4">Questions Page</h1>
    <!-- Placeholder for the questionnaire content -->
    <p class="mb-4">Here the questionnaire will be displayed.</p>
    <button
      @click="finishQuestions"
      class="bg-green-500 hover:bg-green-600 text-white font-semibold py-2 px-4 rounded"
    >
      Finish Questionnaire
    </button>
  </div>
</template>

<script>
export default {
  name: 'QuestionsPage',
  methods: {
    finishQuestions() {
      this.$router.push('/report')
    }
  }
}
</script>
