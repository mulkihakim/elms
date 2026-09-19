<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useConfirm } from 'primevue/useconfirm'
import { useAuthStore } from '@/stores/authStore'

defineProps({
  sidebarOpen: {
    type: Boolean,
    default: true,
  },
})

const emit = defineEmits(['toggle-sidebar'])

const router = useRouter()
const confirm = useConfirm()
const authStore = useAuthStore()

const displayName = computed(() => authStore.user?.fullName || authStore.user?.name || 'User')
const displayEmail = computed(() => authStore.user?.email || '')
const displayRole = computed(() => authStore.currentRole || '')

const roleBadgeClass = computed(() => {
  switch (displayRole.value) {
    case 'HR':
      return 'bg-purple-50 text-purple-700 border-purple-200'
    case 'MANAGER':
      return 'bg-blue-50 text-blue-700 border-blue-200'
    case 'EMPLOYEE':
    default:
      return 'bg-emerald-50 text-emerald-700 border-emerald-200'
  }
})

function handleLogout() {
  confirm.require({
    message: 'Apakah Anda yakin ingin keluar dari sistem?',
    header: 'Konfirmasi Keluar',
    icon: 'pi pi-sign-out',
    rejectLabel: 'Batal',
    acceptLabel: 'Keluar',
    rejectProps: {
      severity: 'secondary',
      outlined: true,
    },
    acceptProps: {
      severity: 'danger',
    },
    accept: () => {
      authStore.logout()
      router.push('/login')
    },
  })
}
</script>

<template>
  <header
    class="sticky top-0 z-30 h-16 bg-white border-b border-slate-200 px-4 sm:px-6 flex items-center justify-between shadow-2xs"
  >
    <!-- Sisi Kiri: Logo & Toggle Sidebar -->
    <div class="flex items-center gap-3">
      <button
        type="button"
        class="p-2 rounded-lg text-slate-500 hover:text-slate-700 hover:bg-slate-100 focus:outline-none cursor-pointer"
        title="Toggle Sidebar"
        @click="emit('toggle-sidebar')"
      >
        <i class="pi pi-bars text-lg"></i>
      </button>

      <div class="flex items-center gap-2">
        <div
          class="w-8 h-8 rounded-lg bg-indigo-600 flex items-center justify-center text-white font-bold text-sm shadow-xs"
        >
          E
        </div>
        <div class="hidden sm:block">
          <span class="font-bold text-slate-800 text-base tracking-tight">ELMS</span>
          <span class="text-xs text-slate-500 block -mt-1">Employee Lifecycle Management</span>
        </div>
      </div>
    </div>

    <!-- Sisi Kanan: Info Profil & Tombol Logout -->
    <div class="flex items-center gap-3 sm:gap-4">
      <!-- Role Badge -->
      <span
        v-if="displayRole"
        class="hidden sm:inline-flex items-center px-2 py-0.5 rounded-md text-[11px] font-semibold border"
        :class="roleBadgeClass"
      >
        {{ displayRole }}
      </span>

      <!-- User Info Badge -->
      <div class="flex items-center gap-2.5 pl-2 sm:pl-3 border-l border-slate-200">
        <div
          class="w-8 h-8 rounded-full bg-indigo-100 text-indigo-700 flex items-center justify-center font-semibold text-xs border border-indigo-200"
        >
          {{ displayName.charAt(0) }}
        </div>
        <div class="hidden md:block text-left">
          <div class="text-xs font-semibold text-slate-800 leading-tight">
            {{ displayName }}
          </div>
          <div class="text-[10px] text-slate-500">
            {{ displayEmail }}
          </div>
        </div>
      </div>

      <!-- Tombol Logout -->
      <button
        type="button"
        class="p-2 rounded-lg text-slate-400 hover:text-red-600 hover:bg-red-50 focus:outline-none transition-colors cursor-pointer"
        title="Keluar dari akun"
        @click="handleLogout"
      >
        <i class="pi pi-sign-out text-base"></i>
      </button>
    </div>
  </header>
</template>
