<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/authStore'
import { usePerformanceStore } from '@/stores/performanceStore'
import { useNotify } from '@/composables/useNotify'
import BaseTable from '@/components/common/BaseTable.vue'

const authStore = useAuthStore()
const performanceStore = usePerformanceStore()
const notify = useNotify()

const page = ref(0)
const pageSize = ref(10)

const columns = [
  { field: 'reviewPeriodName', header: 'Periode', sortable: true },
  { field: 'aspectScores', header: 'Rincian Aspek' },
  { field: 'overallScore', header: 'Skor Akhir' },
  { field: 'comments', header: 'Umpan Balik / Komentar' },
  { field: 'reviewerName', header: 'Penilai' },
  { field: 'createdAt', header: 'Tanggal Penilaian' },
]

// Review terbaru untuk kartu ringkasan
const latestReview = computed(() => {
  return performanceStore.myReviews[0] || null
})

onMounted(() => {
  loadMyReviews()
})

async function loadMyReviews() {
  if (!authStore.isAuthenticated) return

  try {
    await performanceStore.fetchMyReviews({
      page: page.value,
      size: pageSize.value,
    })
  } catch (err) {
    notify.showError(err.message || 'Gagal memuat evaluasi kinerja Anda')
  }
}

function getScoreBadgeClass(score) {
  const num = Number(score) || 0
  if (num >= 4.0) return 'bg-emerald-50 text-emerald-700 border-emerald-200'
  if (num >= 3.0) return 'bg-blue-50 text-blue-700 border-blue-200'
  if (num >= 2.0) return 'bg-amber-50 text-amber-700 border-amber-200'
  return 'bg-rose-50 text-rose-700 border-rose-200'
}

function formatDate(val) {
  if (!val) return '-'
  return new Date(val).toLocaleDateString('id-ID', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  })
}
</script>

<template>
  <div class="space-y-6">
    <!-- Kartu Highlight Hasil Review Terakhir -->
    <div
      v-if="latestReview"
      class="bg-gradient-to-br from-indigo-50/80 to-white border border-indigo-100 rounded-xl p-6 shadow-xs"
    >
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div>
          <div class="flex items-center gap-2 text-indigo-700 font-semibold text-sm mb-1">
            <i class="pi pi-star-fill text-amber-400"></i>
            <span>Hasil Evaluasi Kinerja Terakhir ({{ latestReview.reviewPeriodName }})</span>
          </div>
          <p class="text-sm text-slate-600 mt-1 max-w-xl">
            "{{ latestReview.comments || 'Tidak ada catatan tambahan.' }}"
          </p>
          <div class="text-xs text-slate-400 mt-2">
            Dinilai oleh <span class="font-medium text-slate-700">{{ latestReview.reviewerName }}</span> pada {{ formatDate(latestReview.createdAt) }}
          </div>
        </div>

        <div class="flex items-center gap-6 self-start md:self-auto shrink-0">
          <div class="grid grid-cols-2 gap-2 text-xs">
            <div class="bg-white px-2.5 py-1.5 rounded-lg border border-slate-200">
              <span class="text-slate-400 block text-[10px]">Teknis</span>
              <span class="font-bold text-slate-800">{{ latestReview.technicalSkill }} / 5</span>
            </div>
            <div class="bg-white px-2.5 py-1.5 rounded-lg border border-slate-200">
              <span class="text-slate-400 block text-[10px]">Komunikasi</span>
              <span class="font-bold text-slate-800">{{ latestReview.communication }} / 5</span>
            </div>
            <div class="bg-white px-2.5 py-1.5 rounded-lg border border-slate-200">
              <span class="text-slate-400 block text-[10px]">Kerjasama</span>
              <span class="font-bold text-slate-800">{{ latestReview.teamwork }} / 5</span>
            </div>
            <div class="bg-white px-2.5 py-1.5 rounded-lg border border-slate-200">
              <span class="text-slate-400 block text-[10px]">Masalah</span>
              <span class="font-bold text-slate-800">{{ latestReview.problemSolving }} / 5</span>
            </div>
          </div>

          <div class="text-center pl-4 border-l border-slate-200">
            <span class="text-[11px] font-bold uppercase tracking-wider text-slate-400 block">Skor Akhir</span>
            <div class="flex items-baseline justify-center gap-1 mt-0.5">
              <span class="text-3xl font-extrabold text-indigo-900">{{ latestReview.overallScore }}</span>
              <span class="text-xs font-semibold text-indigo-600">/ 5.0</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Tabel Riwayat Semua Review Saya -->
    <div class="bg-white rounded-xl border border-slate-200 p-5 shadow-xs">
      <div class="mb-4">
        <h3 class="text-base font-semibold text-slate-800">Riwayat Penilaian Kinerja Saya</h3>
        <p class="text-sm text-slate-500">
          Daftar seluruh evaluasi kinerja yang diberikan oleh atasan langsung Anda
        </p>
      </div>

      <BaseTable
        :columns="columns"
        :value="performanceStore.myReviews"
        :loading="performanceStore.loading"
        :total-records="performanceStore.totalMyReviews"
        :rows="pageSize"
        lazy
        @page="(e) => (page = e.page)"
      >
        <!-- Slot Periode -->
        <template #body-reviewPeriodName="{ data }">
          <span class="text-sm font-semibold text-slate-800">{{ data.reviewPeriodName }}</span>
        </template>

        <!-- Slot Rincian Aspek -->
        <template #body-aspectScores="{ data }">
          <div class="flex items-center gap-1.5 text-xs">
            <span class="px-1.5 py-0.5 rounded-sm bg-slate-100 text-slate-700 font-mono" title="Kemampuan Teknis">
              T:{{ data.technicalSkill }}
            </span>
            <span class="px-1.5 py-0.5 rounded-sm bg-slate-100 text-slate-700 font-mono" title="Komunikasi">
              K:{{ data.communication }}
            </span>
            <span class="px-1.5 py-0.5 rounded-sm bg-slate-100 text-slate-700 font-mono" title="Kerjasama Tim">
              T:{{ data.teamwork }}
            </span>
            <span class="px-1.5 py-0.5 rounded-sm bg-slate-100 text-slate-700 font-mono" title="Pemecahan Masalah">
              P:{{ data.problemSolving }}
            </span>
          </div>
        </template>

        <!-- Slot Skor Akhir -->
        <template #body-overallScore="{ data }">
          <span
            class="inline-flex items-center px-2.5 py-1 rounded-lg text-xs font-bold border"
            :class="getScoreBadgeClass(data.overallScore)"
          >
            {{ data.overallScore }} / 5.0
          </span>
        </template>

        <!-- Slot Komentar -->
        <template #body-comments="{ data }">
          <span class="text-sm text-slate-600 max-w-sm truncate block" :title="data.comments">
            {{ data.comments || '-' }}
          </span>
        </template>

        <!-- Slot Penilai -->
        <template #body-reviewerName="{ data }">
          <span class="text-sm text-slate-700 font-medium">{{ data.reviewerName }}</span>
        </template>

        <!-- Slot Tanggal -->
        <template #body-createdAt="{ data }">
          <span class="text-sm text-slate-500">{{ formatDate(data.createdAt) }}</span>
        </template>
      </BaseTable>
    </div>
  </div>
</template>
