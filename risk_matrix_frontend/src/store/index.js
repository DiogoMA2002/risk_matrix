import { createStore } from 'vuex'
import axios from 'axios'

export default createStore({
  state: {
    questions: [],
    questionnaires: [],
    selectedQuestionnaire: null,
    allAnswers: {},
    categories: [],          
    selectedQuestionnaireId: null,
    
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
      // If the category doesn’t exist, create it.
      if (!state.allAnswers[category]) {
        state.allAnswers[category] = {};
      }
      state.allAnswers[category][questionId] = answer;
    },
    clearAllAnswers(state) {
      state.allAnswers = {};
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
    }
  },
})
