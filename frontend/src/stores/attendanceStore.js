import { defineStore } from 'pinia'
import { ref } from 'vue'
import attendanceApi from '@/api/attendanceApi'

export const useAttendanceStore = defineStore('attendance', () => {
  const todayStatus = ref(null)
  const historyList = ref([])
  const teamList = ref([])
  const allList = ref([])
  const summary = ref(null)
  const loading = ref(false)

  const totalHistory = ref(0)
  const totalTeam = ref(0)
  const totalAll = ref(0)

  async function fetchTodayStatus() {
    try {
      const data = await attendanceApi.getTodayStatus()
      todayStatus.value = data
      return data
    } catch (err) {
      console.error('Failed to fetch today attendance status:', err)
    }
  }

  async function checkIn(notes = '') {
    loading.value = true
    try {
      const data = await attendanceApi.checkIn({ notes })
      await fetchTodayStatus()
      return data
    } finally {
      loading.value = false
    }
  }

  async function checkOut() {
    loading.value = true
    try {
      const data = await attendanceApi.checkOut()
      await fetchTodayStatus()
      return data
    } finally {
      loading.value = false
    }
  }

  async function fetchMyHistory(params = {}) {
    loading.value = true
    try {
      const data = await attendanceApi.getMyHistory(params)
      historyList.value = data.content || []
      totalHistory.value = data.totalElements || 0
      return data
    } finally {
      loading.value = false
    }
  }

  async function fetchTeamAttendance(params = {}) {
    loading.value = true
    try {
      const data = await attendanceApi.getTeamAttendance(params)
      teamList.value = data.content || []
      totalTeam.value = data.totalElements || 0
      return data
    } finally {
      loading.value = false
    }
  }

  async function fetchAllAttendance(params = {}) {
    loading.value = true
    try {
      const data = await attendanceApi.getAllAttendance(params)
      allList.value = data.content || []
      totalAll.value = data.totalElements || 0
      return data
    } finally {
      loading.value = false
    }
  }

  async function fetchSummary() {
    try {
      const data = await attendanceApi.getTodaySummary()
      summary.value = data
      return data
    } catch (err) {
      console.error('Failed to fetch attendance summary:', err)
    }
  }

  return {
    todayStatus,
    historyList,
    teamList,
    allList,
    summary,
    loading,
    totalHistory,
    totalTeam,
    totalAll,
    fetchTodayStatus,
    checkIn,
    checkOut,
    fetchMyHistory,
    fetchTeamAttendance,
    fetchAllAttendance,
    fetchSummary,
  }
})

export default useAttendanceStore
