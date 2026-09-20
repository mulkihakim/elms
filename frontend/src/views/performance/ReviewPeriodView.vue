<script setup>
import { ref, onMounted } from 'vue'
import { usePerformanceStore } from '@/stores/performanceStore'
import { useNotify } from '@/composables/useNotify'
import BaseTable from '@/components/common/BaseTable.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import BaseInput from '@/components/common/BaseInput.vue'
import BaseModal from '@/components/common/BaseModal.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'

const performanceStore = usePerformanceStore()
const notify = useNotify()

const isModalOpen = ref(false)
const submitting = ref(false)

const form = ref({
  name: '',
  startDate: '',
  endDate: '',
})

const errors = ref({
  name: '',
  startDate: '',
  endDate: '',
})

const columns = [
  { field: 'name', header: 'Nama Periode', sortable: true },
  { field: 'startDate', header: 'Tanggal Mulai', sortable: true },
  { field: 'endDate', header: 'Tanggal Selesai', sortable: true },
  { field: 'status', header: 'Status' },
]

onMounted(() => {
  loadPeriods()
})

async function loadPeriods() {
  try {
    await performanceStore.fetchPeriods()
  } catch (err) {
    notify.showError(err.message || 'Gagal memuat daftar periode evaluasi')
  }
}

function openCreateModal() {
  form.value = {
    name: '',
    startDate: '',
    endDate: '',
  }
  errors.value = {
    name: '',
    startDate: '',
    endDate: '',
  }
  isModalOpen.value = true
}

function validateForm() {
  let valid = true
  errors.value = { name: '', startDate: '', endDate: '' }

  if (!form.value.name.trim()) {
    errors.value.name = 'Nama periode wajib diisi'
    valid = false
  }
  if (!form.value.startDate) {
    errors.value.startDate = 'Tanggal mulai wajib diisi'
    valid = false
  }
  if (!form.value.endDate) {
    errors.value.endDate = 'Tanggal selesai wajib diisi'
    valid = false
  }
  if (form.value.startDate && form.value.endDate && form.value.startDate > form.value.endDate) {
    errors.value.endDate = 'Tanggal selesai tidak boleh lebih awal dari tanggal mulai'
    valid = false
  }

  return valid
}

async function handleSubmit() {
  if (!validateForm()) return

  submitting.value = true
  try {
    await performanceStore.createPeriod({
      name: form.value.name.trim(),
      startDate: form.value.startDate,
      endDate: form.value.endDate,
    })
    notify.showSuccess('Periode evaluasi berhasil dibuat.')
    isModalOpen.value = false
  } catch (err) {
    notify.showError(err.message || 'Gagal membuat periode evaluasi')
  } finally {
    submitting.value = false
  }
}

function formatDate(val) {
  if (!val) return '-'
  return new Date(val).toLocaleDateString('id-ID', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  })
}
</script>

<template>
  <div class="space-y-6">
    <div class="bg-white rounded-xl border border-slate-200 p-5 shadow-xs">
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-4">
        <div>
          <h3 class="text-base font-semibold text-slate-800">Daftar Periode Evaluasi</h3>
          <p class="text-sm text-slate-500">Kelola jadwal periode evaluasi kinerja karyawan</p>
        </div>
        <BaseButton
          label="Buat Periode Baru"
          icon="pi pi-plus"
          size="small"
          @click="openCreateModal"
        />
      </div>

      <BaseTable
        :columns="columns"
        :value="performanceStore.periods"
        :loading="performanceStore.loading"
        :paginator="false"
      >
        <template #body-name="{ data }">
          <span class="font-semibold text-slate-800 text-sm">{{ data.name }}</span>
        </template>

        <template #body-startDate="{ data }">
          <span class="text-sm text-slate-600">{{ formatDate(data.startDate) }}</span>
        </template>

        <template #body-endDate="{ data }">
          <span class="text-sm text-slate-600">{{ formatDate(data.endDate) }}</span>
        </template>

        <template #body-status="{ data }">
          <StatusBadge :status="data.status" />
        </template>
      </BaseTable>
    </div>

    <!-- Modal Form Pembuatan Periode -->
    <BaseModal
      v-model="isModalOpen"
      title="Buat Periode Evaluasi Baru"
      @confirm="handleSubmit"
    >
      <form @submit.prevent="handleSubmit" class="space-y-4">
        <div>
          <BaseInput
            id="period-name"
            v-model="form.name"
            label="Nama Periode"
            placeholder="misal: Q1 2026 atau Semester 1 2026"
            :error="errors.name"
            required
          />
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1">
              Tanggal Mulai <span class="text-red-500">*</span>
            </label>
            <input
              v-model="form.startDate"
              type="date"
              class="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:outline-hidden focus:ring-2 focus:ring-indigo-500"
              required
            />
            <small v-if="errors.startDate" class="text-xs text-red-600 font-medium">
              {{ errors.startDate }}
            </small>
          </div>

          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1">
              Tanggal Selesai <span class="text-red-500">*</span>
            </label>
            <input
              v-model="form.endDate"
              type="date"
              :min="form.startDate"
              class="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:outline-hidden focus:ring-2 focus:ring-indigo-500"
              required
            />
            <small v-if="errors.endDate" class="text-xs text-red-600 font-medium">
              {{ errors.endDate }}
            </small>
          </div>
        </div>
      </form>

      <template #footer>
        <div class="flex justify-end gap-2">
          <BaseButton
            label="Batal"
            variant="secondary"
            outlined
            @click="isModalOpen = false"
          />
          <BaseButton
            label="Simpan Periode"
            icon="pi pi-check"
            :loading="submitting"
            @click="handleSubmit"
          />
        </div>
      </template>
    </BaseModal>
  </div>
</template>
