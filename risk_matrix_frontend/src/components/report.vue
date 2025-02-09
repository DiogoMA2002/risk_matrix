<template>
  <div class="flex flex-col items-center justify-center min-h-screen bg-gray-100 p-4">
    <h1 class="text-3xl font-bold mb-4">Report Page</h1>
    
    <!-- Loading Message -->
    <div v-if="loading" class="mb-4">
      A calcular o risco...
    </div>
    
    <!-- Error Message -->
    <div v-else-if="error" class="mb-4 text-red-500">
      {{ error }}
    </div>
    
    <!-- Display Result -->
    <div v-else-if="result">
      <p class="mb-2">
        <span class="font-semibold">Risco:</span> {{ result.risco }}
      </p>
      <p class="mb-2">
        <span class="font-semibold">Classificação:</span> {{ result.classificacao }}
      </p>
    </div>
    
    <button
      @click="goHome"
      class="bg-gray-500 hover:bg-gray-600 text-white font-semibold py-2 px-4 rounded mt-4"
    >
      Back to Home
    </button>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'ReportPage',
  data() {
    return {
      result: null,   // Stores the risk calculation result
      loading: false, // Loading state
      error: null     // Error message, if any
    }
  },
  mounted() {
    this.calculateRisk()
  },
  methods: {
    goHome() {
      this.$router.push('/')
    },
    calculateRisk() {
      // Retrieve values from query parameters (if available) or use default values
      const prob = this.$route.query.probabilidade || 3
      const impact = this.$route.query.impacto || 4

      this.loading = true
      axios.post('http://127.0.0.1:5000/api/risk', {
        probabilidade: prob,
        impacto: impact
      })
      .then(response => {
        this.result = response.data
        this.loading = false
      })
      .catch(err => {
        console.error(err)
        this.error = 'Erro ao calcular o risco.'
        this.loading = false
      })
    }
  }
}
</script>
