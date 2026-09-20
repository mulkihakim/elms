<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { useNotify } from '@/composables/useNotify'
import { useEmployeeStore } from '@/stores/employeeStore'
import { useDepartmentStore } from '@/stores/departmentStore'
import { EMPLOYMENT_STATUS_OPTIONS } from '@/utils/constants'
import BaseButton from '@/components/common/BaseButton.vue'
import BaseInput from '@/components/common/BaseInput.vue'
import BaseSelect from '@/components/common/BaseSelect.vue'
import BaseTable from '@/components/common/BaseTable.vue'
import BaseModal from '@/components/common/BaseModal.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'

const router = useRouter()
const notify = useNotify()
const employeeStore = useEmployeeStore()
const departmentStore = useDepartmentStore()

const searchQuery = ref('')
const selectedDepartment = ref(null)
const selectedStatus = ref(null)

const isStatusModalOpen = ref(false)
const selectedEmployee = ref(null)
const statusForm = ref({ status: 'ACTIVE' })
const updatingStatus = ref(false)

const tableColumns = [
  { field: 'fullName', header: 'Karyawan', sortable: true },
  { field: 'departmentName', header: 'Departemen', sortable: true },
  { field: 'positionTitle', header: 'Posisi', sortable: true },
  { field: 'managerName', header: 'Atasan / Manager' },
  { field: 'employmentStatus', header: 'Status', sortable: true },
  { field: 'leaveBalance', header: 'Cuti (Hari)', style: { width: '100px', textAlign: 'center' } },
]

onMounted(async () => {
  await Promise.all([departmentStore.fetchAllDepartments(), loadData()])
})

async function loadData(page = 0) {
  try {
    const filters = {
      search: searchQuery.value.trim() || undefined,
      departmentId: selectedDepartment.value || undefined,
      status: selectedStatus.value || undefined,
    }
    await employeeStore.fetchEmployees(filters, page, 10)
  } catch (err) {
    notify.showError(
      err.message || 'Terjadi kesalahan saat memuat daftar karyawan.',
      'Gagal Memuat Data',
    )
  }
}

function handleSearch() {
  loadData(0)
}

function handleDepartmentChange(val) {
  selectedDepartment.value = val
  loadData(0)
}

function handleStatusChange(val) {
  selectedStatus.value = val
  loadData(0)
}

function handlePageChange(event) {
  const page = event.page
  loadData(page)
}

function navigateToEdit(id) {
  router.push(`/employees/${id}/edit`)
}

function openStatusModal(employee) {
  selectedEmployee.value = employee
  statusForm.value.status = employee.employmentStatus
  isStatusModalOpen.value = true
}

async function handleUpdateStatus() {
  if (!selectedEmployee.value) return
  updatingStatus.value = true
  try {
    await employeeStore.updateEmployeeStatus(selectedEmployee.value.id, statusForm.value.status)
    notify.showSuccess(
      `Status ${selectedEmployee.value.fullName} berhasil diubah menjadi ${statusForm.value.status}.`,
    )
    isStatusModalOpen.value = false
    loadData(employeeStore.currentPage)
  } catch (err) {
    notify.showError(err.message || 'Gagal mengubah status karyawan.', 'Gagal Menyimpan')
  } finally {
    updatingStatus.value = false
  }
}
</script>

<template>
  <div class="space-y-5">
    <!-- Header Halaman -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
      <div>
        <h1 class="text-2xl font-bold text-slate-800 tracking-tight">Karyawan</h1>
        <p class="text-sm text-slate-500 mt-0.5">
          Kelola data induk karyawan, struktur organisasi, dan profil kepegawaian
        </p>
      </div>

      <div>
        <RouterLink to="/employees/create">
          <BaseButton label="Tambah Karyawan" icon="pi pi-user-plus" variant="primary" />
        </RouterLink>
      </div>
    </div>

    <!-- Filter & Search Toolbar -->
    <div
      class="bg-white p-4 rounded-lg border border-slate-200 shadow-2xs flex flex-col md:flex-row md:items-center justify-between gap-3"
    >
      <div class="grid grid-cols-1 sm:grid-cols-3 gap-3 w-full md:w-auto md:flex-1 max-w-3xl">
        <!-- Pencarian Keyword Nama/Email -->
        <div class="w-full">
          <BaseInput
            v-model="searchQuery"
            placeholder="Cari nama atau email..."
            class="text-sm"
            @blur="handleSearch"
            @keyup.enter="handleSearch"
          />
        </div>

        <!-- Filter Departemen -->
        <div class="w-full">
          <BaseSelect
            :model-value="selectedDepartment"
            :options="departmentStore.allDepartments"
            option-label="name"
            option-value="id"
            placeholder="Semua Departemen"
            show-clear
            filter
            @update:model-value="handleDepartmentChange"
          />
        </div>

        <!-- Filter Status Kepegawaian -->
        <div class="w-full">
          <BaseSelect
            :model-value="selectedStatus"
            :options="EMPLOYMENT_STATUS_OPTIONS"
            option-label="label"
            option-value="value"
            placeholder="Semua Status"
            show-clear
            @update:model-value="handleStatusChange"
          />
        </div>
      </div>

      <div class="flex items-center gap-2 self-end md:self-auto shrink-0">
        <BaseButton
          label="Cari"
          icon="pi pi-search"
          variant="secondary"
          size="small"
          outlined
          @click="handleSearch"
        />
        <BaseButton
          icon="pi pi-refresh"
          variant="secondary"
          size="small"
          outlined
          title="Muat ulang data"
          :loading="employeeStore.loading"
          @click="loadData(0)"
        />
      </div>
    </div>

    <!-- Tabel Data Karyawan -->
    <BaseTable
      :value="employeeStore.employees"
      :columns="tableColumns"
      :loading="employeeStore.loading"
      :rows="employeeStore.pageSize"
      :total-records="employeeStore.totalElements"
      :lazy="true"
      empty-message="Belum ada data karyawan. Klik 'Tambah Karyawan' untuk menambahkan data baru."
      @page="handlePageChange"
    >
      <!-- Custom Kolom Karyawan (Nama + Email) -->
      <template #body-fullName="{ data }">
        <div class="flex items-center gap-2.5">
          <div
            class="w-8 h-8 rounded-full bg-indigo-50 text-indigo-700 flex items-center justify-center font-bold text-xs shrink-0 border border-indigo-100"
          >
            {{ data.fullName?.charAt(0) || 'E' }}
          </div>
          <div>
            <span class="font-semibold text-slate-800 block text-sm leading-snug">
              {{ data.fullName }}
            </span>
            <span class="text-xs text-slate-500 block">
              {{ data.email }}
            </span>
          </div>
        </div>
      </template>

      <!-- Custom Kolom Manager -->
      <template #body-managerName="{ data }">
        <span v-if="data.managerName" class="text-slate-700 text-sm font-medium">
          {{ data.managerName }}
        </span>
        <span v-else class="text-slate-400 text-xs italic"> Tanpa Atasan Langsung </span>
      </template>

      <!-- Custom Kolom Status -->
      <template #body-employmentStatus="{ data }">
        <StatusBadge :status="data.employmentStatus" />
      </template>

      <!-- Custom Kolom Sisa Cuti -->
      <template #body-leaveBalance="{ data }">
        <span class="font-semibold text-slate-700">
          {{ data.leaveBalance ?? '-' }}
        </span>
      </template>

      <!-- Kolom Aksi -->
      <template #actions="{ data }">
        <BaseButton
          icon="pi pi-pencil"
          variant="secondary"
          size="small"
          text
          rounded
          title="Ubah Data"
          @click="navigateToEdit(data.id)"
        />
        <BaseButton
          icon="pi pi-user-edit"
          variant="secondary"
          size="small"
          text
          rounded
          title="Ubah Status Kepegawaian"
          @click="openStatusModal(data)"
        />
      </template>
    </BaseTable>

    <!-- Modal Ubah Status Kepegawaian (Soft Delete / Status Change) -->
    <BaseModal v-model="isStatusModalOpen" title="Ubah Status Kepegawaian" width="450px">
      <div v-if="selectedEmployee" class="space-y-4">
        <p class="text-sm text-slate-600">
          Ubah status kepegawaian untuk karyawan:
          <strong class="text-slate-800">{{ selectedEmployee.fullName }}</strong> ({{
            selectedEmployee.email
          }}).
        </p>

        <BaseSelect
          v-model="statusForm.status"
          label="Status Kepegawaian"
          :options="EMPLOYMENT_STATUS_OPTIONS"
          option-label="label"
          option-value="value"
          required
        />

        <div
          v-if="statusForm.status === 'RESIGNED' || statusForm.status === 'TERMINATED'"
          class="p-3 bg-amber-50 border border-amber-200 rounded-lg text-xs text-amber-800"
        >
          <i class="pi pi-exclamation-triangle mr-1"></i>
          Karyawan dengan status <strong>{{ statusForm.status }}</strong> tidak akan dapat melakukan
          login atau mencatat absensi.
        </div>
      </div>

      <template #footer>
        <div class="flex justify-end gap-2">
          <BaseButton
            label="Batal"
            variant="secondary"
            :disabled="updatingStatus"
            @click="isStatusModalOpen = false"
          />
          <BaseButton
            label="Simpan Status"
            variant="primary"
            :loading="updatingStatus"
            @click="handleUpdateStatus"
          />
        </div>
      </template>
    </BaseModal>
  </div>
</template>
