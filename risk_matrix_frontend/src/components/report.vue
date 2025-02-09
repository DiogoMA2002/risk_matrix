<template>
  <div class="min-h-screen bg-gradient-to-b from-blue-50 to-indigo-50 p-6 h-[80vh] overflow-y-auto">
    <!-- Cabeçalho -->
    <header class="mb-6">
      <h1 class="text-4xl font-bold text-center text-indigo-800">Relatório de Risco</h1>
    </header>

    <!-- Área principal com rolagem natural -->
    <main class="flex-grow overflow-y-auto">
      <div class="max-w-3xl mx-auto bg-white shadow-xl rounded-lg p-8">
        <!-- Mensagem de Carregamento -->
        <div v-if="loading" class="text-center text-blue-500 text-lg">
          A calcular o risco...
        </div>

        <!-- Mensagem de Erro -->
        <div v-else-if="error" class="text-center text-red-500 text-lg">
          {{ error }}
        </div>

        <!-- Exibição do Resultado -->
        <div v-else-if="result">
          <!-- Risco Geral -->
          <section class="mb-8 border-b pb-4">
            <h2 class="text-2xl font-semibold text-indigo-700 mb-3">Risco Geral</h2>
            <p class="text-lg text-gray-700">
              <span class="font-semibold">Risco:</span> {{ result.risco_geral }}
            </p>
            <p class="text-lg text-gray-700">
              <span class="font-semibold">Classificação:</span> {{ result.classificacao_geral }}
            </p>
          </section>

          <!-- Riscos por Categoria -->
          <section>
            <h2 class="text-2xl font-semibold text-indigo-700 mb-4">Riscos por Categoria</h2>
            <div v-for="(cat, key) in result.categorias" :key="key" class="mb-4">
              <p class="text-xl font-semibold capitalize text-gray-800">{{ key }}</p>
              <div class="ml-4">
                <p class="text-lg text-gray-700">
                  <span class="font-semibold">Risco:</span> {{ cat.risco }}
                </p>
                <p class="text-lg text-gray-700">
                  <span class="font-semibold">Classificação:</span> {{ cat.classificacao }}
                </p>
              </div>
            </div>
          </section>
        </div>
      </div>
    </main>

    <!-- Rodapé -->
    <footer class="mt-6 text-center">
      <button
        @click="goHome"
        class="bg-indigo-600 hover:bg-indigo-700 text-white font-semibold py-3 px-6 rounded-full transition duration-300"
      >
        Voltar para Home
      </button>
    </footer>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'ReportPage',
  data() {
    return {
      result: null,    // Armazena o resultado do cálculo do risco
      loading: false,  // Estado de carregamento
      error: null      // Mensagem de erro, se ocorrer
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
      // Obtém os valores de probabilidade e impacto dos parâmetros da rota ou usa valores padrão
      const prob = this.$route.query.probabilidade || 3
      const impact = this.$route.query.impacto || 4
      
      // Exemplo: enviamos também dados para as categorias
      const categorias = {
        "fatores_humanos": { "probabilidade": 3, "impacto": 4 },
        "riscos_externos": { "probabilidade": 2, "impacto": 5 },
        "fatores_materiais": { "probabilidade": 4, "impacto": 2 },
        "fatores_tecnologicos": { "probabilidade": 5, "impacto": 4 },
        "rgpd": { "probabilidade": 1, "impacto": 3 },
        "localizacao": { "probabilidade": 2, "impacto": 2 }
      }

      this.loading = true
      axios.post('http://127.0.0.1:5000/api/risk', {
        probabilidade: prob,
        impacto: impact,
        categorias: categorias
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
