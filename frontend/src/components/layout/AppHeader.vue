<script setup>
import { useAuthStore } from '@/stores/authStore'

defineProps({
  sidebarOpen: {
    type: Boolean,
    default: true,
  },
})

const emit = defineEmits(['toggle-sidebar'])

const authStore = useAuthStore()

function handleRoleChange(e) {
  authStore.setRole(e.target.value)
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
        class="p-2 rounded-lg text-slate-500 hover:text-slate-700 hover:bg-slate-100 focus:outline-none"
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

    <!-- Sisi Kanan: Role Simulator Switcher & Info Profil -->
    <div class="flex items-center gap-3 sm:gap-4">
      <!-- Role Tester Dropdown (membantu demo & evaluasi menu per role) -->
      <div class="flex items-center gap-1.5 bg-slate-50 border border-slate-200 px-2.5 py-1 rounded-md text-xs">
        <span class="text-slate-500 font-medium hidden md:inline">Peran Aktif:</span>
        <select
          :value="authStore.currentRole"
          class="bg-transparent font-semibold text-indigo-700 focus:outline-none cursor-pointer"
          @change="handleRoleChange"
        >
          <option value="HR">HR / Admin</option>
          <option value="MANAGER">Manager</option>
          <option value="EMPLOYEE">Employee</option>
        </select>
      </div>

      <!-- User Info Badge -->
      <div class="flex items-center gap-2.5 pl-2 sm:pl-3 border-l border-slate-200">
        <div
          class="w-8 h-8 rounded-full bg-indigo-100 text-indigo-700 flex items-center justify-center font-semibold text-xs border border-indigo-200"
        >
          {{ authStore.user?.name ? authStore.user.name.charAt(0) : 'U' }}
        </div>
        <div class="hidden md:block text-left">
          <div class="text-xs font-semibold text-slate-800 leading-tight">
            {{ authStore.user?.name }}
          </div>
          <div class="text-[10px] text-slate-500">
            {{ authStore.user?.email }}
          </div>
        </div>
      </div>
    </div>
  </header>
</template>
