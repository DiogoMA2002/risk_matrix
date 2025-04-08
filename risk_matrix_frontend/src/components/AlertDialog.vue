<template>
  <div v-if="show" class="fixed inset-0 bg-white bg-opacity-20 backdrop-blur-sm flex items-center justify-center z-50">
    <div class="bg-white bg-opacity-90 backdrop-blur-sm rounded-xl shadow-xl max-w-md w-full mx-4">
      <div class="p-6">
        <h3 class="text-xl font-semibold text-blue-800 mb-2 flex items-center">
          <svg v-if="type === 'success'" xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
          </svg>
          <svg v-else-if="type === 'error'" xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
          <svg v-else xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          {{ title }}
        </h3>
        <p class="text-gray-600">{{ message }}</p>
      </div>
      <div class="bg-gray-50 px-6 py-4 rounded-b-lg flex justify-end space-x-3">
        <button v-if="type === 'confirm'" 
          @click="handleCancel"
          class="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-all duration-300">
          Cancelar
        </button>
        <button 
  @click="handleConfirm"
  :class="[
    'px-4 py-2 text-sm font-medium text-white rounded-lg focus:outline-none focus:ring-2 focus:ring-offset-2 transition-all duration-300',
    {
      'bg-blue-600 hover:bg-blue-700 focus:ring-blue-500': type === 'confirm' || type === 'info',
      'bg-green-600 hover:bg-green-700 focus:ring-green-500': type === 'success',
      'bg-red-600 hover:bg-red-700 focus:ring-red-500': type === 'error'
    }
  ]">
  {{ ['success', 'error', 'info'].includes(type) ? 'OK' : 'Confirmar' }}
</button>

      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AlertDialog',
  props: {
    show: {
      type: Boolean,
      required: true
    },
    title: {
      type: String,
      default: 'Aviso'
    },
    message: {
      type: String,
      required: true
    },
    type: {
      type: String,
      default: 'info',
      validator: value => ['info', 'confirm', 'success', 'error'].includes(value)
    }
  },
  methods: {
    handleConfirm() {
      this.$emit('confirm');
    },
    handleCancel() {
      this.$emit('cancel');
    }
  }
}
</script> 