<!-- SkeletonLoader.vue -->
<template>
    <div class="animate-pulse">
      <!-- Header placeholder -->
      <div class="h-8 bg-gray-300 rounded w-1/2 mb-6"></div>
      <!-- Grid placeholder for categories -->
      <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
        <!-- Repeat a placeholder for each category item -->
        <div v-for="n in 6" :key="n" class="h-32 bg-gray-200 rounded-lg"></div>
      </div>
    </div>
  </template>
  
  <script>
  export default {
    name: "SkeletonLoader",
  };
  </script>
  
  <style scoped>
  /* Optionally add more styling if needed */
  </style>
  