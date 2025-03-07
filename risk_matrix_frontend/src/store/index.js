import Vue from 'vue';
import Vuex from 'vuex';
import axios from 'axios';

Vue.use(Vuex);

export default new Vuex.Store({
  state: {
    questions: [],
  },
  mutations: {
    setQuestions(state, questions) {
      state.questions = questions;
    },
  },
  actions: {
    fetchQuestions({ commit }) {
      axios.get('/api/questions/all')
        .then(response => { commit('setQuestions', response.data); })
        .catch(error => { console.error("Erro ao buscar perguntas:", error); });
    },
  },
});
