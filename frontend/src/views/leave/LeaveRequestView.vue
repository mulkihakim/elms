<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useAuthStore } from '@/stores/authStore'
import { useLeaveStore } from '@/stores/leaveStore'
import { useNotify } from '@/composables/useNotify'
import { LEAVE_STATUS_OPTIONS, LEAVE_TYPE_OPTIONS } from '@/utils/constants'
import BaseTable from '@/components/common/BaseTable.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import BaseSelect from '@/components/common/BaseSelect.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'

const authStore = useAuthStore()
const leaveStore = useLeaveStore()
const notify = useNotify()

// State Form Pengajuan Cuti
const startDate = ref('')
const endDate = ref('')
const leaveType = ref('ANNUAL')
const reason = ref('')
const submitting = ref(false)

// State Filter Riwayat Cuti
const selectedStatus = ref(null)
const page = ref(0)
const pageSize = ref(10)

// Kolom Tabel Riwayat Cuti
const columns = [
  { field: 'createdAt', header: 'Tanggal Pengajuan', sortable: true },
  { field: 'dateRange', header: 'Rentang Tanggal' },
  { field: 'requestedDays', header: 'Durasi' },
  { field: 'leaveType', header: 'Jenis Cuti' },
  { field: 'reason', header: 'Alasan' },
  { field: 'status', header: 'Status' },
  { field: 'approvedByName', header: 'Diputuskan Oleh' },
]

// Saldo Cuti Karyawan
const leaveBalance = computed(() => {
  return authStore.user?.leaveBalance ?? 0
})

// Dapatkan string tanggal hari ini (YYYY-MM-DD)
const todayStr = computed(() => {
  const d = new Date()
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
})

// Perhitungan jumlah hari kerja (skip Sabtu & Minggu) secara reaktif
const estimatedWorkingDays = computed(() => {
  if (!startDate.value || !endDate.value) return 0
  const start = new Date(startDate.value + 'T00:00:00')
  const end = new Date(endDate.value + 'T00:00:00')

  if (start > end) return 0

  let count = 0
  const cur = new Date(start)
  while (cur <= end) {
    const day = cur.getDay()
    if (day !== 0 && day !== 6) {
      count++
    }
    cur.setDate(cur.getDate() + 1)
  }
  return count
})

// Validasi saldo
const isBalanceInsufficient = computed(() => {
  return leaveType.value === 'ANNUAL' && estimatedWorkingDays.value > leaveBalance.value
})

onMounted(() => {
  loadMyLeaves()
})

watch([page, selectedStatus], () => {
  loadMyLeaves()
})

async function loadMyLeaves() {
  if (!authStore.isAuthenticated) return

  try {
    const params = {
      page: page.value,
      size: pageSize.value,
    }
    if (selectedStatus.value) {
      params.status = selectedStatus.value
    }
    await leaveStore.fetchMyLeaves(params)
  } catch (err) {
    notify.showError(err.message || 'Gagal memuat riwayat cuti')
  }
}

async function handleSubmit() {
  if (!startDate.value || !endDate.value) {
    notify.showError('Tanggal mulai dan selesai wajib diisi')
    return
  }

  if (startDate.value > endDate.value) {
    notify.showError('Tanggal mulai tidak boleh lebih besar dari tanggal selesai')
    return
  }

  if (startDate.value < todayStr.value) {
    notify.showError('Tanggal cuti tidak boleh di masa lampau (backdate)')
    return
  }

  if (estimatedWorkingDays.value <= 0) {
    notify.showError('Pengajuan cuti harus mencakup minimal 1 hari kerja (Senin–Jumat)')
    return
  }

  if (isBalanceInsufficient.value) {
    notify.showError('Sisa saldo cuti Anda tidak mencukupi untuk pengajuan ini')
    return
  }

  if (!reason.value.trim()) {
    notify.showError('Alasan cuti wajib diisi')
    return
  }

  submitting.value = true
  try {
    await leaveStore.submitLeave({
      startDate: startDate.value,
      endDate: endDate.value,
      leaveType: leaveType.value,
      reason: reason.value.trim(),
    })
    notify.showSuccess('Pengajuan cuti berhasil dikirim dan menunggu persetujuan.')
    // Reset form
    startDate.value = ''
    endDate.value = ''
    reason.value = ''
    page.value = 0
    await loadMyLeaves()
    // Refresh user profile untuk memperbarui data jika perlu
    await authStore.fetchCurrentUser()
  } catch (err) {
    notify.showError(err.message || 'Gagal mengajukan permohonan cuti')
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

function formatTypeLabel(type) {
  switch (type) {
    case 'ANNUAL':
      return 'Tahunan'
    case 'SICK':
      return 'Sakit'
    case 'UNPAID':
      return 'Tanpa Gaji'
    default:
      return type || '-'
  }
}
</script>

<template>
  <div class="space-y-6">
    <!-- Header & Info Saldo Cuti -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <div class="md:col-span-2 bg-white rounded-xl border border-slate-200 p-5 shadow-xs">
        <h2 class="text-lg font-semibold text-slate-800 mb-1">Form Pengajuan Cuti</h2>
        <p class="text-sm text-slate-500">
          Silakan lengkapi rentang tanggal dan alasan pengajuan cuti. Pengajuan hanya menghitung hari kerja (Senin s/d Jumat).
        </p>

        <!-- Form Pengajuan -->
        <form @submit.prevent="handleSubmit" class="mt-5 space-y-4">
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-1">Tanggal Mulai</label>
              <input
                v-model="startDate"
                type="date"
                :min="todayStr"
                class="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:outline-hidden focus:ring-2 focus:ring-indigo-500"
                required
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-1">Tanggal Selesai</label>
              <input
                v-model="endDate"
                type="date"
                :min="startDate || todayStr"
                class="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:outline-hidden focus:ring-2 focus:ring-indigo-500"
                required
              />
            </div>
          </div>

          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <BaseSelect
                v-model="leaveType"
                label="Jenis Cuti"
                :options="LEAVE_TYPE_OPTIONS"
                option-label="label"
                option-value="value"
                placeholder="Pilih jenis cuti"
                required
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-1">Estimasi Hari Kerja</label>
              <div
                class="px-3 py-2 rounded-lg border text-sm font-medium flex items-center justify-between"
                :class="
                  isBalanceInsufficient
                    ? 'bg-rose-50 border-rose-200 text-rose-700'
                    : 'bg-slate-50 border-slate-200 text-slate-700'
                "
              >
                <span>{{ estimatedWorkingDays }} Hari Kerja</span>
                <span v-if="isBalanceInsufficient" class="text-xs font-normal text-rose-600">
                  Melebihi saldo cuti
                </span>
              </div>
            </div>
          </div>

          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1">Alasan Pengajuan</label>
            <textarea
              v-model="reason"
              rows="3"
              class="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:outline-hidden focus:ring-2 focus:ring-indigo-500"
              placeholder="Jelaskan alasan pengajuan cuti Anda..."
              required
            ></textarea>
          </div>

          <div class="flex justify-end pt-2">
            <BaseButton
              type="submit"
              label="Ajukan Permohonan Cuti"
              icon="pi pi-send"
              :loading="submitting"
              :disabled="submitting || isBalanceInsufficient || estimatedWorkingDays === 0"
            />
          </div>
        </form>
      </div>

      <!-- Ringkasan Saldo -->
      <div class="bg-indigo-50/70 border border-indigo-100 rounded-xl p-5 shadow-xs flex flex-col justify-between">
        <div>
          <div class="flex items-center gap-2 text-indigo-700 font-medium mb-1">
            <i class="pi pi-calendar-minus"></i>
            <span>Sisa Kuota Cuti Tahunan</span>
          </div>
          <p class="text-xs text-indigo-600/80">
            Jatah cuti tahun berjalan yang dapat Anda gunakan.
          </p>
          <div class="mt-4 flex items-baseline gap-2">
            <span class="text-4xl font-bold text-indigo-900">{{ leaveBalance }}</span>
            <span class="text-sm font-medium text-indigo-700">Hari Kerja</span>
          </div>
        </div>

        <div class="mt-6 pt-4 border-t border-indigo-100/80 text-xs text-slate-600 space-y-1.5">
          <div class="flex items-center gap-2">
            <i class="pi pi-info-circle text-indigo-500"></i>
            <span>Weekend (Sabtu & Minggu) tidak memotong kuota.</span>
          </div>
          <div class="flex items-center gap-2">
            <i class="pi pi-check-circle text-indigo-500"></i>
            <span>Saldo baru terpotong setelah disetujui atasan/HR.</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Tabel Riwayat Pengajuan Cuti Saya -->
    <div class="bg-white rounded-xl border border-slate-200 p-5 shadow-xs">
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-4">
        <div>
          <h3 class="text-base font-semibold text-slate-800">Riwayat Pengajuan Cuti Saya</h3>
          <p class="text-sm text-slate-500">Daftar permohonan cuti yang pernah Anda buat</p>
        </div>
        <div class="w-full sm:w-64">
          <BaseSelect
            v-model="selectedStatus"
            :options="LEAVE_STATUS_OPTIONS"
            option-label="label"
            option-value="value"
            placeholder="Filter Status"
          />
        </div>
      </div>

      <BaseTable
        :columns="columns"
        :value="leaveStore.myLeaves"
        :loading="leaveStore.loading"
        :total-records="leaveStore.totalMyLeaves"
        :rows="pageSize"
        lazy
        @page="(e) => (page = e.page)"
      >
        <!-- Slot Tanggal Pengajuan -->
        <template #body-createdAt="{ data }">
          <span class="text-sm text-slate-600">{{ formatDate(data.createdAt) }}</span>
        </template>

        <!-- Slot Rentang Tanggal -->
        <template #body-dateRange="{ data }">
          <span class="text-sm font-medium text-slate-800">
            {{ formatDate(data.startDate) }} &ndash; {{ formatDate(data.endDate) }}
          </span>
        </template>

        <!-- Slot Durasi -->
        <template #body-requestedDays="{ data }">
          <span class="inline-flex items-center px-2 py-0.5 rounded-sm text-xs font-semibold bg-indigo-50 text-indigo-700">
            {{ data.requestedDays }} Hari Kerja
          </span>
        </template>

        <!-- Slot Jenis Cuti -->
        <template #body-leaveType="{ data }">
          <span class="text-sm text-slate-700">{{ formatTypeLabel(data.leaveType) }}</span>
        </template>

        <!-- Slot Alasan -->
        <template #body-reason="{ data }">
          <span class="text-sm text-slate-600 max-w-xs truncate block" :title="data.reason">
            {{ data.reason || '-' }}
          </span>
        </template>

        <!-- Slot Status -->
        <template #body-status="{ data }">
          <StatusBadge :status="data.status" />
        </template>

        <!-- Slot Diputuskan Oleh -->
        <template #body-approvedByName="{ data }">
          <div v-if="data.approvedByName" class="text-sm">
            <div class="font-medium text-slate-800">{{ data.approvedByName }}</div>
            <div class="text-xs text-slate-400">{{ formatDate(data.decidedAt) }}</div>
          </div>
          <span v-else class="text-sm text-slate-400">&ndash;</span>
        </template>
      </BaseTable>
    </div>
  </div>
</template>
