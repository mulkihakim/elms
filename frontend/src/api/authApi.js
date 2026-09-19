import apiClient from './axios'

export const authApi = {
  /**
   * Melakukan autentikasi login
   * @param {{ email: string, password: string }} credentials
   * @returns {Promise<{ accessToken: string, tokenType: string, user: Object }>}
   */
  async login(credentials) {
    const response = await apiClient.post('/auth/login', credentials)
    return response.data
  },

  /**
   * Mengambil data profil user yang sedang login
   * @returns {Promise<Object>}
   */
  async getMe() {
    const response = await apiClient.get('/auth/me')
    return response.data
  },
}

export default authApi
