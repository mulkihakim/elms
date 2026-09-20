import { defineStore } from 'pinia'
import { ref } from 'vue'
import employeeApi from '@/api/employeeApi'

export const useEmployeeStore = defineStore('employee', () => {
  const employees = ref([])
  const activeEmployees = ref([])
  const teamMembers = ref([])
  const totalElements = ref(0)
  const totalPages = ref(0)
  const currentPage = ref(0)
  const pageSize = ref(10)
  const loading = ref(false)
  const error = ref(null)

  /**
   * Mengambil daftar karyawan berpaginasi dengan berbagai filter
   */
  async function fetchEmployees(filters = {}, page = 0, size = 10) {
    loading.value = true
    error.value = null

    const params = {
      page,
      size,
      sort: 'fullName,asc',
      ...filters,
    }

    // Bersihkan parameter null/undefined/empty string
    Object.keys(params).forEach((key) => {
      if (params[key] === null || params[key] === undefined || params[key] === '') {
        delete params[key]
      }
    })

    try {
      const response = await employeeApi.getEmployees(params)
      if (response.success && response.data) {
        employees.value = response.data.content || []
        totalElements.value = response.data.totalElements || 0
        totalPages.value = response.data.totalPages || 0
        currentPage.value = response.data.number || 0
        pageSize.value = response.data.size || size
      }
      return response
    } catch (err) {
      error.value = err.message || 'Gagal memuat data karyawan'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Mengambil seluruh karyawan aktif untuk opsi dropdown manager
   */
  async function fetchActiveEmployees() {
    try {
      const response = await employeeApi.getActiveEmployees()
      if (response.success && response.data) {
        activeEmployees.value = response.data || []
      }
      return activeEmployees.value
    } catch (err) {
      console.error('Failed to fetch active employees:', err)
      return []
    }
  }

  /**
   * Mengambil anggota tim bawahan langsung dari manager yang login
   */
  async function fetchTeamMembers() {
    loading.value = true
    error.value = null
    try {
      const response = await employeeApi.getTeamMembers()
      teamMembers.value = response.data || []
      return teamMembers.value
    } catch (err) {
      error.value = err.message || 'Gagal memuat anggota tim'
      console.error('Failed to fetch team members:', err)
      return []
    } finally {
      loading.value = false
    }
  }

  /**
   * Mengambil detail karyawan berdasarkan UUID
   */
  async function getEmployeeById(id) {
    loading.value = true
    error.value = null
    try {
      const response = await employeeApi.getEmployeeById(id)
      return response.data
    } catch (err) {
      error.value = err.message || 'Gagal memuat detail karyawan'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Menambahkan karyawan baru
   */
  async function createEmployee(payload) {
    loading.value = true
    error.value = null
    try {
      const response = await employeeApi.createEmployee(payload)
      return response
    } catch (err) {
      error.value = err.message || 'Gagal menambahkan karyawan'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Memperbarui karyawan
   */
  async function updateEmployee(id, payload) {
    loading.value = true
    error.value = null
    try {
      const response = await employeeApi.updateEmployee(id, payload)
      return response
    } catch (err) {
      error.value = err.message || 'Gagal memperbarui karyawan'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Menghapus karyawan
   */
  async function deleteEmployee(id) {
    loading.value = true
    error.value = null
    try {
      const response = await employeeApi.deleteEmployee(id)
      await fetchEmployees({}, currentPage.value, pageSize.value)
      return response
    } catch (err) {
      error.value = err.message || 'Gagal menghapus karyawan'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Mengambil profil karyawan yang sedang login
   */
  async function getMyProfile() {
    loading.value = true
    error.value = null
    try {
      const response = await employeeApi.getMyProfile()
      return response.data
    } catch (err) {
      error.value = err.message || 'Gagal memuat profil karyawan'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Mengubah status karyawan (soft delete/status mutation)
   */
  async function updateEmployeeStatus(id, status) {
    loading.value = true
    error.value = null
    try {
      const response = await employeeApi.updateEmployeeStatus(id, status)
      await fetchEmployees({}, currentPage.value, pageSize.value)
      return response
    } catch (err) {
      error.value = err.message || 'Gagal memperbarui status karyawan'
      throw err
    } finally {
      loading.value = false
    }
  }

  return {
    employees,
    activeEmployees,
    teamMembers,
    totalElements,
    totalPages,
    currentPage,
    pageSize,
    loading,
    error,
    fetchEmployees,
    fetchActiveEmployees,
    fetchTeamMembers,
    getEmployeeById,
    getMyProfile,
    createEmployee,
    updateEmployee,
    updateEmployeeStatus,
    deleteEmployee,
  }
})

export default useEmployeeStore
