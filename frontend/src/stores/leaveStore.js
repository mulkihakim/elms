import { defineStore } from 'pinia'
import { ref } from 'vue'
import leaveApi from '@/api/leaveApi'

export const useLeaveStore = defineStore('leave', () => {
  const myLeaves = ref([])
  const totalMyLeaves = ref(0)
  const teamLeaves = ref([])
  const totalTeamLeaves = ref(0)
  const loading = ref(false)

  async function fetchMyLeaves(params = {}) {
    loading.value = true
    try {
      const data = await leaveApi.getMyLeaves(params)
      myLeaves.value = data.content || []
      totalMyLeaves.value = data.totalElements || 0
      return data
    } finally {
      loading.value = false
    }
  }

  async function fetchTeamLeaves(params = {}) {
    loading.value = true
    try {
      const data = await leaveApi.getTeamLeaves(params)
      teamLeaves.value = data.content || []
      totalTeamLeaves.value = data.totalElements || 0
      return data
    } finally {
      loading.value = false
    }
  }

  async function submitLeave(payload) {
    loading.value = true
    try {
      const data = await leaveApi.submitLeave(payload)
      return data
    } finally {
      loading.value = false
    }
  }

  async function approveLeave(id) {
    loading.value = true
    try {
      const data = await leaveApi.approveLeave(id)
      return data
    } finally {
      loading.value = false
    }
  }

  async function rejectLeave(id) {
    loading.value = true
    try {
      const data = await leaveApi.rejectLeave(id)
      return data
    } finally {
      loading.value = false
    }
  }

  return {
    myLeaves,
    totalMyLeaves,
    teamLeaves,
    totalTeamLeaves,
    loading,
    fetchMyLeaves,
    fetchTeamLeaves,
    submitLeave,
    approveLeave,
    rejectLeave,
  }
})

export default useLeaveStore
