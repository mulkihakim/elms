<script setup>
import { computed } from 'vue'
import Tag from 'primevue/tag'

const props = defineProps({
  status: {
    type: String,
    required: true,
  },
  value: {
    type: String,
    default: undefined,
  },
})

const severity = computed(() => {
  const s = props.status ? props.status.toUpperCase() : ''
  switch (s) {
    case 'ACTIVE':
    case 'APPROVED':
    case 'ON_TIME':
      return 'success'
    case 'PENDING':
    case 'LATE':
    case 'ON_LEAVE':
      return 'warn'
    case 'REJECTED':
    case 'ABSENT':
    case 'TERMINATED':
    case 'RESIGNED':
      return 'danger'
    default:
      return 'info'
  }
})

const displayValue = computed(() => props.value || props.status)
</script>

<template>
  <Tag :severity="severity" :value="displayValue" class="text-xs font-semibold px-2 py-0.5" />
</template>
