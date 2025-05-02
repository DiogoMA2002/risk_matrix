<template>
  <div class="bg-white bg-opacity-90 backdrop-blur-sm rounded-xl shadow-md p-6 mb-8">
    <h2 class="text-xl font-semibold mb-4 text-blue-800 flex items-center">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
      </svg>
      Adicionar Nova Questão
    </h2>

    <div class="space-y-4">
      <!-- Texto da Questão -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Texto da Questão</label>
        <input
          v-model="newQuestion"
          type="text"
          class="w-full p-3 border border-gray-300 rounded-lg"
          placeholder="Digite o texto da pergunta"
        />
      </div>

      <!-- Categoria -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Categoria</label>
        <select v-model="selectedCategory" class="w-full p-3 border border-gray-300 rounded-lg">
          <option disabled value="">Selecione uma Categoria</option>
          <option v-for="category in categories" :key="category.id" :value="category.name">
            {{ formatCategoryName(category.name) }}
          </option>
        </select>
      </div>

      <!-- Nova Categoria -->
      <div class="flex items-center space-x-4">
        <input
          v-model="newCategory"
          type="text"
          placeholder="Nova Categoria"
          class="w-full p-3 border border-gray-300 rounded-lg"
        />
        <button @click="createCategory" class="px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600">
          Criar Categoria
        </button>
      </div>

      <!-- Questionários -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Questionários (selecione um ou mais)</label>
        <div class="flex flex-col space-y-2">
          <label v-for="q in questionnaires" :key="q.id" class="inline-flex items-center space-x-2">
            <input type="checkbox" v-model="selectedQuestionnaires" :value="q.id" class="form-checkbox">
            <span>{{ q.title }}</span>
          </label>
        </div>
      </div>


      <!-- Opções da Questão -->
      <div class="mt-6">
        <h3 class="text-lg font-semibold mb-2 text-gray-800">Opções por Questão</h3>
        <div v-for="(option, index) in newOptions" :key="index" class="p-4 mb-3 border border-gray-200 rounded bg-gray-50">
          <div class="grid md:grid-cols-4 gap-4">
            <!-- Texto da Opção -->
            <div>
              <label class="text-sm font-medium text-gray-700">Texto</label>
              <input v-model="option.optionText" type="text" class="w-full p-2 border border-gray-300 rounded" />
            </div>

            <!-- Tipo -->
            <div>
              <label class="text-sm font-medium text-gray-700">Tipo</label>
              <select
                v-model="option.optionType"
                class="w-full p-2 border border-gray-300 rounded"
              >
                <option v-for="type in optionTypes" :key="type" :value="type">{{ type }}</option>
              </select>
            </div>

            <!-- Nível -->
            <div>
              <label class="text-sm font-medium text-gray-700">Nível</label>
              <select v-model="option.optionLevel" class="w-full p-2 border border-gray-300 rounded">
                <option v-for="level in optionLevels" :key="level" :value="level">{{ level }}</option>
              </select>
            </div>

            <!-- Recomendação -->
            <div>
              <label class="text-sm font-medium text-gray-700">Recomendação</label>
              <input v-model="option.recommendation" type="text" class="w-full p-2 border border-gray-300 rounded" placeholder="Texto da recomendação" />
            </div>
          </div>

          <!-- Remover -->
          <div class="mt-2">
            <button v-if="newOptions.length > 1" @click="removeOption(index)" class="text-red-600 hover:underline">
              Remover
            </button>
          </div>
        </div>

        <button @click="addOption" class="px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600">
          Adicionar outra opção
        </button>
      </div>

      <!-- Submeter -->
      <div class="pt-4">
        <button
          @click="submitQuestion"
          class="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition flex items-center"
        >
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
      default: () => ["IMPACT", "PROBABILITY"],
    },
    optionLevels: {
      type: Array,
      default: () => ["LOW", "MEDIUM", "HIGH"],
    },
  },
  data() {
    return {
      newQuestion: "",
      selectedCategory: "",
      selectedQuestionnaires: [],
      newOptions: [
        {
          optionText: "",
          optionType: "IMPACT",
          optionLevel: "LOW",
          recommendation: ""
        }
      ],
      newCategory: ""
    };
  },
  methods: {
    formatCategoryName(name) {
      return name
        .replace(/_/g, " ")
        .split(" ")
        .map(w => w.charAt(0).toUpperCase() + w.slice(1).toLowerCase())
        .join(" ");
    },
    createCategory() {
      if (!this.newCategory.trim()) {
        alert("Nome da nova categoria não pode estar vazio.");
        return;
      }

      this.$emit("create-category", this.newCategory.trim());
      this.selectedCategory = this.newCategory.trim();
      this.newCategory = "";
    },
    addOption() {
      this.newOptions.push({
        optionText: "",
        optionType: this.newOptions[0]?.optionType || "IMPACT",
        optionLevel: "LOW",
        recommendation: ""
      });
    },
    removeOption(index) {
      this.newOptions.splice(index, 1);
    },
    lockOptionType(type) {
      this.newOptions.forEach(opt => {
        opt.optionType = type;
      });
    },
    submitQuestion() {
      if (!this.newQuestion.trim()) {
        alert("Texto da questão é obrigatório.");
        return;
      }

      if (!this.selectedCategory && !this.newCategory) {
        alert("Selecione uma categoria existente ou crie uma nova.");
        return;
      }

      if (this.selectedQuestionnaires.length === 0) {
        alert("Selecione pelo menos um questionário.");
        return;
      }

      const questionData = {
        newQuestion: this.newQuestion.trim(),
        selectedCategory: this.newCategory.trim() || this.selectedCategory,
        newOptions: this.newOptions.map(opt => ({
          optionText: opt.optionText.trim(),
          optionType: opt.optionType,
          optionLevel: opt.optionLevel,
          recommendation: opt.recommendation?.trim() || ""
        })),
        selectedQuestionnaires: this.selectedQuestionnaires
      };

      this.$emit("add-question", questionData);

      // Reset form
      this.newQuestion = "";
      this.selectedCategory = "";
      this.selectedQuestionnaires = [];
      this.newOptions = [
        {
          optionText: "",
          optionType: "IMPACT",
          optionLevel: "LOW",
          recommendation: ""
        }
      ];
      this.newCategory = "";
    }
  }
};
</script>
