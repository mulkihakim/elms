import { defineStore } from 'pinia'
import { ref } from 'vue'
import departmentApi from '@/api/departmentApi'

export const useDepartmentStore = defineStore('department', () => {
  const departments = ref([])
  const allDepartments = ref([]) // Untuk kebutuhan dropdown di modul lain (mis. Position)
  const totalElements = ref(0)
  const totalPages = ref(0)
  const currentPage = ref(0)
  const pageSize = ref(10)
  const loading = ref(false)
  const error = ref(null)

  /**
   * Mengambil data departemen dengan pagination
   */
  async function fetchDepartments(page = 0, size = 10) {
    loading.value = true
    error.value = null
    try {
      const response = await departmentApi.getDepartments({ page, size, sort: 'id,asc' })
      if (response.success && response.data) {
        departments.value = response.data.content || []
        totalElements.value = response.data.totalElements || 0
        totalPages.value = response.data.totalPages || 0
        currentPage.value = response.data.number || 0
        pageSize.value = response.data.size || size
      }
      return response
    } catch (err) {
      error.value = err.message || 'Gagal memuat data departemen'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Mengambil semua departemen untuk pilihan dropdown (size besar)
   */
  async function fetchAllDepartments() {
    try {
      const response = await departmentApi.getDepartments({ page: 0, size: 100, sort: 'name,asc' })
      if (response.success && response.data) {
        allDepartments.value = response.data.content || []
      }
      return allDepartments.value
    } catch (err) {
      console.error('Failed to fetch all departments for dropdown', err)
      return []
    }
  }

  /**
   * Menambahkan departemen baru
   */
  async function createDepartment(payload) {
    loading.value = true
    error.value = null
    try {
      const response = await departmentApi.createDepartment(payload)
      // Refresh list
      await fetchDepartments(currentPage.value, pageSize.value)
      await fetchAllDepartments()
      return response
    } catch (err) {
      error.value = err.message || 'Gagal menambahkan departemen'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Memperbarui departemen
   */
  async function updateDepartment(id, payload) {
    loading.value = true
    error.value = null
    try {
      const response = await departmentApi.updateDepartment(id, payload)
      await fetchDepartments(currentPage.value, pageSize.value)
      await fetchAllDepartments()
      return response
    } catch (err) {
      error.value = err.message || 'Gagal memperbarui departemen'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Menghapus departemen
   */
  async function deleteDepartment(id) {
    loading.value = true
    error.value = null
    try {
      const response = await departmentApi.deleteDepartment(id)
      await fetchDepartments(currentPage.value, pageSize.value)
      await fetchAllDepartments()
      return response
    } catch (err) {
      error.value = err.message || 'Gagal menghapus departemen'
      throw err
    } finally {
      loading.value = false
    }
  }

  return {
    departments,
    allDepartments,
    totalElements,
    totalPages,
    currentPage,
    pageSize,
    loading,
    error,
    fetchDepartments,
    fetchAllDepartments,
    createDepartment,
    updateDepartment,
    deleteDepartment,
  }
})

export default useDepartmentStore
