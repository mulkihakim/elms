<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useAuthStore } from '@/stores/authStore'
import { useAttendanceStore } from '@/stores/attendanceStore'
import { useDepartmentStore } from '@/stores/departmentStore'
import AttendanceWidget from '@/components/attendance/AttendanceWidget.vue'
import AttendanceSummaryCards from '@/components/attendance/AttendanceSummaryCards.vue'
import BaseTable from '@/components/common/BaseTable.vue'
import BaseInput from '@/components/common/BaseInput.vue'
import BaseSelect from '@/components/common/BaseSelect.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'

const authStore = useAuthStore()
const attendanceStore = useAttendanceStore()
const departmentStore = useDepartmentStore()

const activeTab = ref('all') // 'me' | 'team' | 'all'

// Filters
const filterFrom = ref('')
const filterTo = ref('')
const selectedDepartment = ref(null)
const selectedStatus = ref(null)

const statusOptions = [
  { label: 'Semua Status', value: null },
  { label: 'Tepat Waktu (ON_TIME)', value: 'ON_TIME' },
  { label: 'Terlambat (LATE)', value: 'LATE' },
  { label: 'Tidak Hadir (ABSENT)', value: 'ABSENT' },
]

const departmentOptions = computed(() => [
  { label: 'Semua Departemen', value: null },
  ...departmentStore.allDepartments.map((d) => ({
    label: d.name,
    value: d.id,
  })),
])

// Kolom tabel Presensi Saya
const myColumns = [
  { field: 'date', header: 'Tanggal', sortable: true },
  { field: 'checkIn', header: 'Jam Masuk' },
  { field: 'checkOut', header: 'Jam Pulang' },
  { field: 'status', header: 'Status' },
  { field: 'workMinutes', header: 'Total Durasi' },
  { field: 'notes', header: 'Catatan' },
]

// Kolom tabel Presensi Tim
const teamColumns = [
  { field: 'employeeName', header: 'Nama Anggota Tim', sortable: true },
  { field: 'departmentName', header: 'Departemen' },
  { field: 'date', header: 'Tanggal', sortable: true },
  { field: 'checkIn', header: 'Jam Masuk' },
  { field: 'checkOut', header: 'Jam Pulang' },
  { field: 'status', header: 'Status' },
  { field: 'workMinutes', header: 'Total Durasi' },
  { field: 'notes', header: 'Catatan' },
]

// Kolom tabel Semua Karyawan (HR)
const allColumns = [
  { field: 'employeeName', header: 'Karyawan', sortable: true },
  { field: 'departmentName', header: 'Departemen' },
  { field: 'positionTitle', header: 'Posisi' },
  { field: 'date', header: 'Tanggal', sortable: true },
  { field: 'checkIn', header: 'Jam Masuk' },
  { field: 'checkOut', header: 'Jam Pulang' },
  { field: 'status', header: 'Status' },
  { field: 'workMinutes', header: 'Total Durasi' },
  { field: 'notes', header: 'Catatan' },
]

onMounted(async () => {
  // Set default tab berdasar role
  if (authStore.isEmployee) {
    activeTab.value = 'me'
  } else if (authStore.isManager) {
    activeTab.value = 'team'
  } else {
    activeTab.value = 'all'
    departmentStore.fetchAllDepartments()
    attendanceStore.fetchSummary()
  }

  loadData()
})

watch(activeTab, (newTab) => {
  resetFilters()
  if (newTab === 'all') {
    if (departmentStore.allDepartments.length === 0) {
      departmentStore.fetchAllDepartments()
    }
    attendanceStore.fetchSummary()
  }
  loadData()
})

async function loadData(page = 0) {
  const params = {
    page,
    size: 10,
    from: filterFrom.value || undefined,
    to: filterTo.value || undefined,
  }

  if (activeTab.value === 'me') {
    await attendanceStore.fetchMyHistory(params)
  } else if (activeTab.value === 'team') {
    await attendanceStore.fetchTeamAttendance(params)
  } else if (activeTab.value === 'all') {
    await attendanceStore.fetchAllAttendance({
      ...params,
      departmentId: selectedDepartment.value || undefined,
      status: selectedStatus.value || undefined,
    })
  }
}

function handleApplyFilter() {
  loadData(0)
}

function resetFilters() {
  filterFrom.value = ''
  filterTo.value = ''
  selectedDepartment.value = null
  selectedStatus.value = null
}

function handleResetFilter() {
  resetFilters()
  loadData(0)
}

function formatTime(val) {
  if (!val) return '-'
  return new Date(val).toLocaleTimeString('id-ID', {
    hour: '2-digit',
    minute: '2-digit',
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

function formatDuration(minutes) {
  if (minutes == null) return '-'
  const hrs = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (hrs === 0) return `${mins}m`
  return `${hrs}j ${mins}m`
}
</script>

<template>
  <div class="space-y-6">
    <!-- Header Halaman -->
    <div>
      <h1 class="text-2xl font-bold text-slate-800 tracking-tight">Presensi & Kehadiran</h1>
      <p class="text-sm text-slate-500 mt-0.5">
        Catat jam kehadiran kerja dan pantau riwayat kehadiran karyawan.
      </p>
    </div>

    <!-- 1. Widget Presensi Utama (Digital Clock + Check-in / Check-out) -->
    <AttendanceWidget />

    <!-- 2. Ringkasan KPI Cards (Khusus HR) -->
    <AttendanceSummaryCards
      v-if="authStore.isHR && attendanceStore.summary"
      :summary="attendanceStore.summary"
    />

    <!-- 3. Tab Switcher untuk Manager dan HR -->
    <div v-if="!authStore.isEmployee" class="flex items-center gap-2 border-b border-slate-200">
      <button
        v-if="authStore.isHR"
        type="button"
        class="px-4 py-2.5 text-sm font-semibold border-b-2 transition-colors cursor-pointer"
        :class="
          activeTab === 'all'
            ? 'border-indigo-600 text-indigo-600'
            : 'border-transparent text-slate-500 hover:text-slate-800'
        "
        @click="activeTab = 'all'"
      >
        <i class="pi pi-building text-xs mr-1.5"></i>
        Seluruh Presensi Karyawan
      </button>

      <button
        v-if="authStore.isManager || authStore.isHR"
        type="button"
        class="px-4 py-2.5 text-sm font-semibold border-b-2 transition-colors cursor-pointer"
        :class="
          activeTab === 'team'
            ? 'border-indigo-600 text-indigo-600'
            : 'border-transparent text-slate-500 hover:text-slate-800'
        "
        @click="activeTab = 'team'"
      >
        <i class="pi pi-users text-xs mr-1.5"></i>
        Presensi Tim
      </button>

      <button
        type="button"
        class="px-4 py-2.5 text-sm font-semibold border-b-2 transition-colors cursor-pointer"
        :class="
          activeTab === 'me'
            ? 'border-indigo-600 text-indigo-600'
            : 'border-transparent text-slate-500 hover:text-slate-800'
        "
        @click="activeTab = 'me'"
      >
        <i class="pi pi-user text-xs mr-1.5"></i>
        Presensi Saya
      </button>
    </div>

    <!-- 4. Area Filter Data -->
    <div class="bg-white p-4 rounded-xl border border-slate-200 shadow-2xs">
      <div class="flex flex-wrap items-end gap-3">
        <!-- Filter Rentang Tanggal -->
        <div class="w-full sm:w-44">
          <BaseInput
            id="filter-from"
            v-model="filterFrom"
            type="date"
            label="Dari Tanggal"
          />
        </div>

        <div class="w-full sm:w-44">
          <BaseInput
            id="filter-to"
            v-model="filterTo"
            type="date"
            label="Sampai Tanggal"
          />
        </div>

        <!-- Filter Departemen (Khusus Tab Seluruh Karyawan / HR) -->
        <div v-if="activeTab === 'all'" class="w-full sm:w-56">
          <BaseSelect
            v-model="selectedDepartment"
            label="Departemen"
            :options="departmentOptions"
            option-label="label"
            option-value="value"
            placeholder="Pilih Departemen"
            show-clear
            filter
          />
        </div>

        <!-- Filter Status Kehadiran -->
        <div v-if="activeTab === 'all'" class="w-full sm:w-52">
          <BaseSelect
            v-model="selectedStatus"
            label="Status"
            :options="statusOptions"
            option-label="label"
            option-value="value"
            placeholder="Pilih Status"
            show-clear
          />
        </div>

        <!-- Tombol Aksi Filter -->
        <div class="flex items-center gap-2">
          <BaseButton
            label="Terapkan"
            icon="pi pi-filter"
            @click="handleApplyFilter"
          />
          <BaseButton
            label="Reset"
            icon="pi pi-refresh"
            variant="secondary"
            outlined
            @click="handleResetFilter"
          />
        </div>
      </div>
    </div>

    <!-- 5. Tabel Data Presensi -->
    <!-- Tab 1: Presensi Saya -->
    <div v-if="activeTab === 'me'">
      <div class="flex items-center justify-between mb-3">
        <h2 class="text-base font-bold text-slate-800">Riwayat Presensi Saya</h2>
        <span class="text-xs text-slate-500 font-medium"
          >Total: {{ attendanceStore.totalHistory }} data</span
        >
      </div>

      <BaseTable
        :value="attendanceStore.historyList"
        :columns="myColumns"
        :loading="attendanceStore.loading"
        :total-records="attendanceStore.totalHistory"
        :lazy="true"
        @page="loadData($event.page)"
      >
        <template #body-date="{ data }">
          <span class="font-medium text-slate-800">{{ formatDate(data.date) }}</span>
        </template>
        <template #body-checkIn="{ data }">
          <span class="font-mono text-slate-700">{{ formatTime(data.checkIn) }}</span>
        </template>
        <template #body-checkOut="{ data }">
          <span class="font-mono text-slate-700">{{ formatTime(data.checkOut) }}</span>
        </template>
        <template #body-status="{ data }">
          <StatusBadge :status="data.status" />
        </template>
        <template #body-workMinutes="{ data }">
          <span class="text-slate-600 font-medium">{{ formatDuration(data.workMinutes) }}</span>
        </template>
        <template #body-notes="{ data }">
          <span class="text-xs text-slate-500 italic">{{ data.notes || '-' }}</span>
        </template>
      </BaseTable>
    </div>

    <!-- Tab 2: Presensi Tim (Manager / HR) -->
    <div v-else-if="activeTab === 'team'">
      <div class="flex items-center justify-between mb-3">
        <h2 class="text-base font-bold text-slate-800">Kehadiran Anggota Tim</h2>
        <span class="text-xs text-slate-500 font-medium"
          >Total: {{ attendanceStore.totalTeam }} data</span
        >
      </div>

      <BaseTable
        :value="attendanceStore.teamList"
        :columns="teamColumns"
        :loading="attendanceStore.loading"
        :total-records="attendanceStore.totalTeam"
        :lazy="true"
        @page="loadData($event.page)"
      >
        <template #body-employeeName="{ data }">
          <div class="font-semibold text-slate-800">{{ data.employeeName }}</div>
          <div class="text-[11px] text-slate-400">{{ data.employeeEmail }}</div>
        </template>
        <template #body-date="{ data }">
          <span class="font-medium text-slate-700">{{ formatDate(data.date) }}</span>
        </template>
        <template #body-checkIn="{ data }">
          <span class="font-mono text-slate-700">{{ formatTime(data.checkIn) }}</span>
        </template>
        <template #body-checkOut="{ data }">
          <span class="font-mono text-slate-700">{{ formatTime(data.checkOut) }}</span>
        </template>
        <template #body-status="{ data }">
          <StatusBadge :status="data.status" />
        </template>
        <template #body-workMinutes="{ data }">
          <span class="text-slate-600 font-medium">{{ formatDuration(data.workMinutes) }}</span>
        </template>
        <template #body-notes="{ data }">
          <span class="text-xs text-slate-500 italic">{{ data.notes || '-' }}</span>
        </template>
      </BaseTable>
    </div>

    <!-- Tab 3: Seluruh Presensi Karyawan (HR) -->
    <div v-else-if="activeTab === 'all'">
      <div class="flex items-center justify-between mb-3">
        <h2 class="text-base font-bold text-slate-800">Rekapitulasi Kehadiran Karyawan</h2>
        <span class="text-xs text-slate-500 font-medium"
          >Total: {{ attendanceStore.totalAll }} data</span
        >
      </div>

      <BaseTable
        :value="attendanceStore.allList"
        :columns="allColumns"
        :loading="attendanceStore.loading"
        :total-records="attendanceStore.totalAll"
        :lazy="true"
        @page="loadData($event.page)"
      >
        <template #body-employeeName="{ data }">
          <div class="font-semibold text-slate-800">{{ data.employeeName }}</div>
          <div class="text-[11px] text-slate-400">{{ data.employeeEmail }}</div>
        </template>
        <template #body-date="{ data }">
          <span class="font-medium text-slate-700">{{ formatDate(data.date) }}</span>
        </template>
        <template #body-checkIn="{ data }">
          <span class="font-mono text-slate-700">{{ formatTime(data.checkIn) }}</span>
        </template>
        <template #body-checkOut="{ data }">
          <span class="font-mono text-slate-700">{{ formatTime(data.checkOut) }}</span>
        </template>
        <template #body-status="{ data }">
          <StatusBadge :status="data.status" />
        </template>
        <template #body-workMinutes="{ data }">
          <span class="text-slate-600 font-medium">{{ formatDuration(data.workMinutes) }}</span>
        </template>
        <template #body-notes="{ data }">
          <span class="text-xs text-slate-500 italic">{{ data.notes || '-' }}</span>
        </template>
      </BaseTable>
    </div>
  </div>
</template>
