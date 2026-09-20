<script setup>
import { ref, onMounted } from 'vue'
import { useEmployeeStore } from '@/stores/employeeStore'
import { useNotify } from '@/composables/useNotify'
import StatusBadge from '@/components/common/StatusBadge.vue'
import BaseButton from '@/components/common/BaseButton.vue'

const employeeStore = useEmployeeStore()
const notify = useNotify()

const profile = ref(null)
const loading = ref(true)

async function loadProfile() {
  loading.value = true
  try {
    const data = await employeeStore.getMyProfile()
    profile.value = data
  } catch (err) {
    notify.showError(err.message || 'Gagal memuat profil', 'Error')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<template>
  <div class="max-w-4xl mx-auto space-y-6">
    <!-- Header Halaman -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-slate-800 tracking-tight">Profil Saya</h1>
        <p class="text-sm text-slate-500 mt-0.5">
          Informasi akun, posisi organisasi, dan sisa saldo cuti Anda
        </p>
      </div>
      <BaseButton
        icon="pi pi-refresh"
        label="Muat Ulang"
        variant="secondary"
        size="small"
        :loading="loading"
        @click="loadProfile"
      />
    </div>

    <div
      v-if="loading && !profile"
      class="bg-white rounded-xl border border-slate-200 p-12 text-center text-slate-400"
    >
      <i class="pi pi-spin pi-spinner text-3xl mb-3 text-indigo-600"></i>
      <p class="text-sm">Memuat profil karyawan...</p>
    </div>

    <div v-else-if="profile" class="space-y-6">
      <!-- Kartu Ringkasan Pengguna -->
      <div
        class="bg-white rounded-2xl border border-slate-200 p-6 shadow-2xs flex flex-col sm:flex-row items-center gap-6"
      >
        <div
          class="w-20 h-20 rounded-full bg-indigo-100 text-indigo-700 font-bold text-2xl flex items-center justify-center shrink-0 border-2 border-indigo-200"
        >
          {{ profile.fullName?.charAt(0) || 'U' }}
        </div>
        <div class="flex-1 text-center sm:text-left space-y-1">
          <div class="flex flex-col sm:flex-row sm:items-center gap-2">
            <h2 class="text-xl font-bold text-slate-800">{{ profile.fullName }}</h2>
            <div class="flex justify-center sm:justify-start gap-2">
              <StatusBadge :status="profile.role" />
              <StatusBadge :status="profile.employmentStatus" />
            </div>
          </div>
          <p class="text-sm text-slate-500">
            {{ profile.email }} &bull; {{ profile.phone || 'Belum ada nomor telepon' }}
          </p>
          <p class="text-xs text-indigo-600 font-medium pt-1">
            {{ profile.positionTitle }} &bull; {{ profile.departmentName }}
          </p>
        </div>

        <!-- Kartu Sisa Saldo Cuti -->
        <div
          class="w-full sm:w-auto bg-indigo-50/70 border border-indigo-100 rounded-xl p-4 text-center sm:text-right shrink-0"
        >
          <span class="text-xs font-semibold uppercase tracking-wider text-indigo-600 block"
            >Sisa Saldo Cuti</span
          >
          <span class="text-3xl font-extrabold text-indigo-700 block mt-0.5">
            {{ profile.leaveBalance ?? 0 }}
            <span class="text-sm font-medium text-indigo-500">hari</span>
          </span>
          <span class="text-[11px] text-slate-500 block mt-1">Tahun berjalan</span>
        </div>
      </div>

      <!-- Detail Informasi Lengkap -->
      <div class="bg-white rounded-2xl border border-slate-200 p-6 shadow-2xs space-y-6">
        <h3 class="text-base font-bold text-slate-800 border-b border-slate-100 pb-3">
          Informasi Kepegawaian & Organisasi
        </h3>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-6 text-sm">
          <div class="space-y-1">
            <span class="text-xs text-slate-400 font-medium">Departemen</span>
            <p class="font-semibold text-slate-700">{{ profile.departmentName || '-' }}</p>
          </div>

          <div class="space-y-1">
            <span class="text-xs text-slate-400 font-medium">Posisi / Jabatan</span>
            <p class="font-semibold text-slate-700">{{ profile.positionTitle || '-' }}</p>
          </div>

          <div class="space-y-1">
            <span class="text-xs text-slate-400 font-medium">Atasan Langsung (Manager)</span>
            <p class="font-semibold text-slate-700">
              {{ profile.managerName ? profile.managerName : 'Tidak memiliki atasan langsung' }}
            </p>
          </div>

          <div class="space-y-1">
            <span class="text-xs text-slate-400 font-medium">Tanggal Bergabung</span>
            <p class="font-semibold text-slate-700">{{ profile.joinDate || '-' }}</p>
          </div>

          <div class="space-y-1">
            <span class="text-xs text-slate-400 font-medium">Tingkat Akses (Role)</span>
            <p class="font-semibold text-slate-700">{{ profile.role }}</p>
          </div>

          <div class="space-y-1">
            <span class="text-xs text-slate-400 font-medium">Status Kepegawaian</span>
            <p class="font-semibold text-slate-700">{{ profile.employmentStatus }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
