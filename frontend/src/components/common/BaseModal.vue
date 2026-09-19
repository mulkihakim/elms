<script setup>
import { computed } from 'vue'
import Dialog from 'primevue/dialog'

const props = defineProps({
  visible: {
    type: Boolean,
    default: undefined,
  },
  modelValue: {
    type: Boolean,
    default: undefined,
  },
  title: {
    type: String,
    default: '',
  },
  width: {
    type: String,
    default: '32rem',
  },
  modal: {
    type: Boolean,
    default: true,
  },
  closable: {
    type: Boolean,
    default: true,
  },
})

const emit = defineEmits(['update:visible', 'update:modelValue', 'hide'])

const isVisible = computed({
  get: () => (props.visible !== undefined ? props.visible : props.modelValue),
  set: (val) => {
    emit('update:visible', val)
    emit('update:modelValue', val)
  },
})
</script>

<template>
  <Dialog
    v-model:visible="isVisible"
    :header="title"
    :modal="modal"
    :closable="closable"
    :style="{ width: width, maxWidth: '95vw' }"
    v-bind="$attrs"
    @hide="$emit('hide')"
  >
    <div class="py-2">
      <slot />
    </div>

    <template v-if="$slots.footer" #footer>
      <div class="flex justify-end gap-2 pt-2 border-t border-slate-100">
        <slot name="footer" />
      </div>
    </template>
  </Dialog>
</template>
