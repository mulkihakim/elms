<script setup>
import { ref } from 'vue'
import { RouterView } from 'vue-router'
import Toast from 'primevue/toast'
import ConfirmDialog from 'primevue/confirmdialog'
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
  <div class="min-h-screen bg-slate-50 flex flex-col text-slate-800">
    <!-- Toast & ConfirmDialog Global Provider -->
    <Toast position="top-right" />
    <ConfirmDialog />

    <!-- Topbar Header -->
    <AppHeader :sidebar-open="sidebarOpen" @toggle-sidebar="toggleSidebar" />

    <!-- Body Layout: Sidebar + Main Content -->
    <div class="flex-1 flex overflow-hidden">
      <AppSidebar :is-open="sidebarOpen" @close="closeSidebar" />

      <!-- Main Content Area -->
      <main class="flex-1 overflow-y-auto p-4 sm:p-6 lg:p-8">
        <div class="max-w-7xl mx-auto">
          <RouterView />
        </div>
      </main>
    </div>
  </div>
</template>
