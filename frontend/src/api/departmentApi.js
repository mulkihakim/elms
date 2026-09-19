import apiClient from './axios'

export const departmentApi = {
  /**
   * Mengambil daftar departemen (dengan pagination)
   * @param {Object} params { page, size, sort }
   */
  getDepartments(params = {}) {
    return apiClient.get('/departments', { params })
  },

  /**
   * Mengambil detail departemen berdasarkan ID
   * @param {number|string} id
   */
  getDepartmentById(id) {
    return apiClient.get(`/departments/${id}`)
  },

  /**
   * Membuat departemen baru
   * @param {Object} data { name }
   */
  createDepartment(data) {
    return apiClient.post('/departments', data)
  },

  /**
   * Mengupdate data departemen
   * @param {number|string} id
   * @param {Object} data { name }
   */
  updateDepartment(id, data) {
    return apiClient.put(`/departments/${id}`, data)
  },

  /**
   * Menghapus departemen
   * @param {number|string} id
   */
  deleteDepartment(id) {
    return apiClient.delete(`/departments/${id}`)
  },
}

export default departmentApi
