<template>
  <div class="h-screen overflow-y-auto bg-gradient-to-br from-blue-600 to-indigo-100 font-sans">
    <div class="container mx-auto px-4 py-6 max-w-7xl">

      <!-- Header with back button and logo on the right -->
      <div class="flex justify-between items-center mb-6">
        <!-- Left side: back button and title -->
        <div class="flex items-center">
          <button @click="$router.go(-1)"
            class="p-2 rounded-full bg-white bg-opacity-20 backdrop-blur-sm text-white hover:bg-opacity-30 transition-all duration-300 mr-4">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24"
              stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
            </svg>
          </button>
          <div class="text-white">
            <h1 class="text-2xl font-bold">Painel Administrativo</h1>
            <p class="text-sm opacity-80">Gerencie questões, questionários e feedbacks</p>
          </div>
        </div>

        <!-- Right side: Logo + C-Network text -->
        <div class="flex items-center space-x-2 text-white">
          <img src="@/assets/logoCCC.png" alt="Logo" class="h-16">
        </div>
      </div>

      <!-- Card: Gerenciar Questionários (Create + Import + List) -->
      <div class="bg-white bg-opacity-90 backdrop-blur-sm rounded-xl shadow-md p-6 mb-8">
        <h2 class="text-xl font-semibold mb-4 text-blue-800 flex items-center">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2" fill="none" viewBox="0 0 24 24"
            stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
          </svg>
          Gerenciar Questionários
        </h2>

        <!-- Top Row: Create + Import -->
        <div class="flex flex-col md:flex-row md:items-end md:space-x-4 space-y-4 md:space-y-0 mb-4">
          <!-- Create Questionnaire -->
          <div class="flex-1">
            <label class="block text-sm font-medium text-gray-700 mb-1">Título do Questionário</label>
            <input v-model="newQuestionnaireTitle" type="text" placeholder="Digite o título do questionário"
              class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-300" />
          </div>
          <div class="flex items-end space-x-4">
            <button @click="addQuestionnaire"
              class="px-6 py-3 bg-blue-600 text-white rounded-lg shadow-md hover:bg-blue-700 transition-all duration-300 flex items-center">
              <span>Criar</span>
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-2" fill="none" viewBox="0 0 24 24"
                stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
              </svg>
            </button>

            <!-- Import Questionnaire -->
            <input type="file" ref="importFile" accept=".json" class="hidden" @change="onImportFileSelected" />
            <button @click="triggerImportFilePicker"
              class="px-6 py-3 bg-green-600 text-white rounded-lg shadow-md hover:bg-green-700 transition-all duration-300 flex items-center">
              <span>Importar</span>
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-2" fill="none" viewBox="0 0 24 24"
                stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v16c0 1.105.895 2 2 2h8m2-2h2c1.105 0 2-.895 2-2V4
                         c0-1.105-.895-2-2-2H6c-1.105 0-2 .895-2 2z" />
              </svg>
            </button>
          </div>
        </div>

        <!-- List of Existing Questionnaires -->
        <div v-if="questionnaires.length > 0">
          <ul class="divide-y divide-gray-200">
            <li v-for="questionnaire in questionnaires" :key="questionnaire.id" class="py-3 flex flex-col">
              <div class="flex justify-between items-center">
                <div>
                  <p class="font-medium">{{ questionnaire.title }}</p>
                </div>
                <div class="flex space-x-2">
                  <!-- Export Button -->
                  <button @click="exportQuestionnaire(questionnaire.id)"
                    class="p-2 text-gray-500 hover:text-blue-600 transition-colors" title="Exportar Questionário">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24"
                      stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M13 10V3L4 14h7v7l9-11h-7z" />
                    </svg>
                  </button>
                  <!-- Delete Questionnaire -->
                  <button @click="deleteQuestionnaire(questionnaire.id)"
                    class="p-2 text-gray-500 hover:text-red-600 transition-colors" title="Excluir Questionário">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24"
                      stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0
                               01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0
                               00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                    </svg>
                  </button>
                </div>
              </div>
            </li>
          </ul>
        </div>
        <div v-else class="py-8 text-center">
          <p class="text-gray-500">Nenhum questionário encontrado.</p>
        </div>
      </div>

      <!-- Card: Add New Question -->
      <div class="bg-white bg-opacity-90 backdrop-blur-sm rounded-xl shadow-md p-6 mb-8">
        <h2 class="text-xl font-semibold mb-4 text-blue-800 flex items-center">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2" fill="none" viewBox="0 0 24 24"
            stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
          </svg>
          Adicionar Nova Questão
        </h2>
        <div class="space-y-4">
          <!-- Question Text -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Texto da Questão</label>
            <input v-model="newQuestion" type="text" placeholder="Digite a questão"
              class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-300" />
          </div>

          <!-- Category -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Categoria</label>
            <select v-model="selectedCategory"
              class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-300 bg-white">
              <option disabled value="">Selecione a Categoria</option>
              <option v-for="cat in categories" :key="cat" :value="cat">
                {{ formatCategoryName(cat) }} <!-- still display human readable -->
              </option>

            </select>
          </div>

          <!-- Associate to an Existing Questionnaire -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Questionário</label>
            <select v-model="selectedQuestionnaire"
              class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-300 bg-white">
              <option disabled value="">Selecione o Questionário</option>
              <option v-for="questionnaire in questionnaires" :key="questionnaire.id" :value="questionnaire.id">
                {{ questionnaire.title }}
              </option>
            </select>
          </div>

          <!-- Dynamic Options List -->
          <div class="mt-6">
            <h3 class="text-lg font-semibold mb-2 text-gray-800">Opções da Questão</h3>
            <div v-for="(option, index) in newOptions" :key="index"
              class="p-4 mb-3 border border-gray-200 rounded-lg bg-gray-50">
              <div class="flex flex-col md:flex-row md:items-center md:space-x-4 space-y-2 md:space-y-0">
                <!-- Option Text -->
                <div class="flex-1">
                  <label class="block text-sm font-medium text-gray-700">Texto da Opção</label>
                  <input v-model="option.optionText" type="text" placeholder="Ex: Sim"
                    class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-300" />
                </div>

                <!-- Option Type -->
                <div class="flex-1">
                  <label class="block text-sm font-medium text-gray-700">Tipo da Opção</label>
                  <select v-model="option.optionType"
                    class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-300">
                    <option v-for="type in optionTypes" :key="type" :value="type">
                      {{ type }}
                    </option>
                  </select>
                </div>

                <!-- Option Level -->
                <div class="flex-1">
                  <label class="block text-sm font-medium text-gray-700">Nível</label>
                  <select v-model="option.optionLevel"
                    class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-300">
                    <option v-for="level in optionLevels" :key="level" :value="level">
                      {{ level }}
                    </option>
                  </select>
                </div>

                <!-- Remove Option Button -->
                <div>
                  <button v-if="newOptions.length > 1" @click="removeOption(index)" type="button"
                    class="mt-2 text-red-600 hover:text-red-800">
                    Remover
                  </button>
                </div>
              </div>
            </div>
            <button type="button" class="px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600"
              @click="addOption">
              Adicionar outra opção
            </button>
          </div>

          <!-- Submit New Question Button -->
          <div class="pt-4">
            <button @click="addQuestion"
              class="px-6 py-3 bg-blue-600 text-white rounded-lg shadow-md hover:bg-blue-700 transition-all duration-300 flex items-center">
              <span>Adicionar Questão</span>
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-2" fill="none" viewBox="0 0 24 24"
                stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
              </svg>
            </button>
          </div>
        </div>
      </div>
      <!-- Filter Section (Place this above the Existing Questions card) -->
      <div class="mb-4">
        <label class="block text-sm font-medium text-gray-700 mb-1">Filtrar por Categoria</label>
        <select v-model="filterCategory" @change="filterQuestionsByCategory"
          class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 transition-all duration-300">
          <option value="">Todos</option>
          <option v-for="cat in categories" :key="cat" :value="cat">
            {{ formatCategoryName(cat) }}
          </option>
        </select>
      </div>

      <!-- Card: Existing Questions -->
      <div class="bg-white bg-opacity-90 backdrop-blur-sm rounded-xl shadow-md p-6 mb-8">
        <h2 class="text-xl font-semibold mb-4 text-blue-800 flex items-center">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2" fill="none" viewBox="0 0 24 24"
            stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0
                     002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2
                     2 0 002 2h2a2 2 0 002-2" />
          </svg>
          Questões Existentes
        </h2>

        <div v-if="questions.length > 0">
          <ul class="divide-y divide-gray-200">
            <li v-for="question in questions" :key="question.id"
              class="py-3 flex justify-between items-center hover:bg-blue-50 transition-colors rounded-lg px-3">
              <div>
                <p class="font-medium">{{ question.questionText }}</p>
                <span class="text-sm text-blue-600 bg-blue-100 px-2 py-1 rounded-full inline-block mt-1">
                  {{ formatCategoryName(question.category) }}
                </span>
              </div>
              <div class="flex space-x-2">
                <!-- Edit button (not implemented) -->
                <button class="p-2 text-gray-500 hover:text-blue-600 transition-colors" title="Editar">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24"
                    stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0
                             002 2h11a2 2 0 002-2v-5m-1.414-9.414
                             a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                  </svg>
                </button>
                <!-- Delete Question Button -->
                <button @click="deleteQuestion(question.id)"
                  class="p-2 text-gray-500 hover:text-red-600 transition-colors" title="Excluir">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24"
                    stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0
                             01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0
                             00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                  </svg>
                </button>
              </div>
            </li>
          </ul>
        </div>
        <div v-else class="py-8 text-center">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 text-gray-400 mx-auto mb-4" fill="none"
            viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0
                     002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2
                     2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2
                     2 0 012 2" />
          </svg>
          <p class="text-gray-500">Nenhuma questão adicionada ainda</p>
        </div>
      </div>

      <!-- Card: User Feedback -->
      <div class="bg-white bg-opacity-90 backdrop-blur-sm rounded-xl shadow-md p-6 mb-8">
        <h2 class="text-xl font-semibold mb-4 text-blue-800 flex items-center">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2" fill="none" viewBox="0 0 24 24"
            stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.228 9c.549-1.165 2.03-2 3.772-2
                     2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006
                     2.907-.542.104-.994.54-.994 1.093m0 3h.01M21
                     12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          Feedback dos Utilizadores
        </h2>

        <!-- Feedback Filter -->
        <div class="mb-4 flex flex-col md:flex-row md:items-center md:space-x-4 space-y-2 md:space-y-0">
          <div class="flex-1">
            <label class="block text-sm font-medium text-gray-700 mb-1">Filtrar por Email</label>
            <input v-model="filterEmail" type="email" placeholder="Digite o email"
              class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-300" />
          </div>
          <div class="flex-1">
            <label class="block text-sm font-medium text-gray-700 mb-1">Tipo</label>
            <select v-model="filterType"
              class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-300">
              <option value="">Todos</option>
              <option value="SUGGESTION">Sugestão</option>
              <option value="HELP">Ajuda</option>
            </select>
          </div>
          <div class="flex items-end">
            <button @click="fetchFeedback"
              class="px-4 py-2 bg-blue-600 text-white rounded-lg shadow-md hover:bg-blue-700 transition-all duration-300">
              Filtrar
            </button>
          </div>
        </div>

        <!-- Feedback List -->
        <div class="max-h-64 overflow-y-auto">
          <div v-if="feedbacks.length > 0">
            <ul class="divide-y divide-gray-200">
              <li v-for="feedback in feedbacks" :key="feedback.id"
                class="py-3 px-3 hover:bg-blue-50 transition-colors rounded-lg">
                <p class="font-medium">{{ feedback.userFeedback }}</p>
                <div class="mt-1 text-sm text-gray-600">
                  <span>Email: {{ feedback.email }}</span>
                  <span class="ml-4">Tipo: {{ formatFeedbackType(feedback.feedbackType) }}</span>
                </div>
              </li>
            </ul>
          </div>
          <div v-else class="py-4 text-center text-gray-500">
            Nenhum feedback encontrado.
          </div>
        </div>
      </div>

      <!-- Card: User Answers -->
      <div class="bg-white bg-opacity-90 backdrop-blur-sm rounded-xl shadow-md p-6">
        <h2 class="text-xl font-semibold mb-4 text-blue-800 flex items-center">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2" fill="none" viewBox="0 0 24 24"
            stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
          </svg>
          Respostas dos Utilizadores
        </h2>

        <!-- Filtro por Email -->
        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-1">Filtrar por Email</label>
          <input v-model="userAnswersEmail" type="email" placeholder="Digite o email"
            class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 transition-all duration-300" />
        </div>

        <!-- Filtro por Datas -->
        <div class="flex flex-col md:flex-row md:space-x-4 mb-4">
          <div class="flex-1">
            <label class="block text-sm font-medium text-gray-700 mb-1">Data Inicial</label>
            <input v-model="filterStartDate" type="date"
              class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 transition" />
          </div>
          <div class="flex-1">
            <label class="block text-sm font-medium text-gray-700 mb-1">Data Final</label>
            <input v-model="filterEndDate" type="date"
              class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 transition" />
          </div>
          <div class="flex items-end">
            <button @click="filterAnswersByDate"
              class="px-4 py-2 bg-indigo-600 text-white rounded-lg shadow-md hover:bg-indigo-700 transition">
              Filtrar por Data
            </button>
          </div>
        </div>

        <!-- Botões de busca -->
        <div class="flex space-x-4 mb-4">
          <button @click="fetchUserAnswersByEmail"
            class="px-4 py-2 bg-blue-600 text-white rounded-lg shadow-md hover:bg-blue-700 transition-all duration-300">
            Procurar por Email
          </button>
          <button @click="fetchAllUserAnswers"
            class="px-4 py-2 bg-green-600 text-white rounded-lg shadow-md hover:bg-green-700 transition-all duration-300">
            Ver Todas as Respostas
          </button>
        </div>

        <!-- Resultados -->
        <div v-if="userAnswers.length > 0">
          <ul class="divide-y divide-gray-200">
            <li v-for="ua in userAnswers" :key="ua.email" class="py-3">
              <p class="font-medium">Email: {{ ua.email }}</p>
              <div v-for="(severity, category) in ua.severitiesByCategory" :key="category" class="ml-4">
                <span class="text-sm text-blue-600">{{ formatCategoryName(category) }}: {{ severity }}</span>
              </div>
              <ul class="mt-2 ml-4 border-l border-gray-300 pl-4">
                <li v-for="answer in ua.answers" :key="answer.questionId" class="py-1">
                  {{ answer.questionText }} - {{ answer.userResponse }}
                </li>
              </ul>
            </li>
          </ul>
        </div>
        <div v-else class="py-4 text-center text-gray-500">
          Nenhuma resposta encontrada.
        </div>
      </div>


    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  name: "AdminDashboard",
  data() {
    return {
      // Questions
      questions: [],
      newQuestion: "",
      selectedCategory: "",
      categories: [
        "Risco_de_Autenticacao",
        "Risco_de_Utilizacao_de_Email",
        "Risco_de_Plataforma_da_Empresa",
        "Risco_de_Armazenamento_de_Dados",
        "Risco_da_Rede_Interna",
        "Risco_de_Infraestrutura_de_Informacao_Externa_Interna",
        "Risco_de_Ataques_Phishing_Engenharia_Social",
        "Risco_de_Acesso_Remoto",
        "Riscos_de_Material",
        "Riscos_Tecnologicos",
        "Riscos_Externos",
        "Risco_de_Configuracao_Inadequada_de_Sistemas",
        "Risco_de_Dependencia_de_Fornecedores_e_Terceiros",
        "Risco_de_Seguranca_em_APIs_e_Integracoes",
        "Risco_de_Falhas_em_Atualizacoes_e_Patches_de_Seguranca",
        "Risco_de_Exposicao_de_Dados_Sensiveis",
        "Risco_de_Manipulacao_de_Logs_e_Registos",
        "Risco_de_Engenharia_Reversa_e_Exploracao_de_Codigo_Fonte",
        "Risco_de_Ataques_de_Negacao_de_Servico",
        "Risco_de_Uso_Indevido_de_Privilegios_Internos"
      ]
      ,
      filterCategory: "", // "" means show all

      // Feedback
      feedbacks: [],
      filterEmail: "",
      filterType: "",
      isLoading: false,

      // Questionnaires
      newQuestionnaireTitle: "",
      questionnaires: [],
      selectedQuestionnaire: "",

      // Dynamic list of options for a new question
      newOptions: [
        {
          optionText: "",
          optionType: "IMPACT",
          optionLevel: "LOW"
        }
      ],
      optionTypes: ["IMPACT", "PROBABILITY"],
      optionLevels: ["LOW", "MEDIUM", "HIGH"],

      // Toggling visible questions
      visibleQuestions: {},

      // User answers
      userAnswersEmail: "",
      userAnswers: [],

      // For import
      selectedImportFile: null,

      //DataFilter
      filterStartDate: "",
      filterEndDate: "",
    };
  },
  created() {
    // Load everything at start
    this.fetchQuestions();
    // Immediately load all feedback so user sees it
    this.fetchFeedback();
    this.fetchQuestionnaires().then(() => {
      const savedId = localStorage.getItem("selectedQuestionnaire");
      if (savedId) {
        this.selectQuestionnaire(savedId);
      } else if (this.questionnaires.length > 0) {
        this.selectQuestionnaire(this.questionnaires[0].id);
      }
    });
  },
  methods: {
    // ======================
    //  QUESTIONS
    // ======================
    async fetchQuestions() {
      this.isLoading = true;
      try {
        const response = await axios.get("/api/questions/all");
        this.questions = response.data;
      } catch (error) {
        console.error("Erro ao procurar questões:", error);
        // fallback example
        this.questions = [
          { id: 1, questionText: "Está habilitada a autenticação de múltiplos fatores?", category: "AUTHENTICATION_RISK" },
          { id: 2, questionText: "Todos os backups de dados são criptografados?", category: "DATA_STORAGE_RISK" },
          { id: 3, questionText: "Utiliza SPF e DKIM para email?", category: "EMAIL_SECURITY" }
        ];
      } finally {
        this.isLoading = false;
      }
    },
    async filterQuestionsByCategory() {
      this.isLoading = true;
      try {
        if (this.filterCategory === "") {
          // No filter, load all questions
          await this.fetchQuestions();
        } else {
          // Fetch questions by category from your endpoint
          const response = await axios.get(`/api/questions/category/${this.filterCategory}`);
          this.questions = response.data;
        }
      } catch (error) {
        console.error("Erro ao filtrar questões:", error);
        alert("Falha ao filtrar questões.");
      } finally {
        this.isLoading = false;
      }
    },
    async addQuestion() {
      if (!this.newQuestion || !this.selectedCategory || !this.selectedQuestionnaire) {
        alert("Por favor, insira a questão, selecione uma categoria e escolha um questionário.");
        return;
      }
      this.isLoading = true;
      try {
        const payload = {
          questionText: this.newQuestion,
          category: this.selectedCategory,
          options: this.newOptions
        };
        await axios.post(`/api/questions/add/${this.selectedQuestionnaire}`, payload);
        // Reset form
        this.newQuestion = "";
        this.selectedCategory = "";
        this.selectedQuestionnaire = "";
        this.newOptions = [
          { optionText: "", optionType: "IMPACT", optionLevel: "LOW" }
        ];
        // Reload
        this.fetchQuestions();
      } catch (error) {
        console.error("Erro ao adicionar questão:", error);
        alert("Falha ao adicionar questão. Tente novamente.");
      } finally {
        this.isLoading = false;
      }
    },
    addOption() {
      this.newOptions.push({
        optionText: "",
        optionType: "IMPACT",
        optionLevel: "LOW"
      });
    },
    removeOption(index) {
      this.newOptions.splice(index, 1);
    },
    async deleteQuestion(id) {
      if (!confirm("Tem certeza que deseja excluir esta questão?")) return;
      try {
        await axios.delete(`/api/questions/delete/${id}`);
        this.fetchQuestions();
      } catch (error) {
        console.error("Erro ao excluir questão:", error);
        alert("Falha ao excluir questão. Tente novamente.");
      }
    },

    // ======================
    //  QUESTIONNAIRES
    // ======================
    async fetchQuestionnaires() {
      try {
        const response = await axios.get("/api/questionnaires/all");
        this.questionnaires = response.data;
      } catch (error) {
        console.error("Erro ao procurar questionários:", error);
        this.questionnaires = [];
      }
    },
    async addQuestionnaire() {
      if (!this.newQuestionnaireTitle) {
        alert("Por favor, insira um título para o questionário.");
        return;
      }
      this.isLoading = true;
      try {
        await axios.post("/api/questionnaires/create", { title: this.newQuestionnaireTitle });
        this.newQuestionnaireTitle = "";
        this.fetchQuestionnaires();
      } catch (error) {
        console.error("Erro ao criar questionário:", error);
        alert("Falha ao criar questionário. Tente novamente.");
      } finally {
        this.isLoading = false;
      }
    },
    async deleteQuestionnaire(id) {
  if (!confirm("Tem certeza que deseja excluir este questionário?")) return;
  try {
    const response = await axios.delete(`/api/questionnaires/delete/${id}`, {
      validateStatus: status => status < 500
    });

    if (response.status === 204 || response.status === 200) {
      this.fetchQuestionnaires();

      // ✅ Vue 3-compatible way to remove property if needed:
      delete this.visibleQuestions?.[id];
    } else {
      console.error("Erro ao excluir questionário. Status:", response.status);
      alert("Falha ao excluir questionário. Tente novamente.");
    }
  } catch (error) {
    console.error("Erro ao excluir questionário:", error);
    alert("Falha ao excluir questionário. Tente novamente.");
  }
}

    ,
    async exportQuestionnaire(questionnaireId) {
      try {
        const response = await axios.get(`/api/questionnaires/${questionnaireId}/export`, {
          responseType: "json"
        });
        const jsonData = response.data;
        // Convert to string
        const jsonString = JSON.stringify(jsonData, null, 2);
        // Create a Blob from the string
        const blob = new Blob([jsonString], { type: "application/json" });
        // Create a link to download the blob
        const url = URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = `questionnaire_${questionnaireId}.json`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);

        alert("Export concluído!");
      } catch (error) {
        console.error("Erro ao exportar questionário:", error);
        alert("Falha ao exportar questionário.");
      }
    },
    async selectQuestionnaire(id) {
      try {
        const response = await axios.get(`/api/questionnaires/${id}`);
        this.selectedQuestionnaire = response.data;
        localStorage.setItem("selectedQuestionnaire", id);
      } catch (error) {
        console.error("Erro ao procurar questionário:", error);
        this.selectedQuestionnaire = null;
      }
    },

    // Import
    triggerImportFilePicker() {
      this.$refs.importFile.click();
    },
    onImportFileSelected(event) {
      this.selectedImportFile = event.target.files[0];
      this.importQuestionnaire();
    },
    async importQuestionnaire() {
      if (!this.selectedImportFile) return;
      try {
        const fileContent = await this.selectedImportFile.text();
        const jsonData = JSON.parse(fileContent);
        console.log("Payload being sent to /api/questionnaires/import:", jsonData);

        await axios.post("/api/questionnaires/import", jsonData, {
          headers: { "Content-Type": "application/json" }
        });
        alert("Questionário importado com sucesso!");
        this.fetchQuestionnaires();
      } catch (error) {
        console.error("Erro ao importar questionário:", error);
        alert("Falha ao importar questionário.");
      } finally {
        this.selectedImportFile = null;
        if (this.$refs.importFile) {
          this.$refs.importFile.value = null;
        }
      }
    },

    // ======================
    //  FEEDBACK
    // ======================
    async fetchFeedback() {
      this.isLoading = true;
      try {
        const params = {};
        if (this.filterEmail) params.email = this.filterEmail;
        if (this.filterType) params.type = this.filterType;
        const response = await axios.get("/api/feedback", { params });
        this.feedbacks = response.data;
      } catch (error) {
        console.error("Erro ao procurar feedback:", error);
        this.feedbacks = [];
      } finally {
        this.isLoading = false;
      }
    },

    // ======================
    //  USER ANSWERS
    // ======================
    async fetchUserAnswersByEmail() {
      if (!this.userAnswersEmail) {
        alert("Por favor, insira um email para filtrar.");
        return;
      }
      this.isLoading = true;
      try {
        const response = await axios.get(`/api/answers/by-email-with-severity/${this.userAnswersEmail}`, {
          // eslint-disable-next-line 
          validateStatus: _status => true
        });
        if (response.status === 401) {
          alert("Você não está autorizado.");
        } else {
          this.userAnswers = [response.data];
        }
      } catch (error) {
        console.error("Erro ao procurar respostas por email:", error);
        alert("Falha ao procurar respostas.");
      } finally {
        this.isLoading = false;
      }
    },
    async fetchAllUserAnswers() {
      this.isLoading = true;
      try {
        const response = await axios.get(`/api/answers/get-all-email`, {
          // eslint-disable-next-line
          validateStatus: _status => true
        });
        if (response.status === 401) {
          alert("Você não está autorizado.");
        } else {
          this.userAnswers = response.data;
        }
      } catch (error) {
        console.error("Erro ao procurar todas as respostas:", error);
        alert("Falha ao procurar respostas.");
      } finally {
        this.isLoading = false;
      }
    },

    // ======================
    //  UTILS
    // ======================
    formatCategoryName(category) {
      if (!category) return "";
      return category
        .replace(/_/g, " ")
        .toLowerCase()
        .replace(/\b\w/g, l => l.toUpperCase());
    },
    formatFeedbackType(type) {
      if (type === "SUGGESTION") return "Sugestão";
      if (type === "HELP") return "Ajuda";
      return type;
    },

    async filterAnswersByDate() {
      if (!this.filterStartDate || !this.filterEndDate) {
        alert("Por favor, selecione ambas as datas.");
        return;
      }

      try {
        const response = await axios.get("/api/answers/by-date-range", {
          params: {
            startDate: this.filterStartDate,
            endDate: this.filterEndDate
          }
        });
        this.userAnswers = response.data;
      } catch (error) {
        console.error("Erro ao filtrar por data:", error);
        alert("Falha ao procurar respostas por data.");
      }
    }
  }
};
</script>

<style>
html,
body {
  height: 100%;
  margin: 0;
  padding: 0;
}
</style>
