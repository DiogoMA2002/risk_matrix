<template>
  <div class="animate-pulse">
    <div class="h-8 bg-gray-300 rounded w-1/2 mb-6"></div>
    <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
      <div v-for="n in count" :key="n" class="h-32 bg-gray-200 rounded-lg"></div>
    </div>
  </div>
</template>

<script>
export default {
  name: "SkeletonLoader",
  props: {
    count: {
      type: Number,
      default: 6
    }
  }
};
</script>
