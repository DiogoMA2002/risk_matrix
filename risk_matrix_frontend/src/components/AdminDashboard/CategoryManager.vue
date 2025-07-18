<template>
  <div class="bg-white bg-opacity-90 backdrop-blur-sm rounded-xl shadow-md p-6 mb-6">
    <header class="mb-6">
      <h2 class="text-xl font-semibold text-blue-800 flex items-center">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
        </svg>
        Gerir Categorias
      </h2>
      <!-- Subtitle if needed -->
      <!-- <p class="text-sm text-gray-600">Gerencie as categorias disponíveis.</p> -->
      <div class="flex justify-between items-center mt-4">
        <div class="flex-1 max-w-md">
          <div class="relative">
            <input type="text" v-model="searchQuery"
              class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all"
              :placeholder="categoryLabels.search"
              :disabled="loading" />
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400 absolute left-3 top-2.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </div>
        </div>
        <button @click="showNewCategoryInput = !showNewCategoryInput"
          class="ml-4 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 focus:ring-2 focus:ring-blue-400 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center"
          :disabled="loading">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
          </svg>
          {{ categoryLabels.addNew }}
        </button>
      </div>
    </header>

    <!-- New Category Input -->
    <div v-if="showNewCategoryInput" class="mb-6 p-4 bg-gray-50 rounded-lg">
      <div class="flex gap-4">
        <input v-model="newCategory" type="text"
          class="flex-1 p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all"
          :placeholder="categoryLabels.categoryName"
          :disabled="loading" />
        <button @click="createCategory"
          class="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 focus:ring-2 focus:ring-green-400 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center"
          :disabled="loading || !newCategory.trim()">
          <svg v-if="loading" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          {{ loading ? categoryLabels.loading : categoryLabels.add }}
        </button>
      </div>
    </div>

    <!-- Categories List -->
    <div v-if="filteredCategories.length === 0" class="text-center py-8 text-gray-500">
      {{ categoryLabels.noResults }}
    </div>
    <div v-else class="space-y-4">
      <div v-for="category in paginatedCategories" :key="category.id"
        class="p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors">
        <div class="flex items-center justify-between">
          <span class="text-gray-900">{{ formatCategoryName(category.name) }}</span>
          <div class="flex items-center space-x-2">
            <button @click="startEdit(category)"
              class="text-blue-600 hover:text-blue-800 focus:ring-2 focus:ring-blue-400 rounded-lg p-1"
              :disabled="loading">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
            </button>
            <button @click="deleteCategory(category.id)"
              class="text-red-600 hover:text-red-800 focus:ring-2 focus:ring-red-400 rounded-lg p-1"
              :disabled="loading">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            </button>
          </div>
        </div>

        <!-- Edit Form -->
        <div v-if="editingCategory?.id === category.id" class="mt-4 p-4 bg-white rounded-lg border border-gray-200">
          <div class="flex gap-4">
            <input v-model="editName" type="text"
              class="flex-1 p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all"
              :placeholder="categoryLabels.categoryName"
              :disabled="loading" />
            <div class="flex gap-2">
              <button @click="saveEdit"
                class="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 focus:ring-2 focus:ring-green-400 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center"
                :disabled="loading || !editName.trim()">
                <svg v-if="loading" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                {{ loading ? categoryLabels.loading : categoryLabels.save }}
              </button>
              <button @click="cancelEdit"
                class="px-4 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700 focus:ring-2 focus:ring-gray-400 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                :disabled="loading">
                {{ categoryLabels.cancel }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div v-if="totalPages > 1" class="mt-6 flex justify-center space-x-2">
      <button v-for="page in totalPages" :key="page"
        @click="changePage(page)"
        class="px-4 py-2 rounded-lg transition-colors"
        :class="{
          'bg-blue-600 text-white': currentPage === page,
          'bg-gray-200 text-gray-700 hover:bg-gray-300': currentPage !== page
        }"
        :disabled="loading">
        {{ page }}
      </button>
    </div>
  </div>
</template>

<script>
export default {
  name: "CategoryManager",
  props: {
    categories: {
      type: Array,
      default: () => [],
    },
    loading: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      searchQuery: "",
      currentPage: 1,
      itemsPerPage: 5,
      editingCategory: null,
      editName: "",
      showNewCategoryInput: false,
      newCategory: "",
    };
  },
  computed: {
    filteredCategories() {
      if (!this.searchQuery.trim()) return this.categories;
      const query = this.searchQuery.toLowerCase().trim();
      return this.categories.filter(cat => 
        cat.name.toLowerCase().includes(query)
      );
    },
    totalPages() {
      return Math.ceil(this.filteredCategories.length / this.itemsPerPage);
    },
    paginatedCategories() {
      const start = (this.currentPage - 1) * this.itemsPerPage;
      const end = start + this.itemsPerPage;
      return this.filteredCategories.slice(start, end);
    },
    categoryLabels() {
      return {
        add: "Adicionar Categoria",
        edit: "Editar Categoria",
        delete: "Eliminar Categoria",
        search: "Pesquisar categorias...",
        noResults: "Nenhuma categoria encontrada",
        cancel: "Cancelar",
        confirm: "Confirmar",
        save: "Salvar",
        addNew: "Adicionar Nova Categoria",
        editCategory: "Editar Categoria",
        deleteCategory: "Eliminar Categoria",
        categoryName: "Nome da Categoria",
        loading: "A processar...",
      };
    }
  },
  methods: {
    formatCategoryName(name) {
      if (!name || typeof name !== 'string') return '';
      return name
        .replace(/_/g, " ")
        .split(" ")
        .map(w => w.charAt(0).toUpperCase() + w.slice(1).toLowerCase())
        .join(" ");
    },
    startEdit(category) {
      this.editingCategory = category;
      this.editName = category.name;
    },
    cancelEdit() {
      this.editingCategory = null;
      this.editName = "";
    },
    async saveEdit() {
      if (!this.editName.trim()) {
        return;
      }

      const forbidden = /[^a-zA-Z0-9\sáàâãéèêíïóôõöúçÁÀÂÃÉÈÍÏÓÔÕÖÚÇ]/;
      if (forbidden.test(this.editName.trim())) {
        return;
      }

      if (this.categories.some(cat => 
        cat.id !== this.editingCategory.id && 
        cat.name.toLowerCase() === this.editName.trim().toLowerCase()
      )) {
        return;
      }

      try {
        await this.$emit("edit-category", {
          id: this.editingCategory.id,
          name: this.editName.trim()
        });
        this.cancelEdit();
      } catch (error) {
        console.error("Error in CategoryManager saveEdit:", error);
      }
    },
    async deleteCategory(id) {
      try {
        await this.$emit("delete-category", id);
      } catch (error) {
        // Error handling is done in the parent component
        console.error("Error in CategoryManager deleteCategory:", error);
      }
    },
    async createCategory() {
      if (!this.newCategory.trim()) {
        return;
      }

      const forbidden = /[^a-zA-Z0-9\sáàâãéèêíïóôõöúçÁÀÂÃÉÈÍÏÓÔÕÖÚÇ]/;
      if (forbidden.test(this.newCategory.trim())) {
        return;
      }

      if (this.categories.some(cat => 
        cat.name.toLowerCase() === this.newCategory.trim().toLowerCase()
      )) {
        return;
      }

      try {
        await this.$emit("create-category", this.newCategory.trim());
        this.newCategory = "";
        this.showNewCategoryInput = false;
      } catch (error) {
        console.error("Error in CategoryManager createCategory:", error);
      }
    },

    changePage(page) {
      this.currentPage = page;
    }
  }
};
</script>

<style scoped>
/* Add smooth transitions */
.transition-all {
  transition-property: all;
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
  transition-duration: 150ms;
}

/* Improve focus styles for better accessibility */
button:focus-visible,
input:focus-visible {
  outline: 2px solid #4f46e5;
  outline-offset: 2px;
}

/* Add hover effect for better interactivity */
button:not(:disabled):hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}

button:not(:disabled):active {
  transform: translateY(0);
}
</style> 