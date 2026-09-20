import { defineStore } from 'pinia'
import { ref } from 'vue'
import dashboardApi from '@/api/dashboardApi'

export const useDashboardStore = defineStore('dashboard', () => {
  const summary = ref(null)
  const departmentDistribution = ref([])
  const weeklyAttendance = ref([])
  const loading = ref(false)
  const error = ref(null)

  async function fetchSummary() {
    loading.value = true
    error.value = null
    try {
      const res = await dashboardApi.getSummary()
      const data = res?.data ?? res ?? null
      summary.value = data
      return data
    } catch (err) {
      error.value = err?.message || 'Gagal memuat ringkasan dashboard'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function fetchDepartmentDistribution() {
    try {
      const res = await dashboardApi.getDepartmentDistribution()
      const data = res?.data ?? res ?? []
      departmentDistribution.value = Array.isArray(data) ? data : []
      return departmentDistribution.value
    } catch (err) {
      console.error('Gagal memuat distribusi departemen:', err)
      return []
    }
  }

  async function fetchWeeklyAttendance() {
    try {
      const res = await dashboardApi.getWeeklyAttendance()
      const data = res?.data ?? res ?? []
      weeklyAttendance.value = Array.isArray(data) ? data : []
      return weeklyAttendance.value
    } catch (err) {
      console.error('Gagal memuat kehadiran mingguan:', err)
      return []
    }
  }

  async function fetchDashboardData(role) {
    loading.value = true
    error.value = null
    try {
      await fetchSummary()
      if (role === 'HR') {
        await Promise.all([fetchDepartmentDistribution(), fetchWeeklyAttendance()])
      } else if (role === 'MANAGER') {
        await fetchWeeklyAttendance()
      }
    } catch (err) {
      console.error('Gagal memuat data dashboard:', err)
    } finally {
      loading.value = false
    }
  }

  function resetState() {
    summary.value = null
    departmentDistribution.value = []
    weeklyAttendance.value = []
    loading.value = false
    error.value = null
  }

  return {
    summary,
    departmentDistribution,
    weeklyAttendance,
    loading,
    error,
    fetchSummary,
    fetchDepartmentDistribution,
    fetchWeeklyAttendance,
    fetchDashboardData,
    resetState,
  }
})
