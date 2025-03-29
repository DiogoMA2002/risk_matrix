<template>
    <div class="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50">
      <div class="bg-white p-6 rounded-lg shadow-lg max-w-sm w-full">
        <p class="text-gray-800">{{ message }}</p>
        <div class="mt-4 flex justify-end space-x-2">
          <button 
            class="px-4 py-2 rounded bg-gray-200 hover:bg-gray-300"
            @click="$emit('cancel')">
            Cancel
          </button>
          <button 
            class="px-4 py-2 rounded bg-blue-600 text-white hover:bg-blue-700"
            @click="$emit('confirm')">
            Confirm
          </button>
        </div>
      </div>
    </div>
  </template>
  
  <script>
  export default {
    name: "ConfirmDialog",
    props: {
      message: {
        type: String,
        required: true
      }
    }
  }
  </script>
  