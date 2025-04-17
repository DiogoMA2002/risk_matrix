<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-600 to-indigo-100 font-sans">
    <!-- Admin access button -->
    <div class="absolute top-4 right-4">
      <button
        @click="goToAdmin"
        class="px-4 py-2 bg-white bg-opacity-20 backdrop-blur-sm text-white rounded-md font-medium transition-all duration-300 hover:bg-opacity-30 shadow-sm flex items-center space-x-1"
      >
        <span>Painel de Admin</span>
        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
        </svg>
      </button>
    </div>

    <!-- Logo and branding -->
    <div class="w-full flex justify-center pt-10">
      <div class="flex items-center space-x-3">
        <div class="flex items-center justify-center">
          <img src="@/assets/logoCCC.webp" alt="Logo" class="h-16 object-contain" v-once />
        </div>
      </div>
    </div>

    <!-- Main content card -->
    <div class="flex flex-col items-center justify-center px-4 py-12">
      <div
        class="bg-white bg-opacity-90 backdrop-blur-md rounded-xl shadow-xl p-8 w-full max-w-md transition-all duration-500 hover:shadow-2xl">
        <h1 class="text-3xl font-bold text-blue-600 mb-2" v-once>Matriz de Risco</h1>
        <p class="text-gray-500 mb-8" v-once>Avalie e gerencie os seus riscos de forma eficiente</p>

        <div class="space-y-6">
          <div class="space-y-1">
            <label for="email" class="block text-sm font-medium text-gray-700" v-once>Email</label>
            <div class="relative">
              <input id="email" type="email" v-model="email" placeholder="email@email.com" class="w-full px-4 py-3 pl-10 border rounded-lg transition-all duration-300 focus:outline-none
             focus:ring-2 focus:ring-blue-500 focus:border-blue-500
             border-gray-200" />
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400 absolute left-3 top-3.5" fill="none"
                viewBox="0 0 24 24" stroke="currentColor" v-once>
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
              </svg>
            </div>
            <p v-if="emailError" class="text-red-600 text-sm mt-1">{{ emailError }}</p>
          </div>
          
          <button @click="proceed"
            class="w-full px-4 py-3 bg-blue-600 text-white rounded-lg font-medium transition-all duration-300 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 shadow-md hover:shadow-lg">
            Prosseguir
          </button>
        </div>

        <!-- Trust indicators -->
        <div class="mt-8 pt-6 border-t border-gray-100 flex justify-center space-x-4 text-gray-400" v-once>
          <div class="flex items-center text-xs">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24"
              stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
            </svg>
            Dados seguros
          </div>
        </div>
      </div>
    </div>
    <!-- Help Button -->
    <div class="fixed bottom-6 right-6">
      <button @click="goToFeedbackForm"
        class="p-4 bg-white rounded-full shadow-lg hover:shadow-xl transition-all duration-300 text-blue-600">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" v-once>
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.228 9c.549-1.165 2.03-2 3.772-2
               2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006
               2.907-.542.104-.994.54-.994 1.093m0 3h.01M21
               12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      </button>
    </div>

    <!-- Logo no canto inferior esquerdo -->
    <div class="fixed bottom-6 left-6">
      <img src="@/assets/LogoFinanciamento.webp" alt="Logo C-Network" class="h-12 object-contain" v-once />
    </div>
  </div>
  <div class="fixed bottom-6 left-1/2 transform -translate-x-1/2">
    <img src="@/assets/Barra-PRR-RP-EU-1024x148.webp" alt="Financiamento Logo" class="h-12 object-contain" v-once />
  </div>
</template>

<script>
export default {
  name: 'HomePage',
  data() {
    return {
      email: '',
      emailError: ''

    }
  },
  methods: {
    goToAdmin() {
      const token = localStorage.getItem('jwt')
      if (token) {
        this.$router.push('/admin') // already logged in
      } else {
        this.$router.push('/login') // not logged in
      }
    },
    // Basic email validation using a simple regex:
    isValidEmail(email) {
      const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      return re.test(String(email).toLowerCase());
    },
    proceed() {
      if (!this.email || !this.isValidEmail(this.email)) {
        this.emailError = 'Informe um email válido';
        return;
      }

      this.emailError = ''; // clear error if valid
      localStorage.setItem('userEmail', this.email);
      this.$router.push('/risk-info');
    }
    ,
    goToFeedbackForm() {
      // Redirect to the feedback form page
      this.$router.push('/feedback-form');
    }
  }
}
</script>