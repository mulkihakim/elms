<script setup>
import { computed, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { useDashboardStore } from '@/stores/dashboardStore'
import BaseButton from '@/components/common/BaseButton.vue'
import BaseChart from '@/components/common/BaseChart.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'

const authStore = useAuthStore()
const dashboardStore = useDashboardStore()

const currentRole = computed(() => authStore.currentRole)
const summary = computed(() => dashboardStore.summary)

onMounted(async () => {
  if (!authStore.isAuthenticated) return
  try {
    await dashboardStore.fetchDashboardData(currentRole.value)
  } catch (e) {
    console.error('Error loading dashboard stats:', e)
  }
})

// Data Chart Distribusi Departemen (Doughnut)
const departmentChartData = computed(() => {
  const dist = dashboardStore.departmentDistribution
  return {
    labels: dist.map((d) => d.departmentName),
    datasets: [
      {
        data: dist.map((d) => d.employeeCount),
        backgroundColor: [
          '#6366f1',
          '#10b981',
          '#f59e0b',
          '#3b82f6',
          '#8b5cf6',
          '#ec4899',
          '#14b8a6',
          '#f97316',
        ],
        hoverOffset: 6,
      },
    ],
  }
})

const doughnutChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'bottom',
      labels: {
        usePointStyle: true,
        boxWidth: 8,
        font: { size: 12 },
      },
    },
  },
}

// Data Chart Presensi Mingguan (Stacked Bar)
const weeklyAttendanceChartData = computed(() => {
  const list = dashboardStore.weeklyAttendance
  return {
    labels: list.map((item) => `${item.dayName} (${item.date ? item.date.slice(5) : ''})`),
    datasets: [
      {
        label: 'Tepat Waktu',
        backgroundColor: '#10b981',
        borderRadius: 4,
        data: list.map((item) => item.presentCount),
      },
      {
        label: 'Terlambat',
        backgroundColor: '#f59e0b',
        borderRadius: 4,
        data: list.map((item) => item.lateCount),
      },
      {
        label: 'Sedang Cuti',
        backgroundColor: '#818cf8',
        borderRadius: 4,
        data: list.map((item) => item.onLeaveCount),
      },
    ],
  }
})

const barChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'bottom',
      labels: {
        usePointStyle: true,
        boxWidth: 8,
        font: { size: 12 },
      },
    },
  },
  scales: {
    x: {
      stacked: true,
      grid: { display: false },
    },
    y: {
      stacked: true,
      beginAtZero: true,
      ticks: {
        stepSize: 1,
        precision: 0,
      },
    },
  },
}
</script>

<template>
  <div v-if="authStore.isAuthenticated" class="space-y-6">
    <!-- Welcome Banner (Role-Aware) -->
    <div
      class="bg-white rounded-xl border border-slate-200 p-6 shadow-2xs flex flex-col md:flex-row md:items-center justify-between gap-4"
    >
      <div class="space-y-1.5">
        <div class="flex items-center gap-2.5">
          <h1 class="text-xl sm:text-2xl font-bold text-slate-800 tracking-tight">
            Selamat Datang, {{ authStore.user?.fullName || authStore.user?.name }}!
          </h1>
          <StatusBadge :status="currentRole" />
        </div>
        <p class="text-sm text-slate-500">
          <template v-if="currentRole === 'HR'">
            Ringkasan operasional SDM, data kehadiran organisasi, cuti, dan manajemen karyawan.
          </template>
          <template v-else-if="currentRole === 'MANAGER'">
            Ringkasan tim kerja bawahan langsung, pemantauan presensi harian, dan persetujuan cuti tim.
          </template>
          <template v-else>
            Beranda personal Anda: status presensi hari ini, sisa saldo cuti, dan perkembangan evaluasi kinerja.
          </template>
        </p>
      </div>

      <!-- Quick Actions (Role-Aware) -->
      <div class="flex items-center flex-wrap gap-2">
        <!-- HR Actions -->
        <template v-if="currentRole === 'HR'">
          <RouterLink to="/employees">
            <BaseButton label="Kelola Karyawan" icon="pi pi-users" variant="primary" size="small" />
          </RouterLink>
          <RouterLink to="/departments">
            <BaseButton label="Departemen" icon="pi pi-building" variant="secondary" size="small" outlined />
          </RouterLink>
          <RouterLink to="/positions">
            <BaseButton label="Posisi" icon="pi pi-briefcase" variant="secondary" size="small" outlined />
          </RouterLink>
        </template>

        <!-- Manager Actions -->
        <template v-else-if="currentRole === 'MANAGER'">
          <RouterLink to="/leaves">
            <BaseButton label="Persetujuan Cuti Tim" icon="pi pi-check-circle" variant="primary" size="small" />
          </RouterLink>
          <RouterLink to="/reviews">
            <BaseButton label="Review Kinerja Tim" icon="pi pi-star" variant="secondary" size="small" outlined />
          </RouterLink>
        </template>

        <!-- Employee Actions -->
        <template v-else>
          <RouterLink to="/attendance">
            <BaseButton label="Presensi Harian" icon="pi pi-clock" variant="primary" size="small" />
          </RouterLink>
          <RouterLink to="/leaves">
            <BaseButton label="Ajukan Cuti" icon="pi pi-calendar-plus" variant="secondary" size="small" outlined />
          </RouterLink>
        </template>
      </div>
    </div>

    <!-- ==================== ROLE: HR DASHBOARD ==================== -->
    <template v-if="currentRole === 'HR'">
      <!-- HR Stat Cards -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <!-- Total Karyawan -->
        <div class="bg-white p-5 rounded-xl border border-slate-200 shadow-2xs flex items-center justify-between">
          <div>
            <span class="text-xs font-semibold uppercase tracking-wider text-slate-400">Total Karyawan Aktif</span>
            <div class="text-2xl font-bold text-slate-800 mt-1">
              {{ summary?.totalEmployees ?? 0 }}
            </div>
            <RouterLink to="/employees" class="text-xs font-medium text-indigo-600 hover:text-indigo-800 mt-2 inline-block">
              Kelola karyawan &rarr;
            </RouterLink>
          </div>
          <div class="w-12 h-12 rounded-lg bg-blue-50 text-blue-600 flex items-center justify-center text-xl">
            <i class="pi pi-users"></i>
          </div>
        </div>

        <!-- Hadir Hari Ini -->
        <div class="bg-white p-5 rounded-xl border border-slate-200 shadow-2xs flex items-center justify-between">
          <div>
            <span class="text-xs font-semibold uppercase tracking-wider text-slate-400">Hadir Hari Ini</span>
            <div class="text-2xl font-bold text-slate-800 mt-1">
              {{ summary?.presentToday ?? 0 }}
            </div>
            <RouterLink to="/attendance" class="text-xs font-medium text-emerald-600 hover:text-emerald-800 mt-2 inline-block">
              Lihat presensi &rarr;
            </RouterLink>
          </div>
          <div class="w-12 h-12 rounded-lg bg-emerald-50 text-emerald-600 flex items-center justify-center text-xl">
            <i class="pi pi-check-circle"></i>
          </div>
        </div>

        <!-- Sedang Cuti -->
        <div class="bg-white p-5 rounded-xl border border-slate-200 shadow-2xs flex items-center justify-between">
          <div>
            <span class="text-xs font-semibold uppercase tracking-wider text-slate-400">Sedang Cuti Hari Ini</span>
            <div class="text-2xl font-bold text-slate-800 mt-1">
              {{ summary?.onLeaveToday ?? 0 }}
            </div>
            <RouterLink to="/leaves" class="text-xs font-medium text-amber-600 hover:text-amber-800 mt-2 inline-block">
              Data cuti &rarr;
            </RouterLink>
          </div>
          <div class="w-12 h-12 rounded-lg bg-amber-50 text-amber-600 flex items-center justify-center text-xl">
            <i class="pi pi-calendar-minus"></i>
          </div>
        </div>

        <!-- Pengajuan Cuti Menunggu Approval -->
        <div class="bg-white p-5 rounded-xl border border-slate-200 shadow-2xs flex items-center justify-between">
          <div>
            <span class="text-xs font-semibold uppercase tracking-wider text-slate-400">Menunggu Approval Cuti</span>
            <div class="text-2xl font-bold text-slate-800 mt-1">
              {{ summary?.pendingLeaveRequests ?? 0 }}
            </div>
            <RouterLink to="/leaves" class="text-xs font-medium text-purple-600 hover:text-purple-800 mt-2 inline-block">
              Tinjau pengajuan &rarr;
            </RouterLink>
          </div>
          <div class="w-12 h-12 rounded-lg bg-purple-50 text-purple-600 flex items-center justify-center text-xl">
            <i class="pi pi-clock"></i>
          </div>
        </div>
      </div>

      <!-- Charts Grid HR -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- Chart Distribusi Departemen -->
        <div class="bg-white rounded-xl border border-slate-200 shadow-2xs p-5">
          <div class="flex items-center justify-between mb-4">
            <div class="flex items-center gap-2">
              <i class="pi pi-building text-indigo-600"></i>
              <h2 class="font-bold text-slate-800 text-base">Distribusi Karyawan per Departemen</h2>
            </div>
            <RouterLink to="/departments" class="text-xs text-indigo-600 font-medium hover:underline">
              Kelola
            </RouterLink>
          </div>

          <div
            v-if="dashboardStore.departmentDistribution.length === 0"
            class="h-72 flex items-center justify-center text-slate-400 text-sm"
          >
            Belum ada data distribusi departemen.
          </div>
          <div v-else class="h-72">
            <BaseChart type="doughnut" :data="departmentChartData" :options="doughnutChartOptions" height="280px" />
          </div>
        </div>

        <!-- Chart Presensi Mingguan Perusahaan -->
        <div class="bg-white rounded-xl border border-slate-200 shadow-2xs p-5">
          <div class="flex items-center justify-between mb-4">
            <div class="flex items-center gap-2">
              <i class="pi pi-chart-bar text-emerald-600"></i>
              <h2 class="font-bold text-slate-800 text-base">Tren Presensi Mingguan (7 Hari Terakhir)</h2>
            </div>
            <RouterLink to="/attendance" class="text-xs text-indigo-600 font-medium hover:underline">
              Detail
            </RouterLink>
          </div>

          <div
            v-if="dashboardStore.weeklyAttendance.length === 0"
            class="h-72 flex items-center justify-center text-slate-400 text-sm"
          >
            Belum ada data kehadiran mingguan.
          </div>
          <div v-else class="h-72">
            <BaseChart type="bar" :data="weeklyAttendanceChartData" :options="barChartOptions" height="280px" />
          </div>
        </div>
      </div>
    </template>

    <!-- ==================== ROLE: MANAGER DASHBOARD ==================== -->
    <template v-else-if="currentRole === 'MANAGER'">
      <!-- Manager Team Stat Cards -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <!-- Total Tim -->
        <div class="bg-white p-5 rounded-xl border border-slate-200 shadow-2xs flex items-center justify-between">
          <div>
            <span class="text-xs font-semibold uppercase tracking-wider text-slate-400">Total Anggota Tim</span>
            <div class="text-2xl font-bold text-slate-800 mt-1">
              {{ summary?.totalTeamMembers ?? 0 }}
            </div>
            <RouterLink to="/reviews" class="text-xs font-medium text-indigo-600 hover:text-indigo-800 mt-2 inline-block">
              Review tim &rarr;
            </RouterLink>
          </div>
          <div class="w-12 h-12 rounded-lg bg-blue-50 text-blue-600 flex items-center justify-center text-xl">
            <i class="pi pi-users"></i>
          </div>
        </div>

        <!-- Tim Hadir Hari Ini -->
        <div class="bg-white p-5 rounded-xl border border-slate-200 shadow-2xs flex items-center justify-between">
          <div>
            <span class="text-xs font-semibold uppercase tracking-wider text-slate-400">Tim Hadir Hari Ini</span>
            <div class="text-2xl font-bold text-slate-800 mt-1">
              {{ summary?.teamPresentToday ?? 0 }}
            </div>
            <span class="text-xs text-slate-400 mt-2 inline-block">dari {{ summary?.totalTeamMembers ?? 0 }} anggota</span>
          </div>
          <div class="w-12 h-12 rounded-lg bg-emerald-50 text-emerald-600 flex items-center justify-center text-xl">
            <i class="pi pi-check-circle"></i>
          </div>
        </div>

        <!-- Tim Sedang Cuti -->
        <div class="bg-white p-5 rounded-xl border border-slate-200 shadow-2xs flex items-center justify-between">
          <div>
            <span class="text-xs font-semibold uppercase tracking-wider text-slate-400">Tim Cuti Hari Ini</span>
            <div class="text-2xl font-bold text-slate-800 mt-1">
              {{ summary?.teamOnLeaveToday ?? 0 }}
            </div>
            <RouterLink to="/leaves" class="text-xs font-medium text-amber-600 hover:text-amber-800 mt-2 inline-block">
              Persetujuan cuti &rarr;
            </RouterLink>
          </div>
          <div class="w-12 h-12 rounded-lg bg-amber-50 text-amber-600 flex items-center justify-center text-xl">
            <i class="pi pi-calendar-minus"></i>
          </div>
        </div>

        <!-- Pengajuan Cuti Tim Menunggu Approval -->
        <div class="bg-white p-5 rounded-xl border border-slate-200 shadow-2xs flex items-center justify-between">
          <div>
            <span class="text-xs font-semibold uppercase tracking-wider text-slate-400">Cuti Perlu Disetujui</span>
            <div class="text-2xl font-bold text-slate-800 mt-1">
              {{ summary?.teamPendingLeaveRequests ?? 0 }}
            </div>
            <RouterLink to="/leaves" class="text-xs font-medium text-purple-600 hover:text-purple-800 mt-2 inline-block">
              Setujui sekarang &rarr;
            </RouterLink>
          </div>
          <div class="w-12 h-12 rounded-lg bg-purple-50 text-purple-600 flex items-center justify-center text-xl">
            <i class="pi pi-bell"></i>
          </div>
        </div>
      </div>

      <!-- Charts & Personal Overview Manager -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- Chart Presensi Tim (2 Kolom) -->
        <div class="lg:col-span-2 bg-white rounded-xl border border-slate-200 shadow-2xs p-5">
          <div class="flex items-center justify-between mb-4">
            <div class="flex items-center gap-2">
              <i class="pi pi-chart-bar text-indigo-600"></i>
              <h2 class="font-bold text-slate-800 text-base">Tren Presensi Tim Mingguan</h2>
            </div>
            <span class="text-xs text-slate-400">7 Hari Terakhir</span>
          </div>

          <div
            v-if="dashboardStore.weeklyAttendance.length === 0"
            class="h-72 flex items-center justify-center text-slate-400 text-sm"
          >
            Belum ada data kehadiran tim.
          </div>
          <div v-else class="h-72">
            <BaseChart type="bar" :data="weeklyAttendanceChartData" :options="barChartOptions" height="280px" />
          </div>
        </div>

        <!-- Card Personal Status Manager (1 Kolom) -->
        <div class="bg-white rounded-xl border border-slate-200 shadow-2xs p-5 flex flex-col justify-between">
          <div>
            <h2 class="font-bold text-slate-800 text-base mb-4 flex items-center gap-2">
              <i class="pi pi-user text-indigo-600"></i>
              Status Pribadi Anda
            </h2>
            <div class="space-y-4 text-sm">
              <div class="p-3 bg-slate-50 rounded-lg border border-slate-100">
                <div class="text-xs text-slate-500 font-medium">Presensi Anda Hari Ini</div>
                <div class="mt-1 flex items-center justify-between">
                  <span v-if="summary?.checkedInToday" class="font-semibold text-emerald-600">
                    Hadir ({{ summary.checkInTime?.slice(0, 5) }})
                  </span>
                  <span v-else class="text-slate-400 font-medium">Belum Check-in</span>
                  <StatusBadge v-if="summary?.attendanceStatus" :status="summary.attendanceStatus" />
                </div>
              </div>

              <div class="p-3 bg-slate-50 rounded-lg border border-slate-100">
                <div class="text-xs text-slate-500 font-medium">Sisa Saldo Cuti Pribadi</div>
                <div class="text-lg font-bold text-slate-800 mt-0.5">
                  {{ summary?.remainingLeaveBalance ?? 0 }} Hari
                </div>
              </div>
            </div>
          </div>

          <div class="pt-4 border-t border-slate-100 mt-4 flex flex-col gap-2">
            <RouterLink to="/attendance">
              <BaseButton label="Presensi Harian" icon="pi pi-clock" variant="primary" size="small" class="w-full" />
            </RouterLink>
            <RouterLink to="/leaves">
              <BaseButton label="Ajukan Cuti Sendiri" icon="pi pi-calendar-plus" variant="secondary" size="small" outlined class="w-full" />
            </RouterLink>
          </div>
        </div>
      </div>
    </template>

    <!-- ==================== ROLE: EMPLOYEE DASHBOARD ==================== -->
    <template v-else>
      <!-- Employee Personal Cards -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <!-- Status Hadir Hari Ini -->
        <div class="bg-white p-5 rounded-xl border border-slate-200 shadow-2xs flex items-center justify-between">
          <div>
            <span class="text-xs font-semibold uppercase tracking-wider text-slate-400">Presensi Hari Ini</span>
            <div class="mt-1">
              <template v-if="summary?.checkedInToday">
                <div class="text-xl font-bold text-emerald-600">
                  {{ summary.checkInTime?.slice(0, 5) }} WIB
                </div>
                <div class="mt-1">
                  <StatusBadge :status="summary.attendanceStatus || 'ON_TIME'" />
                </div>
              </template>
              <template v-else>
                <div class="text-lg font-bold text-slate-400">Belum Hadir</div>
                <RouterLink to="/attendance" class="text-xs font-medium text-indigo-600 hover:underline mt-1 inline-block">
                  Check-in sekarang &rarr;
                </RouterLink>
              </template>
            </div>
          </div>
          <div class="w-12 h-12 rounded-lg bg-emerald-50 text-emerald-600 flex items-center justify-center text-xl">
            <i class="pi pi-clock"></i>
          </div>
        </div>

        <!-- Sisa Saldo Cuti -->
        <div class="bg-white p-5 rounded-xl border border-slate-200 shadow-2xs flex items-center justify-between">
          <div>
            <span class="text-xs font-semibold uppercase tracking-wider text-slate-400">Sisa Saldo Cuti</span>
            <div class="text-2xl font-bold text-slate-800 mt-1">
              {{ summary?.remainingLeaveBalance ?? 0 }} <span class="text-sm font-normal text-slate-500">Hari</span>
            </div>
            <RouterLink to="/leaves" class="text-xs font-medium text-indigo-600 hover:text-indigo-800 mt-2 inline-block">
              Ajukan cuti &rarr;
            </RouterLink>
          </div>
          <div class="w-12 h-12 rounded-lg bg-indigo-50 text-indigo-600 flex items-center justify-center text-xl">
            <i class="pi pi-calendar"></i>
          </div>
        </div>

        <!-- Status Cuti Terakhir -->
        <div class="bg-white p-5 rounded-xl border border-slate-200 shadow-2xs flex items-center justify-between">
          <div>
            <span class="text-xs font-semibold uppercase tracking-wider text-slate-400">Cuti Terakhir</span>
            <div class="mt-1.5">
              <StatusBadge v-if="summary?.latestLeaveStatus" :status="summary.latestLeaveStatus" />
              <span v-else class="text-sm text-slate-400">Belum ada pengajuan</span>
            </div>
            <RouterLink to="/leaves" class="text-xs font-medium text-slate-500 hover:text-slate-700 mt-2 inline-block">
              Lihat riwayat &rarr;
            </RouterLink>
          </div>
          <div class="w-12 h-12 rounded-lg bg-amber-50 text-amber-600 flex items-center justify-center text-xl">
            <i class="pi pi-file"></i>
          </div>
        </div>

        <!-- Evaluasi Kinerja Terakhir -->
        <div class="bg-white p-5 rounded-xl border border-slate-200 shadow-2xs flex items-center justify-between">
          <div>
            <span class="text-xs font-semibold uppercase tracking-wider text-slate-400">Review Kinerja</span>
            <div class="text-2xl font-bold text-slate-800 mt-1">
              <template v-if="summary?.latestReviewScore != null">
                {{ summary.latestReviewScore.toFixed(2) }} <span class="text-xs font-normal text-slate-400">/ 5.0</span>
              </template>
              <template v-else>
                <span class="text-sm font-normal text-slate-400">Belum dinilai</span>
              </template>
            </div>
            <RouterLink to="/reviews" class="text-xs font-medium text-purple-600 hover:text-purple-800 mt-2 inline-block">
              Detail review &rarr;
            </RouterLink>
          </div>
          <div class="w-12 h-12 rounded-lg bg-purple-50 text-purple-600 flex items-center justify-center text-xl">
            <i class="pi pi-star"></i>
          </div>
        </div>
      </div>

      <!-- Panduan / Quick Overview Karyawan -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div class="bg-white rounded-xl border border-slate-200 shadow-2xs p-5">
          <h2 class="font-bold text-slate-800 text-base mb-3 flex items-center gap-2">
            <i class="pi pi-info-circle text-indigo-600"></i>
            Ketentuan Jam Kerja & Presensi
          </h2>
          <ul class="text-sm text-slate-600 space-y-2 list-disc list-inside">
            <li>Jam masuk kantor dimulai pukul <strong class="text-slate-800">09:00 WIB</strong>.</li>
            <li>Check-in setelah pukul 09:00 WIB akan dicatat dengan status <strong class="text-amber-600">LATE (Terlambat)</strong>.</li>
            <li>Jangan lupa melakukan <strong class="text-slate-800">Check-out</strong> saat selesai jam kerja harian Anda.</li>
          </ul>
        </div>

        <div class="bg-white rounded-xl border border-slate-200 shadow-2xs p-5">
          <h2 class="font-bold text-slate-800 text-base mb-3 flex items-center gap-2">
            <i class="pi pi-calendar text-emerald-600"></i>
            Ketentuan Pengajuan Cuti
          </h2>
          <ul class="text-sm text-slate-600 space-y-2 list-disc list-inside">
            <li>Hari libur akhir pekan (Sabtu & Minggu) tidak memotong saldo cuti tahunan Anda.</li>
            <li>Pengajuan cuti memerlukan persetujuan dari manajer atau atasan langsung Anda.</li>
            <li>Saldo cuti tahunan akan berkurang otomatis setelah pengajuan berstatus <strong class="text-emerald-600">APPROVED</strong>.</li>
          </ul>
        </div>
      </div>
    </template>
  </div>
</template>
