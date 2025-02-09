<template>
  <div class="min-h-screen bg-gradient-to-b from-blue-50 to-indigo-50 p-4 h-[80vh] overflow-y-auto">
    <div class="max-w-2xl mx-auto bg-white shadow-lg rounded-lg p-8">
      <h1 class="text-3xl font-bold text-indigo-700 mb-4">
        {{ questionnaire.titulo || 'Questionário' }}
      </h1>
      <p class="text-lg text-indigo-600 mb-6">
        {{ questionnaire.descricao }}
      </p>
      
      <!-- Mensagem de Carregamento -->
      <div v-if="loading" class="mb-4 text-center text-blue-500">
        A carregar as perguntas...
      </div>
      
      <!-- Mensagem de Erro -->
      <div v-else-if="error" class="mb-4 text-center text-red-500">
        {{ error }}
      </div>
      
      <!-- Exibição das Secções e Perguntas -->
      <div v-else>
        <div 
          v-for="secao in questionnaire.secoes" 
          :key="secao.id" 
          class="mb-8 border-b border-indigo-200 pb-4"
        >
          <h2 class="text-2xl font-semibold text-indigo-800 mb-2">
            {{ secao.titulo }}
          </h2>
          
          <!-- Perguntas diretas da secção -->
          <div v-if="secao.perguntas">
            <div 
              v-for="pergunta in secao.perguntas" 
              :key="pergunta.id" 
              class="mb-4 p-3 bg-indigo-50 rounded"
            >
              <p class="text-indigo-700 mb-2">
                {{ pergunta.texto }}
              </p>
              <!-- Input conforme o tipo da pergunta -->
              <div v-if="pergunta.tipo === 'number'">
                <input
                  type="number"
                  :min="pergunta.min"
                  :max="pergunta.max"
                  class="w-full border border-indigo-300 p-2 rounded focus:outline-none focus:ring-2 focus:ring-indigo-400"
                />
              </div>
              <div v-else-if="pergunta.tipo === 'text'">
                <input
                  type="text"
                  class="w-full border border-indigo-300 p-2 rounded focus:outline-none focus:ring-2 focus:ring-indigo-400"
                />
              </div>
              <div v-else-if="pergunta.tipo === 'opcao'">
                <select class="w-full border border-indigo-300 p-2 rounded focus:outline-none focus:ring-2 focus:ring-indigo-400">
                  <option 
                    v-for="opcao in pergunta.opcoes" 
                    :key="opcao" 
                    :value="opcao"
                  >
                    {{ opcao }}
                  </option>
                </select>
              </div>
            </div>
          </div>
          
          <!-- Secção com subdivisões -->
          <div v-if="secao.subdivisoes">
            <div 
              v-for="sub in secao.subdivisoes" 
              :key="sub.id" 
              class="mb-4 pl-4 border-l border-indigo-200"
            >
              <h3 class="text-xl font-medium text-indigo-800 mb-2">
                {{ sub.titulo }}
              </h3>
              <div 
                v-for="pergunta in sub.perguntas" 
                :key="pergunta.id" 
                class="mb-4 p-3 bg-indigo-50 rounded"
              >
                <p class="text-indigo-700 mb-2">
                  {{ pergunta.texto }}
                </p>
                <div v-if="pergunta.tipo === 'number'">
                  <input
                    type="number"
                    :min="pergunta.min"
                    :max="pergunta.max"
                    class="w-full border border-indigo-300 p-2 rounded focus:outline-none focus:ring-2 focus:ring-indigo-400"
                  />
                </div>
                <div v-else-if="pergunta.tipo === 'text'">
                  <input
                    type="text"
                    class="w-full border border-indigo-300 p-2 rounded focus:outline-none focus:ring-2 focus:ring-indigo-400"
                  />
                </div>
                <div v-else-if="pergunta.tipo === 'opcao'">
                  <select class="w-full border border-indigo-300 p-2 rounded focus:outline-none focus:ring-2 focus:ring-indigo-400">
                    <option 
                      v-for="opcao in pergunta.opcoes" 
                      :key="opcao" 
                      :value="opcao"
                    >
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
        class="w-full bg-gradient-to-r from-green-500 to-teal-500 hover:from-green-600 hover:to-teal-600 text-white font-bold py-3 px-6 rounded-full mt-4 transition duration-300"
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
      questionnaire: {}, // Armazena os dados do questionário
      loading: false,    // Estado de carregamento
      error: null        // Mensagem de erro, se houver
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
