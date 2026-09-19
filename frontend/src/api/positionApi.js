import apiClient from './axios'

export const positionApi = {
  /**
   * Mengambil daftar posisi (dengan filter departmentId dan pagination)
   * @param {Object} params { departmentId, page, size, sort }
   */
  getPositions(params = {}) {
    return apiClient.get('/positions', { params })
  },

  /**
   * Mengambil detail posisi berdasarkan ID
   * @param {number|string} id
   */
  getPositionById(id) {
    return apiClient.get(`/positions/${id}`)
  },

  /**
   * Membuat posisi baru
   * @param {Object} data { title, departmentId }
   */
  createPosition(data) {
    return apiClient.post('/positions', data)
  },

  /**
   * Mengupdate data posisi
   * @param {number|string} id
   * @param {Object} data { title, departmentId }
   */
  updatePosition(id, data) {
    return apiClient.put(`/positions/${id}`, data)
  },

  /**
   * Menghapus posisi
   * @param {number|string} id
   */
  deletePosition(id) {
    return apiClient.delete(`/positions/${id}`)
  },
}

export default positionApi
