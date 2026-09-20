<script setup>
import Chart from 'primevue/chart'

defineProps({
  type: {
    type: String,
    required: true,
  },
  data: {
    type: Object,
    required: true,
  },
  options: {
    type: Object,
    default: () => ({}),
  },
  height: {
    type: String,
    default: '300px',
  },
})
</script>

<template>
  <div class="relative w-full flex items-center justify-center" :style="{ height }">
    <Chart :type="type" :data="data" :options="options" class="h-full w-full" />
  </div>
</template>
