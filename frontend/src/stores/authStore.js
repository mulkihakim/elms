import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import authApi from '@/api/authApi'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('elms_token') || null)

  let savedUser = null
  try {
    const rawUser = localStorage.getItem('elms_user')
    if (rawUser) {
      savedUser = JSON.parse(rawUser)
    }
  } catch {
    savedUser = null
  }
  const user = ref(savedUser)
  const isLoading = ref(false)
  const error = ref(null)

  const isAuthenticated = computed(() => !!token.value && !!user.value)
  const currentRole = computed(() => user.value?.role || null)
  const isHR = computed(() => currentRole.value === 'HR')
  const isManager = computed(() => currentRole.value === 'MANAGER')
  const isEmployee = computed(() => currentRole.value === 'EMPLOYEE')

  /**
   * Melakukan login dengan email dan password
   * @param {{ email: string, password: string }} credentials
   */
  async function login(credentials) {
    isLoading.value = true
    error.value = null
    try {
      const data = await authApi.login(credentials)
      token.value = data.accessToken
      user.value = data.user
      localStorage.setItem('elms_token', data.accessToken)
      localStorage.setItem('elms_user', JSON.stringify(data.user))
      return data
    } catch (err) {
      error.value = err.message || 'Login gagal. Periksa kembali email dan password.'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  /**
   * Mengambil data profil user terbaru dari backend
   */
  async function fetchCurrentUser() {
    if (!token.value) return null
    try {
      const profile = await authApi.getMe()
      user.value = profile
      localStorage.setItem('elms_user', JSON.stringify(profile))
      return profile
    } catch (err) {
      logout()
      throw err
    }
  }

  /**
   * Logout dan membersihkan session
   */
  function logout() {
    token.value = null
    user.value = null
    error.value = null
    localStorage.removeItem('elms_token')
    localStorage.removeItem('elms_user')
  }

  return {
    user,
    token,
    isLoading,
    error,
    isAuthenticated,
    currentRole,
    isHR,
    isManager,
    isEmployee,
    login,
    fetchCurrentUser,
    logout,
  }
})

export default useAuthStore
