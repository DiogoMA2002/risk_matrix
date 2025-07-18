<template>
  <div>
    <router-view />
    <AlertDialog
      v-if="$store.state.error"
      :show="$store.state.error !== null"
      :title="'Erro'"
      :message="$store.state.error"
      type="error"
      @confirm="$store.commit('clearError')"
      @cancel="$store.commit('clearError')"
    />
  </div>
</template>

<script>
import AlertDialog from '@/components/Static/AlertDialog.vue';
export default {
  name: 'App',
  components: { AlertDialog }
}
</script>
