<script setup>
import { ref } from 'vue'
import { RouterView } from 'vue-router'
import AppConfirmDialog from '@/components/common/AppConfirmDialog.vue'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppSidebar from '@/components/layout/AppSidebar.vue'

// Sidebar terbuka secara default pada layar desktop (lg = 1024px+)
const sidebarOpen = ref(window.innerWidth >= 1024)

function toggleSidebar() {
  sidebarOpen.value = !sidebarOpen.value
}

function closeSidebar() {
  sidebarOpen.value = false
}
</script>

<template>
  <div class="h-screen bg-slate-50 flex flex-col text-slate-800 overflow-hidden">
    <!-- ConfirmDialog Global Provider -->
    <AppConfirmDialog />

    <!-- Topbar Header -->
    <AppHeader :sidebar-open="sidebarOpen" @toggle-sidebar="toggleSidebar" />

    <!-- Body Layout: Sidebar + Main Content -->
    <div class="flex-1 flex min-h-0 overflow-hidden">
      <AppSidebar :is-open="sidebarOpen" @close="closeSidebar" />

      <!-- Main Content Area -->
      <main class="flex-1 overflow-y-auto min-h-0 p-4 sm:p-6 lg:p-8">
        <div class="max-w-7xl mx-auto">
          <RouterView />
        </div>
      </main>
    </div>
  </div>
</template>
