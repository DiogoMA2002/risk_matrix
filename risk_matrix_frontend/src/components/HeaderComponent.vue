<template>
  <div class="flex justify-between items-center mb-6">
    <div class="flex items-center">
      <button
        @click="$router.go(-1)"
        class="p-2 rounded-full bg-white bg-opacity-20 backdrop-blur-sm text-white hover:bg-opacity-30 transition-all duration-300 mr-4"
      >
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
        </svg>
      </button>
      <div class="text-white">
        <h1 class="text-2xl font-bold">Painel Administrativo</h1>
        <p class="text-sm opacity-80">Gerencie questões, questionários e feedbacks</p>
      </div>
    </div>
    <div class="flex items-center space-x-2 text-white">
      <img src="@/assets/logoCCC.png" alt="Logo" class="h-16" />
    </div>
  </div>
</template>

<script>
export default {
  name: "HeaderComponent",
};
</script>
