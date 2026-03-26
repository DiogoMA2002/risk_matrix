<template>
  <button
    :class="[
      'px-6 py-3 text-white rounded-lg shadow-md transition-all duration-300 flex items-center',
      colorClasses
    ]"
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
  },
  computed: {
    colorClasses() {
      const map = {
        blue:   'bg-blue-600 hover:bg-blue-700 focus:ring-2 focus:ring-blue-400',
        green:  'bg-green-600 hover:bg-green-700 focus:ring-2 focus:ring-green-400',
        orange: 'bg-orange-500 hover:bg-orange-600 focus:ring-2 focus:ring-orange-400',
        red:    'bg-red-600 hover:bg-red-700 focus:ring-2 focus:ring-red-400',
      }
      return map[this.color] || map.blue
    }
  }
}
</script>
