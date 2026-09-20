import apiClient from './axios'

export const dashboardApi = {
  /**
   * Mengambil ringkasan metrik dashboard berdasarkan role login
   */
  getSummary() {
    return apiClient.get('/dashboard/summary')
  },

  /**
   * Mengambil distribusi karyawan per departemen (khusus HR)
   */
  getDepartmentDistribution() {
    return apiClient.get('/dashboard/department-distribution')
  },

  /**
   * Mengambil data kehadiran 7 hari terakhir (HR untuk perusahaan, Manager untuk tim)
   */
  getWeeklyAttendance() {
    return apiClient.get('/dashboard/attendance-weekly')
  },
}

export default dashboardApi
