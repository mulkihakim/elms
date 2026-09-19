<script setup>
import { computed } from 'vue'
import InputText from 'primevue/inputtext'

const props = defineProps({
  modelValue: {
    type: [String, Number],
    default: '',
  },
  id: {
    type: String,
    default: () => `input-${Math.random().toString(36).substring(2, 9)}`,
  },
  label: {
    type: String,
    default: undefined,
  },
  placeholder: {
    type: String,
    default: '',
  },
  type: {
    type: String,
    default: 'text',
  },
  disabled: {
    type: Boolean,
    default: false,
  },
  required: {
    type: Boolean,
    default: false,
  },
  error: {
    type: String,
    default: undefined,
  },
  helpText: {
    type: String,
    default: undefined,
  },
})

const emit = defineEmits(['update:modelValue', 'blur', 'focus'])

const inputValue = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
})
</script>

<template>
  <div class="flex flex-col gap-1.5 w-full">
    <label
      v-if="label"
      :for="id"
      class="text-sm font-medium text-slate-700 dark:text-slate-200"
    >
      {{ label }}
      <span v-if="required" class="text-red-500">*</span>
    </label>

    <InputText
      :id="id"
      v-model="inputValue"
      :type="type"
      :placeholder="placeholder"
      :disabled="disabled"
      :invalid="!!error"
      class="w-full"
      v-bind="$attrs"
      @blur="$emit('blur', $event)"
      @focus="$emit('focus', $event)"
    />

    <small v-if="error" class="text-xs text-red-600 font-medium">
      {{ error }}
    </small>
    <small v-else-if="helpText" class="text-xs text-slate-500">
      {{ helpText }}
    </small>
  </div>
</template>
