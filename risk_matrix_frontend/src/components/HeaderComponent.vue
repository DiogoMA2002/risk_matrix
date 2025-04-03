<template>
  <div>
    <!-- Header -->
    <div class="flex justify-between items-center mb-6 px-4">
      <div class="flex items-center">
        <button
          @click="$router.push('/')"
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

      <!-- Right side: Logout + Logo -->
      <div class="flex items-center space-x-4">
        <button
          @click="logout"
          class="px-5 py-2 rounded-full bg-white bg-opacity-20 hover:bg-opacity-30 text-sm font-medium transition text-white"
        >
          Sair
        </button>

        <img src="@/assets/logoCCC.png" alt="Logo" class="h-12 object-contain" />
      </div>
    </div>

    <!-- Toast message -->
    <transition name="fade">
      <div v-if="showLogoutMessage" class="fixed top-4 left-1/2 transform -translate-x-1/2 bg-green-500 text-white px-6 py-3 rounded-full shadow-lg z-50">
        Logout realizado com sucesso
      </div>
    </transition>
  </div>
</template>

<script>
export default {
  name: "HeaderComponent",
  data() {
    return {
      showLogoutMessage: false
    }
  },
  methods: {
    logout() {
      localStorage.removeItem('jwt')
      this.showLogoutMessage = true
      setTimeout(() => {
        this.showLogoutMessage = false
        this.$router.push('/')
      }, 500) // show message for 1.5s before redirecting
    }
  }
}
</script>

<style>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s ease;
}
.fade-enter,
.fade-leave-to {
  opacity: 0;
}
</style>
