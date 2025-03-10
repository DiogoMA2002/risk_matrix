<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-600 to-indigo-100 font-sans">
    <!-- Admin access button -->
    <div class="absolute top-4 right-4">
      <router-link to="/admin" class="px-4 py-2 bg-white bg-opacity-20 backdrop-blur-sm text-white rounded-md font-medium transition-all duration-300 hover:bg-opacity-30 shadow-sm flex items-center space-x-1">
        <span>Acessar Admin</span>
        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
        </svg>
      </router-link>
    </div>

    <!-- Logo and branding -->
    <div class="w-full flex justify-center pt-10">
  <div class="flex items-center space-x-3">
    <!-- Removed the bg-blue-600 and replaced with a neutral container -->
    <div class="flex items-center justify-center">
      <!-- Increased the size from h-6 w-6 to h-12 w-12 -->
      <img src="@/assets/logo.png" alt="Logo" class="h-12 w-12" />
    </div>
    <div class="text-white">
      <div class="text-2xl font-bold">C-Network</div>
      <div class="text-sm font-light">Os Nossos Serviços</div>
    </div>
  </div>
</div>


    <!-- Main content card -->
    <div class="flex flex-col items-center justify-center px-4 py-12">
      <div class="bg-white bg-opacity-90 backdrop-blur-md rounded-xl shadow-xl p-8 w-full max-w-md transition-all duration-500 hover:shadow-2xl">
        <h1 class="text-3xl font-bold text-blue-600 mb-2">Matriz de Risco</h1>
        <p class="text-gray-500 mb-8">Avalie e gerencie seus riscos de forma eficiente</p>
        
        <div class="space-y-6">
          <div class="space-y-2">
            <label for="email" class="block text-sm font-medium text-gray-700">Email Profissional</label>
            <div class="relative">
              <input 
                id="email"
                type="email" 
                v-model="email" 
                placeholder="email@email.com" 
                class="w-full px-4 py-3 pl-10 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-300"
              />
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400 absolute left-3 top-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
              </svg>
            </div>
          </div>
          
          <button 
            @click="proceed" 
            class="w-full px-4 py-3 bg-blue-600 text-white rounded-lg font-medium transition-all duration-300 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 shadow-md hover:shadow-lg"
          >
            Prosseguir
          </button>
          

        </div>

        <!-- Trust indicators -->
        <div class="mt-8 pt-6 border-t border-gray-100 flex justify-center space-x-4 text-gray-400">
          <div class="flex items-center text-xs">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
            </svg>
            Dados seguros
          </div>

        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'HomePage',
  data() {
    return {
      email: ''
    }
  },
  methods: {
    proceed() {
      if (!this.email) {
        alert("Informe um email válido");
        return;
      }
      // Optionally, store the email in a global state or localStorage here
      localStorage.setItem('userEmail', this.email);
      this.$router.push('/risk-info');
    }
  }
}
</script>