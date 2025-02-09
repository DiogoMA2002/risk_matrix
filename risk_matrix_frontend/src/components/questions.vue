<template>
  <div class="h-screen bg-gray-100 p-4 overflow-y-scroll">
    <div class="max-w-2xl mx-auto">
      <h1 class="text-3xl font-bold mb-4">{{ questionnaire.titulo || 'Questionário' }}</h1>
      <p class="mb-6">{{ questionnaire.descricao }}</p>
      
      <!-- Loading Message -->
      <div v-if="loading" class="mb-4">
        A carregar as perguntas...
      </div>
      
      <!-- Error Message -->
      <div v-else-if="error" class="mb-4 text-red-500">
        {{ error }}
      </div>
      
      <!-- Display Sections and Questions -->
      <div v-else>
        <div v-for="secao in questionnaire.secoes" :key="secao.id" class="mb-6">
          <h2 class="text-2xl font-semibold mb-2">{{ secao.titulo }}</h2>
          
          <!-- If section has direct questions -->
          <div v-if="secao.perguntas">
            <div v-for="pergunta in secao.perguntas" :key="pergunta.id" class="mb-4">
              <p class="mb-1">{{ pergunta.texto }}</p>
              <!-- Input based on question type -->
              <div v-if="pergunta.tipo === 'number'">
                <input
                  type="number"
                  :min="pergunta.min"
                  :max="pergunta.max"
                  class="border p-1 rounded w-full"
                />
              </div>
              <div v-else-if="pergunta.tipo === 'text'">
                <input
                  type="text"
                  class="border p-1 rounded w-full"
                />
              </div>
              <div v-else-if="pergunta.tipo === 'opcao'">
                <select class="border p-1 rounded w-full">
                  <option v-for="opcao in pergunta.opcoes" :key="opcao" :value="opcao">
                    {{ opcao }}
                  </option>
                </select>
              </div>
            </div>
          </div>
          
          <!-- If section contains subdivisions -->
          <div v-if="secao.subdivisoes">
            <div v-for="sub in secao.subdivisoes" :key="sub.id" class="mb-4 pl-4 border-l">
              <h3 class="text-xl font-medium mb-2">{{ sub.titulo }}</h3>
              <div v-for="pergunta in sub.perguntas" :key="pergunta.id" class="mb-4">
                <p class="mb-1">{{ pergunta.texto }}</p>
                <div v-if="pergunta.tipo === 'number'">
                  <input
                    type="number"
                    :min="pergunta.min"
                    :max="pergunta.max"
                    class="border p-1 rounded w-full"
                  />
                </div>
                <div v-else-if="pergunta.tipo === 'text'">
                  <input
                    type="text"
                    class="border p-1 rounded w-full"
                  />
                </div>
                <div v-else-if="pergunta.tipo === 'opcao'">
                  <select class="border p-1 rounded w-full">
                    <option v-for="opcao in pergunta.opcoes" :key="opcao" :value="opcao">
                      {{ opcao }}
                    </option>
                  </select>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <button
        @click="finishQuestions"
        class="bg-green-500 hover:bg-green-600 text-white font-semibold py-2 px-4 rounded mt-4"
      >
        Finalizar Questionário
      </button>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'QuestionsPage',
  data() {
    return {
      questionnaire: {}, // Stores the questionnaire data
      loading: false,    // Loading state
      error: null        // Error message, if any
    }
  },
  mounted() {
    this.fetchQuestions()
  },
  methods: {
    fetchQuestions() {
      this.loading = true
      axios.get('http://127.0.0.1:5000/api/questions')
        .then(response => {
          this.questionnaire = response.data
          this.loading = false
        })
        .catch(err => {
          console.error(err)
          this.error = 'Erro ao carregar as perguntas.'
          this.loading = false
        })
    },
    finishQuestions() {
      this.$router.push('/report')
    }
  }
}
</script>
