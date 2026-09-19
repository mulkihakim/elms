import apiClient from './axios'

export const employeeApi = {
  /**
   * Mengambil daftar karyawan (dengan filter departemen, status, role, keyword, dan pagination)
   * @param {Object} params { departmentId, status, role, search, page, size, sort }
   */
  getEmployees(params = {}) {
    return apiClient.get('/employees', { params })
  },

  /**
   * Mengambil semua karyawan berstatus aktif (digunakan untuk pilihan dropdown manager)
   */
  getActiveEmployees() {
    return apiClient.get('/employees/active')
  },

  /**
   * Mengambil detail karyawan berdasarkan UUID
   * @param {string} id UUID
   */
  getEmployeeById(id) {
    return apiClient.get(`/employees/${id}`)
  },

  /**
   * Mendaftarkan karyawan baru
   * @param {Object} data EmployeeRequest payload
   */
  createEmployee(data) {
    return apiClient.post('/employees', data)
  },

  /**
   * Memperbarui data karyawan
   * @param {string} id UUID
   * @param {Object} data EmployeeRequest payload
   */
  updateEmployee(id, data) {
    return apiClient.put(`/employees/${id}`, data)
  },

  /**
   * Menghapus karyawan
   * @param {string} id UUID
   */
  deleteEmployee(id) {
    return apiClient.delete(`/employees/${id}`)
  },
}

export default employeeApi
