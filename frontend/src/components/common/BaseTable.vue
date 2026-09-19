<script setup>
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'

defineProps({
  value: {
    type: Array,
    default: () => [],
  },
  columns: {
    type: Array,
    default: () => [],
  },
  loading: {
    type: Boolean,
    default: false,
  },
  paginator: {
    type: Boolean,
    default: true,
  },
  rows: {
    type: Number,
    default: 10,
  },
  totalRecords: {
    type: Number,
    default: 0,
  },
  lazy: {
    type: Boolean,
    default: false,
  },
  emptyMessage: {
    type: String,
    default: 'Tidak ada data yang ditemukan.',
  },
  stripedRows: {
    type: Boolean,
    default: true,
  },
  rowHover: {
    type: Boolean,
    default: true,
  },
})

defineEmits(['page', 'sort'])
</script>

<template>
  <div class="bg-white rounded-lg border border-slate-200 shadow-xs overflow-hidden">
    <DataTable
      :value="value"
      :loading="loading"
      :paginator="paginator"
      :rows="rows"
      :total-records="totalRecords"
      :lazy="lazy"
      :striped-rows="stripedRows"
      :row-hover="rowHover"
      responsive-layout="scroll"
      class="p-datatable-sm"
      @page="$emit('page', $event)"
      @sort="$emit('sort', $event)"
    >
      <template #empty>
        <div class="text-center py-8 text-slate-500 text-sm">
          <i class="pi pi-inbox text-3xl text-slate-400 mb-2 block"></i>
          {{ emptyMessage }}
        </div>
      </template>

      <template #loading>
        <div class="text-center py-6 text-slate-500 text-sm">
          <i class="pi pi-spin pi-spinner text-2xl text-indigo-600 mb-2 block"></i>
          Memuat data...
        </div>
      </template>

      <!-- Kolom-kolom data terkonfigurasi -->
      <Column
        v-for="col in columns"
        :key="col.field"
        :field="col.field"
        :header="col.header"
        :sortable="col.sortable || false"
        :style="col.style"
        :class="col.class"
      >
        <template v-if="$slots[`body-${col.field}`]" #body="slotProps">
          <slot :name="`body-${col.field}`" v-bind="slotProps" />
        </template>
      </Column>

      <!-- Kolom Slot Aksi Khusus -->
      <Column v-if="$slots.actions" header="Aksi" :style="{ width: '130px', textAlign: 'center' }">
        <template #body="slotProps">
          <div class="flex items-center justify-center gap-1.5">
            <slot name="actions" v-bind="slotProps" />
          </div>
        </template>
      </Column>
    </DataTable>
  </div>
</template>
