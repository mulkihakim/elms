<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useAuthStore } from '@/stores/authStore'
import { usePerformanceStore } from '@/stores/performanceStore'
import { useEmployeeStore } from '@/stores/employeeStore'
import { useNotify } from '@/composables/useNotify'
import BaseTable from '@/components/common/BaseTable.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import BaseSelect from '@/components/common/BaseSelect.vue'
import ReviewFormModal from './ReviewFormModal.vue'

const authStore = useAuthStore()
const performanceStore = usePerformanceStore()
const employeeStore = useEmployeeStore()
const notify = useNotify()

const selectedPeriod = ref(null)
const page = ref(0)
const pageSize = ref(10)

// Modal state
const isModalOpen = ref(false)
const selectedReviewForEdit = ref(null)

const columns = [
  { field: 'employee', header: 'Karyawan' },
  { field: 'reviewPeriodName', header: 'Periode' },
  { field: 'aspectScores', header: 'Rincian Aspek' },
  { field: 'overallScore', header: 'Skor Akhir' },
  { field: 'comments', header: 'Komentar' },
  { field: 'reviewerName', header: 'Penilai' },
]

const periodOptions = computed(() => [
  { label: 'Semua Periode', value: null },
  ...performanceStore.periods.map((p) => ({
    label: p.name,
    value: p.id,
  })),
])

// Ambil anggota tim bawahan (hanya untuk Manager)
const teamMembers = computed(() => {
  return employeeStore.employees.filter((e) => e.managerId === authStore.user?.id)
})

onMounted(async () => {
  await performanceStore.fetchPeriods()
  if (authStore.isManager) {
    // Ambil karyawan untuk dropdown penilai
    await employeeStore.fetchEmployees({ page: 0, size: 100 })
  }
  loadReviews()
})

watch([page, selectedPeriod], () => {
  loadReviews()
})

async function loadReviews() {
  if (!authStore.isAuthenticated) return

  try {
    const params = {
      page: page.value,
      size: pageSize.value,
    }
    if (selectedPeriod.value) {
      params.periodId = selectedPeriod.value
    }
    await performanceStore.fetchTeamReviews(params)
  } catch (err) {
    notify.showError(err.message || 'Gagal memuat evaluasi kinerja tim')
  }
}

function openCreateModal() {
  selectedReviewForEdit.value = null
  isModalOpen.value = true
}

function openEditModal(review) {
  selectedReviewForEdit.value = review
  isModalOpen.value = true
}

function getScoreBadgeClass(score) {
  const num = Number(score) || 0
  if (num >= 4.0) return 'bg-emerald-50 text-emerald-700 border-emerald-200'
  if (num >= 3.0) return 'bg-blue-50 text-blue-700 border-blue-200'
  if (num >= 2.0) return 'bg-amber-50 text-amber-700 border-amber-200'
  return 'bg-rose-50 text-rose-700 border-rose-200'
}
</script>

<template>
  <div class="space-y-6">
    <div class="bg-white rounded-xl border border-slate-200 p-5 shadow-xs">
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-4">
        <div>
          <h3 class="text-base font-semibold text-slate-800">
            {{ authStore.isHR ? 'Daftar Evaluasi Kinerja Karyawan' : 'Evaluasi Kinerja Anggota Tim' }}
          </h3>
          <p class="text-sm text-slate-500">
            {{ authStore.isHR ? 'Pantau hasil penilaian kinerja seluruh organisasi' : 'Beri penilaian dan pantau perkembangan anggota tim langsung Anda' }}
          </p>
        </div>

        <div class="flex items-center gap-3">
          <div class="w-48 sm:w-56">
            <BaseSelect
              v-model="selectedPeriod"
              :options="periodOptions"
              option-label="label"
              option-value="value"
              placeholder="Filter Periode"
            />
          </div>

          <!-- Tombol Beri Penilaian hanya untuk Manager -->
          <BaseButton
            v-if="authStore.isManager"
            label="Beri Nilai Tim"
            icon="pi pi-plus"
            size="small"
            @click="openCreateModal"
          />
        </div>
      </div>

      <BaseTable
        :columns="columns"
        :value="performanceStore.teamReviews"
        :loading="performanceStore.loading"
        :total-records="performanceStore.totalTeamReviews"
        :rows="pageSize"
        lazy
        @page="(e) => (page = e.page)"
      >
        <!-- Slot Karyawan -->
        <template #body-employee="{ data }">
          <div class="text-sm">
            <div class="font-medium text-slate-800">{{ data.employeeName }}</div>
            <div class="text-xs text-slate-500">{{ data.departmentName || '-' }} &bull; {{ data.positionTitle || '-' }}</div>
          </div>
        </template>

        <!-- Slot Periode -->
        <template #body-reviewPeriodName="{ data }">
          <span class="text-sm font-semibold text-slate-700">{{ data.reviewPeriodName }}</span>
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
          <span class="text-sm text-slate-600 max-w-xs truncate block" :title="data.comments">
            {{ data.comments || '-' }}
          </span>
        </template>

        <!-- Slot Penilai -->
        <template #body-reviewerName="{ data }">
          <span class="text-sm text-slate-700 font-medium">{{ data.reviewerName }}</span>
        </template>

        <!-- Slot Aksi Edit (Hanya untuk Manager pembuat) -->
        <template v-if="authStore.isManager" #actions="{ data }">
          <BaseButton
            v-if="data.reviewerId === authStore.user?.id"
            icon="pi pi-pencil"
            size="small"
            variant="secondary"
            outlined
            class="!p-2"
            title="Edit Evaluasi"
            @click="openEditModal(data)"
          />
        </template>
      </BaseTable>
    </div>

    <!-- Modal Penilaian / Edit Evaluasi -->
    <ReviewFormModal
      v-model:visible="isModalOpen"
      :review-data="selectedReviewForEdit"
      :team-members="teamMembers"
      :periods="performanceStore.periods"
      @saved="loadReviews"
    />
  </div>
</template>
