import apiClient from './axios'

export const leaveApi = {
  /**
   * Mengajukan cuti baru
   * @param {{ startDate: string, endDate: string, leaveType: string, reason: string }} data
   */
  async submitLeave(data) {
    const response = await apiClient.post('/leave-requests', data)
    return response.data
  },

  /**
   * Mengambil riwayat pengajuan cuti pribadi
   * @param {{ page?: number, size?: number, status?: string, from?: string, to?: string }} params
   */
  async getMyLeaves(params = {}) {
    const response = await apiClient.get('/leave-requests/me', { params })
    return response.data
  },

  /**
   * Mengambil daftar pengajuan cuti anggota tim (Manager / HR)
   * @param {{ page?: number, size?: number, employeeId?: string, departmentId?: number, status?: string, from?: string, to?: string }} params
   */
  async getTeamLeaves(params = {}) {
    const response = await apiClient.get('/leave-requests/team', { params })
    return response.data
  },

  /**
   * Menyetujui permohonan cuti
   * @param {string} id UUID pengajuan cuti
   */
  async approveLeave(id) {
    const response = await apiClient.patch(`/leave-requests/${id}/approve`)
    return response.data
  },

  /**
   * Menolak permohonan cuti
   * @param {string} id UUID pengajuan cuti
   */
  async rejectLeave(id) {
    const response = await apiClient.patch(`/leave-requests/${id}/reject`)
    return response.data
  },
}

export default leaveApi
