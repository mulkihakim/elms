<script setup>
import { ref, computed, onMounted } from 'vue'
import { useNotify } from '@/composables/useNotify'
import { useConfirm } from 'primevue/useconfirm'
import { useDepartmentStore } from '@/stores/departmentStore'
import BaseButton from '@/components/common/BaseButton.vue'
import BaseInput from '@/components/common/BaseInput.vue'
import BaseTable from '@/components/common/BaseTable.vue'
import BaseModal from '@/components/common/BaseModal.vue'

const notify = useNotify()
const confirm = useConfirm()
const departmentStore = useDepartmentStore()

const searchQuery = ref('')
const isModalOpen = ref(false)
const isEditing = ref(false)
const editingId = ref(null)
const submitting = ref(false)

const form = ref({
  name: '',
})

const errors = ref({
  name: '',
})

const tableColumns = [
  { field: 'id', header: 'ID', sortable: true, style: { width: '80px' } },
  { field: 'name', header: 'Nama Departemen', sortable: true },
]

// Filter data lokal berdasarkan pencarian nama
const filteredDepartments = computed(() => {
  if (!searchQuery.value.trim()) {
    return departmentStore.departments
  }
  const q = searchQuery.value.toLowerCase()
  return departmentStore.departments.filter((d) => d.name?.toLowerCase().includes(q))
})

onMounted(() => {
  loadData()
})

async function loadData() {
  try {
    await departmentStore.fetchDepartments(0, 50)
  } catch (err) {
    notify.showError(
      err.message || 'Terjadi kesalahan saat memuat departemen.',
      'Gagal Memuat Data',
    )
  }
}

function openCreateModal() {
  isEditing.value = false
  editingId.value = null
  form.value = { name: '' }
  errors.value = { name: '' }
  isModalOpen.value = true
}

function openEditModal(department) {
  isEditing.value = true
  editingId.value = department.id
  form.value = { name: department.name }
  errors.value = { name: '' }
  isModalOpen.value = true
}

function validateForm() {
  let valid = true
  errors.value = { name: '' }

  if (!form.value.name.trim()) {
    errors.value.name = 'Nama departemen wajib diisi'
    valid = false
  } else if (form.value.name.length > 100) {
    errors.value.name = 'Nama departemen maksimal 100 karakter'
    valid = false
  }

  return valid
}

async function handleSubmit() {
  if (!validateForm()) return

  submitting.value = true
  try {
    if (isEditing.value) {
      await departmentStore.updateDepartment(editingId.value, {
        name: form.value.name.trim(),
      })
      notify.showSuccess('Departemen berhasil diperbarui.')
    } else {
      await departmentStore.createDepartment({
        name: form.value.name.trim(),
      })
      notify.showSuccess('Departemen baru berhasil ditambahkan.')
    }
    isModalOpen.value = false
  } catch (err) {
    if (err.message && err.message.toLowerCase().includes('already exists')) {
      errors.value.name = 'Nama departemen sudah digunakan'
    } else {
      notify.showError(err.message || 'Terjadi kesalahan saat menyimpan data.', 'Gagal Menyimpan')
    }
  } finally {
    submitting.value = false
  }
}

function confirmDelete(department) {
  confirm.require({
    message: `Apakah Anda yakin ingin menghapus departemen "${department.name}"?`,
    header: 'Konfirmasi Hapus',
    icon: 'pi pi-exclamation-triangle',
    rejectLabel: 'Batal',
    acceptLabel: 'Hapus',
    acceptClass: 'p-button-danger',
    accept: async () => {
      try {
        await departmentStore.deleteDepartment(department.id)
        notify.showSuccess('Departemen berhasil dihapus.')
      } catch (err) {
        notify.showError(err.message || 'Tidak dapat menghapus departemen.', 'Gagal Menghapus')
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
        <h1 class="text-2xl font-bold text-slate-800 tracking-tight">Departemen</h1>
        <p class="text-sm text-slate-500 mt-0.5">
          Kelola master data struktur departemen organisasi
        </p>
      </div>

      <div>
        <BaseButton
          label="Tambah Departemen"
          icon="pi pi-plus"
          variant="primary"
          @click="openCreateModal"
        />
      </div>
    </div>

    <!-- Filter & Search Toolbar -->
    <div
      class="bg-white p-3.5 rounded-lg border border-slate-200 shadow-2xs flex items-center justify-between gap-3"
    >
      <div class="max-w-xs w-full">
        <BaseInput v-model="searchQuery" placeholder="Cari nama departemen..." class="text-sm" />
      </div>

      <div class="flex items-center gap-2">
        <BaseButton
          icon="pi pi-refresh"
          variant="secondary"
          size="small"
          outlined
          title="Muat ulang data"
          :loading="departmentStore.loading"
          @click="loadData"
        />
      </div>
    </div>

    <!-- Tabel Data Departemen -->
    <BaseTable
      :value="filteredDepartments"
      :columns="tableColumns"
      :loading="departmentStore.loading"
      :rows="10"
      empty-message="Belum ada data departemen. Klik 'Tambah Departemen' untuk membuat baru."
    >
      <template #actions="{ data }">
        <BaseButton
          icon="pi pi-pencil"
          variant="secondary"
          size="small"
          text
          rounded
          title="Ubah"
          @click="openEditModal(data)"
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

    <!-- Modal Form Tambah / Edit -->
    <BaseModal
      v-model="isModalOpen"
      :title="isEditing ? 'Ubah Departemen' : 'Tambah Departemen Baru'"
    >
      <form class="space-y-4" @submit.prevent="handleSubmit">
        <BaseInput
          id="dept-name"
          v-model="form.name"
          label="Nama Departemen"
          placeholder="Contoh: Engineering, Human Resources"
          :error="errors.name"
          required
        />
      </form>

      <template #footer>
        <BaseButton label="Batal" variant="secondary" outlined @click="isModalOpen = false" />
        <BaseButton
          :label="isEditing ? 'Simpan Perubahan' : 'Tambah'"
          variant="primary"
          :loading="submitting"
          @click="handleSubmit"
        />
      </template>
    </BaseModal>
  </div>
</template>
