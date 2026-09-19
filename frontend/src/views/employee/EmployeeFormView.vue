<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import { useToast } from 'primevue/usetoast'
import { useEmployeeStore } from '@/stores/employeeStore'
import { useDepartmentStore } from '@/stores/departmentStore'
import positionApi from '@/api/positionApi'
import { EMPLOYMENT_STATUS_OPTIONS, ROLE_OPTIONS } from '@/utils/constants'
import BaseButton from '@/components/common/BaseButton.vue'
import BaseInput from '@/components/common/BaseInput.vue'
import BaseSelect from '@/components/common/BaseSelect.vue'

const route = useRoute()
const router = useRouter()
const toast = useToast()
const employeeStore = useEmployeeStore()
const departmentStore = useDepartmentStore()

const employeeId = computed(() => route.params.id)
const isEditing = computed(() => !!employeeId.value)

const loading = ref(false)
const submitting = ref(false)
const departmentPositions = ref([])

const form = ref({
  fullName: '',
  email: '',
  phone: '',
  departmentId: null,
  positionId: null,
  managerId: null,
  joinDate: new Date().toISOString().split('T')[0],
  employmentStatus: 'ACTIVE',
  role: 'EMPLOYEE',
  leaveBalance: 12,
  password: '',
})

const errors = ref({
  fullName: '',
  email: '',
  departmentId: '',
  positionId: '',
  joinDate: '',
  employmentStatus: '',
  role: '',
})

// Opsi Manager: seluruh karyawan aktif, mengecualikan diri sendiri saat mode edit
const availableManagers = computed(() => {
  return employeeStore.activeEmployees
    .filter((emp) => emp.id !== employeeId.value)
    .map((emp) => ({
      id: emp.id,
      name: `${emp.fullName} (${emp.positionTitle || emp.departmentName || 'Karyawan'})`,
    }))
})

// Ketika departemen berubah, ambil daftar posisi untuk departemen tersebut
watch(
  () => form.value.departmentId,
  async (newDeptId, oldDeptId) => {
    if (!newDeptId) {
      departmentPositions.value = []
      form.value.positionId = null
      return
    }

    // Jika user mengganti departemen yang berbeda, reset pilihan posisi
    if (oldDeptId && newDeptId !== oldDeptId) {
      form.value.positionId = null
    }

    try {
      const res = await positionApi.getPositions({ departmentId: newDeptId, page: 0, size: 100 })
      if (res.success && res.data) {
        departmentPositions.value = res.data.content || []
      }
    } catch (err) {
      console.error('Failed to load positions for department:', err)
      departmentPositions.value = []
    }
  }
)

onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([
      departmentStore.fetchAllDepartments(),
      employeeStore.fetchActiveEmployees(),
    ])

    if (isEditing.value) {
      const data = await employeeStore.getEmployeeById(employeeId.value)
      if (data) {
        form.value = {
          fullName: data.fullName || '',
          email: data.email || '',
          phone: data.phone || '',
          departmentId: data.departmentId,
          positionId: data.positionId,
          managerId: data.managerId || null,
          joinDate: data.joinDate || '',
          employmentStatus: data.employmentStatus || 'ACTIVE',
          role: data.role || 'EMPLOYEE',
          leaveBalance: data.leaveBalance ?? 12,
          password: '',
        }
      }
    }
  } catch (err) {
    toast.add({
      severity: 'error',
      summary: 'Gagal Memuat Data',
      detail: err.message || 'Terjadi kesalahan saat memuat data karyawan.',
      life: 4000,
    })
    router.push('/employees')
  } finally {
    loading.value = false
  }
})

function validateForm() {
  let valid = true
  errors.value = {
    fullName: '',
    email: '',
    departmentId: '',
    positionId: '',
    joinDate: '',
    employmentStatus: '',
    role: '',
  }

  if (!form.value.fullName.trim()) {
    errors.value.fullName = 'Nama lengkap wajib diisi'
    valid = false
  }

  if (!form.value.email.trim()) {
    errors.value.email = 'Email wajib diisi'
    valid = false
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.email.trim())) {
    errors.value.email = 'Format email tidak valid'
    valid = false
  }

  if (!form.value.departmentId) {
    errors.value.departmentId = 'Departemen wajib dipilih'
    valid = false
  }

  if (!form.value.positionId) {
    errors.value.positionId = 'Posisi wajib dipilih'
    valid = false
  }

  if (!form.value.joinDate) {
    errors.value.joinDate = 'Tanggal bergabung wajib diisi'
    valid = false
  }

  if (!form.value.employmentStatus) {
    errors.value.employmentStatus = 'Status kepegawaian wajib dipilih'
    valid = false
  }

  if (!form.value.role) {
    errors.value.role = 'Role wajib dipilih'
    valid = false
  }

  return valid
}

async function handleSubmit() {
  if (!validateForm()) return

  submitting.value = true
  try {
    const payload = {
      fullName: form.value.fullName.trim(),
      email: form.value.email.trim().toLowerCase(),
      phone: form.value.phone.trim() || null,
      departmentId: Number(form.value.departmentId),
      positionId: Number(form.value.positionId),
      managerId: form.value.managerId || null,
      joinDate: form.value.joinDate,
      employmentStatus: form.value.employmentStatus,
      role: form.value.role,
      leaveBalance: Number(form.value.leaveBalance) || 0,
      password: form.value.password.trim() || undefined,
    }

    if (isEditing.value) {
      await employeeStore.updateEmployee(employeeId.value, payload)
      toast.add({
        severity: 'success',
        summary: 'Berhasil Diperbarui',
        detail: 'Data karyawan berhasil disimpan.',
        life: 3000,
      })
    } else {
      await employeeStore.createEmployee(payload)
      toast.add({
        severity: 'success',
        summary: 'Berhasil Ditambahkan',
        detail: 'Karyawan baru berhasil didaftarkan.',
        life: 3000,
      })
    }

    router.push('/employees')
  } catch (err) {
    if (err.message && err.message.toLowerCase().includes('already exists')) {
      errors.value.email = 'Email sudah digunakan oleh karyawan lain'
    } else {
      toast.add({
        severity: 'error',
        summary: 'Gagal Menyimpan',
        detail: err.message || 'Terjadi kesalahan saat menyimpan data.',
        life: 4000,
      })
    }
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="max-w-4xl mx-auto space-y-6 pb-12">
    <!-- Header Navigasi Form -->
    <div class="flex items-center justify-between">
      <div class="flex items-center gap-3">
        <RouterLink to="/employees">
          <BaseButton
            icon="pi pi-arrow-left"
            variant="secondary"
            size="small"
            outlined
            title="Kembali ke Daftar Karyawan"
          />
        </RouterLink>
        <div>
          <h1 class="text-2xl font-bold text-slate-800 tracking-tight">
            {{ isEditing ? 'Ubah Data Karyawan' : 'Tambah Karyawan Baru' }}
          </h1>
          <p class="text-xs text-slate-500 mt-0.5">
            {{ isEditing ? 'Perbarui informasi dan struktur organisasi karyawan' : 'Isi formulir lengkap untuk mendaftarkan karyawan baru ke dalam sistem' }}
          </p>
        </div>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="bg-white p-12 rounded-xl border border-slate-200 text-center text-slate-500">
      <i class="pi pi-spin pi-spinner text-3xl text-indigo-600 mb-3 block"></i>
      Memuat formulir...
    </div>

    <!-- Form Container -->
    <form v-else class="space-y-6" @submit.prevent="handleSubmit">
      <!-- Section 1: Informasi Pribadi -->
      <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-2xs space-y-4">
        <div class="border-b border-slate-100 pb-3">
          <h2 class="font-bold text-slate-800 text-base flex items-center gap-2">
            <i class="pi pi-id-card text-indigo-600"></i>
            Informasi Pribadi & Kontak
          </h2>
          <p class="text-xs text-slate-500 mt-0.5">Identitas dasar karyawan untuk pencatatan dan korespondensi</p>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div class="sm:col-span-2">
            <BaseInput
              id="full-name"
              v-model="form.fullName"
              label="Nama Lengkap"
              placeholder="Contoh: Ahmad Fauzi"
              :error="errors.fullName"
              required
            />
          </div>

          <div>
            <BaseInput
              id="email"
              v-model="form.email"
              type="email"
              label="Alamat Email"
              placeholder="nama@elms.com"
              help-text="Email ini juga akan digunakan sebagai akun login"
              :error="errors.email"
              required
            />
          </div>

          <div>
            <BaseInput
              id="phone"
              v-model="form.phone"
              label="Nomor Telepon"
              placeholder="Contoh: +628123456789"
            />
          </div>
        </div>
      </div>

      <!-- Section 2: Struktur Organisasi & Penempatan -->
      <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-2xs space-y-4">
        <div class="border-b border-slate-100 pb-3">
          <h2 class="font-bold text-slate-800 text-base flex items-center gap-2">
            <i class="pi pi-sitemap text-indigo-600"></i>
            Penempatan Organisasi & Relasi Atasan
          </h2>
          <p class="text-xs text-slate-500 mt-0.5">Departemen, jabatan, dan garis pelaporan atasan langsung (Manager)</p>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <!-- Dropdown Departemen -->
          <div>
            <BaseSelect
              id="department"
              v-model="form.departmentId"
              :options="departmentStore.allDepartments"
              option-label="name"
              option-value="id"
              label="Departemen"
              placeholder="Pilih Departemen"
              :error="errors.departmentId"
              filter
              required
            />
          </div>

          <!-- Dropdown Posisi (terfilter berdasar departemen) -->
          <div>
            <BaseSelect
              id="position"
              v-model="form.positionId"
              :options="departmentPositions"
              option-label="title"
              option-value="id"
              label="Posisi / Jabatan"
              :placeholder="form.departmentId ? 'Pilih Posisi' : 'Pilih Departemen terlebih dahulu'"
              :disabled="!form.departmentId"
              :error="errors.positionId"
              filter
              required
            />
          </div>

          <!-- Dropdown Atasan Langsung (Manager) -->
          <div class="sm:col-span-2">
            <BaseSelect
              id="manager"
              v-model="form.managerId"
              :options="availableManagers"
              option-label="name"
              option-value="id"
              label="Atasan Langsung (Manager)"
              placeholder="Pilih Manager (opsional jika tingkat teratas)"
              show-clear
              filter
            />
            <small class="text-xs text-slate-500 block mt-1">
              Atasan ini yang akan menerima dan menyetujui pengajuan cuti serta mengisi evaluasi kinerja karyawan.
            </small>
          </div>
        </div>
      </div>

      <!-- Section 3: Status & Hak Akses -->
      <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-2xs space-y-4">
        <div class="border-b border-slate-100 pb-3">
          <h2 class="font-bold text-slate-800 text-base flex items-center gap-2">
            <i class="pi pi-shield text-indigo-600"></i>
            Status Kepegawaian & Akses Sistem
          </h2>
          <p class="text-xs text-slate-500 mt-0.5">Peran hak akses, status siklus kerja, dan jatah cuti awal tahun</p>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <BaseInput
              id="join-date"
              v-model="form.joinDate"
              type="date"
              label="Tanggal Bergabung"
              :error="errors.joinDate"
              required
            />
          </div>

          <div>
            <BaseInput
              id="leave-balance"
              v-model="form.leaveBalance"
              type="number"
              label="Saldo Jatah Cuti (Hari)"
              placeholder="12"
            />
          </div>

          <div>
            <BaseSelect
              id="status"
              v-model="form.employmentStatus"
              :options="EMPLOYMENT_STATUS_OPTIONS"
              option-label="label"
              option-value="value"
              label="Status Kepegawaian"
              :error="errors.employmentStatus"
              required
            />
          </div>

          <div>
            <BaseSelect
              id="role"
              v-model="form.role"
              :options="ROLE_OPTIONS"
              option-label="label"
              option-value="value"
              label="Peran / Hak Akses (Role)"
              :error="errors.role"
              required
            />
          </div>

          <div class="sm:col-span-2">
            <BaseInput
              id="password"
              v-model="form.password"
              type="password"
              label="Kata Sandi (Password)"
              :placeholder="isEditing ? 'Kosongkan jika tidak ingin mengubah password' : 'Masukkan password akun (default: password123)'"
              :help-text="isEditing ? 'Biarkan kosong untuk mempertahankan password lama' : 'Password awal yang dipakai karyawan untuk login ke sistem'"
            />
          </div>
        </div>
      </div>

      <!-- Tombol Aksi Form -->
      <div class="flex items-center justify-end gap-3 pt-2">
        <RouterLink to="/employees">
          <BaseButton label="Batal" variant="secondary" outlined />
        </RouterLink>
        <BaseButton
          :label="isEditing ? 'Simpan Perubahan' : 'Daftarkan Karyawan'"
          type="submit"
          variant="primary"
          icon="pi pi-check"
          :loading="submitting"
        />
      </div>
    </form>
  </div>
</template>
