import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authAPI } from '../api'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const username = ref(localStorage.getItem('username') || '')
  const user = ref(null)

  const setAuth = (authToken, authUsername) => {
    token.value = authToken
    username.value = authUsername
    localStorage.setItem('token', authToken)
    localStorage.setItem('username', authUsername)
  }

  const clearAuth = () => {
    token.value = ''
    username.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('username')
  }

  const fetchProfile = async () => {
    try {
      const response = await authAPI.getProfile()
      user.value = response.data
      return response.data
    } catch (error) {
      console.error('Failed to fetch profile:', error)
      throw error
    }
  }

  const isAuthenticated = () => !!token.value

  return {
    token,
    username,
    user,
    setAuth,
    clearAuth,
    fetchProfile,
    isAuthenticated
  }
})
