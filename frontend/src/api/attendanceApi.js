import apiClient from './axios'

export const attendanceApi = {
  /**
   * Catat check-in presensi hari ini
   * @param {{ notes?: string }} data
   */
  async checkIn(data = {}) {
    const response = await apiClient.post('/attendance/check-in', data)
    return response.data
  },

  /**
   * Catat check-out presensi hari ini
   */
  async checkOut() {
    const response = await apiClient.post('/attendance/check-out')
    return response.data
  },

  /**
   * Ambil status presensi hari ini untuk karyawan yang login
   */
  async getTodayStatus() {
    const response = await apiClient.get('/attendance/today')
    return response.data
  },

  /**
   * Ambil riwayat presensi karyawan yang login
   * @param {{ page?: number, size?: number, from?: string, to?: string }} params
   */
  async getMyHistory(params = {}) {
    const response = await apiClient.get('/attendance/me', { params })
    return response.data
  },

  /**
   * Ambil presensi tim bawahan langsung (Manager / HR)
   * @param {{ page?: number, size?: number, employeeId?: string, from?: string, to?: string }} params
   */
  async getTeamAttendance(params = {}) {
    const response = await apiClient.get('/attendance/team', { params })
    return response.data
  },

  /**
   * Ambil seluruh data presensi perusahaan (HR)
   * @param {{ page?: number, size?: number, departmentId?: number, employeeId?: string, status?: string, from?: string, to?: string }} params
   */
  async getAllAttendance(params = {}) {
    const response = await apiClient.get('/attendance', { params })
    return response.data
  },

  /**
   * Ambil ringkasan statistik kehadiran hari ini (HR)
   */
  async getTodaySummary() {
    const response = await apiClient.get('/attendance/summary')
    return response.data
  },
}

export default attendanceApi
