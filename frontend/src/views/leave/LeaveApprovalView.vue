<script setup>
import { ref, onMounted, watch } from 'vue'
import { useConfirm } from 'primevue/useconfirm'
import { useAuthStore } from '@/stores/authStore'
import { useLeaveStore } from '@/stores/leaveStore'
import { useNotify } from '@/composables/useNotify'
import { LEAVE_STATUS_OPTIONS } from '@/utils/constants'
import BaseTable from '@/components/common/BaseTable.vue'
import BaseSelect from '@/components/common/BaseSelect.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'

const authStore = useAuthStore()
const leaveStore = useLeaveStore()
const notify = useNotify()
const confirm = useConfirm()

const selectedStatus = ref('PENDING')
const page = ref(0)
const pageSize = ref(10)
const actionLoading = ref({})

const columns = [
  { field: 'employee', header: 'Karyawan' },
  { field: 'dateRange', header: 'Rentang Tanggal' },
  { field: 'requestedDays', header: 'Durasi' },
  { field: 'leaveType', header: 'Jenis Cuti' },
  { field: 'reason', header: 'Alasan' },
  { field: 'status', header: 'Status' },
]

onMounted(() => {
  loadTeamLeaves()
})

watch([page, selectedStatus], () => {
  loadTeamLeaves()
})

async function loadTeamLeaves() {
  if (!authStore.isAuthenticated) return

  try {
    const params = {
      page: page.value,
      size: pageSize.value,
    }
    if (selectedStatus.value) {
      params.status = selectedStatus.value
    }
    await leaveStore.fetchTeamLeaves(params)
  } catch (err) {
    notify.showError(err.message || 'Gagal memuat daftar permohonan cuti tim')
  }
}

function handleApprove(row) {
  confirm.require({
    message: `Setujui pengajuan cuti untuk ${row.employeeName} selama ${row.requestedDays} hari kerja? Saldo cuti karyawan akan otomatis dipotong.`,
    header: 'Konfirmasi Persetujuan Cuti',
    icon: 'pi pi-check-circle',
    acceptLabel: 'Ya, Setujui',
    rejectLabel: 'Batal',
    acceptClass: 'p-button-success',
    accept: async () => {
      actionLoading.value[row.id] = true
      try {
        await leaveStore.approveLeave(row.id)
        notify.showSuccess(`Pengajuan cuti ${row.employeeName} berhasil disetujui.`)
        await loadTeamLeaves()
      } catch (err) {
        notify.showError(err.message || 'Gagal menyetujui pengajuan cuti', 'Persetujuan Ditolak')
      } finally {
        actionLoading.value[row.id] = false
      }
    },
  })
}

function handleReject(row) {
  confirm.require({
    message: `Tolak pengajuan cuti untuk ${row.employeeName}? Saldo cuti karyawan tidak akan berkurang.`,
    header: 'Konfirmasi Penolakan Cuti',
    icon: 'pi pi-times-circle',
    acceptLabel: 'Ya, Tolak',
    rejectLabel: 'Batal',
    acceptClass: 'p-button-danger',
    accept: async () => {
      actionLoading.value[row.id] = true
      try {
        await leaveStore.rejectLeave(row.id)
        notify.showSuccess(`Pengajuan cuti ${row.employeeName} telah ditolak.`)
        await loadTeamLeaves()
      } catch (err) {
        notify.showError(err.message || 'Gagal menolak pengajuan cuti', 'Penolakan Gagal')
      } finally {
        actionLoading.value[row.id] = false
      }
    },
  })
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
    <div class="bg-white rounded-xl border border-slate-200 p-5 shadow-xs">
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-4">
        <div>
          <h3 class="text-base font-semibold text-slate-800">Daftar Persetujuan Cuti Tim</h3>
          <p class="text-sm text-slate-500">
            Tinjau dan putuskan permohonan cuti dari anggota tim Anda
          </p>
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
        :value="leaveStore.teamLeaves"
        :loading="leaveStore.loading"
        :total-records="leaveStore.totalTeamLeaves"
        :rows="pageSize"
        lazy
        @page="(e) => (page = e.page)"
      >
        <!-- Slot Karyawan -->
        <template #body-employee="{ data }">
          <div class="text-sm">
            <div class="font-medium text-slate-800">{{ data.employeeName }}</div>
            <div class="text-xs text-slate-500">{{ data.departmentName || '-' }} &bull; {{ data.positionTitle || '-' }}</div>
          </div>
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

        <!-- Slot Aksi -->
        <template #actions="{ data }">
          <div v-if="data.status === 'PENDING'" class="flex items-center gap-2">
            <BaseButton
              icon="pi pi-check"
              size="small"
              class="p-button-success p-button-sm !p-2"
              title="Setujui"
              :loading="actionLoading[data.id]"
              @click="handleApprove(data)"
            />
            <BaseButton
              icon="pi pi-times"
              size="small"
              class="p-button-danger p-button-sm !p-2"
              title="Tolak"
              :loading="actionLoading[data.id]"
              @click="handleReject(data)"
            />
          </div>
          <div v-else class="text-xs text-slate-400">
            {{ data.approvedByName ? `Oleh ${data.approvedByName}` : '-' }}
          </div>
        </template>
      </BaseTable>
    </div>
  </div>
</template>
