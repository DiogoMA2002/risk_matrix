<template>
    <div class="flex items-center">
      <div class="w-8 h-8 rounded-full bg-blue-600 flex items-center justify-center text-white font-medium">
        {{ number }}
      </div>
      <div class="ml-2 text-white">{{ text }}</div>
    </div>
  </template>
  
  <script>
  export default {
    name: "ProgressStep",
    props: {
      number: {
        type: String,
        required: true
      },
      text: {
        type: String,
        required: true
      }
    }
  }
  </script>