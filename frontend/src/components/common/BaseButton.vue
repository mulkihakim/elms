<script setup>
import { computed } from 'vue'
import Button from 'primevue/button'

const props = defineProps({
  label: {
    type: String,
    default: undefined,
  },
  icon: {
    type: String,
    default: undefined,
  },
  iconPos: {
    type: String,
    default: 'left',
  },
  variant: {
    type: String,
    default: 'primary', // 'primary' | 'secondary' | 'danger' | 'success' | 'warning' | 'info'
    validator: (v) => ['primary', 'secondary', 'danger', 'success', 'warning', 'info'].includes(v),
  },
  loading: {
    type: Boolean,
    default: false,
  },
  disabled: {
    type: Boolean,
    default: false,
  },
  size: {
    type: String,
    default: undefined, // 'small' | 'large'
  },
  type: {
    type: String,
    default: 'button',
  },
  outlined: {
    type: Boolean,
    default: false,
  },
  text: {
    type: Boolean,
    default: false,
  },
  rounded: {
    type: Boolean,
    default: false,
  },
})

defineEmits(['click'])

// Mapping variant proyek ke severity PrimeVue
const severity = computed(() => {
  switch (props.variant) {
    case 'secondary':
      return 'secondary'
    case 'danger':
      return 'danger'
    case 'success':
      return 'success'
    case 'warning':
      return 'warn'
    case 'info':
      return 'info'
    case 'primary':
    default:
      return undefined // default primevue theme primary (indigo)
  }
})
</script>

<template>
  <Button
    :label="label"
    :icon="icon"
    :icon-pos="iconPos"
    :severity="severity"
    :loading="loading"
    :disabled="disabled"
    :size="size"
    :type="type"
    :outlined="outlined"
    :text="text"
    :rounded="rounded"
    v-bind="$attrs"
    @click="$emit('click', $event)"
  >
    <slot />
  </Button>
</template>
