import { createStore } from 'vuex'
import axios from 'axios'

export default createStore({
  state: {
    questions: [],
    questionnaires: [],
    selectedQuestionnaire: null,
    allAnswers: {}
  },
  mutations: {
    setQuestions(state, questions) {
      state.questions = questions
    },
    setQuestionnaires(state, questionnaires) {
      state.questionnaires = questionnaires
    },
    setSelectedQuestionnaire(state, questionnaire) {
      state.selectedQuestionnaire = questionnaire
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
      return axios.get('/api/questions/all')
        .then(response => commit('setQuestions', response.data))
        .catch(error => console.error('Erro ao buscar perguntas:', error))
    },
    fetchQuestionnaires({ commit }) {
      return axios.get('/api/questionnaires/all')
        .then(response => {
          commit('setQuestionnaires', response.data)
          if (response.data.length > 0) {
            commit('setSelectedQuestionnaire', response.data[0])
          }
        })
        .catch(error => console.error('Erro ao buscar questionários:', error))
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
