<script setup>
import { ref, computed, watch } from 'vue'
import { usePerformanceStore } from '@/stores/performanceStore'
import { useNotify } from '@/composables/useNotify'
import BaseModal from '@/components/common/BaseModal.vue'
import BaseSelect from '@/components/common/BaseSelect.vue'
import BaseButton from '@/components/common/BaseButton.vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  reviewData: {
    type: Object,
    default: null,
  },
  teamMembers: {
    type: Array,
    default: () => [],
  },
  periods: {
    type: Array,
    default: () => [],
  },
})

const emit = defineEmits(['update:visible', 'saved'])

const performanceStore = usePerformanceStore()
const notify = useNotify()

const submitting = ref(false)

const form = ref({
  periodId: null,
  employeeId: null,
  technicalSkill: 3,
  communication: 3,
  teamwork: 3,
  problemSolving: 3,
  comments: '',
})

const isEditMode = computed(() => !!props.reviewData?.id)

// Pilihan anggota tim untuk BaseSelect
const memberOptions = computed(() => {
  return props.teamMembers.map((m) => ({
    label: `${m.fullName} (${m.positionTitle || m.departmentName || 'Staff'})`,
    value: m.id,
  }))
})

// Pilihan periode evaluasi untuk BaseSelect (hanya yang ACTIVE untuk buat baru)
const periodOptions = computed(() => {
  return props.periods.map((p) => ({
    label: `${p.name} (${p.status === 'ACTIVE' ? 'Aktif' : p.status})`,
    value: p.id,
  }))
})

// Hitung rata-rata skor secara reaktif
const liveOverallScore = computed(() => {
  const t = Number(form.value.technicalSkill) || 0
  const c = Number(form.value.communication) || 0
  const w = Number(form.value.teamwork) || 0
  const p = Number(form.value.problemSolving) || 0
  const avg = (t + c + w + p) / 4.0
  return avg.toFixed(2)
})

watch(
  () => props.reviewData,
  (val) => {
    if (val) {
      form.value = {
        periodId: val.reviewPeriodId,
        employeeId: val.employeeId,
        technicalSkill: val.technicalSkill,
        communication: val.communication,
        teamwork: val.teamwork,
        problemSolving: val.problemSolving,
        comments: val.comments || '',
      }
    } else {
      // Reset form ke default
      const defaultPeriod = props.periods.find((p) => p.status === 'ACTIVE')?.id || null
      form.value = {
        periodId: defaultPeriod,
        employeeId: props.teamMembers[0]?.id || null,
        technicalSkill: 3,
        communication: 3,
        teamwork: 3,
        problemSolving: 3,
        comments: '',
      }
    }
  },
  { immediate: true },
)

async function handleSubmit() {
  if (!isEditMode.value) {
    if (!form.value.periodId) {
      notify.showError('Periode evaluasi wajib dipilih')
      return
    }
    if (!form.value.employeeId) {
      notify.showError('Karyawan yang dinilai wajib dipilih')
      return
    }
  }

  submitting.value = true
  try {
    if (isEditMode.value) {
      await performanceStore.updateReview(props.reviewData.id, {
        technicalSkill: Number(form.value.technicalSkill),
        communication: Number(form.value.communication),
        teamwork: Number(form.value.teamwork),
        problemSolving: Number(form.value.problemSolving),
        comments: form.value.comments?.trim() || '',
      })
      notify.showSuccess('Evaluasi kinerja berhasil diperbarui.')
    } else {
      await performanceStore.createReview({
        periodId: form.value.periodId,
        employeeId: form.value.employeeId,
        technicalSkill: Number(form.value.technicalSkill),
        communication: Number(form.value.communication),
        teamwork: Number(form.value.teamwork),
        problemSolving: Number(form.value.problemSolving),
        comments: form.value.comments?.trim() || '',
      })
      notify.showSuccess('Evaluasi kinerja berhasil disimpan.')
    }
    emit('update:visible', false)
    emit('saved')
  } catch (err) {
    notify.showError(err.message || 'Gagal menyimpan evaluasi kinerja')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <BaseModal
    :model-value="visible"
    :title="isEditMode ? 'Edit Evaluasi Kinerja' : 'Form Penilaian Kinerja Karyawan'"
    @update:model-value="$emit('update:visible', $event)"
  >
    <form @submit.prevent="handleSubmit" class="space-y-4">
      <!-- Periode & Anggota Tim (Hanya pilih saat buat baru) -->
      <div v-if="!isEditMode" class="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div>
          <BaseSelect
            v-model="form.periodId"
            label="Periode Evaluasi"
            :options="periodOptions"
            option-label="label"
            option-value="value"
            placeholder="Pilih periode"
            required
          />
        </div>
        <div>
          <BaseSelect
            v-model="form.employeeId"
            label="Karyawan yang Dinilai"
            :options="memberOptions"
            option-label="label"
            option-value="value"
            placeholder="Pilih anggota tim"
            required
          />
        </div>
      </div>

      <div v-else class="p-3 bg-slate-50 rounded-lg border border-slate-200 text-sm">
        <div class="font-semibold text-slate-800">{{ reviewData.employeeName }}</div>
        <div class="text-xs text-slate-500">
          Periode: {{ reviewData.reviewPeriodName }} &bull; {{ reviewData.departmentName }}
        </div>
      </div>

      <!-- Skor 4 Aspek (Skala 1 - 5) -->
      <div class="border-t border-b border-slate-100 py-3 space-y-3">
        <h4 class="text-xs font-bold text-slate-500 uppercase tracking-wider">
          Aspek Penilaian (Skala 1 - 5)
        </h4>

        <!-- Aspek 1: Kemampuan Teknis -->
        <div class="flex items-center justify-between">
          <div>
            <label class="text-sm font-medium text-slate-700">Kemampuan Teknis</label>
            <p class="text-xs text-slate-400">Penguasaan skill teknis dan kualitas hasil kerja</p>
          </div>
          <div class="flex items-center gap-1.5">
            <button
              v-for="val in [1, 2, 3, 4, 5]"
              :key="`tech-${val}`"
              type="button"
              class="w-8 h-8 rounded-lg text-xs font-semibold border transition-all cursor-pointer"
              :class="
                form.technicalSkill === val
                  ? 'bg-indigo-600 border-indigo-600 text-white shadow-xs'
                  : 'bg-white border-slate-200 text-slate-700 hover:bg-slate-50'
              "
              @click="form.technicalSkill = val"
            >
              {{ val }}
            </button>
          </div>
        </div>

        <!-- Aspek 2: Komunikasi -->
        <div class="flex items-center justify-between">
          <div>
            <label class="text-sm font-medium text-slate-700">Komunikasi</label>
            <p class="text-xs text-slate-400">Penyampaian informasi dan kejelasan koordinasi</p>
          </div>
          <div class="flex items-center gap-1.5">
            <button
              v-for="val in [1, 2, 3, 4, 5]"
              :key="`comm-${val}`"
              type="button"
              class="w-8 h-8 rounded-lg text-xs font-semibold border transition-all cursor-pointer"
              :class="
                form.communication === val
                  ? 'bg-indigo-600 border-indigo-600 text-white shadow-xs'
                  : 'bg-white border-slate-200 text-slate-700 hover:bg-slate-50'
              "
              @click="form.communication = val"
            >
              {{ val }}
            </button>
          </div>
        </div>

        <!-- Aspek 3: Kerjasama Tim -->
        <div class="flex items-center justify-between">
          <div>
            <label class="text-sm font-medium text-slate-700">Kerjasama Tim</label>
            <p class="text-xs text-slate-400">Kolaborasi dan kontribusi dalam pencapaian tim</p>
          </div>
          <div class="flex items-center gap-1.5">
            <button
              v-for="val in [1, 2, 3, 4, 5]"
              :key="`team-${val}`"
              type="button"
              class="w-8 h-8 rounded-lg text-xs font-semibold border transition-all cursor-pointer"
              :class="
                form.teamwork === val
                  ? 'bg-indigo-600 border-indigo-600 text-white shadow-xs'
                  : 'bg-white border-slate-200 text-slate-700 hover:bg-slate-50'
              "
              @click="form.teamwork = val"
            >
              {{ val }}
            </button>
          </div>
        </div>

        <!-- Aspek 4: Pemecahan Masalah -->
        <div class="flex items-center justify-between">
          <div>
            <label class="text-sm font-medium text-slate-700">Pemecahan Masalah</label>
            <p class="text-xs text-slate-400">Inisiatif dan ketangkasan dalam mengatasi kendala</p>
          </div>
          <div class="flex items-center gap-1.5">
            <button
              v-for="val in [1, 2, 3, 4, 5]"
              :key="`prob-${val}`"
              type="button"
              class="w-8 h-8 rounded-lg text-xs font-semibold border transition-all cursor-pointer"
              :class="
                form.problemSolving === val
                  ? 'bg-indigo-600 border-indigo-600 text-white shadow-xs'
                  : 'bg-white border-slate-200 text-slate-700 hover:bg-slate-50'
              "
              @click="form.problemSolving = val"
            >
              {{ val }}
            </button>
          </div>
        </div>
      </div>

      <!-- Live Overall Score Preview -->
      <div class="p-4 bg-indigo-50/70 border border-indigo-100 rounded-xl flex items-center justify-between">
        <div>
          <span class="text-xs font-bold text-indigo-700 uppercase tracking-wider block">
            Skor Akhir (Rata-rata Otomatis)
          </span>
          <span class="text-xs text-slate-500">Dihitung otomatis dari 4 aspek di atas</span>
        </div>
        <div class="flex items-baseline gap-1">
          <span class="text-3xl font-bold text-indigo-900">{{ liveOverallScore }}</span>
          <span class="text-xs text-indigo-600 font-semibold">/ 5.00</span>
        </div>
      </div>

      <!-- Komentar & Catatan Evaluasi -->
      <div>
        <label class="block text-sm font-medium text-slate-700 mb-1">
          Komentar & Catatan Evaluasi
        </label>
        <textarea
          v-model="form.comments"
          rows="3"
          class="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:outline-hidden focus:ring-2 focus:ring-indigo-500"
          placeholder="Berikan umpan balik atau apresiasi atas kinerja karyawan..."
        ></textarea>
      </div>
    </form>

    <template #footer>
      <div class="flex justify-end gap-2">
        <BaseButton
          label="Batal"
          variant="secondary"
          outlined
          @click="$emit('update:visible', false)"
        />
        <BaseButton
          :label="isEditMode ? 'Perbarui Evaluasi' : 'Simpan Evaluasi'"
          icon="pi pi-check"
          :loading="submitting"
          @click="handleSubmit"
        />
      </div>
    </template>
  </BaseModal>
</template>
