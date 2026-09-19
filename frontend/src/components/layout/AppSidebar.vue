<script setup>
import { computed } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

defineProps({
  isOpen: {
    type: Boolean,
    default: true,
  },
})

const emit = defineEmits(['close'])

/**
 * Tutup sidebar hanya di layar kecil (mobile/tablet).
 * Di desktop (lg+), klik menu tidak menutup sidebar.
 */
function closeSidebarOnMobile() {
  if (window.innerWidth < 1024) {
    emit('close')
  }
}

const route = useRoute()
const authStore = useAuthStore()

// Daftar menu berdasar role sesuai dokumen perencanaan (PRD & Architecture)
const menuSections = computed(() => {
  const role = authStore.currentRole

  if (role === 'HR') {
    return [
      {
        title: 'Utama',
        items: [
          { name: 'Dashboard', path: '/dashboard', icon: 'pi pi-th-large' },
        ],
      },
      {
        title: 'Master Data Organisasi',
        items: [
          { name: 'Departemen', path: '/departments', icon: 'pi pi-building', active: true },
          { name: 'Posisi', path: '/positions', icon: 'pi pi-briefcase', active: true },
          { name: 'Karyawan', path: '/employees', icon: 'pi pi-users', badge: 'Segera' },
        ],
      },
      {
        title: 'Operasional HR',
        items: [
          { name: 'Presensi', path: '/attendance', icon: 'pi pi-calendar-clock', badge: 'Segera' },
          { name: 'Pengajuan Cuti', path: '/leaves', icon: 'pi pi-calendar-minus', badge: 'Segera' },
          { name: 'Review Kinerja', path: '/reviews', icon: 'pi pi-star', badge: 'Segera' },
        ],
      },
    ]
  }

  if (role === 'MANAGER') {
    return [
      {
        title: 'Utama',
        items: [
          { name: 'Dashboard Tim', path: '/dashboard', icon: 'pi pi-th-large' },
        ],
      },
      {
        title: 'Manajemen Tim',
        items: [
          { name: 'Presensi Tim', path: '/attendance', icon: 'pi pi-calendar-clock', badge: 'Segera' },
          { name: 'Persetujuan Cuti', path: '/leaves', icon: 'pi pi-check-circle', badge: 'Segera' },
          { name: 'Review Kinerja Tim', path: '/reviews', icon: 'pi pi-star', badge: 'Segera' },
        ],
      },
    ]
  }

  // EMPLOYEE
  return [
    {
      title: 'Utama',
      items: [
        { name: 'Dashboard Saya', path: '/dashboard', icon: 'pi pi-th-large' },
      ],
    },
    {
      title: 'Aktivitas Saya',
      items: [
        { name: 'Presensi Saya', path: '/attendance', icon: 'pi pi-clock', badge: 'Segera' },
        { name: 'Pengajuan Cuti Saya', path: '/leaves', icon: 'pi pi-calendar-minus', badge: 'Segera' },
        { name: 'Review Kinerja Saya', path: '/reviews', icon: 'pi pi-star', badge: 'Segera' },
      ],
    },
  ]
})

function isActive(path) {
  return route.path === path || (path !== '/dashboard' && route.path.startsWith(path))
}
</script>

<template>
  <div>
    <!-- Backdrop overlay (hanya tampil di layar kecil saat sidebar terbuka) -->
    <div
      v-if="isOpen"
      class="fixed inset-0 z-40 bg-slate-900/40 lg:hidden"
      @click="emit('close')"
    ></div>

    <!-- Sidebar Container -->
    <aside
      class="fixed lg:static top-16 lg:top-0 bottom-0 left-0 z-40 w-64 bg-white border-r border-slate-200 flex flex-col transition-all duration-200 ease-in-out shrink-0"
      :class="isOpen ? 'translate-x-0' : '-translate-x-full lg:-ml-64 lg:translate-x-0'"
    >
      <div class="flex-1 overflow-y-auto px-4 py-4 space-y-6">
        <div v-for="(section, idx) in menuSections" :key="idx" class="space-y-1">
          <h3 class="px-3 text-[11px] font-bold uppercase tracking-wider text-slate-400">
            {{ section.title }}
          </h3>

          <div class="space-y-1 pt-1">
            <RouterLink
              v-for="item in section.items"
              :key="item.path"
              :to="item.path"
              class="flex items-center justify-between px-3 py-2 text-sm font-medium rounded-lg transition-colors group"
              :class="
                isActive(item.path)
                  ? 'bg-indigo-50 text-indigo-700 font-semibold'
                  : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900'
              "
              @click="closeSidebarOnMobile"
            >
              <div class="flex items-center gap-3">
                <i
                  :class="[item.icon, isActive(item.path) ? 'text-indigo-600' : 'text-slate-400 group-hover:text-slate-600']"
                  class="text-base"
                ></i>
                <span>{{ item.name }}</span>
              </div>

              <span
                v-if="item.badge"
                class="text-[10px] px-1.5 py-0.5 rounded-full bg-slate-100 text-slate-500 font-normal"
              >
                {{ item.badge }}
              </span>
            </RouterLink>
          </div>
        </div>
      </div>

      <!-- Footer Info di Bawah Sidebar -->
      <div class="p-3 border-t border-slate-100 text-[11px] text-slate-400 text-center">
        ELMS &copy; 2026
      </div>
    </aside>
  </div>
</template>
