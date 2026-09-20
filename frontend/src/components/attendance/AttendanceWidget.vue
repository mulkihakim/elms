<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useNotify } from '@/composables/useNotify'
import { useAttendanceStore } from '@/stores/attendanceStore'
import BaseButton from '@/components/common/BaseButton.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'

const attendanceStore = useAttendanceStore()
const notify = useNotify()

const currentTime = ref('')
const currentDate = ref('')
const checkInNotes = ref('')
const showNotesInput = ref(false)

let timerId = null

function updateClock() {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('id-ID', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  })
  currentDate.value = now.toLocaleDateString('id-ID', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  })
}

onMounted(() => {
  updateClock()
  timerId = setInterval(updateClock, 1000)
  attendanceStore.fetchTodayStatus()
})

onUnmounted(() => {
  if (timerId) clearInterval(timerId)
})

const todayStatus = computed(() => attendanceStore.todayStatus)
const isCheckedIn = computed(() => todayStatus.value?.checkedIn)
const isCheckedOut = computed(() => todayStatus.value?.checkedOut)

const checkInTimeFormatted = computed(() => {
  if (!todayStatus.value?.checkIn) return '-'
  return new Date(todayStatus.value.checkIn).toLocaleTimeString('id-ID', {
    hour: '2-digit',
    minute: '2-digit',
  })
})

const checkOutTimeFormatted = computed(() => {
  if (!todayStatus.value?.checkOut) return '-'
  return new Date(todayStatus.value.checkOut).toLocaleTimeString('id-ID', {
    hour: '2-digit',
    minute: '2-digit',
  })
})

const formattedDuration = computed(() => {
  const minutes = todayStatus.value?.workMinutes || todayStatus.value?.currentWorkMinutes || 0
  const hrs = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (hrs === 0) return `${mins} menit`
  return `${hrs} jam ${mins} menit`
})

async function handleCheckIn() {
  try {
    await attendanceStore.checkIn(checkInNotes.value)
    notify.showSuccess('Kehadiran Anda hari ini telah dicatat.', 'Check-in Berhasil')
    showNotesInput.value = false
    checkInNotes.value = ''
  } catch (err) {
    notify.showError(err.message || 'Terjadi kesalahan saat check-in', 'Gagal Check-in')
  }
}

async function handleCheckOut() {
  try {
    await attendanceStore.checkOut()
    notify.showSuccess(
      'Jam kepulangan dan total durasi kerja telah tercatat.',
      'Check-out Berhasil',
    )
  } catch (err) {
    notify.showError(err.message || 'Terjadi kesalahan saat check-out', 'Gagal Check-out')
  }
}
</script>

<template>
  <div class="bg-white border border-slate-200 rounded-2xl p-6 shadow-2xs">
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-6">
      <!-- Sisi Kiri: Jam Digital & Tanggal -->
      <div class="flex items-center gap-4">
        <div
          class="w-14 h-14 rounded-2xl bg-indigo-50 border border-indigo-100 flex items-center justify-center text-indigo-600 shrink-0"
        >
          <i class="pi pi-clock text-2xl"></i>
        </div>
        <div>
          <div class="text-3xl font-extrabold text-slate-800 tracking-tight font-mono">
            {{ currentTime }}
            <span class="text-xs font-semibold text-slate-400 font-sans">WIB</span>
          </div>
          <div class="text-xs text-slate-500 font-medium capitalize mt-0.5">
            {{ currentDate }}
          </div>
        </div>
      </div>

      <!-- Sisi Kanan: Status & Tombol Aksi -->
      <div class="flex flex-col sm:flex-row sm:items-center gap-4">
        <!-- Kondisi 1: Belum Check-in -->
        <div v-if="!isCheckedIn" class="flex flex-col gap-2">
          <div class="flex items-center gap-2">
            <input
              v-if="showNotesInput"
              v-model="checkInNotes"
              type="text"
              placeholder="Catatan (opsional)"
              class="px-3 py-1.5 text-xs rounded-lg border border-slate-300 focus:outline-none focus:ring-1 focus:ring-indigo-500 w-48"
            />
            <button
              v-else
              type="button"
              class="text-xs text-indigo-600 hover:text-indigo-800 underline underline-offset-2"
              @click="showNotesInput = true"
            >
              + Tambah catatan
            </button>

            <BaseButton
              label="Check In Sekarang"
              icon="pi pi-sign-in"
              variant="success"
              class="font-semibold shadow-xs"
              :loading="attendanceStore.loading"
              @click="handleCheckIn"
            />
          </div>
          <span class="text-[11px] text-slate-400">
            Jadwal masuk tepat waktu: <strong class="text-slate-600">09:00 WIB</strong>
          </span>
        </div>

        <!-- Kondisi 2: Sudah Check-in, Belum Check-out -->
        <div v-else-if="!isCheckedOut" class="flex flex-wrap items-center gap-4">
          <div
            class="flex items-center gap-3 bg-slate-50 border border-slate-200 px-4 py-2.5 rounded-xl"
          >
            <div>
              <div class="text-[10px] uppercase font-bold text-slate-400">Masuk</div>
              <div class="text-sm font-bold text-slate-800">{{ checkInTimeFormatted }} WIB</div>
            </div>
            <div class="h-6 w-px bg-slate-200"></div>
            <div>
              <div class="text-[10px] uppercase font-bold text-slate-400">Status</div>
              <StatusBadge :status="todayStatus?.status" />
            </div>
            <div class="h-6 w-px bg-slate-200"></div>
            <div>
              <div class="text-[10px] uppercase font-bold text-slate-400">Durasi Kerja</div>
              <div class="text-xs font-semibold text-indigo-600">{{ formattedDuration }}</div>
            </div>
          </div>

          <BaseButton
            label="Check Out"
            icon="pi pi-sign-out"
            variant="warning"
            class="font-semibold shadow-xs"
            :loading="attendanceStore.loading"
            @click="handleCheckOut"
          />
        </div>

        <!-- Kondisi 3: Sudah Selesai Check-in & Check-out -->
        <div
          v-else
          class="flex items-center gap-3 bg-slate-50 border border-slate-200 px-4 py-2.5 rounded-xl"
        >
          <div>
            <div class="text-[10px] uppercase font-bold text-slate-400">Masuk</div>
            <div class="text-sm font-bold text-slate-800">{{ checkInTimeFormatted }}</div>
          </div>
          <div class="h-6 w-px bg-slate-200"></div>
          <div>
            <div class="text-[10px] uppercase font-bold text-slate-400">Pulang</div>
            <div class="text-sm font-bold text-slate-800">{{ checkOutTimeFormatted }}</div>
          </div>
          <div class="h-6 w-px bg-slate-200"></div>
          <div>
            <div class="text-[10px] uppercase font-bold text-slate-400">Total Durasi</div>
            <div class="text-xs font-bold text-slate-700">{{ formattedDuration }}</div>
          </div>
          <div class="h-6 w-px bg-slate-200"></div>
          <StatusBadge :status="todayStatus?.status" />
          <span
            class="px-2 py-0.5 rounded-full text-[10px] font-bold bg-indigo-50 text-indigo-700 border border-indigo-200"
          >
            Selesai
          </span>
        </div>
      </div>
    </div>
  </div>
</template>
