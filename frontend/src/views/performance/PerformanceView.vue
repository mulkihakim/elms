<script setup>
import { ref } from 'vue'
import { useAuthStore } from '@/stores/authStore'
import MyReviewView from './MyReviewView.vue'
import ReviewListView from './ReviewListView.vue'
import ReviewPeriodView from './ReviewPeriodView.vue'

const authStore = useAuthStore()

// Default tab: Untuk Manager/HR default ke 'team' / 'all', Employee ke 'my'
const activeTab = ref(
  authStore.isEmployee ? 'my' : authStore.isHR ? 'all' : 'team'
)
</script>

<template>
  <div class="space-y-6">
    <!-- Header Halaman -->
    <div>
      <h1 class="text-2xl font-bold text-slate-900 tracking-tight">Review Kinerja</h1>
      <p class="text-sm text-slate-500 mt-1">
        Kelola periode evaluasi, penilaian berkala kinerja tim, dan perkembangan performa kerja
      </p>
    </div>

    <!-- Tab Navigasi Khusus MANAGER -->
    <div
      v-if="authStore.isAuthenticated && authStore.isManager"
      class="flex border-b border-slate-200 gap-2"
    >
      <button
        type="button"
        class="px-4 py-2.5 text-sm font-semibold border-b-2 transition-colors cursor-pointer"
        :class="
          activeTab === 'team'
            ? 'border-indigo-600 text-indigo-600'
            : 'border-transparent text-slate-500 hover:text-slate-800'
        "
        @click="activeTab = 'team'"
      >
        <i class="pi pi-users text-xs mr-1.5"></i>
        Evaluasi Tim Saya
      </button>

      <button
        type="button"
        class="px-4 py-2.5 text-sm font-semibold border-b-2 transition-colors cursor-pointer"
        :class="
          activeTab === 'my'
            ? 'border-indigo-600 text-indigo-600'
            : 'border-transparent text-slate-500 hover:text-slate-800'
        "
        @click="activeTab = 'my'"
      >
        <i class="pi pi-user text-xs mr-1.5"></i>
        Evaluasi Saya
      </button>
    </div>

    <!-- Tab Navigasi Khusus HR -->
    <div
      v-if="authStore.isAuthenticated && authStore.isHR"
      class="flex border-b border-slate-200 gap-2"
    >
      <button
        type="button"
        class="px-4 py-2.5 text-sm font-semibold border-b-2 transition-colors cursor-pointer"
        :class="
          activeTab === 'all'
            ? 'border-indigo-600 text-indigo-600'
            : 'border-transparent text-slate-500 hover:text-slate-800'
        "
        @click="activeTab = 'all'"
      >
        <i class="pi pi-chart-bar text-xs mr-1.5"></i>
        Seluruh Evaluasi Kinerja
      </button>

      <button
        type="button"
        class="px-4 py-2.5 text-sm font-semibold border-b-2 transition-colors cursor-pointer"
        :class="
          activeTab === 'period'
            ? 'border-indigo-600 text-indigo-600'
            : 'border-transparent text-slate-500 hover:text-slate-800'
        "
        @click="activeTab = 'period'"
      >
        <i class="pi pi-calendar text-xs mr-1.5"></i>
        Kelola Periode Evaluasi
      </button>
    </div>

    <!-- Konten Tab (hanya render jika user terautentikasi) -->
    <div v-if="authStore.isAuthenticated">
      <!-- HR: Tab Kelola Periode -->
      <ReviewPeriodView v-if="authStore.isHR && activeTab === 'period'" />

      <!-- HR: Tab Seluruh Evaluasi -->
      <ReviewListView v-else-if="authStore.isHR && activeTab === 'all'" />

      <!-- Manager: Tab Evaluasi Tim -->
      <ReviewListView v-else-if="authStore.isManager && activeTab === 'team'" />

      <!-- Manager / Employee: Tab Evaluasi Pribadi -->
      <MyReviewView v-else />
    </div>
  </div>
</template>
