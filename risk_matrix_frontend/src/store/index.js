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
    }
  },
  actions: {
    fetchQuestions({ commit }) {
      return axios.get('/api/questions')
        .then(response => commit('setQuestions', response.data))
        .catch(error => console.error('Erro ao buscar perguntas:', error))
    },
    fetchQuestionnaires({ commit }) {
      return axios.get('/api/questionnaires')
        .then(response => {
          commit('setQuestionnaires', response.data)
          if (response.data.length > 0) {
            commit('setSelectedQuestionnaire', response.data[0])
          }
        })
        .catch(error => console.error('Erro ao buscar questionários:', error))
    },
    fetchQuestionnairesForAdmin({ commit }) {
      const token = localStorage.getItem("jwt");
      return axios.get('/api/questionnaires/admin', {
        headers: { Authorization: `Bearer ${token}` }
      })
        .then(response => {
          commit('setQuestionnaires', response.data)
          if (response.data.length > 0) {
            commit('setSelectedQuestionnaire', response.data[0])
          }
        })
        .catch(error => console.error('Erro ao buscar questionários para admin:', error))
    },
    fetchCategories({ commit }) {                
      return axios.get('/api/categories')
        .then(response => {
          commit('setCategories', response.data.filter(cat => cat.name))
        })
        .catch(error => console.error('Erro ao buscar categorias:', error))
    },
    async fetchQuestionnaireById({ commit }, id) {
      try {
        const response = await axios.get(`/api/questionnaires/${id}`);
        commit('setSelectedQuestionnaire', response.data);
      } catch (error) {
        console.error('Erro ao buscar questionário:', error);
      }
    },
    async fetchQuestionnaireByIdForAdmin({ commit }, id) {
      try {
        const token = localStorage.getItem("jwt");
        const response = await axios.get(`/api/questionnaires/${id}`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        commit('setSelectedQuestionnaire', response.data);
      } catch (error) {
        console.error('Erro ao buscar questionário para admin:', error);
      }
    },
    async fetchUserAnswersByEmail({ commit }, email) {
      commit('setLoadingAnswers', true);
      try {
        const token = localStorage.getItem("jwt");
        const response = await axios.get(`/api/answers/by-email-with-severity/${email}`, {
          validateStatus: _status => true,
          headers: {
            Authorization: `Bearer ${token}`
          },
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
      } catch (error) {
        console.error("Error fetching user answers by email:", error);
        commit('setUserAnswers', []);
        throw error;
      } finally {
        commit('setLoadingAnswers', false);
      }
    },
    async fetchAllUserAnswers({ commit }) {
      commit('setLoadingAnswers', true);
      try {
        const token = localStorage.getItem("jwt");
        const response = await axios.get("/api/answers/get-all-submissions", {
          validateStatus: _status => true,
          headers: {
            Authorization: `Bearer ${token}`
          },
        });
        
        if (response.status === 401) {
          throw new Error("Você não está autorizado.");
        } else {
          commit('setUserAnswers', response.data);
        }
      } catch (error) {
        console.error("Error fetching all user answers:", error);
        commit('setUserAnswers', []);
        throw error;
      } finally {
        commit('setLoadingAnswers', false);
      }
    },
    async filterAnswersByDate({ commit }, { startDate, endDate }) {
      if (!startDate || !endDate) {
        throw new Error("Por favor, selecione ambas as datas.");
      }
      
      commit('setLoadingAnswers', true);
      try {
        const token = localStorage.getItem("jwt");
        const response = await axios.get("/api/answers/by-date-range", {
          params: { startDate, endDate },
          headers: {
            Authorization: `Bearer ${token}`
          },
        });
        commit('setUserAnswers', response.data);
      } catch (error) {
        console.error("Error filtering answers by date:", error);
        commit('setUserAnswers', []);
        throw error;
      } finally {
        commit('setLoadingAnswers', false);
      }
    }
  },
})
