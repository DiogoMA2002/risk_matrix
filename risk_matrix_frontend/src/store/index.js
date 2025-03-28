import { createStore } from 'vuex'
import axios from 'axios'

export default createStore({
  state: {
    questions: [],
    questionnaires: [],
    selectedQuestionnaire: null,
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
  },
  actions: {
    fetchQuestions({ commit }) {
      axios.get('/api/questions/all')
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
    fetchQuestionnaireById({ commit }, id) {
      return axios.get(`/api/questionnaires/${id}`)
        .then(response => {
          commit('setSelectedQuestionnaire', response.data)
        })
        .catch(error => console.error('Erro ao buscar questionário:', error))
    },
  },
})
