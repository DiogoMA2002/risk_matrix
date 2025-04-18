<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-600 to-indigo-100 font-sans flex items-center justify-center px-4 py-12">
    <div class="bg-white bg-opacity-90 backdrop-blur-md rounded-xl shadow-xl p-8 w-full max-w-md">
      
      <!-- Header -->
      <div class="flex items-center mb-6">
        <button @click="goBack" class="p-2 rounded-full bg-white bg-opacity-20 backdrop-blur-sm text-blue-600 hover:bg-opacity-30 transition-all duration-300 mr-4">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
          </svg>
        </button>
        <h1 class="text-2xl font-bold text-blue-600">Feedback</h1>
      </div>

      <!-- Feedback Form -->
      <form @submit.prevent="submitFeedback" class="space-y-6">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
          <input
            v-model="email"
            type="email"
            placeholder="email@email.com"
            class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-300"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Tipo</label>
          <select
            v-model="feedbackType"
            class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-300"
          >
            <option disabled value="">Selecione o tipo</option>
            <option value="SUGGESTION">Sugestão</option>
            <option value="HELP">Ajuda</option>
          </select>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Mensagem</label>
          <textarea
            v-model="userFeedback"
            placeholder="Digite sua mensagem"
            class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-300"
            rows="4"
          ></textarea>
          <p class="text-xs text-gray-400 mt-1">{{ feedbackWordCount }}/250 palavras</p>
        </div>

        <p v-if="error" class="text-red-600 text-sm">{{ error }}</p>

        <button
          type="submit"
          :disabled="submitting"
          class="w-full px-4 py-3 bg-blue-600 text-white rounded-lg font-medium transition-all duration-300 hover:bg-blue-700 disabled:opacity-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 shadow-md hover:shadow-lg"
        >
          {{ submitting ? 'Enviando...' : 'Enviar Feedback' }}
        </button>
      </form>
    </div>

    <!-- Confirmation dialog -->
    <ConfirmDialog
      v-if="showSuccessDialog"
      message="Feedback enviado com sucesso!"
      @confirm="handleDialogConfirm"
      @cancel="handleDialogConfirm"
    />
  </div>
</template>

<script>
import axios from "axios";
import ConfirmDialog from "../Static/ConfirmDialogue.vue";

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const MAX_WORDS = 250;

export default {
  name: "FeedbackForm",
  components: {
    ConfirmDialog,
  },
  data() {
    return {
      email: "",
      feedbackType: "",
      userFeedback: "",
      showSuccessDialog: false,
      error: null,
      submitting: false,
    };
  },
  computed: {
    feedbackWordCount() {
      return this.userFeedback.trim().split(/\s+/).filter(Boolean).length;
    },
  },
  methods: {
    goBack() {
      if (window.history.length > 1) this.$router.go(-1);
      else this.$router.push('/');
    },
    async submitFeedback() {
      this.error = null;

      // Basic required check
      if (!this.email || !this.feedbackType || !this.userFeedback) {
        this.error = "Por favor, preencha todos os campos.";
        return;
      }

      // Email validation
      if (!EMAIL_REGEX.test(this.email.trim())) {
        this.error = "Informe um email válido.";
        return;
      }

      // Word count validation
      if (this.feedbackWordCount > MAX_WORDS) {
        this.error = `A sua mensagem excede o limite de ${MAX_WORDS} palavras.`;
        return;
      }

      try {
        this.submitting = true;

        await axios.post("/api/feedback", {
          email: this.email.trim(),
          feedbackType: this.feedbackType,
          userFeedback: this.userFeedback.trim(),
        });

        this.email = "";
        this.feedbackType = "";
        this.userFeedback = "";
        this.showSuccessDialog = true;
      } catch (err) {
        console.error("Erro ao enviar feedback:", err);
        this.error = "Falha ao enviar feedback. Tente novamente.";
      } finally {
        this.submitting = false;
      }
    },
    handleDialogConfirm() {
      this.showSuccessDialog = false;
      this.$router.push("/");
    },
  },
};
</script>
