<script setup>
import { ref } from 'vue'
import { useAuthStore } from '@/stores/authStore'
import LeaveRequestView from './LeaveRequestView.vue'
import LeaveApprovalView from './LeaveApprovalView.vue'

const authStore = useAuthStore()

// Default tab: Untuk Manager/HR buka tab 'approval' atau 'request'
const activeTab = ref(authStore.isManager || authStore.isHR ? 'approval' : 'request')
</script>

<template>
  <div class="space-y-6">
    <!-- Header Halaman -->
    <div>
      <h1 class="text-2xl font-bold text-slate-900 tracking-tight">Manajemen Cuti</h1>
      <p class="text-sm text-slate-500 mt-1">
        Kelola pengajuan cuti pribadi dan persetujuan cuti anggota tim
      </p>
    </div>

    <!-- Tab Navigasi (hanya tampil jika user adalah Manager atau HR) -->
    <div
      v-if="authStore.isManager || authStore.isHR"
      class="flex border-b border-slate-200 gap-2"
    >
      <button
        type="button"
        class="px-4 py-2.5 text-sm font-semibold border-b-2 transition-colors cursor-pointer"
        :class="
          activeTab === 'approval'
            ? 'border-indigo-600 text-indigo-600'
            : 'border-transparent text-slate-500 hover:text-slate-800'
        "
        @click="activeTab = 'approval'"
      >
        <i class="pi pi-check-circle text-xs mr-1.5"></i>
        Persetujuan Cuti Tim
      </button>

      <button
        type="button"
        class="px-4 py-2.5 text-sm font-semibold border-b-2 transition-colors cursor-pointer"
        :class="
          activeTab === 'request'
            ? 'border-indigo-600 text-indigo-600'
            : 'border-transparent text-slate-500 hover:text-slate-800'
        "
        @click="activeTab = 'request'"
      >
        <i class="pi pi-calendar-minus text-xs mr-1.5"></i>
        Pengajuan Cuti Saya
      </button>
    </div>

    <!-- Konten Tab -->
    <div v-if="authStore.isAuthenticated">
      <LeaveApprovalView v-if="activeTab === 'approval' && (authStore.isManager || authStore.isHR)" />
      <LeaveRequestView v-else-if="activeTab === 'request' || authStore.isEmployee" />
    </div>
  </div>
</template>
