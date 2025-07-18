import { createStore } from 'vuex'
import axios from 'axios'
/* eslint-disable */
export default createStore({
  state: {
    questions: [],
    questionnaires: [],
    selectedQuestionnaire: null,
    allAnswers: {},
    categories: [],          
    selectedQuestionnaireId: null,
    userAnswers: [],
    isLoadingAnswers: false,
    error: null, // global error state
  },
  mutations: {
    setQuestions(state, questions) {
      state.questions = questions
    },
    setQuestionnaires(state, questionnaires) {
      state.questionnaires = questionnaires
    },
    setCategories(state, categories) {        
      state.categories = categories
    },
    setSelectedQuestionnaire(state, questionnaire) {
      state.selectedQuestionnaire = questionnaire
    },
    setSelectedQuestionnaireId(state, id) {
      state.selectedQuestionnaireId = id;
    },
    setAllAnswers(state, answers) {
      state.allAnswers = answers;
    },
    updateAnswer(state, { category, questionId, answer }) {
      // If the category doesn't exist, create it.
      if (!state.allAnswers[category]) {
        state.allAnswers[category] = {};
      }
      state.allAnswers[category][questionId] = answer;
    },
    clearAllAnswers(state) {
      state.allAnswers = {};
    },
    setUserAnswers(state, answers) {
      state.userAnswers = answers;
    },
    setLoadingAnswers(state, isLoading) {
      state.isLoadingAnswers = isLoading;
    },
    setError(state, error) {
      state.error = error;
    },
    clearError(state) {
      state.error = null;
    }
  },
  actions: {
    async fetchQuestions({ commit }) {
      try {
        const response = await axios.get('/api/questions');
        commit('setQuestions', response.data);
        commit('clearError');
      } catch (error) {
        commit('setError', 'Erro ao buscar perguntas.');
      }
    },
    async fetchQuestionnaires({ commit }) {
      try {
        const response = await axios.get('/api/questionnaires');
        commit('setQuestionnaires', response.data);
        if (response.data.length > 0) {
          commit('setSelectedQuestionnaire', response.data[0]);
        }
        commit('clearError');
      } catch (error) {
        commit('setError', 'Erro ao buscar questionários.');
      }
    },
    async fetchQuestionnairesForAdmin({ commit }) {
      try {
        const response = await axios.get('/api/questionnaires/admin');
        commit('setQuestionnaires', response.data);
        if (response.data.length > 0) {
          commit('setSelectedQuestionnaire', response.data[0]);
        }
        commit('clearError');
      } catch (error) {
        commit('setError', 'Erro ao buscar questionários para admin.');
      }
    },
    async fetchCategories({ commit }) {                
      try {
        const response = await axios.get('/api/categories');
        commit('setCategories', response.data.filter(cat => cat.name));
        commit('clearError');
      } catch (error) {
        commit('setError', 'Erro ao buscar categorias.');
      }
    },
    async fetchQuestionnaireById({ commit }, id) {
      try {
        const response = await axios.get(`/api/questionnaires/${id}`);
        commit('setSelectedQuestionnaire', response.data);
        commit('clearError');
      } catch (error) {
        commit('setError', 'Erro ao buscar questionário.');
      }
    },
    async fetchQuestionnaireByIdForAdmin({ commit }, id) {
      try {
        const response = await axios.get(`/api/questionnaires/${id}`);
        commit('setSelectedQuestionnaire', response.data);
        commit('clearError');
      } catch (error) {
        commit('setError', 'Erro ao buscar questionário para admin.');
      }
    },
    async fetchUserAnswersByEmail({ commit }, email) {
      commit('setLoadingAnswers', true);
      try {
        const response = await axios.get(`/api/answers/by-email-with-severity/${email}`, {
          validateStatus: _status => true,
        });

        if (response.status === 401) {
          throw new Error("Você não está autorizado.");
        } else if (response.status >= 200 && response.status < 300) {
          if (Array.isArray(response.data)) {
            commit('setUserAnswers', response.data);
          } else {
            commit('setUserAnswers', response.data ? [response.data] : []);
          }
        } else {
          throw new Error(`Erro ${response.status} ao buscar respostas por email.`);
        }
        commit('clearError');
      } catch (error) {
        commit('setUserAnswers', []);
        commit('setError', error.message || 'Erro ao buscar respostas por email.');
        throw error;
      } finally {
        commit('setLoadingAnswers', false);
      }
    },
    async fetchAllUserAnswers({ commit }) {
      commit('setLoadingAnswers', true);
      try {
        const response = await axios.get("/api/answers/get-all-submissions", {
          validateStatus: _status => true,
        });
        
        if (response.status === 401) {
          throw new Error("Você não está autorizado.");
        } else {
          commit('setUserAnswers', response.data);
        }
        commit('clearError');
      } catch (error) {
        commit('setUserAnswers', []);
        commit('setError', error.message || 'Erro ao buscar todas as respostas.');
        throw error;
      } finally {
        commit('setLoadingAnswers', false);
      }
    },
    async filterAnswersByDate({ commit }, { startDate, endDate }) {
      if (!startDate || !endDate) {
        commit('setError', 'Por favor, selecione ambas as datas.');
        throw new Error("Por favor, selecione ambas as datas.");
      }
      
      commit('setLoadingAnswers', true);
      try {
        const response = await axios.get("/api/answers/by-date-range", {
          params: { startDate, endDate },
        });
        commit('setUserAnswers', response.data);
        commit('clearError');
      } catch (error) {
        commit('setUserAnswers', []);
        commit('setError', error.message || 'Erro ao filtrar respostas por data.');
        throw error;
      } finally {
        commit('setLoadingAnswers', false);
      }
    }
  },
})
