<template>
  <div class="bg-white bg-opacity-90 backdrop-blur-sm rounded-xl shadow-md p-6 mb-8">
    <h2 class="text-xl font-semibold mb-4 text-blue-800 flex items-center">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
      </svg>
      Adicionar Nova Questão
    </h2>
    <div class="space-y-4">
      <!-- Question Text -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Texto da Questão</label>
        <input v-model="newQuestion" type="text" placeholder="Digite a questão" class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 transition-all duration-300" />
      </div>
      <!-- Category -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Categoria</label>
        <select v-model="selectedCategory" class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 transition-all duration-300 bg-white">
          <option disabled value="">Selecione a Categoria</option>
          <option v-for="cat in categories" :key="cat" :value="cat">
            {{ formatCategoryName(cat) }}
          </option>
        </select>
      </div>
      <!-- Associate to a Questionnaire -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Questionário</label>
        <select v-model="selectedQuestionnaire" class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 transition-all duration-300 bg-white">
          <option disabled value="">Selecione o Questionário</option>
          <option v-for="questionnaire in questionnaires" :key="questionnaire.id" :value="questionnaire.id">
            {{ questionnaire.title }}
          </option>
        </select>
      </div>
      <!-- Dynamic Options List -->
      <div class="mt-6">
        <h3 class="text-lg font-semibold mb-2 text-gray-800">Opções da Questão</h3>
        <div v-for="(option, index) in newOptions" :key="index" class="p-4 mb-3 border border-gray-200 rounded-lg bg-gray-50">
          <div class="flex flex-col md:flex-row md:items-center md:space-x-4 space-y-2 md:space-y-0">
            <!-- Option Text -->
            <div class="flex-1">
              <label class="block text-sm font-medium text-gray-700">Texto da Opção</label>
              <input v-model="option.optionText" type="text" placeholder="Ex: Sim" class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 transition-all duration-300" />
            </div>
            <!-- Option Type -->
            <div class="flex-1">
              <label class="block text-sm font-medium text-gray-700">Tipo da Opção</label>
              <select v-model="option.optionType" class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 transition-all duration-300">
                <option v-for="type in optionTypes" :key="type" :value="type">{{ type }}</option>
              </select>
            </div>
            <!-- Option Level -->
            <div class="flex-1">
              <label class="block text-sm font-medium text-gray-700">Nível</label>
              <select v-model="option.optionLevel" class="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 transition-all duration-300">
                <option v-for="level in optionLevels" :key="level" :value="level">{{ level }}</option>
              </select>
            </div>
            <!-- Remove Option Button -->
            <div>
              <button v-if="newOptions.length > 1" @click="removeOption(index)" type="button" class="mt-2 text-red-600 hover:text-red-800">
                Remover
              </button>
            </div>
          </div>
        </div>
        <button type="button" class="px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600" @click="addOption">
          Adicionar outra opção
        </button>
      </div>
      <!-- Submit Button -->
      <div class="pt-4">
        <button @click="onAddQuestion" class="px-6 py-3 bg-blue-600 text-white rounded-lg shadow-md hover:bg-blue-700 transition-all duration-300 flex items-center">
          <span>Adicionar Questão</span>
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "QuestionForm",
  props: {
    categories: {
      type: Array,
      default: () => [],
    },
    questionnaires: {
      type: Array,
      default: () => [],
    },
    optionTypes: {
      type: Array,
      default: () => [],
    },
    optionLevels: {
      type: Array,
      default: () => [],
    },
  },
  data() {
    return {
      newQuestion: "",
      selectedCategory: "",
      selectedQuestionnaire: "",
      newOptions: [
        { optionText: "", optionType: "IMPACT", optionLevel: "LOW" },
      ],
    };
  },
  methods: {
    formatCategoryName(category) {
      if (!category) return "";
      return category.replace(/_/g, " ").toLowerCase().replace(/\b\w/g, l => l.toUpperCase());
    },
    addOption() {
      this.newOptions.push({ optionText: "", optionType: "IMPACT", optionLevel: "LOW" });
    },
    removeOption(index) {
      this.newOptions.splice(index, 1);
    },
    onAddQuestion() {
      this.$emit("add-question", {
        newQuestion: this.newQuestion,
        selectedCategory: this.selectedCategory,
        selectedQuestionnaire: this.selectedQuestionnaire,
        newOptions: this.newOptions,
      });
      // Reset form
      this.newQuestion = "";
      this.selectedCategory = "";
      this.selectedQuestionnaire = "";
      this.newOptions = [{ optionText: "", optionType: "IMPACT", optionLevel: "LOW" }];
    },
  },
};
</script>
