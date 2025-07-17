<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-600 to-indigo-100 font-sans flex items-center justify-center px-4">
    <div class="text-center">
      <!-- 404 Number -->
      <div class="text-9xl font-bold text-white opacity-20 mb-4">404</div>
      
      <!-- Error Message -->
      <h1 class="text-4xl font-bold text-white mb-4">Página não encontrada</h1>
      <p class="text-xl text-blue-100 mb-8 max-w-md mx-auto">
        A página que está a procurar não existe ou foi movida.
      </p>
      
      <!-- Action Buttons -->
      <div class="flex flex-col sm:flex-row gap-4 justify-center">
        <button 
          @click="goHome"
          class="px-8 py-3 bg-white text-blue-600 rounded-lg font-semibold hover:bg-blue-50 transition-all duration-300 shadow-lg hover:shadow-xl"
        >
          Voltar ao Início
        </button>
        <button 
          @click="goBack"
          class="px-8 py-3 bg-blue-500 text-white rounded-lg font-semibold hover:bg-blue-600 transition-all duration-300 shadow-lg hover:shadow-xl"
        >
          Voltar Atrás
        </button>
      </div>
      
      <!-- Logo -->
      <div class="mt-12">
        <img src="@/assets/logoCCC.webp" alt="Logo CCC" class="h-16 mx-auto opacity-80" />
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'NotFound',
  methods: {
    goHome() {
      this.$router.push('/');
    },
    goBack() {
      if (window.history.length > 1) {
        this.$router.go(-1);
      } else {
        this.$router.push('/');
      }
    }
  }
}
</script>

<style scoped>
/* Add any additional custom styles here */
</style> 