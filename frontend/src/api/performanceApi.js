import apiClient from './axios'

export const performanceApi = {
  /**
   * Mengambil daftar seluruh periode evaluasi
   */
  async getPeriods() {
    const response = await apiClient.get('/review-periods')
    return response.data
  },

  /**
   * Mengambil detail periode evaluasi berdasarkan ID
   * @param {number} id
   */
  async getPeriodById(id) {
    const response = await apiClient.get(`/review-periods/${id}`)
    return response.data
  },

  /**
   * Membuat periode evaluasi baru (HR)
   * @param {{ name: string, startDate: string, endDate: string }} data
   */
  async createPeriod(data) {
    const response = await apiClient.post('/review-periods', data)
    return response.data
  },

  /**
   * Mengambil riwayat evaluasi kerja pribadi karyawan yang login
   * @param {{ page?: number, size?: number }} params
   */
  async getMyReviews(params = {}) {
    const response = await apiClient.get('/reviews/me', { params })
    return response.data
  },

  /**
   * Mengambil daftar evaluasi kinerja tim (Manager) atau seluruh organisasi (HR)
   * @param {{ page?: number, size?: number, periodId?: number, employeeId?: string, departmentId?: number }} params
   */
  async getTeamReviews(params = {}) {
    const response = await apiClient.get('/reviews/team', { params })
    return response.data
  },

  /**
   * Membuat evaluasi kinerja baru untuk anggota tim (Manager)
   * @param {{ periodId: number, employeeId: string, technicalSkill: number, communication: number, teamwork: number, problemSolving: number, comments?: string }} data
   */
  async createReview(data) {
    const response = await apiClient.post('/reviews', data)
    return response.data
  },

  /**
   * Memperbarui evaluasi kinerja (Manager)
   * @param {string} id UUID review
   * @param {{ technicalSkill: number, communication: number, teamwork: number, problemSolving: number, comments?: string }} data
   */
  async updateReview(id, data) {
    const response = await apiClient.put(`/reviews/${id}`, data)
    return response.data
  },
}

export default performanceApi
