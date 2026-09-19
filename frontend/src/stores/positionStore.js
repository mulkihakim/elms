import { defineStore } from 'pinia'
import { ref } from 'vue'
import positionApi from '@/api/positionApi'

export const usePositionStore = defineStore('position', () => {
  const positions = ref([])
  const totalElements = ref(0)
  const totalPages = ref(0)
  const currentPage = ref(0)
  const pageSize = ref(10)
  const departmentFilter = ref(null)
  const loading = ref(false)
  const error = ref(null)

  /**
   * Mengambil data posisi dengan filter departmentId dan pagination
   */
  async function fetchPositions(deptId = departmentFilter.value, page = 0, size = 10) {
    loading.value = true
    error.value = null
    departmentFilter.value = deptId

    const params = {
      page,
      size,
      sort: 'id,asc',
    }
    if (deptId) {
      params.departmentId = deptId
    }

    try {
      const response = await positionApi.getPositions(params)
      if (response.success && response.data) {
        positions.value = response.data.content || []
        totalElements.value = response.data.totalElements || 0
        totalPages.value = response.data.totalPages || 0
        currentPage.value = response.data.number || 0
        pageSize.value = response.data.size || size
      }
      return response
    } catch (err) {
      error.value = err.message || 'Gagal memuat data posisi'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Menambahkan posisi baru
   */
  async function createPosition(payload) {
    loading.value = true
    error.value = null
    try {
      const response = await positionApi.createPosition(payload)
      await fetchPositions(departmentFilter.value, currentPage.value, pageSize.value)
      return response
    } catch (err) {
      error.value = err.message || 'Gagal menambahkan posisi'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Memperbarui posisi
   */
  async function updatePosition(id, payload) {
    loading.value = true
    error.value = null
    try {
      const response = await positionApi.updatePosition(id, payload)
      await fetchPositions(departmentFilter.value, currentPage.value, pageSize.value)
      return response
    } catch (err) {
      error.value = err.message || 'Gagal memperbarui posisi'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Menghapus posisi
   */
  async function deletePosition(id) {
    loading.value = true
    error.value = null
    try {
      const response = await positionApi.deletePosition(id)
      await fetchPositions(departmentFilter.value, currentPage.value, pageSize.value)
      return response
    } catch (err) {
      error.value = err.message || 'Gagal menghapus posisi'
      throw err
    } finally {
      loading.value = false
    }
  }

  return {
    positions,
    totalElements,
    totalPages,
    currentPage,
    pageSize,
    departmentFilter,
    loading,
    error,
    fetchPositions,
    createPosition,
    updatePosition,
    deletePosition,
  }
})

export default usePositionStore
