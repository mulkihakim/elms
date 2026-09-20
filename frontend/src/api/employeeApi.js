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
   * Mengambil anggota tim bawahan langsung dari manajer yang login
   */
  getTeamMembers() {
    return apiClient.get('/employees/team')
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
   * Mengambil profil karyawan yang sedang login
   */
  getMyProfile() {
    return apiClient.get('/employees/me')
  },

  /**
   * Mengubah status karyawan (soft delete/status change)
   * @param {string} id UUID
   * @param {string} status ACTIVE | RESIGNED | TERMINATED | ON_LEAVE
   */
  updateEmployeeStatus(id, status) {
    return apiClient.patch(`/employees/${id}/status`, null, { params: { status } })
  },
}

export default employeeApi
