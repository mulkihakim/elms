import { useAuthStore } from '@/stores/authStore'

export function setupRouteGuards(router) {
  router.beforeEach((to, from, next) => {
    const authStore = useAuthStore()

    // 1. Jika rute khusus tamu (misal halaman Login) dan user sudah login
    if (to.meta.guestOnly && authStore.isAuthenticated) {
      return next({ path: '/dashboard' })
    }

    // 2. Jika rute membutuhkan autentikasi
    const requiresAuth = to.matched.some((record) => record.meta.requiresAuth)
    if (requiresAuth) {
      if (!authStore.isAuthenticated) {
        return next({
          name: 'login',
          query: { redirect: to.fullPath },
        })
      }

      // 3. Jika rute membutuhkan role tertentu
      if (to.meta.roles && Array.isArray(to.meta.roles)) {
        if (!to.meta.roles.includes(authStore.currentRole)) {
          // Role tidak memiliki izin akses ke rute ini
          return next({ path: '/dashboard' })
        }
      }
    }

    next()
  })
}

export default setupRouteGuards
