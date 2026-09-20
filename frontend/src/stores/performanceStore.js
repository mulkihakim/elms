import { defineStore } from 'pinia'
import { ref } from 'vue'
import performanceApi from '@/api/performanceApi'

export const usePerformanceStore = defineStore('performance', () => {
  const periods = ref([])
  const myReviews = ref([])
  const totalMyReviews = ref(0)
  const teamReviews = ref([])
  const totalTeamReviews = ref(0)
  const loading = ref(false)

  async function fetchPeriods() {
    loading.value = true
    try {
      const data = await performanceApi.getPeriods()
      periods.value = data || []
      return data
    } finally {
      loading.value = false
    }
  }

  async function createPeriod(payload) {
    loading.value = true
    try {
      const data = await performanceApi.createPeriod(payload)
      await fetchPeriods()
      return data
    } finally {
      loading.value = false
    }
  }

  async function fetchMyReviews(params = {}) {
    loading.value = true
    try {
      const data = await performanceApi.getMyReviews(params)
      myReviews.value = data.content || []
      totalMyReviews.value = data.totalElements || 0
      return data
    } finally {
      loading.value = false
    }
  }

  async function fetchTeamReviews(params = {}) {
    loading.value = true
    try {
      const data = await performanceApi.getTeamReviews(params)
      teamReviews.value = data.content || []
      totalTeamReviews.value = data.totalElements || 0
      return data
    } finally {
      loading.value = false
    }
  }

  async function createReview(payload) {
    loading.value = true
    try {
      const data = await performanceApi.createReview(payload)
      return data
    } finally {
      loading.value = false
    }
  }

  async function updateReview(id, payload) {
    loading.value = true
    try {
      const data = await performanceApi.updateReview(id, payload)
      return data
    } finally {
      loading.value = false
    }
  }

  return {
    periods,
    myReviews,
    totalMyReviews,
    teamReviews,
    totalTeamReviews,
    loading,
    fetchPeriods,
    createPeriod,
    fetchMyReviews,
    fetchTeamReviews,
    createReview,
    updateReview,
  }
})

export default usePerformanceStore
