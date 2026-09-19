<script setup>
import { onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { useDepartmentStore } from '@/stores/departmentStore'
import { usePositionStore } from '@/stores/positionStore'
import BaseButton from '@/components/common/BaseButton.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'

const authStore = useAuthStore()
const departmentStore = useDepartmentStore()
const positionStore = usePositionStore()

onMounted(async () => {
  try {
    await Promise.all([
      departmentStore.fetchDepartments(0, 10),
      positionStore.fetchPositions(null, 0, 10),
    ])
  } catch (e) {
    console.error('Error loading dashboard stats:', e)
  }
})
</script>

<template>
  <div class="space-y-6">
    <!-- Welcome Banner -->
    <div class="bg-white rounded-xl border border-slate-200 p-6 shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div class="space-y-1">
        <div class="flex items-center gap-2">
          <h1 class="text-xl sm:text-2xl font-bold text-slate-800 tracking-tight">
            Selamat Datang, {{ authStore.user?.name }}!
          </h1>
          <StatusBadge :status="authStore.currentRole" />
        </div>
        <p class="text-sm text-slate-500">
          Employee Lifecycle Management System (ELMS) — Dasbor operasional HR & siklus karyawan.
        </p>
      </div>

      <div class="flex items-center gap-2">
        <RouterLink to="/departments">
          <BaseButton label="Kelola Departemen" icon="pi pi-building" variant="primary" size="small" />
        </RouterLink>
        <RouterLink to="/positions">
          <BaseButton label="Kelola Posisi" icon="pi pi-briefcase" variant="secondary" size="small" outlined />
        </RouterLink>
      </div>
    </div>

    <!-- Quick Stat Cards -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <!-- Card Departemen -->
      <div class="bg-white p-5 rounded-xl border border-slate-200 shadow-2xs flex items-center justify-between">
        <div>
          <span class="text-xs font-semibold uppercase tracking-wider text-slate-400">Total Departemen</span>
          <div class="text-2xl font-bold text-slate-800 mt-1">
            {{ departmentStore.totalElements }}
          </div>
          <RouterLink to="/departments" class="text-xs font-medium text-indigo-600 hover:text-indigo-800 mt-2 inline-block">
            Lihat data &rarr;
          </RouterLink>
        </div>
        <div class="w-12 h-12 rounded-lg bg-indigo-50 text-indigo-600 flex items-center justify-center text-xl">
          <i class="pi pi-building"></i>
        </div>
      </div>

      <!-- Card Posisi -->
      <div class="bg-white p-5 rounded-xl border border-slate-200 shadow-2xs flex items-center justify-between">
        <div>
          <span class="text-xs font-semibold uppercase tracking-wider text-slate-400">Total Posisi</span>
          <div class="text-2xl font-bold text-slate-800 mt-1">
            {{ positionStore.totalElements }}
          </div>
          <RouterLink to="/positions" class="text-xs font-medium text-indigo-600 hover:text-indigo-800 mt-2 inline-block">
            Lihat data &rarr;
          </RouterLink>
        </div>
        <div class="w-12 h-12 rounded-lg bg-emerald-50 text-emerald-600 flex items-center justify-center text-xl">
          <i class="pi pi-briefcase"></i>
        </div>
      </div>

      <!-- Card Karyawan (Placeholder status) -->
      <div class="bg-white p-5 rounded-xl border border-slate-200 shadow-2xs flex items-center justify-between">
        <div>
          <span class="text-xs font-semibold uppercase tracking-wider text-slate-400">Karyawan Aktif</span>
          <div class="text-2xl font-bold text-slate-800 mt-1">
            -
          </div>
          <span class="text-xs text-slate-400 mt-2 inline-block">
            Modul Employee Segera
          </span>
        </div>
        <div class="w-12 h-12 rounded-lg bg-blue-50 text-blue-600 flex items-center justify-center text-xl">
          <i class="pi pi-users"></i>
        </div>
      </div>

      <!-- Card Cuti (Placeholder status) -->
      <div class="bg-white p-5 rounded-xl border border-slate-200 shadow-2xs flex items-center justify-between">
        <div>
          <span class="text-xs font-semibold uppercase tracking-wider text-slate-400">Pengajuan Cuti</span>
          <div class="text-2xl font-bold text-slate-800 mt-1">
            -
          </div>
          <span class="text-xs text-slate-400 mt-2 inline-block">
            Modul Leave Segera
          </span>
        </div>
        <div class="w-12 h-12 rounded-lg bg-amber-50 text-amber-600 flex items-center justify-center text-xl">
          <i class="pi pi-calendar-minus"></i>
        </div>
      </div>
    </div>

    <!-- Quick Preview Grid: Departemen & Posisi Terkini -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- Tabel Departemen Singkat -->
      <div class="bg-white rounded-xl border border-slate-200 shadow-2xs p-5">
        <div class="flex items-center justify-between mb-4">
          <div class="flex items-center gap-2">
            <i class="pi pi-building text-indigo-600"></i>
            <h2 class="font-bold text-slate-800 text-base">Departemen Terbaru</h2>
          </div>
          <RouterLink to="/departments" class="text-xs text-indigo-600 font-medium hover:underline">
            Semua ({{ departmentStore.totalElements }})
          </RouterLink>
        </div>

        <div v-if="departmentStore.departments.length === 0" class="py-6 text-center text-slate-400 text-sm">
          Belum ada data departemen.
        </div>
        <ul v-else class="divide-y divide-slate-100 text-sm">
          <li
            v-for="dept in departmentStore.departments.slice(0, 5)"
            :key="dept.id"
            class="py-2.5 flex items-center justify-between"
          >
            <span class="font-medium text-slate-700">{{ dept.name }}</span>
            <span class="text-xs text-slate-400">ID: {{ dept.id }}</span>
          </li>
        </ul>
      </div>

      <!-- Tabel Posisi Singkat -->
      <div class="bg-white rounded-xl border border-slate-200 shadow-2xs p-5">
        <div class="flex items-center justify-between mb-4">
          <div class="flex items-center gap-2">
            <i class="pi pi-briefcase text-emerald-600"></i>
            <h2 class="font-bold text-slate-800 text-base">Posisi Terbaru</h2>
          </div>
          <RouterLink to="/positions" class="text-xs text-indigo-600 font-medium hover:underline">
            Semua ({{ positionStore.totalElements }})
          </RouterLink>
        </div>

        <div v-if="positionStore.positions.length === 0" class="py-6 text-center text-slate-400 text-sm">
          Belum ada data posisi.
        </div>
        <ul v-else class="divide-y divide-slate-100 text-sm">
          <li
            v-for="pos in positionStore.positions.slice(0, 5)"
            :key="pos.id"
            class="py-2.5 flex items-center justify-between"
          >
            <div>
              <span class="font-medium text-slate-700 block">{{ pos.title }}</span>
              <span class="text-xs text-slate-400">{{ pos.departmentName }}</span>
            </div>
            <span class="text-xs text-slate-400">ID: {{ pos.id }}</span>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>
