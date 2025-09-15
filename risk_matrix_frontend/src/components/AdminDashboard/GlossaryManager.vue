<template>
  <div class="bg-white bg-opacity-90 backdrop-blur-sm rounded-xl shadow-md p-6 mb-8 w-full mt-8">
    <div class="flex items-center mb-6 gap-3">
      <svg class="h-6 w-6 text-blue-600 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 20V6m8 6H4m16 0a8 8 0 11-16 0 8 8 0 0116 0z" />
      </svg>
      <div>
        <h2 class="text-xl font-semibold text-blue-800 flex items-center">Gerir Glossário</h2>
        <p class="text-sm text-gray-600">Adicione, edite, elimine, importe ou exporte termos do glossário.</p>
      </div>
    </div>
    <div class="flex flex-wrap gap-2 mb-6 items-end">
      <div class="flex flex-col">
        <label class="text-xs text-gray-500 mb-1">Novo termo</label>
        <input v-model="newTerm" placeholder="Termo" class="input" />
      </div>
      <div class="flex flex-col flex-1">
        <label class="text-xs text-gray-500 mb-1">Definição</label>
        <input v-model="newDefinition" placeholder="Definição" class="input" />
      </div>
      <button @click="addEntry" class="action-btn bg-blue-600 hover:bg-blue-700" title="Adicionar termo">
        <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/></svg>
      </button>
      <button @click="triggerImport" class="action-btn bg-purple-600 hover:bg-purple-700" title="Importar JSON">
        <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/></svg>
      </button>
      <button @click="exportGlossary" class="action-btn bg-green-600 hover:bg-green-700" title="Exportar JSON">
        <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v2a2 2 0 002 2h12a2 2 0 002-2v-2M7 10l5 5 5-5M12 15V3"/></svg>
      </button>
      <input ref="importFile" type="file" accept="application/json" class="hidden" @change="importFromJSON" />
    </div>
    <div class="flex items-center mb-4">
      <input v-model="search" placeholder="Pesquisar termo ou definição..." class="input w-full" />
    </div>
    <div v-if="error" class="bg-red-100 text-red-700 px-4 py-2 rounded mb-4">{{ error }}</div>
    <div v-if="success" class="bg-green-100 text-green-700 px-4 py-2 rounded mb-4">{{ success }}</div>
    <div class="overflow-x-auto">
      <table class="w-full text-base rounded-lg overflow-hidden">
        <thead>
          <tr class="bg-blue-50 text-blue-700">
            <th class="p-3 text-left">Termo</th>
            <th class="p-3 text-left">Definição</th>
            <th class="p-3 text-center">Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="entry in paginatedGlossary" :key="entry.id" class="hover:bg-blue-50 transition">
            <td class="p-3 align-top w-1/4">
              <input v-if="editId === entry.id" v-model="editTerm" class="input w-full" />
              <span v-else>{{ entry.term }}</span>
            </td>
            <td class="p-3 align-top">
              <input v-if="editId === entry.id" v-model="editDefinition" class="input w-full" />
              <span v-else>{{ entry.definition }}</span>
            </td>
            <td class="p-3 text-center">
              <template v-if="editId === entry.id">
                <button @click="saveEdit(entry.id)" class="icon-btn bg-green-600 hover:bg-green-700 mr-2" title="Guardar">
                  <svg class="w-5 h-5 inline-block mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/></svg>
                  Guardar
                </button>
                <button @click="cancelEdit" class="icon-btn bg-gray-400 hover:bg-gray-500" title="Cancelar">
                  <svg class="w-5 h-5 inline-block mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
                  Cancelar
                </button>
              </template>
              <template v-else>
                <div class="flex gap-2 justify-center">
                  <button @click="startEdit(entry)" class="btn-edit" title="Editar">
                    Editar
                  </button>
                  <button @click="confirmDelete(entry.id)" class="btn-delete" title="Eliminar">
                    Eliminar
                  </button>
                </div>
              </template>
            </td>
          </tr>
          <tr v-if="paginatedGlossary.length === 0">
            <td colspan="3" class="text-center text-gray-400 py-6">Nenhum termo encontrado.</td>
          </tr>
        </tbody>
      </table>
    </div>
    <div class="flex justify-between items-center mt-4" v-if="totalPages > 1">
      <button @click="prevPage" :disabled="currentPage === 1" class="action-btn bg-gray-200 text-gray-700 hover:bg-gray-300 disabled:opacity-50">
        &lt;
      </button>
      <div class="flex gap-1">
        <button v-for="page in totalPages" :key="page" @click="goToPage(page)" :class="['action-btn', currentPage === page ? 'bg-blue-600 text-white' : 'bg-gray-200 text-gray-700 hover:bg-gray-300']">
          {{ page }}
        </button>
      </div>
      <button @click="nextPage" :disabled="currentPage === totalPages" class="action-btn bg-gray-200 text-gray-700 hover:bg-gray-300 disabled:opacity-50">
        &gt;
      </button>
    </div>
    <AlertDialog
      :show="showAlert"
      :title="alertTitle"
      :message="alertMessage"
      :type="alertType"
      @confirm="handleAlertConfirm"
      @cancel="handleAlertCancel"
    />
  </div>
</template>

<script>
import axios from 'axios';
import AlertDialog from '@/components/Static/AlertDialog.vue';
export default {
  name: 'GlossaryManager',
  components: { AlertDialog },
  data() {
    return {
      glossary: [],
      newTerm: '',
      newDefinition: '',
      editId: null,
      editTerm: '',
      editDefinition: '',
      error: '',
      success: '',
      search: '',
      currentPage: 1,
      pageSize: 10,
      showAlert: false,
      alertTitle: '',
      alertMessage: '',
      alertType: 'confirm',
      pendingDeleteId: null
    };
  },
  computed: {
    filteredGlossary() {
      if (!this.search) return this.glossary;
      const s = this.search.toLowerCase();
      return this.glossary.filter(item =>
        item.term.toLowerCase().includes(s) ||
        item.definition.toLowerCase().includes(s)
      );
    },
    totalPages() {
      return Math.max(1, Math.ceil(this.filteredGlossary.length / this.pageSize));
    },
    paginatedGlossary() {
      const start = (this.currentPage - 1) * this.pageSize;
      return this.filteredGlossary.slice(start, start + this.pageSize);
    }
  },
  watch: {
    search() {
      this.currentPage = 1;
    },
    glossary() {
      if (this.currentPage > this.totalPages) this.currentPage = this.totalPages;
    }
  },
  async mounted() {
    await this.fetchGlossary();
  },
  methods: {
    async fetchGlossary() {
      try {
        const token = localStorage.getItem('jwt');
        const res = await axios.get('/api/glossary', {
          headers: { Authorization: `Bearer ${token}` }
        });
        this.glossary = res.data;
      } catch (e) {
        this.error = 'Erro ao carregar o glossário.';
      }
    },
    async addEntry() {
      if (!this.newTerm.trim() || !this.newDefinition.trim()) {
        this.error = 'Preencha ambos os campos.';
        return;
      }
      try {
        const token = localStorage.getItem('jwt');
        const res = await axios.post('/api/glossary', {
          term: this.newTerm,
          definition: this.newDefinition
        }, {
          headers: { Authorization: `Bearer ${token}` }
        });
        this.glossary.push(res.data);
        this.newTerm = '';
        this.newDefinition = '';
        this.success = 'Termo adicionado!';
        this.error = '';
      } catch (e) {
        this.error = 'Erro ao adicionar termo.';
      }
    },
    startEdit(entry) {
      this.editId = entry.id;
      this.editTerm = entry.term;
      this.editDefinition = entry.definition;
      this.success = '';
      this.error = '';
    },
    async saveEdit(id) {
      if (!this.editTerm.trim() || !this.editDefinition.trim()) {
        this.error = 'Preencha ambos os campos.';
        return;
      }
      try {
        const token = localStorage.getItem('jwt');
        const res = await axios.put(`/api/glossary/${id}`, {
          term: this.editTerm,
          definition: this.editDefinition
        }, {
          headers: { Authorization: `Bearer ${token}` }
        });
        const idx = this.glossary.findIndex(e => e.id === id);
        if (idx !== -1) this.glossary[idx] = res.data;
        this.editId = null;
        this.editTerm = '';
        this.editDefinition = '';
        this.success = 'Termo atualizado!';
        this.error = '';
      } catch (e) {
        this.error = 'Erro ao atualizar termo.';
      }
    },
    cancelEdit() {
      this.editId = null;
      this.editTerm = '';
      this.editDefinition = '';
      this.success = '';
      this.error = '';
    },
    confirmDelete(id) {
      this.pendingDeleteId = id;
      this.alertTitle = 'Confirmar Eliminação';
      this.alertMessage = 'Tem certeza que deseja eliminar este termo?';
      this.alertType = 'confirm';
      this.showAlert = true;
    },
    async handleAlertConfirm() {
      if (this.pendingDeleteId !== null) {
        await this.deleteEntry(this.pendingDeleteId);
        this.pendingDeleteId = null;
      }
      this.showAlert = false;
    },
    handleAlertCancel() {
      this.pendingDeleteId = null;
      this.showAlert = false;
    },
    async deleteEntry(id) {
      try {
        const token = localStorage.getItem('jwt');
        await axios.delete(`/api/glossary/${id}`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        this.glossary = this.glossary.filter(e => e.id !== id);
        this.success = 'Termo eliminado!';
        this.error = '';
      } catch (e) {
        this.error = 'Erro ao eliminar termo.';
      }
    },
    triggerImport() {
      this.$refs.importFile.click();
    },
    async importFromJSON(event) {
      const file = event.target.files[0];
      if (!file) return;
      const reader = new FileReader();
      reader.onload = async (e) => {
        try {
          const imported = JSON.parse(e.target.result);
          if (!Array.isArray(imported)) throw new Error('Formato inválido.');
          const token = localStorage.getItem('jwt');
          const res = await axios.post('/api/glossary/import', imported, {
            headers: { Authorization: `Bearer ${token}` }
          });
          this.glossary = res.data;
          this.success = 'Glossário importado!';
          this.error = '';
        } catch (err) {
          this.error = 'Erro ao importar JSON.';
        }
      };
      reader.readAsText(file);
    },
    async exportGlossary() {
      try {
        const token = localStorage.getItem('jwt');
        const res = await axios.get('/api/glossary/export', {
          headers: { Authorization: `Bearer ${token}` }
        });
        const data = JSON.stringify(res.data, null, 2);
        const blob = new Blob([data], { type: 'application/json' });
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = 'glossary.json';
        link.click();
        URL.revokeObjectURL(link.href);
        this.success = 'Glossário exportado!';
        this.error = '';
      } catch (e) {
        this.error = 'Erro ao exportar JSON.';
      }
    },
    prevPage() {
      if (this.currentPage > 1) this.currentPage--;
    },
    nextPage() {
      if (this.currentPage < this.totalPages) this.currentPage++;
    },
    goToPage(page) {
      if (page >= 1 && page <= this.totalPages) this.currentPage = page;
    }
  }
};
</script>

<style scoped>
.input {
  @apply border border-blue-200 rounded px-2 py-1 focus:outline-none focus:ring-2 focus:ring-blue-400 transition text-base;
}
.action-btn {
  @apply px-3 py-2 rounded shadow flex items-center justify-center transition text-white focus:outline-none focus:ring-2 focus:ring-offset-2;
}
.icon-btn {
  @apply p-2 rounded-full shadow flex items-center justify-center transition text-white focus:outline-none focus:ring-2 focus:ring-offset-2;
}
.btn-edit {
  @apply px-4 py-2 rounded bg-blue-100 text-blue-700 hover:bg-blue-200 font-semibold flex items-center justify-center shadow transition focus:outline-none focus:ring-2 focus:ring-blue-300;
}
.btn-delete {
  @apply px-4 py-2 rounded bg-red-100 text-red-700 hover:bg-red-200 font-semibold flex items-center justify-center shadow transition focus:outline-none focus:ring-2 focus:ring-red-300;
}
</style> 