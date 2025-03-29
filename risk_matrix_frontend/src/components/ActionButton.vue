<template>
    <button 
      :class="`px-6 py-3 bg-${color}-600 text-white rounded-lg shadow-md hover:bg-${color}-700 transition-all duration-300 flex items-center`"
      @click="$emit('click')">
      <span>{{ text }}</span>
      <svg v-if="icon === 'send'" xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-2" fill="none" viewBox="0 0 24 24"
        stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 5l7 7-7 7M5 5l7 7-7 7" />
      </svg>
      <svg v-else-if="icon === 'export'" xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-2" fill="none" viewBox="0 0 24 24"
        stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
          d="M4 16v1a2 2 0 002 2h12a2 2 0 002-2v-1M8 12l4 4 4-4M12 4v12" />
      </svg>
      <svg v-else-if="icon === 'import'" xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-2" fill="none" viewBox="0 0 24 24"
        stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
          d="M12 4v12m0 0l-4-4m4 4l4-4M20 12a8 8 0 11-16 0 8 8 0 0116 0z" />
      </svg>
    </button>
  </template>
  
  <script>
  export default {
    name: "ActionButton",
    props: {
      text: {
        type: String,
        required: true
      },
      icon: {
        type: String,
        required: true,
        validator: function(value) {
          return ['send', 'export', 'import'].includes(value)
        }
      },
      color: {
        type: String,
        default: "blue",
        validator: function(value) {
          return ['blue', 'green', 'orange', 'red'].includes(value)
        }
      }
    }
  }
  </script>