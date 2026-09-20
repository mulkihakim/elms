<script setup>
defineProps({
  summary: {
    type: Object,
    default: () => ({
      totalEmployees: 0,
      present: 0,
      onTime: 0,
      late: 0,
      absent: 0,
    }),
  },
})
</script>

<template>
  <div class="grid grid-cols-2 lg:grid-cols-4 gap-4">
    <!-- Total Karyawan -->
    <div class="bg-white p-4 rounded-xl border border-slate-200 flex items-center gap-3.5 shadow-2xs">
      <div class="w-11 h-11 rounded-lg bg-indigo-50 text-indigo-600 flex items-center justify-center shrink-0">
        <i class="pi pi-users text-lg"></i>
      </div>
      <div>
        <div class="text-[11px] font-medium text-slate-500 uppercase tracking-wider">Total Karyawan</div>
        <div class="text-xl font-bold text-slate-800">{{ summary?.totalEmployees || 0 }}</div>
      </div>
    </div>

    <!-- Hadir Hari Ini -->
    <div class="bg-white p-4 rounded-xl border border-slate-200 flex items-center gap-3.5 shadow-2xs">
      <div class="w-11 h-11 rounded-lg bg-emerald-50 text-emerald-600 flex items-center justify-center shrink-0">
        <i class="pi pi-check-circle text-lg"></i>
      </div>
      <div>
        <div class="text-[11px] font-medium text-slate-500 uppercase tracking-wider">Hadir Hari Ini</div>
        <div class="text-xl font-bold text-emerald-700">{{ summary?.present || 0 }}</div>
      </div>
    </div>

    <!-- Tepat Waktu -->
    <div class="bg-white p-4 rounded-xl border border-slate-200 flex items-center gap-3.5 shadow-2xs">
      <div class="w-11 h-11 rounded-lg bg-blue-50 text-blue-600 flex items-center justify-center shrink-0">
        <i class="pi pi-clock text-lg"></i>
      </div>
      <div>
        <div class="text-[11px] font-medium text-slate-500 uppercase tracking-wider">Tepat Waktu</div>
        <div class="text-xl font-bold text-blue-700">{{ summary?.onTime || 0 }}</div>
      </div>
    </div>

    <!-- Terlambat -->
    <div class="bg-white p-4 rounded-xl border border-slate-200 flex items-center gap-3.5 shadow-2xs">
      <div class="w-11 h-11 rounded-lg bg-amber-50 text-amber-600 flex items-center justify-center shrink-0">
        <i class="pi pi-exclamation-triangle text-lg"></i>
      </div>
      <div>
        <div class="text-[11px] font-medium text-slate-500 uppercase tracking-wider">Terlambat</div>
        <div class="text-xl font-bold text-amber-700">{{ summary?.late || 0 }}</div>
      </div>
    </div>
  </div>
</template>
