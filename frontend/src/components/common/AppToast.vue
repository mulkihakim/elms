<script setup>
import Toast from 'primevue/toast'

defineProps({
  position: {
    type: String,
    default: 'top-right',
  },
})
</script>

<template>
  <Toast :position="position" />
</template>
