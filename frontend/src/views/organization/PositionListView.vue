<script setup>
import { ref, computed, onMounted } from 'vue'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'
import { usePositionStore } from '@/stores/positionStore'
import { useDepartmentStore } from '@/stores/departmentStore'
import BaseButton from '@/components/common/BaseButton.vue'
import BaseInput from '@/components/common/BaseInput.vue'
import BaseSelect from '@/components/common/BaseSelect.vue'
import BaseTable from '@/components/common/BaseTable.vue'
import BaseModal from '@/components/common/BaseModal.vue'

const toast = useToast()
const confirm = useConfirm()
const positionStore = usePositionStore()
const departmentStore = useDepartmentStore()

const searchQuery = ref('')
const selectedDeptFilter = ref(null)
const isModalOpen = ref(false)
const isEditing = ref(false)
const editingId = ref(null)
const submitting = ref(false)

const form = ref({
  title: '',
  departmentId: null,
})

const errors = ref({
  title: '',
  departmentId: '',
})

const tableColumns = [
  { field: 'id', header: 'ID', sortable: true, style: { width: '80px' } },
  { field: 'title', header: 'Judul Posisi', sortable: true },
  { field: 'departmentName', header: 'Departemen', sortable: true },
]

// Filter data lokal berdasarkan pencarian judul
const filteredPositions = computed(() => {
  if (!searchQuery.value.trim()) {
    return positionStore.positions
  }
  const q = searchQuery.value.toLowerCase()
  return positionStore.positions.filter((p) => p.title?.toLowerCase().includes(q))
})

onMounted(async () => {
  await Promise.all([
    departmentStore.fetchAllDepartments(),
    loadData(),
  ])
})

async function loadData() {
  try {
    await positionStore.fetchPositions(selectedDeptFilter.value, 0, 50)
  } catch (err) {
    toast.add({
      severity: 'error',
      summary: 'Gagal Memuat Data',
      detail: err.message || 'Terjadi kesalahan saat memuat posisi.',
      life: 4000,
    })
  }
}

function handleFilterChange(deptId) {
  selectedDeptFilter.value = deptId
  loadData()
}

function openCreateModal() {
  isEditing.value = false
  editingId.value = null
  form.value = {
    title: '',
    departmentId: selectedDeptFilter.value || null,
  }
  errors.value = { title: '', departmentId: '' }
  isModalOpen.value = true
}

function openEditModal(pos) {
  isEditing.value = true
  editingId.value = pos.id
  form.value = {
    title: pos.title,
    departmentId: pos.departmentId,
  }
  errors.value = { title: '', departmentId: '' }
  isModalOpen.value = true
}

function validateForm() {
  let valid = true
  errors.value = { title: '', departmentId: '' }

  if (!form.value.title.trim()) {
    errors.value.title = 'Judul posisi wajib diisi'
    valid = false
  } else if (form.value.title.length > 100) {
    errors.value.title = 'Judul posisi maksimal 100 karakter'
    valid = false
  }

  if (!form.value.departmentId) {
    errors.value.departmentId = 'Departemen wajib dipilih'
    valid = false
  }

  return valid
}

async function handleSubmit() {
  if (!validateForm()) return

  submitting.value = true
  try {
    const payload = {
      title: form.value.title.trim(),
      departmentId: Number(form.value.departmentId),
    }

    if (isEditing.value) {
      await positionStore.updatePosition(editingId.value, payload)
      toast.add({
        severity: 'success',
        summary: 'Berhasil',
        detail: 'Posisi berhasil diperbarui.',
        life: 3000,
      })
    } else {
      await positionStore.createPosition(payload)
      toast.add({
        severity: 'success',
        summary: 'Berhasil',
        detail: 'Posisi baru berhasil ditambahkan.',
        life: 3000,
      })
    }
    isModalOpen.value = false
  } catch (err) {
    toast.add({
      severity: 'error',
      summary: 'Gagal Menyimpan',
      detail: err.message || 'Terjadi kesalahan saat menyimpan data posisi.',
      life: 4000,
    })
  } finally {
    submitting.value = false
  }
}

function confirmDelete(pos) {
  confirm.require({
    message: `Apakah Anda yakin ingin menghapus posisi "${pos.title}" (${pos.departmentName})?`,
    header: 'Konfirmasi Hapus',
    icon: 'pi pi-exclamation-triangle',
    rejectLabel: 'Batal',
    acceptLabel: 'Hapus',
    acceptClass: 'p-button-danger',
    accept: async () => {
      try {
        await positionStore.deletePosition(pos.id)
        toast.add({
          severity: 'success',
          summary: 'Berhasil',
          detail: 'Posisi berhasil dihapus.',
          life: 3000,
        })
      } catch (err) {
        toast.add({
          severity: 'error',
          summary: 'Gagal Menghapus',
          detail: err.message || 'Tidak dapat menghapus posisi.',
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
        <h1 class="text-2xl font-bold text-slate-800 tracking-tight">Posisi / Jabatan</h1>
        <p class="text-sm text-slate-500 mt-0.5">
          Kelola master data posisi dan keterkaitannya dengan departemen
        </p>
      </div>

      <div>
        <BaseButton
          label="Tambah Posisi"
          icon="pi pi-plus"
          variant="primary"
          @click="openCreateModal"
        />
      </div>
    </div>

    <!-- Filter Toolbar -->
    <div
      class="bg-white p-3.5 rounded-lg border border-slate-200 shadow-2xs flex flex-col sm:flex-row sm:items-center justify-between gap-3"
    >
      <div class="flex flex-col sm:flex-row sm:items-center gap-3 w-full sm:w-auto">
        <!-- Cari Judul -->
        <div class="w-full sm:w-64">
          <BaseInput
            v-model="searchQuery"
            placeholder="Cari judul posisi..."
            class="text-sm"
          />
        </div>

        <!-- Filter Departemen -->
        <div class="w-full sm:w-60">
          <BaseSelect
            :model-value="selectedDeptFilter"
            :options="departmentStore.allDepartments"
            option-label="name"
            option-value="id"
            placeholder="Semua Departemen"
            show-clear
            filter
            @update:model-value="handleFilterChange"
          />
        </div>
      </div>

      <div class="flex items-center gap-2 self-end sm:self-auto">
        <BaseButton
          icon="pi pi-refresh"
          variant="secondary"
          size="small"
          outlined
          title="Muat ulang data"
          :loading="positionStore.loading"
          @click="loadData"
        />
      </div>
    </div>

    <!-- Tabel Data Posisi -->
    <BaseTable
      :value="filteredPositions"
      :columns="tableColumns"
      :loading="positionStore.loading"
      :rows="10"
      empty-message="Belum ada data posisi. Klik 'Tambah Posisi' untuk membuat baru."
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
      :title="isEditing ? 'Ubah Posisi' : 'Tambah Posisi Baru'"
    >
      <form class="space-y-4" @submit.prevent="handleSubmit">
        <BaseInput
          id="position-title"
          v-model="form.title"
          label="Judul Posisi"
          placeholder="Contoh: Backend Engineer, HR Specialist"
          :error="errors.title"
          required
        />

        <BaseSelect
          id="position-dept"
          v-model="form.departmentId"
          :options="departmentStore.allDepartments"
          option-label="name"
          option-value="id"
          label="Departemen"
          placeholder="Pilih departemen terkait"
          :error="errors.departmentId"
          filter
          required
        />
      </form>

      <template #footer>
        <BaseButton
          label="Batal"
          variant="secondary"
          outlined
          @click="isModalOpen = false"
        />
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
