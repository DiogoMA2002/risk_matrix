<template>
  <div class="bg-white bg-opacity-90 backdrop-blur-sm rounded-xl shadow-md p-6 mb-8">
    <h2 class="text-xl font-semibold mb-4 text-blue-800 flex items-center">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" d="M19 11H5m14 0a2 2 0 0 1 2 2v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-6a2 2 0 0 1 2-2m14 0V9a2 2 0 0 0-2-2M5 11V9a2 2 0 0 1 2-2m0 0V5a2 2 0 0 1 2-2h6a2 2 0 0 1 2 2v2M7 7h10" />
      </svg>
      Gerir Categorias
    </h2>
    <div v-if="loading" class="text-center text-gray-500">Carregando categorias...</div>
    <div v-else-if="!localCategories || localCategories.length === 0" class="text-center text-gray-500">Nenhuma categoria encontrada.</div>
    <ul v-else class="space-y-3">
      <li v-for="category in paginatedCategories" :key="category.id" class="flex items-center justify-between p-3 bg-gray-50 rounded-lg shadow-sm">
        <div v-if="editingCategoryId === category.id" class="flex-grow mr-2">
          <input 
            v-model="editingCategoryName" 
            type="text" 
            class="w-full px-2 py-1 border border-blue-300 rounded focus:ring-blue-500 focus:border-blue-500"
            @keyup.enter="saveEdit(category.id)"
            @keyup.esc="cancelEdit"
            ref="editInput"
          />
        </div>
        <span v-else class="text-gray-800 flex-grow">{{ category.name }}</span>
        <div class="flex-shrink-0 space-x-2">
          <template v-if="editingCategoryId === category.id">
            <button @click="saveEdit(category.id)" class="p-1 text-green-600 hover:text-green-800 transition-colors">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" />
              </svg>
            </button>
            <button @click="cancelEdit" class="p-1 text-gray-500 hover:text-gray-700 transition-colors">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </template>
          <template v-else>
            <button @click="startEdit(category)" class="p-1 text-blue-600 hover:text-blue-800 transition-colors" title="Editar">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M11 5H6a2 2 0 0 0-2 2v11a2 2 0 0 0 2 2h11a2 2 0 0 0 2-2v-5m-1.414-9.414a2 2 0 1 1 2.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
            </button>
            <button @click="requestDelete(category.id)" class="p-1 text-red-600 hover:text-red-800 transition-colors" title="Excluir">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M19 7l-.867 12.142A2 2 0 0 1 16.138 21H7.862a2 2 0 0 1-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 0 0-1-1h-4a1 1 0 0 0-1 1v3M4 7h16" />
              </svg>
            </button>
          </template>
        </div>
      </li>
    </ul>
    <div v-if="totalPages > 1" class="flex justify-center items-center space-x-2 mt-6 pt-4 border-t border-gray-200">
      <button @click="prevPage" :disabled="currentPage === 1" 
              class="px-4 py-2 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed border border-gray-300">
        Anterior
      </button>
      <span class="text-sm text-gray-700">Página {{ currentPage }} de {{ totalPages }}</span>
      <button @click="nextPage" :disabled="currentPage === totalPages" 
              class="px-4 py-2 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed border border-gray-300">
        Próxima
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
      default: () => []
    },
    loading: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      editingCategoryId: null,
      editingCategoryName: "",
      localCategories: [],
      currentPage: 1,
      itemsPerPage: 5, // Adjust as needed
    };
  },
  computed: {
    totalPages() {
      return Math.ceil(this.localCategories.length / this.itemsPerPage);
    },
    paginatedCategories() {
      const start = (this.currentPage - 1) * this.itemsPerPage;
      const end = start + this.itemsPerPage;
      return this.localCategories.slice(start, end);
    }
  },
  watch: {
    categories: {
      immediate: true,
      handler(newVal) {
        // Ensure it's always an array of objects with id and name
        this.localCategories = newVal && Array.isArray(newVal) 
          ? newVal.map(cat => typeof cat === 'object' && cat !== null && cat.id && cat.name ? cat : { id: Date.now() + Math.random(), name: String(cat) }) // Fallback if structure is wrong
          : [];
      }
    },
    // Reset page if categories change and current page becomes invalid
    localCategories() {
       if (this.currentPage > this.totalPages && this.totalPages > 0) {
        this.currentPage = this.totalPages; // Go to last page if possible
      } else if (this.totalPages === 0) {
          this.currentPage = 1; // Reset to 1 if list becomes empty
      }
       // Reset to page 1 if filter/search reduced items below current page (optional)
       // Consider if filtering logic is added later
       // if (this.currentPage > 1 && this.paginatedCategories.length === 0) {
       //   this.currentPage = 1;
       // }
    }
  },
  methods: {
    startEdit(category) {
      this.editingCategoryId = category.id;
      this.editingCategoryName = category.name;
      // Focus the input field after it becomes visible
      this.$nextTick(() => {
         if (this.$refs.editInput && this.$refs.editInput[0]) {
             this.$refs.editInput[0].focus();
         } else if (this.$refs.editInput) { // If only one item, refs might not be an array
             this.$refs.editInput.focus();
         }
      });
    },
    cancelEdit() {
      this.editingCategoryId = null;
      this.editingCategoryName = "";
    },
    saveEdit(categoryId) {
      if (!this.editingCategoryName.trim()) {
        // Optionally emit an error or show a message
        console.warn("Category name cannot be empty."); 
        return;
      }
      if (this.editingCategoryId === categoryId) { // Ensure we're saving the right one
         this.$emit("edit-category", { id: categoryId, name: this.editingCategoryName.trim() });
         this.cancelEdit(); // Reset editing state after emitting
      }
    },
    requestDelete(categoryId) {
       this.$emit("delete-category", categoryId);
    },
    nextPage() {
      if (this.currentPage < this.totalPages) {
        this.currentPage++;
      }
    },
    prevPage() {
      if (this.currentPage > 1) {
        this.currentPage--;
      }
    }
  }
};
</script>

<style scoped>
/* Add any specific styles for CategoryManager if needed */
</style> 