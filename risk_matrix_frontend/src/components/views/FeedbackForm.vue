<template>
  <div
    class="min-h-screen bg-gradient-to-br from-blue-600 to-indigo-100 font-sans flex items-center justify-center px-4 py-12">
    <div class="bg-white bg-opacity-90 backdrop-blur-md rounded-xl shadow-xl p-8 w-full max-w-md">
      <!-- Cabeçalho com botão de voltar -->
      <div class="flex items-center mb-6">
        <button @click="$router.go(-1)"
          class="p-2 rounded-full bg-white bg-opacity-20 backdrop-blur-sm text-blue-600 hover:bg-opacity-30 transition-all duration-300 mr-4">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
          </svg>
        </button>
        <h1 class="text-2xl font-bold text-blue-600">Feedback</h1>
      </div>
      <!-- Formulário de Feedback -->
      <form @submit.prevent="submitFeedback" class="space-y-6">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
          <input v-model="email" type="email" placeholder="email@email.com"
            class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-300" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Tipo</label>
          <select v-model="feedbackType"
            class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-300">
            <option disabled value="">Selecione o tipo</option>
            <option value="SUGGESTION">Sugestão</option>
            <option value="HELP">Ajuda</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Mensagem</label>
          <textarea v-model="userFeedback" placeholder="Digite sua mensagem"
            class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-300"
            rows="4"></textarea>
        </div>
        <button type="submit"
          class="w-full px-4 py-3 bg-blue-600 text-white rounded-lg font-medium transition-all duration-300 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 shadow-md hover:shadow-lg">
          Enviar Feedback
        </button>
      </form>
    </div>
  </div>
  <ConfirmDialog v-if="showSuccessDialog" message="Feedback enviado com sucesso!" @confirm="handleDialogConfirm"
    @cancel="handleDialogConfirm" />

</template>

<script>
import axios from "axios";
import ConfirmDialog from "../Static/ConfirmDialogue.vue";

export default {
  components: {
    ConfirmDialog,
  },
  name: "FeedbackForm",
  data() {

    return {
      email: "",
      feedbackType: "",
      userFeedback: "",
      showSuccessDialog: false,

    };
  },
  methods: {
    async submitFeedback() {
      // Validação básica
      if (!this.email || !this.feedbackType || !this.userFeedback) {
        alert("Por favor, preencha todos os campos.");
        return;
      }

      // Validação de email
      const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!re.test(String(this.email).toLowerCase())) {
        alert("Informe um email válido.");
        return;
      }

      // Limite de palavras no frontend (por exemplo, 250)
      const wordCount = this.userFeedback.trim().split(/\s+/).length;
      if (wordCount > 250) {
        alert("A sua mensagem excede o limite de 250 palavras.");
        return;
      }

      try {
        await axios.post("/api/feedback", {
          email: this.email,
          feedbackType: this.feedbackType,
          userFeedback: this.userFeedback,
        });

        // Clear form but show dialog instead of alert
        this.email = "";
        this.feedbackType = "";
        this.userFeedback = "";
        this.showSuccessDialog = true;

      } catch (error) {
        console.error("Erro ao enviar feedback:", error);
        alert("Falha ao enviar feedback. Tente novamente.");
      }

    },
    handleDialogConfirm() {
      this.showSuccessDialog = false;
      this.$router.push("/");
    }

  }
};
</script>