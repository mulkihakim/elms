<script setup>
import { computed } from 'vue'
import Select from 'primevue/select'

const props = defineProps({
  modelValue: {
    type: [String, Number, Object],
    default: null,
  },
  options: {
    type: Array,
    default: () => [],
  },
  optionLabel: {
    type: String,
    default: 'name',
  },
  optionValue: {
    type: String,
    default: 'id',
  },
  placeholder: {
    type: String,
    default: 'Pilih opsi',
  },
  id: {
    type: String,
    default: () => `select-${Math.random().toString(36).substring(2, 9)}`,
  },
  label: {
    type: String,
    default: undefined,
  },
  required: {
    type: Boolean,
    default: false,
  },
  error: {
    type: String,
    default: undefined,
  },
  loading: {
    type: Boolean,
    default: false,
  },
  disabled: {
    type: Boolean,
    default: false,
  },
  filter: {
    type: Boolean,
    default: false,
  },
  showClear: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['update:modelValue', 'change'])

const selectedValue = computed({
  get: () => props.modelValue,
  set: (val) => {
    emit('update:modelValue', val)
    emit('change', val)
  },
})
</script>

<template>
  <div class="flex flex-col gap-1.5 w-full">
    <label v-if="label" :for="id" class="text-sm font-medium text-slate-700">
      {{ label }}
      <span v-if="required" class="text-red-500">*</span>
    </label>

    <Select
      :id="id"
      v-model="selectedValue"
      :options="options"
      :option-label="optionLabel"
      :option-value="optionValue"
      :placeholder="placeholder"
      :loading="loading"
      :disabled="disabled"
      :filter="filter"
      :show-clear="showClear"
      :invalid="!!error"
      class="w-full"
      v-bind="$attrs"
    />

    <small v-if="error" class="text-xs text-red-600 font-medium">
      {{ error }}
    </small>
  </div>
</template>
