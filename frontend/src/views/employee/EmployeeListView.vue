<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'
import { useEmployeeStore } from '@/stores/employeeStore'
import { useDepartmentStore } from '@/stores/departmentStore'
import { EMPLOYMENT_STATUS_OPTIONS } from '@/utils/constants'
import BaseButton from '@/components/common/BaseButton.vue'
import BaseInput from '@/components/common/BaseInput.vue'
import BaseSelect from '@/components/common/BaseSelect.vue'
import BaseTable from '@/components/common/BaseTable.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'

const router = useRouter()
const toast = useToast()
const confirm = useConfirm()
const employeeStore = useEmployeeStore()
const departmentStore = useDepartmentStore()

const searchQuery = ref('')
const selectedDepartment = ref(null)
const selectedStatus = ref(null)

const tableColumns = [
  { field: 'fullName', header: 'Karyawan', sortable: true },
  { field: 'departmentName', header: 'Departemen', sortable: true },
  { field: 'positionTitle', header: 'Posisi', sortable: true },
  { field: 'managerName', header: 'Atasan / Manager' },
  { field: 'employmentStatus', header: 'Status', sortable: true },
  { field: 'leaveBalance', header: 'Cuti (Hari)', style: { width: '100px', textAlign: 'center' } },
]

onMounted(async () => {
  await Promise.all([
    departmentStore.fetchAllDepartments(),
    loadData(),
  ])
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
    toast.add({
      severity: 'error',
      summary: 'Gagal Memuat Data',
      detail: err.message || 'Terjadi kesalahan saat memuat daftar karyawan.',
      life: 4000,
    })
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

function confirmDelete(employee) {
  confirm.require({
    message: `Apakah Anda yakin ingin menghapus data karyawan "${employee.fullName}" (${employee.email})?`,
    header: 'Konfirmasi Hapus Karyawan',
    icon: 'pi pi-exclamation-triangle',
    rejectLabel: 'Batal',
    acceptLabel: 'Hapus',
    acceptClass: 'p-button-danger',
    accept: async () => {
      try {
        await employeeStore.deleteEmployee(employee.id)
        toast.add({
          severity: 'success',
          summary: 'Berhasil',
          detail: 'Data karyawan berhasil dihapus.',
          life: 3000,
        })
        loadData(employeeStore.currentPage)
      } catch (err) {
        toast.add({
          severity: 'error',
          summary: 'Gagal Menghapus',
          detail: err.message || 'Tidak dapat menghapus karyawan.',
          life: 4000,
        })
      }
    },
  })
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
          <BaseButton
            label="Tambah Karyawan"
            icon="pi pi-user-plus"
            variant="primary"
          />
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
        <span v-else class="text-slate-400 text-xs italic">
          Tanpa Atasan Langsung
        </span>
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
          icon="pi pi-trash"
          variant="danger"
          size="small"
          text
          rounded
          title="Hapus"
          @click="confirmDelete(data)"
        />
      </template>
    </BaseTable>
  </div>
</template>
