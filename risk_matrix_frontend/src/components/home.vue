<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-100 to-purple-100 flex items-center justify-center">
    <div class="bg-white shadow-xl rounded-lg p-8 max-w-md text-center">
      <h1 class="text-4xl font-bold text-gray-800 mb-4">Bem-vindo à Matriz de Risco</h1>
      <p class="text-gray-600 mb-6">
        Avalie e gerencie os riscos da sua organização com facilidade e segurança.
      </p>
      <button 
        @click="goToQuestions"
        class="bg-blue-500 hover:bg-blue-600 text-white font-bold py-3 px-6 rounded-full transition duration-300"
      >
        Iniciar Avaliação
      </button>
    </div>
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
