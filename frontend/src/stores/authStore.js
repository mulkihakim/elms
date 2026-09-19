import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  // Mock user default (HR) agar semua menu master data dapat diakses langsung
  const user = ref({
    name: 'Budi Santoso',
    email: 'budi.hr@elms.com',
    role: 'HR', // HR | MANAGER | EMPLOYEE
  })

  const token = ref(localStorage.getItem('elms_token') || 'mock-dev-token')

  const availableRoles = ['HR', 'MANAGER', 'EMPLOYEE']

  const currentRole = computed(() => user.value?.role || 'HR')
  const isHR = computed(() => currentRole.value === 'HR')
  const isManager = computed(() => currentRole.value === 'MANAGER')
  const isEmployee = computed(() => currentRole.value === 'EMPLOYEE')

  /**
   * Mengubah role aktif (berguna untuk demo/testing navigasi per role)
   */
  function setRole(role) {
    if (!availableRoles.includes(role)) return

    let name = 'Budi Santoso'
    let email = 'budi.hr@elms.com'

    if (role === 'MANAGER') {
      name = 'Siti Rahma (Engineering Manager)'
      email = 'siti.manager@elms.com'
    } else if (role === 'EMPLOYEE') {
      name = 'Ahmad Fauzi (Software Engineer)'
      email = 'ahmad.emp@elms.com'
    }

    user.value = {
      name,
      email,
      role,
    }
  }

  function setUser(newUser) {
    user.value = newUser
  }

  function logout() {
    token.value = null
    localStorage.removeItem('elms_token')
    // Reset ke default
    user.value = null
  }

  return {
    user,
    token,
    availableRoles,
    currentRole,
    isHR,
    isManager,
    isEmployee,
    setRole,
    setUser,
    logout,
  }
})

export default useAuthStore
