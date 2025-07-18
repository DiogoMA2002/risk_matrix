<template>
  <transition name="slide">
    <div v-if="open" class="fixed inset-0 z-50 flex justify-end">
      <div class="bg-black bg-opacity-40 w-full h-full" @click="$emit('close')"></div>
      <aside class="w-full max-w-md h-full bg-white shadow-2xl rounded-bl-3xl p-0 flex flex-col relative border-l-4 border-blue-600">
        <header class="flex items-center justify-between px-8 py-6 bg-gradient-to-r from-blue-600 to-blue-400 shadow">
          <h2 class="text-2xl font-extrabold text-white tracking-wide">{{ title }}</h2>
          <button class="text-white hover:text-blue-200 transition p-2 rounded-full focus:outline-none focus:ring-2 focus:ring-white" @click="$emit('close')" aria-label="Fechar Glossário">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" class="w-7 h-7">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </header>
        <div class="px-8 py-6 flex-1 overflow-y-auto">
          <input
            v-model="search"
            type="text"
            placeholder="Pesquisar termos..."
            class="w-full mb-6 px-4 py-2 border-2 border-blue-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-400 text-lg"
          />
          <ul>
            <li v-for="item in filteredTerms" :key="item.term" class="mb-6">
              <div class="font-bold text-blue-700 text-lg">{{ item.term }}</div>
              <div class="text-gray-700 text-base mt-1">{{ item.definition }}</div>
            </li>
          </ul>
          <div v-if="filteredTerms.length === 0" class="text-gray-400 mt-12 text-center text-lg">Nenhum termo encontrado.</div>
        </div>
      </aside>
    </div>
  </transition>
</template>

<script>
import axios from 'axios';
export default {
  name: 'GlossaryDrawer',
  props: {
    open: {
      type: Boolean,
      required: true
    },
    title: {
      type: String,
      default: 'Glossário'
    }
  },
  data() {
    return {
      search: '',
      glossary: [],
      loading: false,
      error: null
    };
  },
  computed: {
    filteredTerms() {
      if (!this.search) return this.glossary;
      const s = this.search.toLowerCase();
      return this.glossary.filter(item =>
        item.term.toLowerCase().includes(s) ||
        item.definition.toLowerCase().includes(s)
      );
    }
  },
  async mounted() {
    this.loading = true;
    try {
      const response = await axios.get('/api/glossary');
      this.glossary = response.data;
    } catch (err) {
      this.error = 'Erro ao carregar o glossário.';
      this.glossary = [];
    } finally {
      this.loading = false;
    }
  }
};
</script>

<style scoped>
.slide-enter-active, .slide-leave-active {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.slide-enter-from, .slide-leave-to {
  transform: translateX(100%);
}
.slide-enter-to, .slide-leave-from {
  transform: translateX(0);
}
</style> 