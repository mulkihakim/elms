<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import BaseButton from '@/components/common/BaseButton.vue'

const route = useRoute()

const moduleDetails = computed(() => {
  const path = route.path
  if (path.includes('employee')) {
    return {
      title: 'Modul Employee Management',
      icon: 'pi pi-users',
      desc: 'Pengelolaan data induk karyawan, riwayat jabatan, relasi manager, dan status kepegawaian (ACTIVE, ON_LEAVE, RESIGNED).',
      section: '3.1 Employee Management (PRD)',
    }
  }
  if (path.includes('attendance')) {
    return {
      title: 'Modul Presensi / Attendance',
      icon: 'pi pi-calendar-clock',
      desc: 'Pencatatan check-in dan check-out mandiri karyawan, kalkulasi keterlambatan, dan pemantauan jam kerja.',
      section: '3.3 Attendance (PRD)',
    }
  }
  if (path.includes('leave')) {
    return {
      title: 'Modul Manajemen Cuti (Leave Management)',
      icon: 'pi pi-calendar-minus',
      desc: 'Alur permohonan cuti karyawan, validasi sisa saldo cuti, serta alur persetujuan atau penolakan oleh Manager langsung.',
      section: '3.4 Leave Management (PRD - Zona Manual)',
    }
  }
  if (path.includes('review')) {
    return {
      title: 'Modul Review Kinerja (Performance Review)',
      icon: 'pi pi-star',
      desc: 'Pembuatan periode penilaian kinerja oleh HR dan pengisian evaluasi kinerja berkala oleh Manager kepada timnya.',
      section: '3.5 Performance Review (PRD)',
    }
  }
  return {
    title: 'Modul Segera Hadir',
    icon: 'pi pi-clock',
    desc: 'Halaman ini sedang dalam tahap pengembangan sesuai roadmap ELMS.',
    section: 'Roadmap ELMS',
  }
})
</script>

<template>
  <div
    class="bg-white rounded-xl border border-slate-200 p-8 sm:p-12 text-center max-w-2xl mx-auto shadow-2xs space-y-4 my-8"
  >
    <div
      class="w-16 h-16 rounded-2xl bg-indigo-50 text-indigo-600 flex items-center justify-center text-3xl mx-auto"
    >
      <i :class="moduleDetails.icon"></i>
    </div>

    <div class="space-y-2">
      <span
        class="text-xs font-semibold uppercase tracking-wider text-indigo-600 bg-indigo-50 px-2.5 py-1 rounded-full"
      >
        {{ moduleDetails.section }}
      </span>
      <h2 class="text-2xl font-bold text-slate-800">
        {{ moduleDetails.title }}
      </h2>
      <p class="text-sm text-slate-500 max-w-md mx-auto">
        {{ moduleDetails.desc }}
      </p>
    </div>

    <div class="pt-4 flex justify-center gap-3">
      <RouterLink to="/departments">
        <BaseButton label="Lihat Modul Departemen" icon="pi pi-building" variant="primary" />
      </RouterLink>
      <RouterLink to="/dashboard">
        <BaseButton
          label="Kembali ke Dashboard"
          icon="pi pi-th-large"
          variant="secondary"
          outlined
        />
      </RouterLink>
    </div>
  </div>
</template>
